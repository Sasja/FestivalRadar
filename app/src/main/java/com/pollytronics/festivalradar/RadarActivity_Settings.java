package com.pollytronics.festivalradar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.pollytronics.festivalradar.lib.RadarActivity;
import com.pollytronics.festivalradar.lib.base.RadarContact;
import com.pollytronics.festivalradar.lib.api_v01.ApiCallGetProfile;
import com.pollytronics.festivalradar.lib.api_v01.ApiCallPostProfile;

import java.io.IOException;

/**
 * TODO: the location update does not do a thing anymore as the update rate is set once in the google play service at the moment
 */


public class RadarActivity_Settings extends RadarActivity {

    private static final String TAG = "RadarActivity_Settings";

    private EditText setIdEditText;
    private EditText setNameEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.radaractivity_settings);

        SeekBar localisationSeekBar = (SeekBar) findViewById(R.id.seekbar_localisation_update_rate);
        localisationSeekBar.setProgress(getRadarPreferences().getLocalisationUpdateTime_percent());
        SeekBar cloudSeekBar = (SeekBar) findViewById(R.id.seekbar_cloud_update_rate);
        cloudSeekBar.setProgress(getRadarPreferences().getCloudUpdateTime_percent());
        setIdEditText = (EditText) findViewById(R.id.edittext_setid);
        Button setIdButton = (Button) findViewById(R.id.button_setid);
        Button setNameButton = (Button) findViewById(R.id.button_setname);
        setNameEditText = (EditText) findViewById(R.id.edittext_setname);

        localisationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getRadarPreferences().setLocalisationUpdateRate_percent(seekBar.getProgress());
                Log.i(TAG, "setting Localisation update rate to " + Integer.toString(seekBar.getProgress()));
                getBoundRadarService().notifyNewSettings();
            }
        });

        cloudSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getRadarPreferences().setCloudUpdateRate_percent(seekBar.getProgress());
                Log.i(TAG, "setting Cloud update rate to " + Integer.toString(seekBar.getProgress()));
                getBoundRadarService().notifyNewSettings();
            }
        });

        setIdEditText.setHint(Long.toString(getRadarDatabase().getSelfContact().getID()));

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
                RadarContact selfContact = getRadarDatabase().getSelfContact();
                selfContact.setID(id);
                getRadarDatabase().updateSelfContact(selfContact);
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
            postProfile.collectData(getRadarDatabase());
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
        protected void onPreExecute() { getProfile.setRequestedId(getRadarDatabase().getSelfContact().getID()); }

        @Override
        protected String doInBackground(Void... params) {
            try {
                getProfile.callAndParse();
                remoteName = getProfile.getName();
                apiCallSucceeded = true;
            } catch (IOException e) {
                e.printStackTrace();
                return "IOException: unable to complete api requests";
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            setNameEditText.setHint(remoteName);
        }
    }
}
