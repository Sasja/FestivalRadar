package com.pollytronics.clique;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.pollytronics.clique.lib.CliqueActivity_MyViewPagerAct;
import com.pollytronics.clique.lib.base.Contact;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.local.DbContact;

/**
 * TODO: (syncing) Study https://developer.android.com/training/sync-adapters/index.html and consider implementing such a thing
 * TODO: receiving database update notifications every three seconds when in contacts activity!?! why is that?
 */
public class MVP_Activity_Contacts extends CliqueActivity_MyViewPagerAct {
    @SuppressWarnings("unused")
    private static final String TAG = "ViewPagerAct_Contacts";

    private Contact selectedContact = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableHeartBeat(3000);
    }

    /**
     * Will regularly call notifyDatabaseUpdate on all fragments in order to keep em up to date
     */
    @Override
    protected void heartBeatCallBack() {
        notifyDatabaseUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contacts, menu);
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
                // new SyncToWebserviceTask().execute();
                CliqueSyncer.getInstance(this).poke();
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
     */
    protected void loadMyFragments() {
        addFragment(Fragment_Contacts_Ping.class, getString(R.string.contacts_tab_ping));               // 0
        addFragment(Fragment_Contacts_MyContacts.class, getString(R.string.contacts_tab_mycontacts));   // 1
        addFragment(Fragment_Contacts_Remote.class, getString(R.string.contacts_tab_remote));           // 2
    }

    //TODO: what is this for again?
    public Fragment_Contacts_Ping getF_Ping() {             return (Fragment_Contacts_Ping)         getFragmentByNr(0); }
    public Fragment_Contacts_MyContacts getF_MyContacts() { return (Fragment_Contacts_MyContacts)   getFragmentByNr(1); }
    public Fragment_Contacts_Remote getF_Remote() {         return (Fragment_Contacts_Remote)       getFragmentByNr(2); }

    /**
     * check for network, do the api call to post new contact, when successful remove it from the list and add it locally.
     * then refresh the mycontact list.
     */
//    public void addNewContact(Contact contact) {
//        Log.i(TAG, "adding contact locally");
//        try {
//            DbContact.add(contact.getGlobalId());   // TODO: when should the profiles be added???
//        } catch (CliqueDbException e) {
//            e.printStackTrace();
//        }
//        notifyDatabaseUpdate();
//        // Log.i(TAG, "launching task to add contact remotely");
//        // new postNewContactTask(contact).execute();
//    }

    public void confirmAndDeleteContact(Contact contact) {
        selectedContact = contact;  //TODO: yo, using global variables, nice!
        DialogFragment mDialog = new TemporaryDeleteDialog();
        Bundle arguments = new Bundle();
        arguments.putLong("id", selectedContact.getGlobalId());
        mDialog.setArguments(arguments);
        mDialog.show(getSupportFragmentManager(), "DeleteContactDialog");
    }

//    //TODO: (syncing) still super ugly, but it works for now
//    private void deleteSelectedContact() {
//        final ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        if (networkInfo != null && networkInfo.isConnected()){
//            long selfId = getCliquePreferences().getAccountId();
//            long deleteId = selectedContact.getGlobalId();
//            final ApiCallDeleteContact deleteContact = new ApiCallDeleteContact(selfId, deleteId);
//            Thread thread = new Thread(new Runnable() {
//                @Override
//                public void run() { // TODO: (errorhandling) what to do when this fails?
//                    try {
//                        deleteContact.callAndParse();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//            Log.i(TAG, "posting a delete request to api for contact id: " + deleteId);
//            thread.start();
//            Log.i(TAG, "deleting selected radar contact (id=" + deleteId + ")");
//            try {
//                DbContact.remove(selectedContact.getGlobalId());    // TODO: what about profiles?
//            } catch (CliqueDbException e) {
//                e.printStackTrace();
//            }
//            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_contact_removed), Toast.LENGTH_SHORT);
//            toast.show();
//            notifyDatabaseUpdate();
//        } else {
//            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_no_network), Toast.LENGTH_SHORT);
//            toast.show();
//        }
//    }

    public static class TemporaryDeleteDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.dialog_delete_this_contact))
                    .setNegativeButton(getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "denied, not doing anything");
                        }
                    })
                    .setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            Log.i(TAG, "calling deleteSelectedContact()");
//                            ((MVP_Activity_Contacts) getActivity()).deleteSelectedContact();
                            Log.i(TAG, "deleting selected contact locally");
                            try {
                                DbContact.remove(getArguments().getLong("id"));
                                ((MVP_Activity_Contacts) getActivity()).notifyDatabaseUpdate(); //TODO: this might be dangerous
                            } catch (CliqueDbException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .create();
        }
    }

//    private class postNewContactTask extends AsyncTask<Void, Void, String> {
//        private final Contact contact;
//        private ApiCallPostContact postContact;
//        private boolean apiCallSucceeded = false;
//
//        public postNewContactTask(Contact contact) {
//            this.contact = contact;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            Log.i(TAG, "gathering own use id");
//            long selfId = 0;
//                selfId = getCliquePreferences().getAccountId();
//            long contactId = contact.getGlobalId();
//            try {
//                postContact = new ApiCallPostContact(selfId, contactId);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            Log.i(TAG, "calling api from postAndAddNewContactTask");
//            try {
//                postContact.callAndParse();
//                apiCallSucceeded = true;
//            } catch (IOException e) {
//                Log.i(TAG, "IOException: unable to complete API requests");
//                return "IOException: unable to complete API requests";
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            if(apiCallSucceeded) {
//                Log.i(TAG, "sucessfully posted contact to webservice");
//            } else {
//                Log.i(TAG, "the api call has failed");
//            }
//        }
//    }

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
//    private class SyncToWebserviceTask extends AsyncTask<Void, Void, String> {
//        private final Set<Long> con = new HashSet<>();
//        private final Set<Long> toDeleteFromCon = new HashSet<>();
//        private final Set<Long> toAddToCon = new HashSet<>();
//        private final Set<Long> toPostToCsm = new HashSet<>();
//        private final Map<Long, Contact> newContacts= new HashMap<>();
//        private ApiCallPostContact apiCallPostContact;
//        private ApiCallGetContactIdsSeeme apiCallGetContactsSeeme;
//        private ApiCallGetContactIdsISee apiCallGetContactsISee;
//        private ApiCallGetProfile apiCallGetProfile;
//        private Set<Long> ics = new HashSet<>();
//        private Set<Long> csm = new HashSet<>();
//        private boolean apiCallsSucceeded = false;
//
//        @Override
//        protected void onPreExecute() {
//            // get selfId (into ApiCall objects)
//            long selfId = 0;
//            selfId = getCliquePreferences().getAccountId();
//            apiCallPostContact = new ApiCallPostContact(selfId);
//            apiCallGetContactsSeeme = new ApiCallGetContactIdsSeeme(selfId);
//            apiCallGetContactsISee = new ApiCallGetContactIdsISee(selfId);
//            // construct list of ids in local contacts
//            try {
//                for (long cid : DbContact.getIds()) {
//                    con.add(cid);
//                }
//            } catch (CliqueDbException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            Log.i(TAG, "calling api");
//            try {
//                apiCallGetContactsISee.callAndParse();
//                apiCallGetContactsSeeme.callAndParse();
//                ics = new HashSet<>(apiCallGetContactsISee.getContactIds());
//                csm = new HashSet<>(apiCallGetContactsSeeme.getContactIds());
//                Log.i(TAG, "successfully loaded all contact lists local and remote");
//                Log.i(TAG, String.format("nCON=%d nICS=%d nCSM=%d" ,con.size(), ics.size(), csm.size()));
//                // find contacts i need to add to local contacts (do in onPostExecute)
//                toAddToCon.addAll(ics);
//                toAddToCon.addAll(csm);
//                toAddToCon.removeAll(con);
//                // find contacts i need to post to the api (do here in doInBackground)
//                toPostToCsm.addAll(ics);
//                toPostToCsm.removeAll(csm);
//                // find contacts i need to remove from my contacts (do in onPostExecute)
//                toDeleteFromCon.addAll(con);
//                toDeleteFromCon.removeAll(ics);
//                toDeleteFromCon.removeAll(csm);
//                // now post the contacts that need to be posted to the api
//                for(long id:toPostToCsm) {
//                    if(!con.contains(id)) {
//                        Log.i(TAG, "autoaccepting new contact, posting to api: " + id);
//                    } else {
//                        Log.i(TAG, "this should not happen: local contact that i can see but cant see me, reposting to api (csm): " + id);
//                    }
//                    apiCallPostContact.setContactId(id);
//                    apiCallPostContact.callAndParse();
//                }
//                // now gather the names of the contacts i need to add locally
//                for(long id:toAddToCon) {
//                    Log.i(TAG, "requesting remote name for new contact (id="+id+")");
//                    apiCallGetProfile = new ApiCallGetProfile(id);
//                    apiCallGetProfile.callAndParse();
//                    newContacts.put(id, apiCallGetProfile.getContact());
//                }
//                apiCallsSucceeded = true;
//            } catch (IOException e) {
//                e.printStackTrace();
//                return "IOException: unable to complete all api requests";
//            } catch (JSONException e) {
//                e.printStackTrace();
//                return "JSONExeption: unable to complete all api request";
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            if (apiCallsSucceeded) {
//                for (long id : toAddToCon) {
//                    if (csm.contains(id)) {
//                        Log.i(TAG, "adding contact to local contacts (contact known in api but not in local contacts!?): " + id);
//                    } else {
//                        Log.i(TAG, "adding contact to local contacts (autoaccept): " + id);
//                    }
//                    try {
//                        DbContact.add(id);      // TODO: what about profiles?
//                    } catch (CliqueDbException e) {
//                        e.printStackTrace();
//                    }
//                }
//                for (long id : toDeleteFromCon) {
//                    Log.i(TAG, "deleting contact from local list (triggered by remote delete): " + id);
//                    try {
//                        DbContact.remove(id);   // TODO: what about profiles?
//                    } catch (CliqueDbException e) {
//                        e.printStackTrace();
//                    }
//                }
//                notifyDatabaseUpdate();
//                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_contacts_synced), Toast.LENGTH_SHORT);
//                toast.show();
//            } else { // apiCallsSucceeded == false
//                Toast toast = Toast.makeText(getApplicationContext(), "failed to sync", Toast.LENGTH_SHORT);
//                toast.show();
//            }
//        }
//    }

}
