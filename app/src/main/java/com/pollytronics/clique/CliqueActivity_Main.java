package com.pollytronics.clique;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import com.pollytronics.clique.lib.CliqueActivity;
import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;
import com.pollytronics.clique.lib.gui_elements.RadarView;

import java.util.Collection;

/**
 * Main app activity, it should give an overview of the situation and provide a simple GUI to
 * the most likely actions a user would want to perform
 * TODO: rotation sensor should be optionally disabled in the settings (idea to use sun and moon as an alternative reference)
 * TODO: make the calculated bearing sensible when holding the phone upright or tilted looking from below or display a warning to prevent confusion
 * TODO: replace the dummy seekbar for zooming with some pinching action
 */
public class CliqueActivity_Main extends CliqueActivity implements SensorEventListener {

    private static final String TAG = "CliqueActivity_Main";
    private Switch toggleService;
    private RadarView radarView;
    private SeekBar zoomSeekBar;
    private SensorManager mSensorManager;
    private Sensor mRotation;

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
                    startAndBindRadarService();
                } else {
                    Log.i(TAG, "CliqueService button disabled");
                    unbindAndStopRadarService();
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
        if (mRotation != null) {
            mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_GAME);  // motoG reports 7mA batt drain for mRotation.getPower()
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRotation != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        Blip centerLocation = getRadarDatabase().getSelfContact().getLastBlip();
        radarView.setCenterLocation(centerLocation);
        radarView.removeAllContacts();
        Collection<Contact> contacts = getRadarDatabase().getAllContacts();
        for(Contact c:contacts){
            radarView.addContact(c);
        }
        radarView.invalidate();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // You're not supposed to do to much work  in this callback but this seems reasonable
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
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
