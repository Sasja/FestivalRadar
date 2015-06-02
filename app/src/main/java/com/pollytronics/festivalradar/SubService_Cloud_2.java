package com.pollytronics.festivalradar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.pollytronics.festivalradar.lib.RadarBlip;
import com.pollytronics.festivalradar.lib.RadarContact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Cloud SubService 2
 *
 * periodically pulls and pushes data from server and updates the local database
 *
 * TODO: minimize tcp connection lifetime to minimize load on server
 * TODO: further develop the APICall classes for the different calls
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

    private String myHttpPost(String myurl, String jsondata) throws IOException {   // TODO: study this code, the api returns a Error 415 Unsupported media type, this is not how a proper post is done
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
            conn.setRequestProperty("Content-Type", "application/json");
            Log.i(TAG, "POST " + myurl);
            Log.i(TAG, "BODY = " + jsondata);
            os = conn.getOutputStream();
            writeToOutputStream(os, jsondata);
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

    /** helper method to write string into an output stream
     * TODO: study this code
     * http://stackoverflow.com/questions/9623158/curl-and-httpurlconnection-post-json-data
     */
    private void writeToOutputStream(OutputStream os, String data) throws IOException {
        //OutputStreamWriter writer = new OutputStreamWriter(os);
        byte[] test = data.getBytes("UTF-8");
        os.write(test);
        //writer.write(data, 0, data.length());
    }

    /**
     * This class is used to bundle all the interactions that happen with the api into one AsyncTask
     * it relies on APICall objects to handle the api details of each api separate interaction.
     * doInBackground can communicate with the other methods by setting member fields, it's safe:
     * http://developer.android.com/reference/android/os/AsyncTask.html (See Memory observability)
     */
    private class SyncToWebserviceTask extends AsyncTask<Void, Void, String> {
        private APICallSetMyBlip setMyBlip = new APICallSetMyBlip();
        private APICallGetBlips getBlips = new APICallGetBlips();
        /**
         * Gathers all the data needed to perform the api calls
         */
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "gathering the data i need to send to webservice");
            setMyBlip.collectData();
            getBlips.collectData();
        }

        /**
         * does the http requests to the api in background, results should be stored in member fields for onPostExecute to work on.
         * answers may be parsed here in order to construct and do new calls. Do not call collectData() or handleResults() here,
         * the other APICall methods should be safe to call from this background thread.
         */
        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "calling api");
            try {
                setMyBlip.parseContent(myHttpPost(setMyBlip.getApiQueryString(), setMyBlip.getApiBodyString()));
                getBlips.parseContent(myHttpGet(getBlips.getApiQueryString()));
            } catch (IOException e) {
                Log.i(TAG, "IOException: unable to complete all API requests");
                setMyBlip.setFailedFlag();
                getBlips.setFailedFlag();
                return "IOExcepion: unable to complete all API requests";
            }
            return null;
        }

        /**
         * call the APICall methods to process the results
         */
        @Override
        protected void onPostExecute(String s) {
            if (setMyBlip.hasFailed() || getBlips.hasFailed()) {
                Log.i(TAG, "the api call has failed, not calling doTheWork() methods for the calls");
            } else {
                Log.i(TAG, "parsing and using the responses of the webservice");
                setMyBlip.doTheWork();
                getBlips.doTheWork();
            }
        }
    }

    /**
     * APICall object implement REST API Calls, separating the work in:
     * 1) collecting all the data needed for the call
     * 2) setting extra parameters manually
     * 3) constructing the query url/headers/body
     * 4) parsing a response into member fields
     * 5) using the results
     *
     * only 1 and 5 should access data outside the object itself as all others might be called on another thread
     * TODO: check the error handling of this thing
     */
    abstract private class APICall {
        protected final String baseUrl = "http://festivalradarservice.herokuapp.com/api/v1/";
        //protected final String baseUrl = "http://192.168.0.5:8080/api/v1/";
        private boolean failed = false;

        public void setFailedFlag() { failed = true; }
        public boolean hasFailed() { return failed; }
        public abstract void collectData();
        public abstract String getApiQueryString();
        public abstract void doTheWork();
    }

    private class APICallSetMyBlip extends APICall {
        protected final String apiResourceName = "blips";
        private JSONObject selfBlipJSON = new JSONObject();
        private long selfId = 0;

        @Override
        public void collectData(){
            Log.i(TAG, "collecting data for APICallSetMyBlip");
            RadarContact selfContact = getRadarDatabase().getSelfContact();
            selfId = selfContact.getID();
            RadarBlip selfBlip = selfContact.getLastBlip();
            try {
                selfBlipJSON.put("lat", selfBlip.getLatitude());
                selfBlipJSON.put("lon", selfBlip.getLongitude());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String getApiQueryString() {
            return baseUrl+apiResourceName+"?userid="+selfId;
        }

        @Override
        public void doTheWork() {}

        public String getApiBodyString(){
            return selfBlipJSON.toString();
        }

        public void parseContent(String content) {
            Log.i(TAG, "api reply = "+content);
        }
    }

    private class APICallGetBlips extends APICall {
        protected final String apiResourceName = "blips";
        private JSONArray blips;
        private long selfId = 0;

        @Override
        public void collectData() {
            selfId = getRadarDatabase().getSelfContact().getID();
        }

        @Override
        public String getApiQueryString() {
            return baseUrl+apiResourceName+"?userid="+selfId;
        }

        public void parseContent(String content) {
            try {
                JSONObject jsonObject = new JSONObject(content);
                blips = jsonObject.getJSONArray("blips");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void doTheWork() {
            JSONObject blipJSON;
            Long id, time;
            double lat, lon;
            RadarBlip blip = new RadarBlip();
            RadarContact contact;
            for (int i = 0; i < blips.length(); i++) {
                try {
                    blipJSON = blips.getJSONObject(i);
                    id = blipJSON.getLong("userid");
                    lat = blipJSON.getDouble("lat");
                    lon = blipJSON.getDouble("lon");
                    time = blipJSON.getLong("time");
                } catch (JSONException e) {
                    e.printStackTrace();
                    continue;
                }
                blip.setLatitude(lat);
                blip.setLongitude(lon);
                blip.setTime(time);
                contact = getRadarDatabase().getContact(id);
                if(contact != null) {   // check if contact is known locally on phone
                    contact.addBlip(blip);
                    getRadarDatabase().updateContact(contact);
                }
            }
            getRadarService().notifyNewData();
        }
    }

}
