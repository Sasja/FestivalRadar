package com.pollytronics.clique;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pollytronics.clique.lib.CliqueActivity;
import com.pollytronics.clique.lib.api_v01.ApiCallPostProfile;
import com.pollytronics.clique.lib.base.Contact;
import com.pollytronics.clique.lib.database.CliqueDbException;

import org.json.JSONException;

import java.io.IOException;

/**
 * Activity that gets started when the user hasn't got a profile yet, it requests a userid from the api and requests a user name from the user
 */
public class CliqueActivity_Welcome extends CliqueActivity {

    private static final String TAG = "CliqueActivity_Welcome";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliqueactivity_welcome);
        final EditText newUsernameEditText = (EditText) findViewById(R.id.edittext_new_username);
        newUsernameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE ||
                   event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    String name = newUsernameEditText.getText().toString();
                    new requestNewProfileTask(name).execute();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /**
     * duplicate code alert... just need to cram this feature in, will clean up later... hehe
     */
    private class requestNewProfileTask extends AsyncTask<Void, Void, String> {
        private ApiCallPostProfile postProfile;

        private boolean apiCallSucceeded = false;

        private Contact newUserProfile;

        public requestNewProfileTask(String name) {
            try {
                postProfile = new ApiCallPostProfile(new Contact(0, name));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "calling api from setRemoteProfileNameTask");
            try {
                postProfile.callAndParse();
                newUserProfile = postProfile.getContact();
                apiCallSucceeded = true;
            } catch (IOException e) {
                Log.i(TAG,"IOException: unable to complete all API requests");
                return "IOException: unable to complete all API requests";
            } catch (JSONException e) {
                Log.i(TAG,"JSONExeption: unable to parse new userid form api response");
                return "JSONExeption";
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(apiCallSucceeded) {
                Log.i(TAG, "posting new profile and retrieving userid succeeded");
                Log.i(TAG, "received userid="+newUserProfile.getGlobalId()+" now storing in locally");
                try {
                    getCliqueDb().updateSelfContact(newUserProfile);
                } catch (CliqueDbException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "profile updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.i(TAG, "posting new name to server failed");
                Toast.makeText(getApplicationContext(), "name update failed, network?", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
