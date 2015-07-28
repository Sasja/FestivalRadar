package com.pollytronics.clique;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.tools.TimeFormatting;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Fragment_Contacts_MyContacts extends MVP_Fragment_Contacts {

    @SuppressWarnings("unused")
    private static final String TAG = "Frag_Cntcts_MyContacts";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_mycontacts, container, false);
        ListView listView = (ListView) (view.findViewById(R.id.listview_mycontacts));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick i=" + position + " id=" + id);
                Contact selectedContact = (Contact) parent.getAdapter().getItem(position);
                ((MVP_Activity_Contacts) getActivity()).confirmAndDeleteContact(selectedContact);
            }
        });
        fillListViewFromLocalDb(listView);
        return view;
    }
    
    private void fillListViewFromLocalDb(ListView listView) {
        List<Contact> localContacts;
        try {
            localContacts = getCligueDb().getAllContacts();
        } catch (CliqueDbException e) {
            e.printStackTrace();
            return;
        }
        sortContactListByName(localContacts);
        CliqueContactAdapter adapter = (CliqueContactAdapter) listView.getAdapter();
        if(adapter == null) {   // there is no adapter yet
            adapter = new CliqueContactAdapter(getActivity(), localContacts);
            listView.setAdapter(adapter);
        } else {                // lets reuse the current adapter
            adapter.clear();
            adapter.addAll(localContacts);
            adapter.notifyDataSetChanged();
        }
    }

    private void sortContactListByName(List<Contact> cList) {
        Collections.sort(cList, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                int result = lhs.getName().compareTo(rhs.getName());
                if (result == 0) result = lhs.getGlobalId() > rhs.getGlobalId() ? 1 : -1;
                return result;
            }
        });
    }

    /**
     * Gets called on every viewpagerfragment when it is called on the parent activity.
     */
    @Override
    public void notifyDatabaseUpdate() {
        super.notifyDatabaseUpdate();
        View view = getView();
        ListView listView = null;
        if(view != null) listView = (ListView) view.findViewById(R.id.listview_mycontacts);
        if (listView != null) {
            Log.i(TAG,"updating listview from notifyDatabaseUpdate");
            fillListViewFromLocalDb(listView);
        } else {
            Log.i(TAG, "failed to find listView from notifyDatabaseUpdate");
        }
    }


    /**
     * TODO: DRY (see ping)
     */
    private class CliqueContactAdapter extends ArrayAdapter<Contact> {

        private static final int layout_resource = R.layout.list_item_mycontacts;

        public CliqueContactAdapter(Context context, List<Contact> objects) {
            super(context, layout_resource, R.id.textview_contact_name, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Contact contact = getItem(position);
            TextView tv_extra = (TextView) view.findViewById(R.id.textview_contact_extra);
            Blip lastBlip = null;
            try {
                lastBlip = getCligueDb().getLastBlip(contact);
            } catch (CliqueDbException e) {
                e.printStackTrace();
            }
            if (lastBlip != null) {
                tv_extra.setText(TimeFormatting.ageStringFromSeconds(lastBlip.getAge_s(), getContext()) + " " + getContext().getString(R.string.timeformatting_ago));
            } else {
                tv_extra.setText(getContext().getString(R.string.no_data_yet));
            }
            return view;
        }
    }
}
