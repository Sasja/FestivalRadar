package com.pollytronics.festivalradar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.pollytronics.festivalradar.lib.RadarContact;
import com.pollytronics.festivalradar.lib.api_v01.ApiCallDeleteContact;
import com.pollytronics.festivalradar.lib.api_v01.ApiCallGetContactsISee;
import com.pollytronics.festivalradar.lib.api_v01.ApiCallGetContactsSeeme;
import com.pollytronics.festivalradar.lib.api_v01.ApiCallGetProfile;
import com.pollytronics.festivalradar.lib.api_v01.ApiCallPostContact;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Study https://developer.android.com/training/sync-adapters/index.html and consider implementing such a thing
 */
public class ViewPagerActivity_Contacts extends RadarActivity_MyViewPagerAct {
    @SuppressWarnings("unused")
    private static final String TAG = "ViewPagerAct_Contacts";

    RadarContact selectedContact = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewpageractivity_contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync_contacts) {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                Log.i(TAG, "syncing contact data");
                new SyncToWebserviceTask().execute();
            } else {
                Log.i(TAG, "cannot sync contact data: no network");
                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_no_network), Toast.LENGTH_SHORT);
                toast.show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    /**
     * make sure the getF methods use the same order!!!
     * TODO: make this foolproof
     */
    protected void loadMyFragments() {
        addFragment(Fragment_Contacts_Ping.class, "PING");
        addFragment(Fragment_Contacts_MyContacts.class, "MY CONTACTS");
        addFragment(Fragment_Contacts_Remote.class, "REMOTE");
    }

    /**
     * TODO: make this foolproof
     * @return
     */
    public Fragment_Contacts_Ping getF_Ping() {
        return (Fragment_Contacts_Ping) getFragmentByNr(0);
    }

    /**
     * TODO: make this foolproof
     * @return
     */
    public Fragment_Contacts_MyContacts getF_MyContacts() {
        return (Fragment_Contacts_MyContacts) getFragmentByNr(1);
    }

    /**
     * TODO: make this foolproof
     * @return
     */
    public Fragment_Contacts_Remote getF_Remote() {
        return (Fragment_Contacts_Remote) getFragmentByNr(2);
    }

    /**
     * check for network, do the api call to post new contact, when successful remove it from the list and add it locally.
     * then refresh the mycontact list.
     */
    public void addNewContact(RadarContact contact) {
        Log.i(TAG, "adding contact locally");
        getRadarDatabase().addContactWithId(contact);
        notifyDatabaseUpdate();
        Log.i(TAG, "launching task to add contact remotely");
        new postNewContactTask(contact).execute();
    }

    public void confirmAndDeleteContact(RadarContact contact) {
        selectedContact = contact;  //TODO: yo, using global variables, nice!
        DialogFragment mDialog = new TemporaryDeleteDialog();
        mDialog.show(getSupportFragmentManager(), "DeleteContactDialog");
    }

    //TODO: still super ugly, but it works for now
    private void deleteSelectedContact() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){
            final ApiCallDeleteContact deleteContact = new ApiCallDeleteContact();
            deleteContact.collectData(getRadarDatabase());
            deleteContact.setContactId(selectedContact.getID());
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() { // TODO: what to do when this fails?
                    try {
                        deleteContact.callAndParse();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            Log.i(TAG, "posting a delete request to api for contact id: " + selectedContact.getID());
            thread.start();
            Log.i(TAG, "deleting selected radar contact (id=" + selectedContact.getID() + ")");
            getRadarDatabase().removeContact(selectedContact);
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_contact_removed), Toast.LENGTH_SHORT);
            toast.show();
            notifyDatabaseUpdate();         // TODO: this shouldn't be called here but happen autamatically
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_no_network), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public static class TemporaryDeleteDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage("Delete this contact?")
                    .setNegativeButton("no", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "denied, not doing anything");
                        }
                    })
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "calling deleteSelectedContact()");
                            ((ViewPagerActivity_Contacts)getActivity()).deleteSelectedContact();
                        }
                    })
                    .create();
        }
    }

    private class postNewContactTask extends AsyncTask<Void, Void, String> {
        private final ApiCallPostContact postContact = new ApiCallPostContact();
        private boolean apiCallSucceeded = false;
        private RadarContact contact;

        public postNewContactTask(RadarContact contact) {
            this.contact = contact;
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "gathering own use id");
            postContact.collectData(getRadarDatabase());
            postContact.setContactId(contact.getID());
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "calling api from postAndAddNewContactTask");
            try {
                postContact.callAndParse();
                apiCallSucceeded = true;
            } catch (IOException e) {
                Log.i(TAG, "IOException: unable to complete API requests");
                return "IOException: unable to complete API requests";
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(apiCallSucceeded) {
                Log.i(TAG, "sucessfully posted contact to webservice");
            } else {
                Log.i(TAG, "the api call has failed");
            }
        }
    }

    /**
     * fetches a list of people than can see me(CSM), that i can see(ICS), and compares it to
     * my local contact list(CON) and takes actions according to:
     *
     *      CON     ICS     CSM     action
     *   ----------------------------------
     *      0       0       0       nop
     *      0       0       1       add to CON, should not happen, forgot local contact
     *      0       1       0       add to CON and post to CSM, (= autoaccept friend)
     *      0       1       1       add to CON, should not happen, forgot local contact
     *      1       0       0       delete from CON, (= autoremove DELETE called on api)
     *      1       0       1       nop (= waiting for contact to accept)
     *      1       1       0       post to CSM, should not happen
     *      1       1       1       nop, users linked
     *
     *      this translates to:
     *
     *      !CON && ( ICS || CSM )      add to CON
     *      ICS && !CSM                 post to CSM
     *      CON && !(ICS || CSM)        delete from CON
     **/
    private class SyncToWebserviceTask extends AsyncTask<Void, Void, String> {
        private final ApiCallPostContact apiCallPostContact = new ApiCallPostContact();
        private final ApiCallGetContactsSeeme apiCallGetContactsSeeme = new ApiCallGetContactsSeeme();
        private final ApiCallGetContactsISee apiCallGetContactsISee = new ApiCallGetContactsISee();
        private final ApiCallGetProfile apiCallGetProfile = new ApiCallGetProfile();
        private final Set<Long> con = new HashSet<>();
        private final Set<Long> toDeleteFromCon = new HashSet<>();
        private final Set<Long> toAddToCon = new HashSet<>();
        private final Set<Long> toPostToCsm = new HashSet<>();
        private Set<Long> ics = new HashSet<>();
        private Set<Long> csm = new HashSet<>();
        private boolean apiCallsSucceeded = false;
        private Map<Long, String> newContactNames= new HashMap<>();

        @Override
        protected void onPreExecute() {
            // get selfId (into ApiCall objects)
            apiCallPostContact.collectData(getRadarDatabase());
            apiCallGetContactsSeeme.collectData(getRadarDatabase());
            apiCallGetContactsISee.collectData(getRadarDatabase());
            // construct list of ids in local contacts
            for (RadarContact c : getRadarDatabase().getAllContacts()) {
                con.add(c.getID());
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "calling api");
            try {
                apiCallGetContactsISee.callAndParse();
                apiCallGetContactsSeeme.callAndParse();
                ics = apiCallGetContactsISee.getCollection();
                csm = apiCallGetContactsSeeme.getCollection();
                Log.i(TAG, "successfully loaded all contact lists local and remote");
                Log.i(TAG, String.format("nCON=%d nICS=%d nCSM=%d" ,con.size(), ics.size(), csm.size()));
                // find contacts i need to add to local contacts (do in onPostExecute)
                toAddToCon.addAll(ics);
                toAddToCon.addAll(csm);
                toAddToCon.removeAll(con);
                // find contacts i need to post to the api (do here in doInBackground)
                toPostToCsm.addAll(ics);
                toPostToCsm.removeAll(csm);
                // find contacts i need to remove from my contacts (do in onPostExecute)
                toDeleteFromCon.addAll(con);
                toDeleteFromCon.removeAll(ics);
                toDeleteFromCon.removeAll(csm);
                // now post the contacts that need to be posted to the api
                for(long id:toPostToCsm) {
                    if(!con.contains(id)) {
                        Log.i(TAG, "autoaccepting new contact, posting to api: " + id);
                    } else {
                        Log.i(TAG, "this should not happen: local contact that i can see but cant see me, reposting to api (csm): " + id);
                    }
                    apiCallPostContact.setContactId(id);
                    apiCallPostContact.callAndParse();
                }
                // now gather the names of the contacts i need to add locally
                for(long id:toAddToCon) {
                    Log.i(TAG, "requesting remote name for new contact (id="+id+")");
                    apiCallGetProfile.setRequestedId(id);
                    apiCallGetProfile.callAndParse();
                    newContactNames.put(id, apiCallGetProfile.getName());
                }
                apiCallsSucceeded = true;
            } catch (IOException e) {
                e.printStackTrace();
                return "IOException: unable to complete all api requests";
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (apiCallsSucceeded) {
                for (long id : toAddToCon) {
                    if (csm.contains(id)) {
                        Log.i(TAG, "adding contact to local contacts (contact known in api but not in local contacts!?): " + id);
                    } else {
                        Log.i(TAG, "adding contact to local contacts (autoaccept): " + id);
                    }
                    String contactName = newContactNames.get(id);
                    RadarContact newContact = new RadarContact().setName(contactName).setID(id);
                    getRadarDatabase().addContactWithId(newContact);
                }
                for (long id : toDeleteFromCon) {
                    Log.i(TAG, "deleting contact from local list (triggered by remote delete): " + id);
                    getRadarDatabase().removeContactById(id);
                }
                notifyDatabaseUpdate();             // TODO: not sure this needs to be called
                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_contacts_synced), Toast.LENGTH_SHORT);
                toast.show();
            } else { // apiCallsSucceeded == false
                Toast toast = Toast.makeText(getApplicationContext(), "failed to sync", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

}
