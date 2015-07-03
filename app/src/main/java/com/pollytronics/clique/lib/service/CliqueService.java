package com.pollytronics.clique.lib.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.pollytronics.clique.CliqueActivity_Main;
import com.pollytronics.clique.R;
import com.pollytronics.clique.SubService_Cloud_2;
import com.pollytronics.clique.SubService_Localisation;
import com.pollytronics.clique.lib.CliqueActivity_Interface4CliqueService;
import com.pollytronics.clique.lib.database.CliqueDb_Interface;
import com.pollytronics.clique.lib.database.CliqueSQLite.CliqueSQLite;

/**
 * The CliqueService class provides access to active CliqueActivity instances if any for SubServices. (e.g. to notify them of new data to present)
 * The actual work is done in the SubService classes that are created and managed by this class.
 *
 * TODO: use a list of subServices to pass on the onEvent callbacks instead of manually calling them, look into EvenListener interface and its subclasses
 * TODO: consider using smth like PreferenceChangeListener interfaces instead of notifyNewSettings
 */
public class CliqueService extends Service implements CliqueService_Interface4SubService, CliqueService_Interface4CliqueActivity {

    private final static String TAG = "CliqueService";
    private final CliqueBinder cliqueBinder = new CliqueBinder();
    private final SubService_Localisation subServiceLocalisation = new SubService_Localisation(this);
    private final SubService_Cloud_2 subServiceCloud = new SubService_Cloud_2(this);
    private CliqueActivity_Interface4CliqueService registeredCliqueActivity;
    private Boolean raRegistered = false;

    public CliqueService() {}

    /**
     * Convenience method to retrieve the database instance.
     * @return the database instance
     */
    public CliqueDb_Interface getCliqueDb() {
        return CliqueSQLite.getInstance(this);
    }

    /**
     * Forwards onCreate() call to all subServices.
     */
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate, initialising subServices");
        subServiceLocalisation.onCreate();
        subServiceCloud.onCreate();
    }

    /**
     * Forwards onDestroy() call to all subServices.
     */
    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy, terminating subServices");
        subServiceLocalisation.onDestroy();
        subServiceCloud.onDestroy();
    }

    /**
     * Forwards onRegister() call to all subServices.
     */
    private void onRegister() {
        Log.i(TAG,"onRegister, calling subService methods");
        subServiceLocalisation.onRegister();
        subServiceCloud.onRegister();
    }

    /**
     * Forwards onUnregister() call to all subServices.
     */
    private void onUnregister() {
        Log.i(TAG,"onUnregister, calling subService methods");
        subServiceLocalisation.onUnregister();
        subServiceCloud.onUnregister();
    }

    /**
     * Forwards notifyNewSettings() call to all subServices so they can reload them.
     */
    @Override
    public void notifyNewSettings() {
        subServiceLocalisation.onNewSettings();
        subServiceCloud.onNewSettings();
    }

    /**
     * Gets called each time when an activity calls startService.
     * Also launches a sticky notification pointing back to the CliqueActivity_Main activity class.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        Intent notificationIntent = new Intent(this, CliqueActivity_Main.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.clique_bw)
                .setContentTitle(getString(R.string.service_notification_title))
                .setContentText(getString(R.string.service_notification_text))
                .setContentIntent(pendingIntent);
        startForeground(1, notificationBuilder.build());
        return Service.START_STICKY;
    }

    /**
     * Gets called when an activity wants to bind. The return value is provided to the binding activity through the onServiceConnected callback.
     * The CliqueBinder class only provides one method to retrieve the instance of this Service.
     * It's a bit of a hack around the whole IBinder thing to enable activities and services to call each others methods directly.
     * (See also class CliqueBinder)
     *
     * @return throwaway instance of CliqueBinder class
     */
    @Override
    public IBinder onBind(Intent intent) {
        return cliqueBinder;
    }

    /**
     * Gets called from CliqueActivity to pass and save its instance in the CliqueService.
     * The service will only remember the last calling activity.
     * It will also call onRegister methods on SubServices.
     */
    public void registerActivity(CliqueActivity_Interface4CliqueService ra){
        Log.i(TAG,"registering activity");
        this.registeredCliqueActivity = ra;
        raRegistered = true;
        onRegister();
    }

    /**
     * Gets called from CliqueActivity.
     * Makes the Service Forget a certain CliqueActivity instance.
     * If the one calling doesn't match the current one: do nothing.
     * It will also call onUnregister methods on SubServices
     */
    public void unregisterActivity(CliqueActivity_Interface4CliqueService ra){
        if (this.registeredCliqueActivity == ra) {
            Log.i(TAG,"unregistering activity");
            this.registeredCliqueActivity = null;
            raRegistered = false;
            onUnregister();
        }
    }

     /**
     * Sends text to a registered activity if any.
     * To be called from within RadarServices itself or SubServices.
     */
    @Override
    public void sendStringToRa(String text){
        if(raRegistered){
            registeredCliqueActivity.print(text);
        }
    }

    /**
     * Notifies the registered activity (if any) of database updates.
     * To be called from within RadarService itself or SubServices.
     */
    @Override
    public void notifyNewData() {
        if(raRegistered){
            registeredCliqueActivity.notifyDatabaseUpdate();
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    /**
     * A throwaway class for activity and services getting each others instances.
     * A bit of a hack around it's intended use. (See also onBind())
     */
    public class CliqueBinder extends Binder {
        public CliqueService getCliqueService(){
            return CliqueService.this;
        }
    }
}
