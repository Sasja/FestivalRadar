package com.pollytronics.festivalradar;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Base class for all Activities
 * This class is only responsible for managing the connection to the RadarService
 * global ActivityBar menu items (for all activities) can also be set up here
 */
public class RadarActivity extends ActionBarActivity implements RadarActivity_Interface4RadarService {

    private static final String TAG = "RadarActivity";
    private RadarService rs;        //needs access to more methods than just the interface, for handshaking,
    private RadarDatabase_Interface4RadarActivity db;
    private boolean rsBound = false;
    private ServiceConnection rsConn;

    /**
     * check if RadarService is running and instance is obtained, check before calling RadarService methods!
     * @return true if up and running, safe to call its methods
     */
    protected boolean isBoundToService() {
        return rsBound;
    }

    /**
     * get instance of running service, it only returns the interface meant for activities to prevent RadarActivity subclasses to call
     * methods for ie SubServices and such
     * @return interface to instance of RadarService
     */
    protected RadarService_interface4RadarActivity getBoundRadarService() {
        return (RadarService_interface4RadarActivity) rs;
    }

    protected RadarDatabase_Interface4RadarActivity getRadarDatabase() {
        return db;
    }

    protected RadarPreferences getRadarPreferences() {
        return RadarPreferences.getInstance(getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = (RadarDatabase_Interface4RadarActivity) RadarDatabase.getInstance();
    }

    /**
     * start RadarService if it is not started yet and bind to it
     * (activity will be registered at service in onServiceConnected callback)
     */
    protected void startAndBindRadarService(){
        if(!isMyServiceRunning(RadarService.class)){
            Log.i(TAG,"starting the service");
            startService(new Intent(RadarActivity.this, RadarService.class));
        } else {
            Log.i(TAG,"service was found running, let's bind and register anyhow");
        }
        Log.i(TAG, "binding to radarservice now");
        bindService(new Intent(RadarActivity.this, RadarService.class), rsConn, 0);
    }

    /**
     * bind to the service only if it is running already.
     * (activity will be registered at service in onServiceConnected callback)
     */
    protected void bindIfRunningRadarService(){
        Log.i(TAG,"bind if service is running");
        if(isMyServiceRunning(RadarService.class)){
            Log.i(TAG,"yup found it running, lets bind to it");
            bindService(new Intent(RadarActivity.this, RadarService.class), rsConn, 0);
        } else {
            Log.i(TAG,"seems its not running");
        }
    }

    /**
     * unregister activity from the service and unbind from the service if it is running
     */
    protected void unBindRadarService(){
        if (rsBound) {
            Log.i(TAG,"calling unregisterActivity() and unBindService()");
            rs.unregisterActivity((RadarActivity_Interface4RadarService) RadarActivity.this);
            rsBound = false;
            unbindService(rsConn);
        }
    }

    /**
     * unregister/unbind and stop service
     */
    protected void unbindAndStopRadarService(){
        unBindRadarService();
        if(isMyServiceRunning(RadarService.class)) {
            Log.i(TAG, "service found running, calling stopservice");
            stopService(new Intent(RadarActivity.this, RadarService.class));
        } else {
            Log.i(TAG, "no running service found...");
        }
    }

    /**
     * connects to service if it is running
     * sets up callbacks to retrieve RadarService instance and register activity at Service on onServiceConnected
     * manages rsBound variable accordingly
     */
    @Override
    protected void onStart() {
        rsConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.i(TAG,"onServiceConnected, getting RadarService object now");
                RadarService.RadarBinder radarBinder = (RadarService.RadarBinder) iBinder;
                rs = radarBinder.getRadarService();
                Log.i(TAG, "now registering Activity at Service");
                rs.registerActivity((RadarActivity_Interface4RadarService) RadarActivity.this);
                rsBound = true;
                RadarActivity.this.onRadarServiceConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.i(TAG, "onServiceDisconnected");
                rsBound = false;
                RadarActivity.this.onRadarServiceDisconnected();
            }
        };
        bindIfRunningRadarService();
        super.onStart();
    }

    /**
     * overload in derived class to handle this event
     */
    protected void onRadarServiceConnected() {
    }

    /**
     * overload in derived class to handle this event
     */
    protected void onRadarServiceDisconnected() {
    }

    /**
     * unbind/unregister if bound
     */
    @Override
    protected void onStop() {
        unBindRadarService();
        super.onStop();
    }

    /**
     * menu items that need to appear in all derived activities get added here
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.radaractivity, menu);
        return true;
    }

    /**
     * menu items that need to appear in all derived activities get handled here
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsRadarActivity.class));
            return true;
        } else if (id == R.id.action_about){
            startActivity(new Intent(this, AboutRadarActivity.class));
            return true;
        } else if (id == R.id.action_manage_contacts) {
            startActivity(new Intent(this, ManageContactsRadarActivity.class));
            return true;
        } else if (id == R.id.action_debug) {
            startActivity(new Intent(this, DebugRadarActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * code snipppet from http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-in-android
     * checks if a Service is running
     * TODO: might have better solution
     * @param serviceClass
     * @return
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * override this method to handle incoming text messages from the service
     * @param text
     */
    @Override
    public void print(String text) {
        Log.i(TAG,".onPrint() " + text);
    }

    @Override
    public void notifyDatabaseUpdate() {
        Log.i(TAG,"received database update notification");
    }
}
