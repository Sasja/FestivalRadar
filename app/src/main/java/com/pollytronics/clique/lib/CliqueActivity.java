package com.pollytronics.clique.lib;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.pollytronics.clique.CliqueActivity_About;
import com.pollytronics.clique.CliqueActivity_Debug;
import com.pollytronics.clique.CliqueActivity_Login;
import com.pollytronics.clique.CliqueActivity_Settings;
import com.pollytronics.clique.MVP_Activity_Contacts;
import com.pollytronics.clique.MVP_Activity_Groups;
import com.pollytronics.clique.R;
import com.pollytronics.clique.lib.database.cliqueSQLite.CliqueSQLite;
import com.pollytronics.clique.lib.preferences.CliquePreferences;
import com.pollytronics.clique.lib.service.CliqueService;
import com.pollytronics.clique.lib.service.CliqueService_Interface4CliqueActivity;

/**
 * Base class for all Activities
 * This class is responsible for managing the connection to the CliqueService
 * It also provides a few services such as getCliqueDb, getCliquePreferences, hearbeatLoop
 * and it also checks for authentication credentials (if not disabled through overriding the function)
 * TODO: (gui) can i use a more funky Theme such as Holo.Light.DarkActionBar with AppCompatActivity??
 */
public abstract class CliqueActivity extends AppCompatActivity implements CliqueActivity_Interface4CliqueService {

    private static final String TAG = "CliqueActivity";
    private CliqueService rs;        //needs access to more methods than just the interface, for handshaking,
    private boolean rsBound = false;
    private ServiceConnection rsConn;
    private Handler handler = new Handler();
    private HeartbeatLoop heartbeatLoop = new HeartbeatLoop();
    private int heartbeatTime = 0;  // 0 is disabled, anything is time in milliseconds

    /**
     * get instance of running service, it only returns the interface meant for activities to prevent CliqueActivity subclasses to call
     * methods for ie SubServices and such
     * @return interface to instance of CliqueService, if no bound service is running, it will return a spoof interface to nothing
     */
    protected CliqueService_Interface4CliqueActivity getBoundCliqueService() {
        if(rsBound){
            return rs;
        } else {
            return new CliqueService_Interface4CliqueActivity() {
                @Override
                public void notifyNewSettings() {
                    Log.i(TAG, "called notifyNewSettings() while not connected to service");
                }
            };
        }
    }

    public CliquePreferences getCliquePreferences() {
        return CliquePreferences.getInstance(getApplicationContext());
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CliqueSQLite.init(this);        // This is important, it allows the static methods to use the right context
        // TODO: state in a 'static' class such as CliqueSQLite is kind of bad, should services and fragments also all call init() to prevent errors in some circumstances?
    }

    /**
     * start CliqueService if it is not started yet and bind to it
     * (activity will be registered at service in onServiceConnected callback)
     */
    protected void startAndBindCliqueService(){
        if(!isMyServiceRunning(CliqueService.class)){
            Log.i(TAG,"starting the service");
            startService(new Intent(CliqueActivity.this, CliqueService.class));
        } else {
            Log.i(TAG,"service was found running, let's bind and register anyhow");
        }
        Log.i(TAG, "binding to cliqueservice now");
        bindService(new Intent(CliqueActivity.this, CliqueService.class), rsConn, 0);
    }

    /**
     * bind to the service only if it is running already.
     * (activity will be registered at service in onServiceConnected callback)
     */
    private void bindIfRunningCliqueService(){
        if(isMyServiceRunning(CliqueService.class)){
            Log.i(TAG,"bindIfRunningCliqueService(): found service running, now binding to it");
            bindService(new Intent(CliqueActivity.this, CliqueService.class), rsConn, 0);
        } else {
            Log.i(TAG, "bindIfRunningCliqueService(): service is not running, so not binding to it");
        }
    }

    /**
     * unregister activity from the service and unbind from the service if it is running
     */
    private void unBindCliqueService(){
        if (rsBound) {
            Log.i(TAG,"calling unregisterActivity() and unBindService()");
            rs.unregisterActivity(CliqueActivity.this);
            rsBound = false;
            unbindService(rsConn);
        }
    }

    /**
     * unregister/unbind and stop service
     */
    protected void unbindAndStopCliqueService(){
        unBindCliqueService();
        if(isMyServiceRunning(CliqueService.class)) {
            Log.i(TAG, "service found running, calling stopservice");
            stopService(new Intent(CliqueActivity.this, CliqueService.class));
        } else {
            Log.i(TAG, "no running service found...");
        }
    }

    /**
     * connects to service if it is running
     * sets up callbacks to retrieve CliqueService instance and register activity at Service on onServiceConnected
     * manages rsBound variable accordingly
     */
    @Override
    protected void onStart() {
        super.onStart();
        rsConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.i(TAG,"onServiceConnected, getting CliqueService object now");
                CliqueService.CliqueBinder cliqueBinder = (CliqueService.CliqueBinder) iBinder;
                rs = cliqueBinder.getCliqueService();
                Log.i(TAG, "now registering Activity at Service");
                rs.registerActivity(CliqueActivity.this);
                rsBound = true;
                CliqueActivity.this.onCliqueServiceConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.i(TAG, "onServiceDisconnected");
                rsBound = false;
                CliqueActivity.this.onCliqueServiceDisconnected();
            }
        };
        bindIfRunningCliqueService();
    }

    /**
     * overload in derived class to handle this event
     */
    protected void onCliqueServiceConnected() {
    }

    /**
     * overload in derived class to handle this event
     */
    protected void onCliqueServiceDisconnected() {
    }

    /**
     * unbind/unregister if bound
     */
    @Override
    protected void onStop() {
        Log.i(TAG, "onStop() : calling unBindCliqueService();");
        unBindCliqueService();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        assertAuthCredentials();
        if(heartbeatTime != 0) handler.postDelayed(heartbeatLoop, heartbeatTime);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(heartbeatLoop);
    }

    /**
     * menu items that need to appear in all derived activities get added here
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.cliqueactivity_base, menu);
        return true;
    }

    /**
     * menu items that need to appear in all derived activities get handled here
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, CliqueActivity_Settings.class));
            return true;
        } else if (id == R.id.action_about){
            startActivity(new Intent(this, CliqueActivity_About.class));
            return true;
        } else if (id == R.id.action_contacts) {
            startActivity(new Intent(this, MVP_Activity_Contacts.class));
            return true;
        } else if (id == R.id.action_debug) {
            startActivity(new Intent(this, CliqueActivity_Debug.class));
            return true;
        } else if (id == R.id.action_groups) {
            startActivity(new Intent(this, MVP_Activity_Groups.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks if there is a login and key present in the sharedpreferences,
     * if there is not: launch the login activity that will help creating these
     * override this function in the login activity itself (for obvious reasons)
     */
    protected void assertAuthCredentials() {
        Log.i(TAG, "asserting presence of account credentials (not validating them)");
        if (getCliquePreferences().getAccountLogin() == null || getCliquePreferences().getAccountKeyb64() == null || getCliquePreferences().getAccountId() == 0) {
            Log.i(TAG, "account login or key missing, launching sign in activity");
            Intent intent = new Intent(this, CliqueActivity_Login.class);
            startActivity(intent);
        }
    }

    /**
     * code snipppet from http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-in-android
     * checks if a Service is running
     * TODO: (optimize) might have better solution
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
     */
    @Override
    public void print(String text) {
        Log.i(TAG,"print() " + text);
    }

    @Override
    public void notifyDatabaseUpdate() {
        Log.i(TAG,"received database update notification");
    }

    protected void enableHeartBeat(int milliseconds) {
        heartbeatTime = milliseconds;
    }

    /**
     * Override this function to do something every x milliseconds. Don't call super.method() in the override
     */
    protected void heartBeatCallBack() {
        Log.i(TAG, "heartbeat callback empty... should not enable me");
    }

    private class HeartbeatLoop implements Runnable {
        @Override
        public void run() {
            heartBeatCallBack();
            handler.postDelayed(heartbeatLoop, heartbeatTime);
        }
    }
}
