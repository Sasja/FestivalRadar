package com.pollytronics.festivalradar.lib.api;

import android.util.Log;

import com.pollytronics.festivalradar.RadarDatabase_Interface4RadarService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by pollywog on 6/3/15.
 *
 *
 * APICall objects implement REST API Calls, separating the work in:
 * 1) collecting all the data needed for the call
 * 2) setting extra parameters manually
 * 3) constructing the query url/headers/body
 * 4) parsing a response into member fields
 * 5) using the results
 *
 * only 1 and 5 should access data outside the object itself as all others might be called on another thread
 * TODO: check the error handling of this thing
 */
abstract public class RadarApiCall {
    protected final String TAG = "RadarApiCall";
    protected final String baseUrl = "http://festivalradarservice.herokuapp.com/api/v1/";
    //protected final String baseUrl = "http://192.168.0.5:8080/api/v1/";
    private boolean failed = false;

    public void setFailedFlag() { failed = true; }
    public boolean hasFailed() { return failed; }
    public abstract void collectData(RadarDatabase_Interface4RadarService db);
    protected abstract String getApiQueryString();
    public abstract void doTheWork(RadarDatabase_Interface4RadarService db);
    public abstract void callAndParse() throws IOException;

    protected String myHttpGet(String myurl) throws IOException {   // TODO: study this code
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

    protected String myHttpPost(String myurl, String jsondata) throws IOException {   // TODO: study this code, the api returns a Error 415 Unsupported media type, this is not how a proper post is done
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
}
