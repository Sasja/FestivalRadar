package com.pollytronics.festivalradar.lib.api_v01;

import android.util.Log;

import com.pollytronics.festivalradar.RadarDatabase_Interface;

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
 * This class implements whatever needed to perform api calls,
 * work is separated into four domains
 * 1) preparing the call by gathering necessary data (must be called on main thread for threadsafety)
 *      this will put all necessary data into attributes local to the object
 * 2) the actual call (method must be called from async thread or exception will be thrown)
 *      this will use the gathered data to construct the right api call and return the body to the parser
 * 2') parsing the api call result body (this could be done on main thread but is done async to allow
 * doing a few consecutive call's that depend on previous call results in one thread)
 *      this parses the api call reply body and stores the information in attributes local to the object
 * 3) methods to retrieve the results from the object after a call (main thread or async thread)
 *      this can then be used to construct following api calls in the same async thread
 * 4) optionally methods that use the final results can be implemented here (main thread)
 *
 *
 * 1) is done through collectData()                                     abstract
 *    optionally some setSomething() methods can be provided
 * 2+2') is done through callAndParse()                                 abstract
 * 3) is done through optional getSomething()
 * 4) is done throug doTheWork() methods
 *
 */
abstract public class RadarApiCall {
    final String baseUrl = "http://festivalradarservice.herokuapp.com/api/v1/";
    private final String TAG = "RadarApiCall";
    //protected final String baseUrl = "http://192.168.0.5:8080/api/v1/";

    public abstract void collectData(RadarDatabase_Interface db);

    public abstract String getHttpMethod();
    protected abstract String getApiQueryString();
    protected String getApiBodyString() { return ""; }

    protected abstract void parseContent(String content);

    final public void callAndParse() throws IOException {
        parseContent(myHttpRequest(getHttpMethod(), getApiQueryString(), getApiBodyString()));
    }

    /**
     * This method will throw an IOException when having any problems with the httprequest including any non 200 http status code.
     * It assumes there is a connection
     * TODO: figure out what would happen if called without a connection?
     * @param method
     * @param myUrl
     * @param myBody
     * @return
     * @throws IOException
     */
    private String myHttpRequest(String method, String myUrl, String myBody) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        URL url = new URL(myUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setReadTimeout(1000);
            conn.setConnectTimeout(10000);
            conn.setRequestMethod(getHttpMethod());
            conn.setDoInput(true);
            Log.i(TAG, getHttpMethod() + " " + myUrl);
            if(getHttpMethod().equals("POST")) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                Log.i(TAG, "BODY = " + myBody);
                os = conn.getOutputStream();
                writeToOutputStream(os, myBody);
            }
            conn.connect();
            int response = conn.getResponseCode();
            Log.i(TAG, "HTTP RESPONSE CODE: " + response);
            if(response!=200) throw new IOException();
            is = conn.getInputStream();
            return readInputStream(is);
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
