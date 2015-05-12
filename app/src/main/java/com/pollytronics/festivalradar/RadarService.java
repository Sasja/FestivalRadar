package com.pollytronics.festivalradar;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

/**
 * The RadarService class manages the connection to RadarActivity and derived classes
 * it creates a few helper classes derived from the AbstractSubService class to implement its features and to delegate calls to
 * This class forms a layer between SubService classes and RadarActivities:
 *      - This class delegates calls from activities to the right helper classes (SubServices)
 *      - This class provide methods to the SubServices to reach the Activities.
 * TODO: checking for api levels below HONEYCOMB is not sensible anymore as minSdkVersion is increased to 15 now
 */
public class RadarService extends Service implements RadarService_Interface4SubService, RadarService_interface4RadarActivity {

    private final static String TAG = "RadarService";
    private RadarBinder radarBinder = new RadarBinder();
    private RadarActivity_Interface4RadarService ra;
    private RadarDatabase_Interface4RadarService db;
    private Boolean raRegistered = false;

    public RadarService() {
    }

    public RadarDatabase_Interface4RadarService getRadarDataBase(){
        return db;
    }

    /*
    create an instance of each helper class here, and add calls in onCreate, onDestroy, onRegister and onUnregister
     */

    private LocalisationSubService localisationSubService = new LocalisationSubService(this);
    private CloudSubService cloudSubService = new CloudSubService(this);

    /**
     * calls onCreate methods of all subServices
     */
    @Override
    public void onCreate() {
        db = RadarDatabase.getInstance(this);
        Log.i(TAG, "onCreate, initialising sub services");
        localisationSubService.onCreate();
        cloudSubService.onCreate();
    }

    /**
     * calls onDestroy methods of all subservices
     */
    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy, terminating sub services");
        localisationSubService.onDestroy();
        cloudSubService.onDestroy();
    }

    /**
     * calls onRegister methods of all subservices
     */
    public void onRegister() {
        Log.i(TAG,"onRegister, calling subservice methods");
        localisationSubService.onRegister();
        cloudSubService.onRegister();
    }

    /**
     * calls onUnregister methods of all subservices
     */
    public void onUnregister() {
        Log.i(TAG,"onRegister, calling subservice methods");
        localisationSubService.onUnregister();
        cloudSubService.onUnregister();
    }

    @Override
    public void notifyNewSettings() {
        localisationSubService.onNewSettings();
        cloudSubService.onNewSettings();
    }

    /**
     * gets called each time when an activity calls startService
     * launches a sticky notification pointing back to MainRadarActivity for SDK >= HONEYCOMB
     * TODO: implement for lower SDKs
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            Intent notificationIntent = new Intent(this, MainRadarActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(getString(R.string.service_notification_title))
                    .setContentText(getString(R.string.service_notification_text))
                    .setContentIntent(pendingIntent)
                    .getNotification();
            startForeground(1, notification);
        }
        return Service.START_STICKY;
    }

    /**
     * throwaway class for activity and services getting each others instances
     */
    public class RadarBinder extends Binder {
        RadarService getRadarService(){
            return RadarService.this;
        }
    }

    /**
     * gets called when activity wants to bind, it sends the binder back
     * @return throwaway instance of RadarBinder class
     */
    @Override
    public IBinder onBind(Intent intent) {
        return radarBinder;
    }

    /**
     * gets called from RadarActivity to pass and save its instance in the RadarService
     * will only remember the last calling activity
     * it will call onRegister methods on SubServices
     */
    public void registerActivity(RadarActivity_Interface4RadarService ra){
        Log.i(TAG,"registering activity");
        this.ra = ra;
        raRegistered = true;
        onRegister();
    }

    /**
     * gets called from RadarActivity
     * forget a certain RadarActivity instance.
     * If the one calling doesn't match the current one, do nothing
     * will call onUnregister methods on SubServices
     */
    public void unregisterActivity(RadarActivity_Interface4RadarService ra){
        if (this.ra == ra) {
            Log.i(TAG,"unregistering activity");
            this.ra = null;
            raRegistered = false;
            onUnregister();
        }
    }

    //------------- Method implementations for SubserviceInterface

     /**
     * send text to a registered activity if any
     * called from within RadarServices and SubServices
     */
    @Override
    public void sendStringToRa(String text){
        if(raRegistered){
            ra.print(text);
        }
    }

    @Override
    public void notifyNewData() {
        if(raRegistered){
            ra.notifyDatabaseUpdate();
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    //------------ Method implementations/delegations for RadarActivityInterface

}
