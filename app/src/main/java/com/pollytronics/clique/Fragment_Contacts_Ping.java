package com.pollytronics.clique;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.pollytronics.clique.lib.base.Contact;
import com.pollytronics.clique.lib.base.Profile;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.local.DbContact;
import com.pollytronics.clique.lib.database.cliqueSQLite.local.DbPing;
import com.pollytronics.clique.lib.database.cliqueSQLite.sync.DbProfile;

import java.util.ArrayList;
import java.util.List;


/**
 * TODO: (syncing) fix duplicate code everywhere checking for network availability (also other files)
 * TODO: (gui) animate adding/ignoring contact (add => fly to the right, ignore => shrink or dissolve or smth)
 * TODO: (syncing) turning screen will not remember the contacts in the ping list(figure out working with Bundle savedinstance state in onCreate)
 * TODO: (syncing) some feedback when pinging is in progress (see basicsyncadapter demo for inspiration (progressbar))
 * TODO: (code) remove the duplicate code for showing toasts
 * TODO: (feature) implement the ignore button
 *
 */
public class Fragment_Contacts_Ping extends MVP_Fragment_Contacts {
    @SuppressWarnings("unused")
    private static final String TAG = "Frag_Contacts_Ping";

    private ListView listView;
    private PingLoop pingLoop = null;
    private Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_contacts_ping, container, false);
        listView = (ListView) view.findViewById(R.id.listview_ping);
        final Button pingButt = (Button) view.findViewById(R.id.button_ping);
        pingButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "pinging...", Toast.LENGTH_SHORT);
                    toast.show();
                    Log.i(TAG, "starting ping");
                    try {
                        com.pollytronics.clique.lib.database.cliqueSQLite.sync.DbPing.flush();
                    } catch (CliqueDbException e) {
                        e.printStackTrace();
                    }
                    handler.removeCallbacks(pingLoop);  // make sure there's not two running
                    pingLoop = new PingLoop();
                    pingLoop.run();
                } else {
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "no network", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        return view;
    }

    private void fillListViewFromList(ListView aListView, List<Contact> contacts) {
        Log.i(TAG, "running fillListViewFromList()");
        CliqueContactAdapter adapter = (CliqueContactAdapter) aListView.getAdapter();
        if (adapter == null) {
            adapter = new CliqueContactAdapter(getActivity(), contacts);
        } else {
            adapter.clear();
            for(Contact c: contacts) adapter.add(c);
        }
        aListView.setAdapter(adapter);
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
            final Button connectButt = (Button) view.findViewById(R.id.button_ping_connect);
            connectButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick()... adding contact");
                    try {
                        DbContact.add(contact.getGlobalId());
                        DbProfile.add(contact.getGlobalId(), contact); // needs to be overwritten later as ping does not give full profile details
                        DbPing.remove(contact.getGlobalId());
                        remove(contact);                                // remove it instantly from the listView
                    } catch (CliqueDbException e) {
                        e.printStackTrace();
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

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacks(pingLoop);
    }

    private class PingLoop implements Runnable {
        private static final int NLOOPS = 10;
        private static final int PERIOD_MS = 2000;
        private int remaining = NLOOPS;
        @Override
        public void run() {
            Log.i(TAG, "calling pingLoopHandler, remaining = " + remaining);
            if(remaining == NLOOPS){
                CliqueSyncer.getInstance(getActivity()).pokePingGetSet(true, true);
            }
            else {
                CliqueSyncer.getInstance(getActivity()).pokePingGetSet(true, false);
            }
            if (--remaining > 0) handler.postDelayed(pingLoop, PERIOD_MS);
        }
    }

    /**
     * TODO: (code) this is pretty retarded, clean it up
     */
    @Override
    public void notifyDatabaseUpdate() {
        super.notifyDatabaseUpdate();
        try {
            List<Pair<Long, String>> pings = DbPing.getPings();
            List<Contact> pingContacts = new ArrayList<>();
            List<Long> allreadyAdded = DbContact.getcanSeeme();
            if(pings != null) {
                for(Pair<Long, String> ping : pings) {
                    // must make sure i don't have the contact added locally allready
                    if(!allreadyAdded.contains(ping.first)) pingContacts.add(new Contact(ping.first, new Profile(ping.second)));
                }
            }
            fillListViewFromList(listView, pingContacts);
        } catch (CliqueDbException e) {
            e.printStackTrace();
        }
    }
}
