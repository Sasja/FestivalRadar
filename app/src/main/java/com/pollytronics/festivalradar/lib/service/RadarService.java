package com.pollytronics.festivalradar.lib.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.pollytronics.festivalradar.R;
import com.pollytronics.festivalradar.RadarActivity_Main;
import com.pollytronics.festivalradar.lib.database.RadarDatabase_SQLite;
import com.pollytronics.festivalradar.lib.database.RadarDatabase_Interface;
import com.pollytronics.festivalradar.SubService_Cloud_2;
import com.pollytronics.festivalradar.SubService_Localisation;
import com.pollytronics.festivalradar.lib.RadarActivity_Interface4RadarService;

/**
 * The RadarService class manages the connection to RadarActivity and derived classes
 * it creates a few helper classes derived from the SubService class to implement its features and to delegate calls to
 * This class forms a layer between SubService classes and RadarActivities:
 *      - This class delegates calls from activities to the right helper classes (SubServices)
 *      - This class provide methods to the SubServices to reach the Activities.
 */
public class RadarService extends Service implements RadarService_Interface4SubService, RadarService_interface4RadarActivity {

    private final static String TAG = "RadarService";
    private final RadarBinder radarBinder = new RadarBinder();
    private final SubService_Localisation subServiceLocalisation = new SubService_Localisation(this);
    private final SubService_Cloud_2 subServiceCloud = new SubService_Cloud_2(this);
    private RadarActivity_Interface4RadarService ra;
    private RadarDatabase_Interface db;
    private Boolean raRegistered = false;

    /*
    create an instance of each helper class here, and add calls in onCreate, onDestroy, onRegister and onUnregister
     */

    public RadarService() {
    }

    public RadarDatabase_Interface getRadarDataBase(){
        return db;
    }

    /**
     * calls onCreate methods of all subServices
     */
    @Override
    public void onCreate() {
        db = RadarDatabase_SQLite.getInstance(this);
        Log.i(TAG, "onCreate, initialising sub services");
        subServiceLocalisation.onCreate();
        subServiceCloud.onCreate();
    }

    /**
     * calls onDestroy methods of all subservices
     */
    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy, terminating sub services");
        subServiceLocalisation.onDestroy();
        subServiceCloud.onDestroy();
    }

    /**
     * calls onRegister methods of all subservices
     */
    private void onRegister() {
        Log.i(TAG,"onRegister, calling subservice methods");
        subServiceLocalisation.onRegister();
        subServiceCloud.onRegister();
    }

    /**
     * calls onUnregister methods of all subservices
     */
    private void onUnregister() {
        Log.i(TAG,"onUnRegister, calling subservice methods");
        subServiceLocalisation.onUnregister();
        subServiceCloud.onUnregister();
    }

    @Override
    public void notifyNewSettings() {
        subServiceLocalisation.onNewSettings();
        subServiceCloud.onNewSettings();
    }

    /**
     * gets called each time when an activity calls startService
     * launches a sticky notification pointing back to RadarActivity_Main
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        Intent notificationIntent = new Intent(this, RadarActivity_Main.class);
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

    //------------- Method implementations for SubserviceInterface

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

    /**
     * throwaway class for activity and services getting each others instances
     */
    public class RadarBinder extends Binder {
        public RadarService getRadarService(){
            return RadarService.this;
        }
    }

    //------------ Method implementations/delegations for RadarActivityInterface

}
