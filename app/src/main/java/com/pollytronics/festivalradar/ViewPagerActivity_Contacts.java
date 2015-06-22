package com.pollytronics.festivalradar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.pollytronics.festivalradar.lib.RadarContact;
import com.pollytronics.festivalradar.lib.api_v01.ApiCallPostContact;

import java.io.IOException;

/**
 * TODO: Study https://developer.android.com/training/sync-adapters/index.html and consider implementing such a thing
 */
public class ViewPagerActivity_Contacts extends RadarActivity_MyViewPagerAct {
    @SuppressWarnings("unused")
    private static final String TAG = "ViewPagerAct_Contacts";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewpageractivity_contacts, menu);
        return true;
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
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){
            Log.i(TAG, "adding contact locally");
            getRadarDatabase().addContactWithId(contact);
            Log.i(TAG, "launching task to add contact remotely");
            new postNewContactTask(contact).execute();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "no connection", Toast.LENGTH_SHORT);
            toast.show();
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
}
