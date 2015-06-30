package com.pollytronics.clique.lib.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.pollytronics.clique.R;
import com.pollytronics.clique.CliqueActivity_Main;
import com.pollytronics.clique.lib.CliqueActivity_Interface4CliqueService;
import com.pollytronics.clique.lib.database.CliqueDb_Interface;
import com.pollytronics.clique.lib.database.CliqueDb_SQLite;
import com.pollytronics.clique.SubService_Cloud_2;
import com.pollytronics.clique.SubService_Localisation;

/**
 * The CliqueService class manages the connection to CliqueActivity and derived classes
 * it creates a few helper classes derived from the SubService class to implement its features and to delegate calls to
 * This class forms a layer between SubService classes and RadarActivities:
 *      - This class delegates calls from activities to the right helper classes (SubServices)
 *      - This class provide methods to the SubServices to reach the Activities.
 */
public class CliqueService extends Service implements CliqueService_Interface4SubService, CliqueService_interface4CliqueActivity {

    private final static String TAG = "CliqueService";
    private final CliqueBinder cliqueBinder = new CliqueBinder();
    private final SubService_Localisation subServiceLocalisation = new SubService_Localisation(this);
    private final SubService_Cloud_2 subServiceCloud = new SubService_Cloud_2(this);
    private CliqueActivity_Interface4CliqueService ra;
    private Boolean raRegistered = false;

    /*
    create an instance of each helper class here, and add calls in onCreate, onDestroy, onRegister and onUnregister
     */

    public CliqueService() {
    }

    public CliqueDb_Interface getCliqueDb(){
        return CliqueDb_SQLite.getInstance(this);
    }

    /**
     * calls onCreate methods of all subServices
     */
    @Override
    public void onCreate() {
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
     * launches a sticky notification pointing back to CliqueActivity_Main
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
     * gets called when activity wants to bind, it sends the binder back
     * @return throwaway instance of CliqueBinder class
     */
    @Override
    public IBinder onBind(Intent intent) {
        return cliqueBinder;
    }

    /**
     * gets called from CliqueActivity to pass and save its instance in the CliqueService
     * will only remember the last calling activity
     * it will call onRegister methods on SubServices
     */
    public void registerActivity(CliqueActivity_Interface4CliqueService ra){
        Log.i(TAG,"registering activity");
        this.ra = ra;
        raRegistered = true;
        onRegister();
    }

    /**
     * gets called from CliqueActivity
     * forget a certain CliqueActivity instance.
     * If the one calling doesn't match the current one, do nothing
     * will call onUnregister methods on SubServices
     */
    public void unregisterActivity(CliqueActivity_Interface4CliqueService ra){
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
    public class CliqueBinder extends Binder {
        public CliqueService getCliqueService(){
            return CliqueService.this;
        }
    }

    //------------ Method implementations/delegations for RadarActivityInterface

}
