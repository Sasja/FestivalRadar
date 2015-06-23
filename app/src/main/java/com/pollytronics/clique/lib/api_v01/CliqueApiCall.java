package com.pollytronics.clique.lib.api_v01;

import android.util.Log;

import com.pollytronics.clique.lib.database.CliqueDb_Interface;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by pollywog on 6/3/15.
 *
 * This class implements the httprequests needed to perform api calls and provides a framework for derived api call classes
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
abstract public class CliqueApiCall {
    final String baseUrl = "http://festivalradarservice.herokuapp.com/api/v1/";
    private final String TAG = "CliqueApiCall";
    //protected final String baseUrl = "http://192.168.0.5:8080/api/v1/";

    public abstract void collectData(CliqueDb_Interface db);

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
            if (os != null) os.close();
            conn.disconnect();
        }
    }

    /**
     * helper method to convert input stream to string
     * http://stackoverflow.com/questions/2492076/android-reading-from-an-input-stream-efficiently
     *
     * study this code, it might remove all newlines
     */
    /*private String readInputStream_bak(InputStream is) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return total.toString();
    }*/

    /**
     * This method will read up to 8KB bytes from a stream of UTF-8 and return a string of it.
     * @param is
     * @return
     * @throws IOException
     */
    private String readInputStream(InputStream is) throws IOException {     // Failure to read this stream will be handled on a higher level
        final int bufferSize = 8 * 1024;
        Reader reader = null;
        reader = new InputStreamReader(is, "UTF-8");
        char[] buffer = new char[bufferSize];
        int charsRead = reader.read(buffer);
        int theresMore = reader.read();
        if (theresMore != -1) { // this happens when the response was longer than the buffer
            Log.i(TAG, "api response longer than expected (bufferSize = " + bufferSize + " ), throwing IOException");
            throw new IOException();
        }
        return new String(buffer, 0, charsRead);
    }

    /** helper method to write string into an output stream
     * TODO: study this code
     * http://stackoverflow.com/questions/9623158/curl-and-httpurlconnection-post-json-data
     */
    private void writeToOutputStream(OutputStream os, String data) throws IOException {
        byte[] buffer = data.getBytes("UTF-8");
        os.write(buffer);
    }
}
