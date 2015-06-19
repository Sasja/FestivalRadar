package com.pollytronics.festivalradar;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.pollytronics.festivalradar.lib.RadarContact;

import java.util.List;


public class Fragment_Contacts_Ping extends MyViewPagerFragment {

    @SuppressWarnings("unused")
    private static final String TAG = "Frag_Contacts_Ping";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_ping, container, false);
        final ListView listView = (ListView) view.findViewById(R.id.listview_ping);
        Button pingButt = (Button) view.findViewById(R.id.button_ping);
        pingButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillListViewFromRemotePingAsync(listView);
            }
        });
        return view;
    }

    /**
     * this method launches a thread that will try to update aListview after it has
     * queried the webservice for other contacts. It should only display contacts
     * that aren't known yet.
     *
     * 1) do the api call for POST ping
     * 2) wait a few seconds, then do the GET ping api call
     * 2) update the listView through the RadarContactAdapter
     *
     * TODO: do the get ping api call using the ApiCallGetPings it has a method that returns a list of radarcontacts for your convenience
     * TODO: do the post api call once it is really implemented and the waiting and whatnot
     *
     * @param aListView
     */
    private void fillListViewFromRemotePingAsync(ListView aListView) {
        Log.i(TAG, "fillListViewFromRemotePingAsync");


    }

    //TODO: kind of duplicate code here with mycontacts
    private class RadarContactAdapter extends ArrayAdapter<RadarContact> {

        private static final int layout_resource = R.layout.list_item_ping;

        public RadarContactAdapter(Context context, List<RadarContact> objects) {
            super(context, layout_resource, R.id.textview_contact_name,objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            RadarContact contact = getItem(position);
            TextView tv_extra = (TextView) view.findViewById(R.id.textview_contact_extra);
            tv_extra.setText("extra text?");
            return view;
        }
    }

}
