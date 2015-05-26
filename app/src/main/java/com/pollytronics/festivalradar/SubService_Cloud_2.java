package com.pollytronics.festivalradar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Cloud SubService 2
 *
 * periodically pulls and pushes data from server and updates the local database
 *
 * TODO: this supersedes Cloud Subservice 1
 * TODO: use HttpUrlConnection instead of the apache lib, it should improve battery drain i've read somewhere (>=Gingerbread...)
 * TODO: minimize tcp connection lifetime to minimize load on server
 * TODO: also the apache lib is depreciated
 * TODO: further develop the APICall classes for the different calls
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
        Log.i(TAG,"onUnregister");
    }

    @Override
    protected void onNewSettings() {
        getMainHandler().removeCallbacks(cloudLoop);
        updateTime_ms = (int) getRadarPreferences().getCloudUpdateTime_ms();
        Log.i(TAG, "set updateTime to (ms) " + Integer.toString(updateTime_ms));
        getMainHandler().post(cloudLoop);
    }

    private class SyncToWebserviceTask extends AsyncTask<Void, Void, String> {  // TODO: this class does the actual api calls using the APICall classes

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "gathering the data i need to send to webservice");      // TODO: use the api call classes here
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "doing the httprequest to the webservice");              // TODO: use the api call classes here
            return "dummy response";
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i(TAG, "parsing and using the response of the webservice");     // TODO: use the api call classes here
            Log.i(TAG, "the response was: " + s);
        }
    }

    abstract private class APICall {
        protected final String baseUrl = "http://festivalradarservice.herokuapp.com/webservice/";
        public String getApiCallUrl() { return baseUrl + getApiMethodName(); }

        protected abstract String getApiMethodName();
    }

    private class APICallSetMyBlip extends APICall {                            // TODO: these kind of classes should prepare api calls and use the responses
        @Override
        protected String getApiMethodName() { return "setMyBlip"; }

        public String getQueryString() {
            return "dummyQueryString";
        }


    }
}
