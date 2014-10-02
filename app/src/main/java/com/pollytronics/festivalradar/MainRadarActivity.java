package com.pollytronics.festivalradar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pollytronics.festivalradar.lib.RadarBlip;
import com.pollytronics.festivalradar.lib.RadarContact;
import com.pollytronics.festivalradar.lib.RadarView;

import java.util.Collection;

/**
 * Main app activity, it should give an overview of the situation and provide a simple GUI to
 * the most likely actions a user would want to perform
 */
public class MainRadarActivity extends RadarActivity {

    private static final String TAG = "MainRadarActivity";

    ToggleButton toggleService;
    RadarView radarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radaractivity_main);

        toggleService = (ToggleButton) findViewById(R.id.toggle_service);
        toggleService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(toggleService.isChecked()) {
                    startAndBindRadarService();
                } else {
                    unbindAndStopRadarService();
                }
            }
        });

        radarView = (RadarView) findViewById(R.id.radar_view);
        radarView.setBearing(0);
        feedDataToRadarView();
    }

    @Override
    protected void onRadarServiceDisconnected(){
        toggleService.setChecked(false);
    }

    @Override
    protected void onRadarServiceConnected(){
        toggleService.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);        //add this to the generated code to include the 'global' option items described in RadarActivity
        getMenuInflater().inflate(R.menu.main_radaractivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_main_dummy) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void notifyDatabaseUpdate() {
        super.notifyDatabaseUpdate();
        feedDataToRadarView();
    }

    private void feedDataToRadarView(){
        RadarBlip centerLocation = getRadarDatabase().getSelfContact().getLastBlip();
        radarView.setCenterLocation(centerLocation);
        radarView.removeAllContacts();
        Collection<RadarContact> contacts = getRadarDatabase().getAllContacts();
        for(RadarContact c:contacts){
            radarView.addContact(c);
        }
        radarView.invalidate();
    }
}
