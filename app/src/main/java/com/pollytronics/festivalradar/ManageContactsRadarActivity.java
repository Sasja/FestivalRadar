package com.pollytronics.festivalradar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.pollytronics.festivalradar.lib.RadarContact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class ManageContactsRadarActivity extends RadarActivity {

    private static final String TAG = "ManageContactRadarActivity";

    private ListView listView;
    private EditText editTextAddContactName;
    private EditText editTextAddContactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radaractivity_manage_contacts);

        listView = (ListView) findViewById(R.id.listview_manage_contacts);
        editTextAddContactName = (EditText) findViewById(R.id.edittext_add_contact_name);
        editTextAddContactId = (EditText) findViewById(R.id.edittext_add_contact_id);

        Button addContactButton = (Button) findViewById(R.id.button_add_contact);
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextAddContactName.getText().toString();
                if(name.length()==0) name = "empty";
                RadarContact newContact = (new RadarContact()).setName(name).addBlip(getRadarDatabase().getSelfContact().getLastBlip());    // make up some blip, TODO: make some sense here, think about contacts without blips
                Long id;
                try {
                    id = Long.decode(editTextAddContactId.getText().toString());
                    newContact.setID(id);
                    getRadarDatabase().addContactWithId(newContact);
                } catch (NumberFormatException e) {
                    Log.i(TAG, "thats not a valid id, lets get a random one...");
                    getRadarDatabase().addContact(newContact);
                }
                //getRadarDatabase().addContact((new RadarContact()).setName(name).addBlip(getRadarDatabase().getSelfContact().getLastBlip()));   //ugly temporary shit
                updateContactListView();
            }
        });

        updateContactListView();
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
        return super.onOptionsItemSelected(item);
    }

    /**
     * set up the listView with data from the Database
     */
    private void updateContactListView(){
        //ArrayList<String> stringArray = new ArrayList<String>();
        ArrayList<RadarContact> contacts = new ArrayList<RadarContact>(getRadarDatabase().getAllContacts());
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
        //for(RadarContact c:contacts) stringArray.add(c.getName()+" ("+c.getLastBlip().toString()+")");
        ArrayAdapter<RadarContact> adapter = (ArrayAdapter<RadarContact>) listView.getAdapter();
        if(adapter==null) {
            adapter = new ArrayAdapter<RadarContact>(this, R.layout.manage_contacs_list_item, contacts) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    //View view = super.getView(position, convertView, parent);
                    RadarContact contact = getItem(position);
                    View view;
                    if (convertView == null) {
                        view = LayoutInflater.from(ManageContactsRadarActivity.this).inflate(R.layout.manage_contacs_list_item, null);
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
}
