package com.pollytronics.clique;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.pollytronics.clique.lib.base.Contact;
import com.pollytronics.clique.lib.api_v01.ApiCallGetPings;

import java.io.IOException;
import java.util.List;

/**
 * TODO: fix duplicate code everywhere checking for network availability (also other files)
 * TODO: animate adding/ignoring contact (add => fly to the right, ignore => shrink or dissolve or smth)
 * TODO: turning screen will not remember the contacts in the ping list
 * TODO: maybe the pingtask belongs in the activity class?
 * TODO: some feedback when pinging is in progress
 *
 */
public class Fragment_Contacts_Ping extends MVP_Fragment_Contacts {
    @SuppressWarnings("unused")
    private static final String TAG = "Frag_Contacts_Ping";

    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_ping, container, false);
        listView = (ListView) view.findViewById(R.id.listview_ping);
        Button pingButt = (Button) view.findViewById(R.id.button_ping);
        pingButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()){
                    new PingTask().execute();
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "pinging...", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "no network", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        return view;
    }

    private void fillListViewFromList(ListView aListView, List<Contact> contacts) {
        Log.i(TAG, "filling listView with cliquecontacts retrieved from api");
        CliqueContactAdapter adapter = (CliqueContactAdapter) aListView.getAdapter();
        if (adapter == null) {
            adapter = new CliqueContactAdapter(getActivity(), contacts);
        } else {
            adapter.clear();
            for(Contact c: contacts) adapter.add(c);
        }
        aListView.setAdapter(adapter);
    }


    /**
     * this class  will try to update aListview after it has
     * queried the webservice for other contacts. It should only display contacts
     * that aren't known yet. (that is a responsability of api)
     *
     * 1) do the api call for POST ping
     * 2) wait a few seconds, then do the GET ping api call
     * 2) update the listView through the CliqueContactAdapter
     *
     * TODO: do the post api call once it is really implemented and the waiting and whatnot
     *
     */
    private class PingTask extends AsyncTask<Void, Void, String> {
        private final ApiCallGetPings getPings = new ApiCallGetPings();
        private boolean apiCallSucceeded = false;

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "gathering own user id");
            getPings.collectData(getCligueDb());
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "calling api from PingTask");
            try {
                getPings.callAndParse();
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
                Log.i(TAG,"using/aplying the responses of the webservice");
                fillListViewFromList(listView, getPings.getAllPingContacts());
            } else {
                Log.i(TAG, "the api call has failed, not doing anything in onPostExcecute");
            }
        }
    }

    //TODO: kind of duplicate code here with mycontacts
    private class CliqueContactAdapter extends ArrayAdapter<Contact> {

        private static final int layout_resource = R.layout.list_item_ping;

        public CliqueContactAdapter(Context context, List<Contact> objects) {
            super(context, layout_resource, R.id.textview_contact_name,objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            final Contact contact = getItem(position);
            // TODO: further apply the values of contact to the view object
            Button connectButt = (Button) view.findViewById(R.id.button_ping_connect);
            connectButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {   //TODO: clean up / refactor
                    Log.i(TAG, "onClick()... adding contact");
                    ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()){
                        getContactActivity().addNewContact(contact);
                        remove(contact);
                    } else {
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), "no network", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });
            Button ignoreButt = (Button) view.findViewById(R.id.button_ping_ignore);
            ignoreButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(contact);
                }
            });
            return view;
        }
    }

}
