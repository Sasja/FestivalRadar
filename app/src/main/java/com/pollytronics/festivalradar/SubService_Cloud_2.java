package com.pollytronics.festivalradar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.pollytronics.festivalradar.lib.api_v01.ApiCallGetBlips;
import com.pollytronics.festivalradar.lib.api_v01.ApiCallSetMyBlip;

import java.io.IOException;

/**
 * Cloud SubService 2
 *
 * periodically pulls and pushes data from server and updates the local database
 * This class uses the api_vxx methods to do the actual api calls
 *
 * TODO: minimize tcp connection lifetime to minimize load on server
 * TODO: figure out how time is to be stored in backend db and into local phone-db, now its broken but not used
 * http://developer.android.com/training/basics/network-ops/connecting.html
 *
 * Created by pollywog on 26/5/2015.
 */
public class SubService_Cloud_2 extends SubService {

    private final String TAG = "SubService_Cloud_2";
    private int updateTime_ms;
    private boolean cleaningUp = false;     //a flag so the network pull loop will stop posting itself
    private final Runnable cloudLoop = new Runnable() {
        @Override
        public void run() {
            try{
                ConnectivityManager connMgr = (ConnectivityManager) getRadarService().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Log.i(TAG, "network available: syncing data");
                    new SyncToWebserviceTask().execute();
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

    public SubService_Cloud_2(RadarService rs) {
        super(rs);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        updateTime_ms = (int) getRadarPreferences().getCloudUpdateTime_ms();
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
    protected void onRegister() {
        Log.i(TAG,"onRegister");
    }

    @Override
    protected void onUnregister() {
        Log.i(TAG, "onUnregister");
    }

    @Override
    protected void onNewSettings() {
        getMainHandler().removeCallbacks(cloudLoop);
        updateTime_ms = (int) getRadarPreferences().getCloudUpdateTime_ms();
        Log.i(TAG, "set updateTime to (ms) " + Integer.toString(updateTime_ms));
        getMainHandler().post(cloudLoop);
    }



    /**
     * This class is used to bundle all the interactions that happen with the api into one AsyncTask
     * it relies on APICall objects to handle the api details of each api separate interaction.
     * doInBackground can communicate with the other methods by setting member fields, it's safe:
     * http://developer.android.com/reference/android/os/AsyncTask.html (See Memory observability)
     * TODO: might be prettier to return a boolean for success from doInbackground instead of using a var
     * TODO: also look for other uses similar to this
     */
    private class SyncToWebserviceTask extends AsyncTask<Void, Void, String> {
        private final ApiCallSetMyBlip setMyBlip = new ApiCallSetMyBlip();
        private final ApiCallGetBlips getBlips = new ApiCallGetBlips();
        private boolean apiCallsSucceeded = false;
        /**
         * Gathers all the data needed to perform the api calls
         */
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "gathering the data i need to send to webservice");
            setMyBlip.collectData(getRadarDatabase());
            getBlips.collectData(getRadarDatabase());
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "calling api from SyncToWebserviceTask");
            try {
                setMyBlip.callAndParse();
                getBlips.callAndParse();
                apiCallsSucceeded = true;
            } catch (IOException e) {
                Log.i(TAG, "IOException: unable to complete all API requests");
                return "IOExcepion: unable to complete all API requests";
            }
            return null;
        }

        /**
         * call the APICall methods to process the results
         */
        @Override
        protected void onPostExecute(String s) {
            if (apiCallsSucceeded) {         // only take action if all api calls were successful
                Log.i(TAG, "using/aplying the responses of the webservice");
                getBlips.doTheWork(getRadarDatabase());
                getRadarService().notifyNewData();
            } else {
                Log.i(TAG, "the api call has failed, not calling doTheWork() methods for the calls");
            }
        }
    }
}
