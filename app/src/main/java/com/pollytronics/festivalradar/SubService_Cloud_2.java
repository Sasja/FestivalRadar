package com.pollytronics.festivalradar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.pollytronics.festivalradar.lib.RadarBlip;
import com.pollytronics.festivalradar.lib.RadarContact;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Cloud SubService 2
 *
 * periodically pulls and pushes data from server and updates the local database
 *
 * TODO: this supersedes Cloud Subservice 1
 * TODO: use HttpUrlConnection instead of the apache lib, it should improve battery drain i've read somewhere (>=Gingerbread (api 9-10)...)
 * TODO: minimize tcp connection lifetime to minimize load on server
 * TODO: also the apache lib is depreciated
 * TODO: further develop the APICall classes for the different calls
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

    private String myHttpGet(String myurl) throws IOException {   // TODO: study this code
        InputStream is = null;
        URL url = new URL(myurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setReadTimeout(1000);
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            Log.i(TAG, "GET " + myurl);
            conn.connect();
            int response = conn.getResponseCode();
            Log.i(TAG, "HTTP RESPONSE CODE: " + response);
            if(response!=200) throw new IOException();
            is = conn.getInputStream();
            String contentString = readInputStream(is);
            return contentString;
        } finally {
            if (is != null) is.close();
            conn.disconnect();
        }
    }

    private String myHttpPost(String myurl, String data) throws IOException {   // TODO: study this code, the api returns a Error 415 Unsupported media type, this is not how a proper post is done
        InputStream is = null;
        OutputStream os = null;
        URL url = new URL(myurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setReadTimeout(1000);
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            Log.i(TAG, "POST " + myurl);
            Log.i(TAG, "BODY = " + data);
            os = conn.getOutputStream();
            writeToOutputStream(os, data);
            conn.connect();
            int response = conn.getResponseCode();
            Log.i(TAG, "HTTP RESPONSE CODE: " + response);
            if(response!=200) throw new IOException();
            is = conn.getInputStream();
            String contentString = readInputStream(is);
            return contentString;
        } finally {
            if (is != null) is.close();
            conn.disconnect();
        }
    }

    /**
     * helper method to convert input stream to string
     * http://stackoverflow.com/questions/2492076/android-reading-from-an-input-stream-efficiently
     * TODO: study this code, it might remove all newlines
     */
    private String readInputStream(InputStream is) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return total.toString();
    }

    /** helper method to write string into a output stream
     * TODO: study this code
     */
    private void writeToOutputStream(OutputStream os, String data) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(os);
        writer.write(data, 0, data.length());
    }

    /**
     * This class is used to bundle all the interactions that happen with the api into one AsyncTask
     * it relies on APICall objects to handle the api details of each api separate interaction.
     * doInBackground can communicate with the other methods by setting member fields, it's safe:
     * http://developer.android.com/reference/android/os/AsyncTask.html (See Memory observability)
     */
    private class SyncToWebserviceTask extends AsyncTask<Void, Void, String> {
        private APICallSetMyBlip setMyBlip = new APICallSetMyBlip();
        /**
         * Gathers all the data needed to perform the api calls
         */
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "gathering the data i need to send to webservice");
            setMyBlip.collectData();
        }

        /**
         * does the http requests to the api in background, results should be stored in member fields for onPostExecute to work on.
         * answers may be parsed here in order to construct and do new calls. Do not call collectData() or handleResults() here,
         * the other APICall methods should be safe to call from this other thread.
         */
        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "doing the httprequests to the webservice");
            try {
                setMyBlip.parseContent(myHttpPost(setMyBlip.getApiQueryString(), setMyBlip.getApiBodyString()));
            } catch (IOException e) {
                Log.i(TAG, "IOException: unable to complete all API requests");
                setMyBlip.flagFailed();
                return "IOExcepion: unable to complete all API requests";
            }
            return null;
        }

        /**
         * call the APICall methods to process the results
         */
        @Override
        protected void onPostExecute(String s) {
            if (setMyBlip.hasFailed()) {
                Log.i(TAG, "the api call has failed, not calling doTheWork()");
            } else {
                Log.i(TAG, "parsing and using the response of the webservice");
                setMyBlip.doTheWork();
            }
        }
    }

    /**
     * APICall object implement REST API Calls, separating the work in:
     * 1) collecting all the data needed for the call
     * 2) setting extra parameters manually
     * 3) constructing the queryString
     * 4) parsing a response into member fields
     * 5) using the results
     *
     * only 1 and 5 should access data outside the object itself as all others might be called on another thread
     * TODO: check the error handling of this thing
     */
    abstract private class APICall {
        protected final String baseUrl = "http://festivalradarservice.herokuapp.com/webservice/";
        private boolean failed = false;

        public void flagFailed() { failed = true; }
        public boolean hasFailed() { return failed; }
        public abstract void collectData();
        public abstract String getApiQueryString();
        public abstract void parseContent(String content);
        public abstract void doTheWork();
    }

    private class APICallSetMyBlip extends APICall {
        protected final String apiMethodName = "setMyBlip";
        String contentString = "";
        private JSONObject selfBlipJSON = new JSONObject();

        @Override
        public void collectData(){
            Log.i(TAG, "collecting data for APICallSetMyBlip");
            RadarContact selfContact = getRadarDatabase().getSelfContact();
            RadarBlip selfBlip = selfContact.getLastBlip();
            try {
                selfBlipJSON.put("lat", selfBlip.getLatitude());
                selfBlipJSON.put("lon", selfBlip.getLongitude());
                selfBlipJSON.put("time", selfBlip.getTime());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String getApiQueryString() {
            return baseUrl+apiMethodName;    // TODO: remove
        }

        public String getApiBodyString(){
            return selfBlipJSON.toString();
        }

        @Override
        public void parseContent(String content) {
            Log.i(TAG, "parsing the content retrieved from API");
            Log.i(TAG, "content retrieved = "+content);
            contentString = content;
        }

        @Override
        public void doTheWork() {
            Log.i(TAG, "doing the work to process the information from the api call");
            Log.i(TAG, contentString);
        }
    }
}
