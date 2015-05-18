package com.pollytronics.festivalradar;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.pollytronics.festivalradar.lib.RadarBlip;
import com.pollytronics.festivalradar.lib.RadarContact;
import com.pollytronics.festivalradar.lib.RadarView;

import java.util.Collection;

/**
 * Main app activity, it should give an overview of the situation and provide a simple GUI to
 * the most likely actions a user would want to perform
 * TODO: replace toggle buttons with Switches (api 11 and is way more recognizable)
 * TODO: figure out how to make sure TYPE_ROTATION_VECTOR sensor uses magnetics so it is usable for a compas
 */
public class MainRadarActivity extends RadarActivity implements SensorEventListener {

    private static final String TAG = "MainRadarActivity";
    ToggleButton toggleService;
    RadarView radarView;
    private SensorManager mSensorManager;
    private Sensor mRotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radaractivity_main);

        toggleService = (ToggleButton) findViewById(R.id.toggle_service);
        toggleService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(toggleService.isChecked()) {
                    Log.i(TAG, "RadarService button enabled");
                    startAndBindRadarService();
                } else {
                    Log.i(TAG, "RadarService button disabled");
                    unbindAndStopRadarService();
                }
            }
        });

        radarView = (RadarView) findViewById(R.id.radar_view);
        radarView.setBearing(0);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_UI);    // TODO: figure out if this doesnt eat to much battery
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
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
        super.onCreateOptionsMenu(menu);        //add this to the generated code to include the 'global' option items described in RadarActivity
        getMenuInflater().inflate(R.menu.main_radaractivity, menu);
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
        RadarBlip centerLocation = getRadarDatabase().getSelfContact().getLastBlip();
        radarView.setCenterLocation(centerLocation);
        radarView.removeAllContacts();
        Collection<RadarContact> contacts = getRadarDatabase().getAllContacts();
        for(RadarContact c:contacts){
            radarView.addContact(c);
        }
        radarView.invalidate();
    }

//                              this is the approach described online, can't get it to work properly yet...
//    float[] mGravity;
//    float[] mGeomagnetic;
//    @Override
//    public void onSensorChanged(SensorEvent sensorEvent) {
//        Log.i(TAG, "sensor event received! : " + sensorEvent.toString());
//        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
//            mGravity = sensorEvent.values;
//        else if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
//            mGeomagnetic = sensorEvent.values;
//        if(mGravity != null && mGeomagnetic != null) {
//            float R[] = new float[9];
//            float I[] = new float[9];
//            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
//            if(success) {
//                float orientation[] = new float[3];
//                SensorManager.getOrientation(R, orientation);
//                radarView.setBearing(orientation[0]*180/3.1415);
//                radarView.invalidate();
//                Log.i(TAG, "bearing reset to : " + Double.toString(orientation[0]));
//            }
//        }
//    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            //Log.i(TAG, "sensor event received! : " + sensorEvent.toString());
            float[] rotMat = new float[16];         // TODO: is it ok to do this within this often called method?
            float[] orient = new float[3];
            SensorManager.getRotationMatrixFromVector(rotMat, sensorEvent.values);
            SensorManager.getOrientation(rotMat, orient);
            double x;
            x = orient[0];
            radarView.setBearing(x*180.0/3.1415);
            radarView.invalidate();
        }
    }

//    @Override
//    public void onSensorChanged(SensorEvent sensorEvent) {
//        if(sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
//            double x;
//            x = sensorEvent.values[0];
//            radarView.setBearing(x);
//            radarView.invalidate();
//        }
//    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}
