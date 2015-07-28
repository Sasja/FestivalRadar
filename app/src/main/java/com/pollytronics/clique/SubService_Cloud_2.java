package com.pollytronics.clique;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.pollytronics.clique.lib.api_v01.ApiCallGetBlips;
import com.pollytronics.clique.lib.api_v01.ApiCallSetMyBlip;
import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.service.CliqueService;
import com.pollytronics.clique.lib.service.SubService;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * Cloud SubService 2
 *
 * periodically pulls and pushes data from server and updates the local database
 * This class uses the api_vxx methods to do the actual api calls
 *
 * TODO: (syncing) minimize tcp connection lifetime to minimize load on server
 * TODO: (errorhandling) look for printStacktrace try catch blocks everywhere and fix it
 * TODO: (syncing) sync sometimes fails... ivestigate!
 * TODO: (syncing) i believe this will generate a shitload of duplicate blips
 * http://developer.android.com/training/basics/network-ops/connecting.html
 *
 * Created by pollywog on 26/5/2015.
 */
public class SubService_Cloud_2 extends SubService {

    private final String TAG = "SubService_Cloud_2";
    private int updateTime_ms;
    private boolean cleaningUp = false;     //a flag so the network pull loop will stop posting itself
    private final Runnable cloudLoop = new Runnable() {
        @Override
        public void run() {
            try{
                ConnectivityManager connMgr = (ConnectivityManager) getCliqueService().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Log.i(TAG, "network available: syncing data");
                    new SyncToWebserviceTask().execute();
                    getMainHandler().removeCallbacks(cloudLoop);    //make sure we dont have 2 loops
                    if(!cleaningUp) getMainHandler().postDelayed(cloudLoop,updateTime_ms);
                } else {
                    Log.i(TAG,"Cannot connect to server: no network");
                    getMainHandler().removeCallbacks(cloudLoop);    //make sure we dont have 2 loops
                    if(!cleaningUp) getMainHandler().postDelayed(cloudLoop,updateTime_ms);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public SubService_Cloud_2(CliqueService rs) {
        super(rs);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        updateTime_ms = (int) getCliquePreferences().getCloudUpdateTime_ms();
        Log.i(TAG, "set updateTime to (ms) " + Integer.toString(updateTime_ms));
        getMainHandler().post(cloudLoop);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy");
        getMainHandler().removeCallbacks(cloudLoop);
        cleaningUp = true;
    }

    @Override
    public void onRegister() {
        Log.i(TAG,"onRegister");
    }

    @Override
    public void onUnregister() {
        Log.i(TAG, "onUnregister");
    }

    @Override
    public void onNewSettings() {
        getMainHandler().removeCallbacks(cloudLoop);
        updateTime_ms = (int) getCliquePreferences().getCloudUpdateTime_ms();
        Log.i(TAG, "set updateTime to (ms) " + Integer.toString(updateTime_ms));
        getMainHandler().post(cloudLoop);
    }



    /**
     * This class is used to bundle all the interactions that happen with the api into one AsyncTask
     * it relies on APICall objects to handle the api details of each api separate interaction.
     * doInBackground can communicate with the other methods by setting member fields, it's safe:
     * http://developer.android.com/reference/android/os/AsyncTask.html (See Memory observability)
     * TODO: (syncing) might be prettier to return a boolean for success from doInbackground instead of using a var
     */
    private class SyncToWebserviceTask extends AsyncTask<Void, Void, String> {
        private ApiCallSetMyBlip setMyBlip;
        private ApiCallGetBlips getBlips;
        private boolean apiCallsSucceeded = false;
        /**
         * Gathers all the data needed to perform the api calls
         */
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "gathering the data i need to send to webservice");
            long selfId = 0;
            try {
                selfId = getCliqueDb().getSelfContact().getGlobalId();
            } catch (CliqueDbException e) {
                e.printStackTrace();
            }
            Blip lastBlip = null;
            try {
                lastBlip = getCliqueDb().getLastSelfBlip();
            } catch (CliqueDbException e) {
                e.printStackTrace();
            }
            try {
                setMyBlip = new ApiCallSetMyBlip(lastBlip, selfId);
                getBlips  = new ApiCallGetBlips(selfId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "calling api from SyncToWebserviceTask");
            try {
                setMyBlip.callAndParse();
                getBlips.callAndParse();
                apiCallsSucceeded = true;
            } catch (IOException e) {
                Log.i(TAG, "IOException: unable to complete all API requests");
                e.printStackTrace();
                return "IOExcepion: unable to complete all API requests";
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * call the APICall methods to process the results
         */
        @Override
        protected void onPostExecute(String s) {
            if (apiCallsSucceeded) {         // only take action if all api calls were successful
                Log.i(TAG, "using/aplying the responses of the webservice");
                List<Blip> blips = getBlips.getBlipList();
                for(Blip b : blips) {
                    Contact contact = null;
                    try {
                        contact = getCliqueDb().getContactById(b.getOwnerId());
                    } catch (CliqueDbException e) {
                        e.printStackTrace();
                    }
                    if(contact != null) {   // check if it is known locally on device
                        try {
                            getCliqueDb().addBlip(b, contact);
                        } catch (CliqueDbException e) {
                            e.printStackTrace();
                        }
                    }
                }
                getCliqueService().notifyNewData();
            } else {
                Log.i(TAG, "the api call has failed, not calling doTheWork() methods for the calls");
            }
        }
    }
}
