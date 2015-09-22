package com.pollytronics.clique;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.pollytronics.clique.lib.service.CliqueService;
import com.pollytronics.clique.lib.service.SubService;

/**
 * Cloud SubService 2
 *
 * periodically pulls and pushes data from server and updates the local database
 * This class uses the api_vxx methods to do the actual api calls
 *
 * TODO: (optimization) minimize tcp connection lifetime to minimize load on server
 * TODO: (errorhandling) look for printStacktrace try catch blocks everywhere and fix it
 * TODO: (syncing) sync sometimes fails... investigate!
 * TODO: (syncing) i believe this will generate a shitload of duplicate blips
 * http://developer.android.com/training/basics/network-ops/connecting.html
 *
 * Created by pollywog on 26/5/2015.
 */
public class SubService_Cloud_2 extends SubService {

    private final String TAG = "SubService_Cloud_2";
    private int updateTime_ms;
    private boolean cleaningUp = false;     // a flag so the network pull loop will stop posting itself
    private final Runnable cloudLoop = new Runnable() {
        @Override
        public void run() {
            try{
                ConnectivityManager connMgr = (ConnectivityManager) getCliqueService().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Log.i(TAG, "network available: syncing data");
                    CliqueSyncer.getInstance(getCliqueService().getContext()).poke();
                    getMainHandler().removeCallbacks(cloudLoop);    //make sure we dont have 2 loops
                    if(!cleaningUp) getMainHandler().postDelayed(cloudLoop,updateTime_ms);
                } else {
                    Log.i(TAG,"Cannot connect to server: no network");
                    getMainHandler().removeCallbacks(cloudLoop);    //make sure we dont have 2 loops
                    if(!cleaningUp) getMainHandler().postDelayed(cloudLoop,updateTime_ms);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public SubService_Cloud_2(CliqueService rs) {
        super(rs);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        updateTime_ms = (int) getCliquePreferences().getCloudUpdateTime_ms();
        Log.i(TAG, "set updateTime to (ms) " + Integer.toString(updateTime_ms));
        getMainHandler().post(cloudLoop);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy");
        getMainHandler().removeCallbacks(cloudLoop);
        cleaningUp = true;
    }

    @Override
    public void onRegister() {
        Log.i(TAG, "onRegister");
    }

    @Override
    public void onUnregister() {
        Log.i(TAG, "onUnregister");
    }

    @Override
    public void onNewSettings() {
        getMainHandler().removeCallbacks(cloudLoop);
        updateTime_ms = (int) getCliquePreferences().getCloudUpdateTime_ms();
        Log.i(TAG, "set updateTime to (ms) " + Integer.toString(updateTime_ms));
        getMainHandler().post(cloudLoop);
    }
}
