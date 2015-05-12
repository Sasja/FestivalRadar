package com.pollytronics.festivalradar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.pollytronics.festivalradar.lib.RadarContact;


public class SettingsRadarActivity extends RadarActivity {

    private static final String TAG = "SettingsRadarActivity";

    private EditText setIdEditText;


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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);    //this will add the global menu actions also
        getMenuInflater().inflate(R.menu.settings_radar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
