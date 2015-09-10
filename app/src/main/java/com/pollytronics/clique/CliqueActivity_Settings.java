package com.pollytronics.clique;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.pollytronics.clique.lib.CliqueActivity;
import com.pollytronics.clique.lib.base.Profile;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.local.DbSelfProfile;

/**
 * TODO: (syncing) syncing local and remote database for self profile should happen more elegantly, now its done at hoc and prone to all kinds of problems
 */
public class CliqueActivity_Settings extends CliqueActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "CliqueActivity_Settings";

    private EditText setIdEditText;
    private EditText setNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliqueactivity_settings);

        Spinner updateRateSpinner = (Spinner) findViewById(R.id.spinner_update_rate);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.update_rate_choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        updateRateSpinner.setAdapter(adapter);
        updateRateSpinner.setOnItemSelectedListener(this);
        updateRateSpinner.setSelection(getCliquePreferences().getUpdateRate());   // call this at the end or it will not work

        setIdEditText = (EditText) findViewById(R.id.edittext_setid);
        Button setIdButton = (Button) findViewById(R.id.button_setid);
        Button setNameButton = (Button) findViewById(R.id.button_setname);
        setNameEditText = (EditText) findViewById(R.id.edittext_setname);

        CheckBox enableCompassCheckBox = (CheckBox) findViewById(R.id.checkbox_enable_compass);
        enableCompassCheckBox.setChecked(getCliquePreferences().getCompassEnabled());
        CheckBox enableSunChechBox = (CheckBox) findViewById(R.id.checkbox_enable_sun);
        enableSunChechBox.setChecked(getCliquePreferences().getSunEnabled());

        setIdEditText.setHint(Long.toString(getCliquePreferences().getAccountId()));

        setIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "don't be silly", Toast.LENGTH_SHORT).show();   //TODO: this should disappear
            }
        });

        setNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    String name = setNameEditText.getText().toString();
                    Log.i(TAG, "updating local self profile");
                    try {
                        DbSelfProfile.set(new Profile(name));
                    } catch (CliqueDbException e) {
                        e.printStackTrace();
                        Log.i(TAG, "could not update local profile");
                    }
                    Log.i(TAG, "updating remote self profile");
                    CliqueSyncer.getInstance(CliqueActivity_Settings.this).poke();  // TODO: get rid of that context shit
                } else {
                    Toast.makeText(getApplicationContext(), "no network", Toast.LENGTH_SHORT).show();
                }
            }
        });

        enableCompassCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCliquePreferences().setCompassEnabled(((CheckBox) v).isChecked());
            }
        });

        enableSunChechBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCliquePreferences().setSunEnabled(((CheckBox) v).isChecked());
            }
        });

        try {
            setNameEditText.setHint(DbSelfProfile.get().getName());
        } catch (CliqueDbException e) {
            e.printStackTrace();
        }
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

    @SuppressWarnings("EmptyMethod")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
