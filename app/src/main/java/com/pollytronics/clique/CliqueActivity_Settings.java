package com.pollytronics.clique;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.pollytronics.clique.lib.CliqueActivity;
import com.pollytronics.clique.lib.api_v01.ApiCallGetProfile;
import com.pollytronics.clique.lib.api_v01.ApiCallPostProfile;
import com.pollytronics.clique.lib.base.Contact;

import org.json.JSONException;

import java.io.IOException;

/**
 * TODO: the location update does not do a thing anymore as the update rate is set once in the google play service at the moment
 */


public class CliqueActivity_Settings extends CliqueActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "CliqueActivity_Settings";

    private EditText setIdEditText;
    private EditText setNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliqueactivity_settings);

        Spinner spinner = (Spinner) findViewById(R.id.spinner_update_rate);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.update_rate_choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setSelection(getCliquePreferences().getUpdateRate());
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        setIdEditText = (EditText) findViewById(R.id.edittext_setid);
        Button setIdButton = (Button) findViewById(R.id.button_setid);
        Button setNameButton = (Button) findViewById(R.id.button_setname);
        setNameEditText = (EditText) findViewById(R.id.edittext_setname);

        setIdEditText.setHint(Long.toString(getCliqueDatabase().getSelfContact().getGlobalId()));

        setIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Long id;
                try {
                    id = Long.decode(setIdEditText.getText().toString());
                } catch (NumberFormatException e) {
                    Log.i(TAG, "thats not a number, cant set this ID");
                    return;
                }
                Contact selfContact = getCliqueDatabase().getSelfContact();
                selfContact.setGlobalId(id);
                getCliqueDatabase().updateSelfContact(selfContact);
            }
        });

        setNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    String name = setNameEditText.getText().toString();
                    new setRemoteProfileNameTask(name).execute();
                } else {
                    Toast.makeText(getApplicationContext(), "no network", Toast.LENGTH_SHORT).show();
                }
            }
        });

        new getRemoteProfileNameIntoHintTask().execute();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, String.format("onItemSelected() position=%d id=%d",position, id));
        Log.i(TAG, String.format("set update rate to %d", position));
        getCliquePreferences().setUpdateRate(position);
        getBoundCliqueService().notifyNewSettings();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.i(TAG, "onNothingSelected()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);    //this will add the global menu actions also
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private class setRemoteProfileNameTask extends AsyncTask<Void, Void, String> {
        private final ApiCallPostProfile postProfile = new ApiCallPostProfile();

        private boolean apiCallSucceeded = false;

        public setRemoteProfileNameTask(String name) {
            postProfile.setName(name);
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "gathering selfId");
            postProfile.collectData(getCliqueDatabase());
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "calling api from setRemoteProfileNameTask");
            try {
                postProfile.callAndParse();
                apiCallSucceeded = true;
            } catch (IOException e) {
                Log.i(TAG,"IOException: unable to complete all API requests");
                return "IOException: unable to complete all API requests";
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(apiCallSucceeded) {
                Log.i(TAG, "posting new name succeeded");
                Toast.makeText(getApplicationContext(), "new name updated", Toast.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "posting new name to server failed");
                Toast.makeText(getApplicationContext(), "name update failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class getRemoteProfileNameIntoHintTask extends AsyncTask<Void, Void, String> {
        private final ApiCallGetProfile getProfile = new ApiCallGetProfile();

        private String remoteName = "";
        private boolean apiCallSucceeded = false;

        @Override
        protected void onPreExecute() { getProfile.setRequestedId(getCliqueDatabase().getSelfContact().getGlobalId()); }

        @Override
        protected String doInBackground(Void... params) {
            try {
                getProfile.callAndParse();
                apiCallSucceeded = true;
            } catch (IOException e) {
                e.printStackTrace();
                return "IOException: unable to complete api requests";
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (apiCallSucceeded) {
                try {
                    setNameEditText.setHint(getProfile.getContact().getName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
