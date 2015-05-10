package com.pollytronics.festivalradar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pollytronics.festivalradar.lib.RadarContact;


public class AddContactByUsernameRadarActivity extends RadarActivity {

    private final String TAG = "AddContactByUsername";
    private EditText editTextAddContactName;
    private EditText editTextAddContactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact_by_username);

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
                    editTextAddContactId.setText("");
                    editTextAddContactName.setText("");
                } catch (NumberFormatException e) {
                    Log.i(TAG, "thats not a valid id, lets get a random one...");
                    getRadarDatabase().addContact(newContact);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_add_contact_by_username, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
