package com.pollytronics.clique;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
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
 * Main app activity, it should give an overview of the situation and provide a simple GUI for
 * the most likely actions a user would want to perform.
 *
 * TODO: check out these possible nullpointerexceptions lint complains about
 */
public class CliqueActivity_Main extends CliqueActivity implements SensorEventListener {

    private static final String TAG = "CliqueActivity_Main";
    private Switch toggleService;
    private RadarView radarView;
    private SensorManager mSensorManager;
    private Sensor mRotation;
    private boolean compassEnabled = false;
    private boolean sunEnabled = false;
    private Handler handler = new Handler();
    private InvalidateLoop invalidateLoop = new InvalidateLoop();

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
        handler.postDelayed(invalidateLoop, 3000);  // starting the loop that invalidates the radarView about every 3 secs
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRotation != null) {
            mSensorManager.unregisterListener(this);
        }
        getCliquePreferences().setZoomRadius(radarView.getZoomRadius());
        handler.removeCallbacks(invalidateLoop);
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

    @SuppressWarnings("EmptyMethod")
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

    /**
     * Calculates the proper bearing to use depending on the sensor values it gets from the the Sensors
     * Holding the phone flat on a table, straight up, even slightly tilted towards you should work without glitches.
     * Lying on your back looking at it from below could later be implemented but would require a discontinuity.
     * HINT: you might find a few ways to do this more efficiently, this function is called quite a lot
     * HINT: when pitch is about 45° upside down (screen aiming down) behaviour is unexpected, might figure that one out.
     * @param sensorEvent a SensorEvent that might be a TYPE_ROTATION_VECTOR
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // You're not supposed to do to much work  in this callback but this seems reasonable
        if(compassEnabled && (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)) {
            final float[] rotMat = new float[16];
            final float[] orient = new float[3];
            final float[] flatRotMat = new float[16];
            final float[] corRotMat = new float[16];
            SensorManager.getRotationMatrixFromVector(rotMat, sensorEvent.values);
            SensorManager.getOrientation(rotMat, orient);
            double pitch = orient[1];
            Matrix.setRotateM(corRotMat, 0, (float) (-pitch * 180.0/3.1415), 1f, 0f, 0f);
            Matrix.multiplyMM(flatRotMat, 0, corRotMat, 0, rotMat, 0);
            SensorManager.getOrientation(flatRotMat, orient);
            double x;
            x = orient[0];
            radarView.setBearing(x*180.0/3.1415);
            radarView.invalidate();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    /**
     * This looping construction is for invalidating the radarView regularly as the blip age progresses without any database changes.
     * So the display should change but nothing will notify the radarView that it needs to be updated. Starting this loop assures this timely updating.
     */
    private class InvalidateLoop implements Runnable {
        @Override
        public void run() {
            Log.i(TAG, "invalidating radarView from timed loop");
            if(radarView != null) radarView.invalidate();
            handler.postDelayed(invalidateLoop, 3000);
        }
    }
}
