package com.pollytronics.festivalradar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pollytronics.festivalradar.lib.RadarContact;
import com.pollytronics.festivalradar.lib.api_v01.ApiCallPostContact;

import java.io.IOException;

/**
 * TODO: post contact to api when adding it locally
 */
public class RadarActivity_AddContactByUsername extends RadarActivity {

    private final String TAG = "AddContactByUsername";
    private EditText editTextAddContactName;
    private EditText editTextAddContactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radaractivity_add_contact_by_username);

        editTextAddContactName = (EditText) findViewById(R.id.edittext_add_contact_name);
        editTextAddContactId = (EditText) findViewById(R.id.edittext_add_contact_id);

        Button addContactButton = (Button) findViewById(R.id.button_add_contact);
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {        //TODO: clean this up a bit, its doing to many things at once
                String name = editTextAddContactName.getText().toString();
                if(name.length()==0) name = "empty";
                RadarContact newContact = (new RadarContact()).setName(name).addBlip(getRadarDatabase().getSelfContact().getLastBlip());    // make up some blip, TODO: make some sense here, think about contacts without blips
                Long id;
                try {
                    id = Long.decode(editTextAddContactId.getText().toString());
                    newContact.setID(id);
                    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()){
                        final ApiCallPostContact postContact = new ApiCallPostContact();
                        postContact.collectData(getRadarDatabase());
                        postContact.setContactId(id);
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {     // TODO: do what when this fails? should at least do some failure notification Toast or something
                                try {
                                    postContact.callAndParse();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        thread.start();

                        getRadarDatabase().addContactWithId(newContact);

                        Toast toast = Toast.makeText(getApplicationContext(), "new user added", Toast.LENGTH_SHORT);
                        toast.show();
                        editTextAddContactId.setText("");
                        editTextAddContactName.setText("");

                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "no connection", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                } catch (NumberFormatException e) {
                    Log.i(TAG, "invalid id input...");
                    Toast toast = Toast.makeText(getApplicationContext(), "invalid id value", Toast.LENGTH_SHORT);
                    toast.show();
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
