package com.pollytronics.festivalradar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;


public class SettingsRadarActivity extends RadarActivity {

    private static final String TAG = "SettingsRadarActivity";

    private SeekBar localisationSeekBar;
    private SeekBar cloudSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radaractivity_settings);

        localisationSeekBar = (SeekBar) findViewById(R.id.seekbar_localisation_update_rate);
        localisationSeekBar.setProgress(getRadarPreferences().getLocalisationUpdateTime_percent());
        cloudSeekBar = (SeekBar) findViewById(R.id.seekbar_cloud_update_rate);
        cloudSeekBar.setProgress(getRadarPreferences().getCloudUpdateTime_percent());

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
                if(isBoundToService()) {
                    getBoundRadarService().notifyNewSettings();
                }
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
                if(isBoundToService()) {
                    getBoundRadarService().notifyNewSettings();
                }
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
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
}
