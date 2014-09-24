package com.pollytronics.festivalradar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.pollytronics.festivalradar.lib.RadarContact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class ManageContactsRadarActivity extends RadarActivity {

    private static final String TAG = "ManageContactRadarActivity";

    private ListView listView;
    private EditText editTextAddContactName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radaractivity_manage_contacts);

        listView = (ListView) findViewById(R.id.listview_manage_contacts);
        editTextAddContactName = (EditText) findViewById(R.id.edittext_add_contact_name);

        Button addContactButton = (Button) findViewById(R.id.button_add_contact);
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextAddContactName.getText().toString();
                if(name.length()==0) name = "empty";
                getRadarDatabase().addContact((new RadarContact()).setName(name).addBlip(getRadarDatabase().getSelfContact().getLastBlip()));   //ugly temporary shit
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    private void updateContactListView(){
        ArrayList<String> stringArray = new ArrayList<String>();
        ArrayList<RadarContact> contacts = new ArrayList<RadarContact>(getRadarDatabase().getAllContacts());
        contacts.add(getRadarDatabase().getSelfContact());
        Collections.sort(contacts, new Comparator<RadarContact>() {
            @Override
            public int compare(RadarContact radarContact1, RadarContact radarContact2) {
                return radarContact1.getName().compareTo(radarContact2.getName());
            }
        });
        for(RadarContact c:contacts) stringArray.add(c.getName()+" ("+c.getLastBlip().toString()+")");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.manage_contacs_list_item, stringArray);
        listView.setAdapter(adapter);
    }

    @Override
    public void notifyDatabaseUpdate() {
        super.notifyDatabaseUpdate();
        updateContactListView();
    }
}
