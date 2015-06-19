package com.pollytronics.festivalradar;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pollytronics.festivalradar.lib.RadarContact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Fragment_Contacts_MyContacts extends MyViewPagerFragment {

    @SuppressWarnings("unused")
    private static final String TAG = "Frag_Contacts_MyContacts";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_mycontacts, container, false);
        ListView listView = (ListView) (view.findViewById(R.id.listview_mycontacts));
        fillListViewFromLocalDb(listView);
        return view;
    }

    private void fillListViewFromLocalDb(ListView listView) {
        List<RadarContact> localContacts = new ArrayList<>(getRadarDatabase().getAllContacts());
        Collections.sort(localContacts);
        RadarContactAdapter adapter = (RadarContactAdapter) listView.getAdapter();
        if(adapter == null) {   // there is no adapter yet
            adapter = new RadarContactAdapter(getActivity(), localContacts);
        } else {                // lets reuse the current adapter
            adapter.clear();
            for(RadarContact c : localContacts) adapter.add(c);
        }
        listView.setAdapter(adapter);
    }

    //TODO: kind of duplicate code here with ping
    private class RadarContactAdapter extends ArrayAdapter<RadarContact> {

        private static final int layout_resource = R.layout.list_item_mycontacts;

        public RadarContactAdapter(Context context, List<RadarContact> objects) {
            super(context, layout_resource, R.id.textview_contact_name, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            RadarContact contact = getItem(position);
            TextView tv_extra = (TextView) view.findViewById(R.id.textview_contact_extra);
            tv_extra.setText(contact.getLastBlip().toString());
            return view;
        }
    }
}
