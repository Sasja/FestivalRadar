package com.pollytronics.clique;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.pollytronics.clique.lib.api_v02.ApiCallSync;
import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Profile;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.CliqueSQLite;
import com.pollytronics.clique.lib.database.cliqueSQLite.sync.DbBlip;
import com.pollytronics.clique.lib.database.cliqueSQLite.sync.DbContact;
import com.pollytronics.clique.lib.database.cliqueSQLite.sync.DbPing;
import com.pollytronics.clique.lib.database.cliqueSQLite.sync.DbProfile;
import com.pollytronics.clique.lib.database.cliqueSQLite.sync.DbSelfBlip;
import com.pollytronics.clique.lib.database.cliqueSQLite.sync.DbSelfProfile;
import com.pollytronics.clique.lib.preferences.CliquePreferences;
import com.pollytronics.clique.lib.tools.MyAssortedTools;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * Created by pollywog on 9/7/15.
 * This class does the singleton pattern thing, it is called from other objects in order to trigger syncing to the remote webservice through the sync api/v2 call
 * it looks into the database to gather new data, uploads it to the server in one api call and it receives the new data
 * it allows new database updates during the the sync operations so local database access remains super fast and responsive
 * it needs every entry in the local database to be tagged with the current dirtyCounter value when added or updated.
 *
 * only call poke, and call it from the main thread! This will make sure all local changes at the moment of calling will be sent as soon as possible.
 *
 * TODO: (UI, bug) find a way to trigger a login when authentication fails (eg user changed password using other device)
 * TODO: (bug) will the sync routine keep running when poked again while in doInBackground?
 * TODO: (code) find out how this class should work together with activities and service so this class can send a signal back to its calling class (if it's still there) to say it's done (notify new data)
 *
 */
public class CliqueSyncer {
    private static final String TAG = "CliqueSyncer";

    private static CliqueSyncer instance = null;
    private SynchronizeTask synchronizeTask = null;
    private boolean extraRun = false;       //TODO: (code) make the extra run if necessary

    private CliquePreferences prefs;
    private Context applicationContext;
    private Context toastContext;

    public static CliqueSyncer getInstance(Context context) {
        if (instance == null) {
            instance = new CliqueSyncer(context);
        } else {
            Log.i(TAG, "reusing the instance of CliqueSyncer");
        }
        return instance;
    }

    private CliqueSyncer(Context context) {
        Log.i(TAG, "Creating new instance of CliqueSyncer");
        prefs = CliquePreferences.getInstance(context);
        applicationContext = context.getApplicationContext();
    }

    /**
     * poke the object to make it check for work, if it is running while being poked, it will remember to check one more time
     *
     * when poked it means there is new dirty data that should be uploaded to the server
     */
    public void poke(Context toastContext) {
        pokePingGetSet(toastContext, false, false);
    }

    //TODO: (code) this is not the most elegant or even right way to allow setting of ping get/set in api call
    //TODO: (code, bug) consider a pingPoke arriving while the task is in doInBackground...
    public void pokePingGetSet(Context toastContext, boolean get, boolean set) {
        this.toastContext = toastContext;
        try {
            CliqueSQLite.increaseGlobalDirtyCounter();
            Log.i(TAG, "increased the globaldirtycounter to " + CliqueSQLite.getGlobalDirtyCounter());
            if(synchronizeTask != null) {
                extraRun = true;
                return;
            } else {
                synchronizeTask = new SynchronizeTask();
                synchronizeTask.pingSet = set;
                synchronizeTask.pingGet = get;
                synchronizeTask.execute();
            }
        } catch (CliqueDbException e) {
            e.printStackTrace();
        }
    }

    /**
     * The this class is the heart of the CliqueSyncer class, the CliqueSyncer class just makes sure one of these is running when necessary
     */
    private class SynchronizeTask extends AsyncTask<Void,Void,Integer> {
        private static final int        SUCCESS = 1;
        private static final int     NO_SUCCESS = 2;
        private static final int     NO_NETWORK = 3;
        private static final int       IO_ERROR = 4;
        private static final int     JSON_ERROR = 5;
        private static final int NOT_AUTHORIZED = 6;

        long maxDirtyCounter; // all entries with a dirtycountervalue higher than 0 and lower to this must be uploaded
        ApiCallSync syncApiCall;
        boolean pingGet = false;
        boolean pingSet = false;

        /**
         * Before calling the api in doInBackground all data needs to be gathered,
         * also the syncApicall is initialized with the data that needs to come from the database (such as lastSync)
         */
        @Override
        protected void onPreExecute() {
            try {
                Log.i(TAG, "running onPreExecute of SynchronizeTask");
                // initializing syncApiCall with database-based data
                syncApiCall = new ApiCallSync(prefs.getAccountLogin(), prefs.getAccountKeyb64());
                syncApiCall.setLastSync(CliqueSQLite.getLastSync());

                // checking and remembering current global dirty counter
                maxDirtyCounter = CliqueSQLite.getGlobalDirtyCounter();
                Log.i(TAG, "grabbing all dirty data with: 0 < counter < " + maxDirtyCounter);

                // blips
                List<Blip> blips = DbSelfBlip.getNew(maxDirtyCounter);
                if(blips.size()>0) {
                    Log.i(TAG, "found n new blips to upload: " + blips.size());
                    for(Blip b:blips) syncApiCall.addBlip(b);
                }
                // new profile name
                Profile newSelfProfile = DbSelfProfile.getChanged(maxDirtyCounter);
                if(newSelfProfile != null) {
                    Log.i(TAG, "detected a new nickname : " + newSelfProfile.getName());
                    syncApiCall.setNickname(newSelfProfile.getName());   // TODO: (code) generify!
                }

                // contacts
                List<Long> addCanSeeMe = DbContact.getAdded(maxDirtyCounter);
                if(addCanSeeMe.size() > 0) {
                    Log.i(TAG, "found n new contacts that may see me to upload: " + addCanSeeMe.size());
                    for(Long id:addCanSeeMe) syncApiCall.addCanSeeme(id);
                }
                List<Long> delCanSeeMe = DbContact.getDeleted(maxDirtyCounter);
                if(delCanSeeMe.size() > 0) {
                    Log.i(TAG, "found n contacts to remotely delete: " + delCanSeeMe.size());
                    for(Long id:delCanSeeMe) syncApiCall.delCanSeeme(id);
                }
                // ping
                if(pingGet || pingSet) syncApiCall.setPingGetSet(pingGet, pingSet);
            } catch (CliqueDbException | JSONException e) {
                e.printStackTrace();
            }
        }

        /**
         * In the call is performed and the reply is parsed and stored within the syncApiCall object
         * it returns true if no errors occured
         */
        @Override
        protected Integer doInBackground(Void... params) {
            Log.i(TAG, "running doInBackground of Synctask");
            if(!MyAssortedTools.isNetworkAvailable(applicationContext)) return NO_NETWORK;
            try {
                // make and parse the syncApiCall
                syncApiCall.callAndParse();
                if(syncApiCall.isAuthSuccess()) {
                    return SUCCESS;
                } else {
                    return NOT_AUTHORIZED;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return JSON_ERROR;
            } catch (IOException e) {
                e.printStackTrace();
                return IO_ERROR;
            }
        }

        /**
         * First off when receiving success from doInbackground this puppy will clear all the relevant dirty flags as all that data is uploaded (0<dirty<maxDirtyCounter)
         * Then it will probe syncApiCall for results and if any apply the right database changes.
         * entries that have a dirty flag should not be overwritten (if new flags are set during doInBackgrounf for instance)
         * also the lastSync value is updated to what is received from the api
         */
        @Override
        protected void onPostExecute(Integer result) {
            Log.i(TAG, "running onPostExecute of Synctask");
            synchronizeTask = null;
            switch (result) {
                case NO_SUCCESS:
                    if (toastContext != null) {
                        Toast.makeText(toastContext, "Sync failed", Toast.LENGTH_SHORT).show();
                    }
                    return;
                case NO_NETWORK:
                    if (toastContext != null) {
                        Toast.makeText(toastContext, "No network", Toast.LENGTH_SHORT).show();
                    }
                    return;
                case JSON_ERROR:
                    if (toastContext != null) {
                        Toast.makeText(toastContext, "API error", Toast.LENGTH_SHORT).show();
                    }
                    return;
                case IO_ERROR:
                    if (toastContext != null) {
                        Toast.makeText(toastContext, "Server unreachable", Toast.LENGTH_SHORT).show();
                    }
                    return;
                case NOT_AUTHORIZED:
                    Log.i(TAG, "NOT AUTHORIZED");
                    if (toastContext != null) {
                        // TODO: trigger appropriate action here
                        Toast.makeText(toastContext, "Not authorized", Toast.LENGTH_SHORT).show();
                    }
                    return;
            }
            // else it must have been succesfull.
            try {
                if (syncApiCall.getCallMessage() != null)
                    Log.i(TAG, "syncapicall message = " + syncApiCall.getCallMessage());
                // clearing dirty flags
                DbSelfBlip.clearDirtyCounters(maxDirtyCounter);
                DbSelfProfile.clearDirtyProfile(maxDirtyCounter);
                DbContact.clearDirtyCounters(maxDirtyCounter);
                // new blips?
                List<Pair<Blip, Long>> newBlips = syncApiCall.getNewBlips();
                if (newBlips.size() > 0) {
                    Log.i(TAG, "received new blips, n = " + newBlips.size());
                    for (Pair<Blip, Long> pair : newBlips) {
                        Blip blip = pair.first;
                        long id = pair.second;
                        DbBlip.add(id, blip);
                    }
                    DbBlip.keepNEntriesForEachId(10);
                }
                // new nickname?
                String newNick = syncApiCall.getNewNickname();
                if (newNick != null) {
                    Log.i(TAG, "new nickname received from the webservice! (" + syncApiCall.getNewNickname() + ")");
                    DbSelfProfile.update(new Profile(syncApiCall.getNewNickname()), maxDirtyCounter);
                }
                // contact adds and deletes?
                List<Long> canSeeMeAdds = syncApiCall.getNewCanSeeMeAdds();
                if (canSeeMeAdds.size() > 0)
                    Log.i(TAG, "received n canseeme-contact adds from server: n = " + canSeeMeAdds.size());
                for (Long id : canSeeMeAdds) DbContact.addCanSeeMe(id, maxDirtyCounter);

                List<Long> canSeeMeDels = syncApiCall.getNewCanSeeMeDels();
                if (canSeeMeDels.size() > 0)
                    Log.i(TAG, "received n canseeme-contact dels from server: n = " + canSeeMeDels.size());
                for (Long id : canSeeMeDels) DbContact.removeCanSeeMe(id, maxDirtyCounter);

                List<Long> iCanSeeAdds = syncApiCall.getNewIcanSeeAdds();
                if (iCanSeeAdds.size() > 0)
                    Log.i(TAG, "received n icansee-contact adds from server: n = " + iCanSeeAdds.size());
                for (Long id : iCanSeeAdds) DbContact.addIcanSee(id);
                for (Long id : iCanSeeAdds) { // TODO: (feature) remove this block and implement some proper contact management
                    Log.i(TAG, "WARNING: autoaccepting matching with user_id = " + id.toString());
                    com.pollytronics.clique.lib.database.cliqueSQLite.local.DbContact.add(id);
                }

                List<Long> iCanSeeDels = syncApiCall.getNewIcanSeeDels();
                if (iCanSeeDels.size() > 0)
                    Log.i(TAG, "received n icansee-contact dels from server: n = " + iCanSeeDels.size());
                for (Long id : iCanSeeDels) DbContact.removeIcanSee(id);

                // new profiles?
                List<Pair<Profile, Long>> newProfiles = syncApiCall.getNewProfiles();
                if (newProfiles.size() > 0)
                    Log.i(TAG, "received n new profiles from the server: n = " + newProfiles.size());
                for (Pair<Profile, Long> pr_id : newProfiles)
                    DbProfile.add(pr_id.second, pr_id.first);

                // new ping data?
                List<Pair<Long, String>> newPings = syncApiCall.getNewPings();
                if (newPings.size() > 0) {
                    Log.i(TAG, "received pings, flushing old pings, and adding new ones");
                    DbPing.flush();
                    for (int i = 0; i < newPings.size(); i++) {
                        Long id = newPings.get(i).first;
                        String nick = newPings.get(i).second;
                        DbPing.add(id, nick, (double) i);   // just pass the rank to the distance for now
                    }
                }

                // if we succesfully got this far, the lastSync may be increased as we never need to get this data again
                CliqueSQLite.setLastSync(syncApiCall.getNewLastSync());
            } catch (CliqueDbException e) {
                e.printStackTrace();
            }
        }
    }
}
