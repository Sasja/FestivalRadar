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

import com.pollytronics.clique.lib.api_v01.ApiCallGetPings;
import com.pollytronics.clique.lib.base.Contact;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.local.DbContact;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: (syncing) fix duplicate code everywhere checking for network availability (also other files)
 * TODO: (gui) animate adding/ignoring contact (add => fly to the right, ignore => shrink or dissolve or smth)
 * TODO: (syncing) turning screen will not remember the contacts in the ping list(figure out working with Bundle savedinstance state in onCreate)
 * TODO: (syncing) maybe the pingtask belongs in the activity class?
 * TODO: (syncing) some feedback when pinging is in progress (see basicsyncadapter demo for inspiration (progressbar))
 *
 */
public class Fragment_Contacts_Ping extends MVP_Fragment_Contacts {
    @SuppressWarnings("unused")
    private static final String TAG = "Frag_Contacts_Ping";

    private ListView listView;

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
     * that aren't known yet.
     *
     * 1) do the api call for POST ping
     * 2) wait a few seconds, then do the GET ping api call
     * 2) update the listView through the CliqueContactAdapter
     *
     * TODO: (api) do the post api call once it is really implemented and the waiting and whatnot
     *
     */
    private class PingTask extends AsyncTask<Void, Void, String> {
        private ApiCallGetPings getPings;
        private boolean apiCallSucceeded = false;

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "gathering own user id");
            getPings = new ApiCallGetPings(getContactActivity().getCliquePreferences().getAccountId());
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(apiCallSucceeded) {
                Log.i(TAG, "using/aplying the responses of the webservice");
                List<Contact> apiContacts = getPings.getAllPingContacts();
                List<Contact> allReadyKnown = new ArrayList<>();
                for (Contact c : apiContacts) {
                    Log.i(TAG, String.format("checking if contact %s is allready known",c.getName()));
                    try {
                        if(DbContact.isContact(c.getGlobalId())) {
                            allReadyKnown.add(c);
                            Log.i(TAG, String.format("yup %s is allready known", c.getName()));
                        }
                    } catch (CliqueDbException e) {
                        e.printStackTrace();
                    }
                }
                apiContacts.removeAll(allReadyKnown);
                fillListViewFromList(listView, apiContacts);
            } else {
                Log.i(TAG, "the api call has failed, not doing anything in onPostExcecute");
            }
        }
    }

    private class CliqueContactAdapter extends ArrayAdapter<Contact> {

        private static final int layout_resource = R.layout.list_item_ping;

        public CliqueContactAdapter(Context context, List<Contact> objects) {
            super(context, layout_resource, R.id.textview_contact_name,objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            final Contact contact = getItem(position);
            // TODO: (gui) further apply the values of contact to the view object
            Button connectButt = (Button) view.findViewById(R.id.button_ping_connect);
            connectButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
