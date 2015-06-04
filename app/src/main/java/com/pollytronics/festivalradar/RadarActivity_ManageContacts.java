package com.pollytronics.festivalradar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pollytronics.festivalradar.lib.RadarContact;
import com.pollytronics.festivalradar.lib.api.ApiCallDeleteContact;
import com.pollytronics.festivalradar.lib.api.ApiCallGetContactsIsee;
import com.pollytronics.festivalradar.lib.api.ApiCallGetContactsSeeme;
import com.pollytronics.festivalradar.lib.api.ApiCallPostContact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;


public class RadarActivity_ManageContacts extends RadarActivity {

    private static final String TAG = "ManageContactRadarAct";
    private DialogFragment mDialog;
    private RadarContact clickedContact;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radaractivity_manage_contacts);

        listView = (ListView) findViewById(R.id.listview_manage_contacts);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.i(TAG, "onItemClick i=" + position + " id=" + id);
                mDialog = new ContactActionDialogFragment();
                clickedContact = (RadarContact) adapterView.getAdapter().getItem(position);
                mDialog.show(getSupportFragmentManager(), "ContactActionDialog");
            }
        });
        updateContactListView();
    }

    public void onDelete(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){
            final ApiCallDeleteContact deleteContact = new ApiCallDeleteContact();
            deleteContact.collectData(getRadarDatabase());
            deleteContact.setContactId(clickedContact.getID());
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        deleteContact.callAndParse();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            Log.i(TAG, "posting a delete request to api for contact id: " + clickedContact.getID());
            thread.start();
            Log.i(TAG, "deleting selected radar contact (id=" + clickedContact.getID() + ")");
            getRadarDatabase().removeContact(clickedContact);
            Toast toast = Toast.makeText(getApplicationContext(), "contact removed", Toast.LENGTH_SHORT);
            toast.show();
            notifyDatabaseUpdate();         // TODO: this shouldn't be called here but happen autamatically
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "no connection", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void onDeny(){
        Log.i(TAG, "canceled deleting radar contact");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.manage_contacts_radar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_contact_by_username) {
            startActivity(new Intent(this, RadarActivity_AddContactByUsername.class));
            return true;
        } else if (id == R.id.action_sync_contacts) {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                Log.i(TAG, "syncing contact data");
                new SyncToWebserviceTask().execute();
            } else {
                Log.i(TAG, "cannot sync contact data: no network");
                Toast toast = Toast.makeText(getApplicationContext(), "no network", Toast.LENGTH_SHORT);
                toast.show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * set up the listView with data from the Database
     */
    private void updateContactListView(){
        ArrayList<RadarContact> contacts = new ArrayList<>(getRadarDatabase().getAllContacts());
        contacts.add(getRadarDatabase().getSelfContact());
        Collections.sort(contacts, new Comparator<RadarContact>() {
            @Override
            public int compare(RadarContact radarContact1, RadarContact radarContact2) {
                int result = radarContact1.getName().toUpperCase().compareTo(radarContact2.getName().toUpperCase());
                if (result == 0) {
                    result = ((Long)radarContact1.getID()).compareTo(radarContact2.getID());
                }
                return result;
            }
        });
        ArrayAdapter<RadarContact> adapter = (ArrayAdapter<RadarContact>) listView.getAdapter();
        if(adapter==null) {
            adapter = new ArrayAdapter<RadarContact>(this, R.layout.manage_contacs_list_item, contacts) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    RadarContact contact = getItem(position);
                    View view;
                    if (convertView == null) {
                        view = LayoutInflater.from(RadarActivity_ManageContacts.this).inflate(R.layout.manage_contacs_list_item, null);   //TODO: figure out why lint complains
                    } else {
                        view = convertView;
                    }
                    TextView tv_name = (TextView) view.findViewById(R.id.textview_contact_name);
                    tv_name.setText(contact.getName() + " (" + Long.toString(contact.getID()) + ")");
                    TextView tv_extra = (TextView) view.findViewById(R.id.textview_contact_extra);
                    tv_extra.setText(contact.getLastBlip().toString());
                    return view;
                }
            };
            listView.setAdapter(adapter);
        } else {
            //just clear everything and rebuild it
            adapter.clear();
            for(RadarContact c : contacts) adapter.add(c);
        }
    }

   /**
     * gets called by service when it has updated the database with new info
     */
    @Override
    public void notifyDatabaseUpdate() {
        super.notifyDatabaseUpdate();
        updateContactListView();
    }

    public static class ContactActionDialogFragment extends DialogFragment {

        RadarActivity_ManageContacts mRadarActivity;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mRadarActivity = (RadarActivity_ManageContacts) activity;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.dialog_delete_contact))
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mRadarActivity.onDeny();
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mRadarActivity.onDelete();
                        }
                    })
                    .create();
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
        private ApiCallPostContact postContact = new ApiCallPostContact();
        private ApiCallGetContactsSeeme apiCallGetContactsSeeme = new ApiCallGetContactsSeeme();
        private ApiCallGetContactsIsee apiCallGetContactsIsee = new ApiCallGetContactsIsee();
        private Set<Long> con = new HashSet<Long>();
        private Set<Long> ics = new HashSet<Long>();
        private Set<Long> csm = new HashSet<Long>();
        private Set<Long> toDeleteFromCon = new HashSet<Long>();
        private Set<Long> toAddToCon = new HashSet<Long>();
        private Set<Long> toPostToCsm = new HashSet<Long>();

        @Override
        protected void onPreExecute() {
            // get selfId (into ApiCall objects)
            postContact.collectData(getRadarDatabase());
            apiCallGetContactsSeeme.collectData(getRadarDatabase());
            apiCallGetContactsIsee.collectData(getRadarDatabase());
            // construct list of ids in local contacts
            for (RadarContact c : getRadarDatabase().getAllContacts()) {
                con.add(c.getID());
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "calling api");
            try {
                apiCallGetContactsIsee.callAndParse();
                apiCallGetContactsSeeme.callAndParse();
                ics = apiCallGetContactsIsee.getCollection();
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
                    postContact.setContactId(id);
                    postContact.callAndParse();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "IOException: unable to complete all api requests";
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            for(long id:toAddToCon){
                String contactName="";
                if (csm.contains(id)) {
                    Log.i(TAG, "adding contact to local contacts (contact known in api but not in local contacts!?): " + id);
                    contactName = "i forgot";
                } else {
                    Log.i(TAG, "adding contact to local contacts (autoaccept): " + id);
                    contactName = "autoaccepted";
                }
                RadarContact newContact = new RadarContact().setName(contactName).setID(id);
                getRadarDatabase().addContactWithId(newContact);
            }
            for(long id:toDeleteFromCon){
                Log.i(TAG, "deleting contact from local list (triggered by remote delete): " + id);
                getRadarDatabase().removeContactById(id);
            }
            notifyDatabaseUpdate();             // TODO: not sure this needs to be called
            Toast toast = Toast.makeText(getApplicationContext(), "contacs synced", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

}
