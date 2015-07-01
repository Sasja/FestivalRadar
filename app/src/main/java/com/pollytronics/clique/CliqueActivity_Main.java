package com.pollytronics.clique;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.pollytronics.clique.lib.CliqueActivity;
import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.gui_elements.RadarView;
import com.pollytronics.clique.lib.tools.nature.SunRelativePosition;

import java.util.Date;
import java.util.List;

/**
 * Main app activity, it should give an overview of the situation and provide a simple GUI to
 * the most likely actions a user would want to perform
 *
 * TODO: make the calculated bearing sensible when holding the phone upright or tilted looking from below or display a warning to prevent confusion
 * TODO: radarView is never invalidated when service and compass is off, so blips will not fade according to age.
 */
public class CliqueActivity_Main extends CliqueActivity implements SensorEventListener {

    private static final String TAG = "CliqueActivity_Main";
    private Switch toggleService;
    private RadarView radarView;
    private SensorManager mSensorManager;
    private Sensor mRotation;
    private boolean compassEnabled = false;
    private boolean sunEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliqueactivity_main);
        getSupportActionBar().setDisplayShowTitleEnabled(false);        //TODO: try not to do these things dynamically but in xml

        toggleService = (Switch) findViewById(R.id.toggle_service);
        toggleService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (toggleService.isChecked()) {
                    Log.i(TAG, "CliqueService button enabled");
                    startAndBindCliqueService();
                } else {
                    Log.i(TAG, "CliqueService button disabled");
                    unbindAndStopCliqueService();
                }
            }
        });

        radarView = (RadarView) findViewById(R.id.radar_view);
        radarView.setBearing(0);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);   // might return null!
    }

    @Override
    protected void onResume() {
        super.onResume();
        compassEnabled = getCliquePreferences().getCompassEnabled();
        if (compassEnabled && (mRotation != null)) {
            mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_GAME);  // motoG reports 7mA batt drain for mRotation.getPower()
        }
        if(!compassEnabled) { radarView.setBearing(0); }

        sunEnabled = getCliquePreferences().getSunEnabled();
        radarView.setSunEnabled(sunEnabled);

        radarView.setZoomRadius(getCliquePreferences().getZoomRadius());

        try {
            if(getCliqueDb().getSelfContact() == null) {
                Log.i(TAG, "selfContact == null, so starting welcome activity");
                Intent intent = new Intent(this, CliqueActivity_Welcome.class);
                startActivity(intent);
            }
        } catch (CliqueDbException e) {
            e.printStackTrace();
        }

        feedDataToRadarView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRotation != null) {
            mSensorManager.unregisterListener(this);
        }
        getCliquePreferences().setZoomRadius(radarView.getZoomRadius());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCliqueServiceDisconnected(){
        toggleService.setChecked(false);
    }

    @Override
    protected void onCliqueServiceConnected(){
        toggleService.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);        //add this to the generated code to include the 'global' option items described in CliqueActivity
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void notifyDatabaseUpdate() {
        super.notifyDatabaseUpdate();
        feedDataToRadarView();
    }

    private void feedDataToRadarView(){
        Blip centerLocation = null;
        try {
            centerLocation = getCliqueDb().getLastSelfBlip();
        } catch (CliqueDbException e) {
            e.printStackTrace();
        }
        radarView.setCenterLocation(centerLocation);
        radarView.removeAllContacts();
        List<Contact> contacts = null;
        try {
            contacts = getCliqueDb().getAllContacts();
        } catch (CliqueDbException e) {
            e.printStackTrace();
        }
        for(Contact c:contacts){
            try {
                radarView.updateContact(c, getCliqueDb().getLastBlip(c));
            } catch (CliqueDbException e) {
                e.printStackTrace();
            }
        }
        if(sunEnabled) {
            SunRelativePosition sunRelativePosition = new SunRelativePosition();
            Blip position = null;
            try {
                position = getCliqueDb().getLastSelfBlip();
            } catch (CliqueDbException e) {
                e.printStackTrace();
            }
            if (position != null) {
                sunRelativePosition.setCoordinate(position.getLongitude(), position.getLatitude());
                sunRelativePosition.setDate(new Date());
                radarView.setSunAzimuth(sunRelativePosition.getAzimuth());
                radarView.setSunElevation(sunRelativePosition.getElevation());
            }
        }
        radarView.invalidate();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // You're not supposed to do to much work  in this callback but this seems reasonable
        if(compassEnabled && (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)) {
            //Log.i(TAG, "sensor event received! : " + sensorEvent.toString());
            float[] rotMat = new float[16];
            float[] orient = new float[3];
            SensorManager.getRotationMatrixFromVector(rotMat, sensorEvent.values);
            SensorManager.getOrientation(rotMat, orient);
            double x;
            x = orient[0];
            radarView.setBearing(x*180.0/3.1415);
            radarView.invalidate();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}
