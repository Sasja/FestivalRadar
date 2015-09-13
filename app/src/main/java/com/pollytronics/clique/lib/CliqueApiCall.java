package com.pollytronics.clique.lib;

import android.util.Log;
import android.util.Pair;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This Class and subclasses are meant as an abstraction layer between the app and the api,
 *
 * An object of this class is used with a AsyncTadk and handles communication with the api as follows:
 * 1) It is initiated with some parameters
 * 2) then optionally some extra calls can be made to set a few extra attributes,
 * 3) then callAndParse needs to be called on an Async thread to start interacting with the api
 * 4) finally results can be retrieved from this object with getters.
 *
 * only 3) needs to be called on an async thread (exception will be thrown otherwise)
 * the other steps can be called also on the async thread but just make sure not to access the db from another thread
 *
 * These classes should take care of all the JSON stuff and just throw JSONExceptions to be handled higher up.
 *
 * TODO: (syncing) optimise server connection for multiple api calls, when to call disconnect, how long are connections kept open, ...
 * TODO: (api) allow connecting to a testing api by changing baseUrl
 * TODO: fix duplicatebaseURL
 *
 */
@SuppressWarnings("FieldCanBeLocal")
abstract public class CliqueApiCall {
    //final String baseUrl = "https://cliqueserver.herokuapp.com/api/v1/";  // piggyback heroku ssl
    //final String baseUrl = "https://cliquedev.herokuapp.com/api/v1/";     // piggyback heroku ssl
    protected final String baseUrl = "http://192.168.44.162:1337/";
    private final String TAG = "CliqueApiCall";

    /**
     * override this method to return true only when all necessary attributes are initialized in the api call object.
     * Normally you can assure it is initialized in the only available constructor so then the implementation will be trivial
     * @return true if and only if the object is ready for callAndParse() on background of an asyncTask
     */
    protected abstract boolean isFullyInitialized();

    //public void collectData(CliqueDb_Interface4Local db) {}

    /**
     * must return "GET" or "POST" or any implemented other http method
     * @return "GET" or "POST" or "DELETE"
     */
    protected abstract String getHttpMethod();

    /**
     * must return the full url that needs to be called, use the baseUrl attribute to construct it
     * @return full url
     */
    protected abstract String getApiQueryString();

    /**
     * Override this one if you need to post application/json data to the api
     * @return string to post to api
     */
    @SuppressWarnings("WeakerAccess")
    protected String getApiBodyString() { return ""; }

    protected List<Pair<String,String>> getExtraHeaders() {return new ArrayList<Pair<String, String>>();}

    /**
     * this method needs to interpret the api reply and store it in the object in a way that can be retrieved later,
     * this could be just as a string or jsonobject but better parse further down to lists of Contacts or Blips and such.
     * @param content the reply of the api
     */
    protected abstract void parseContent(String content) throws JSONException;

    final public void callAndParse() throws IOException, JSONException {
        if(!isFullyInitialized()) throw new RuntimeException(TAG + " CliqueApiCall object not fully initialized");
        parseContent(myHttpRequest(getHttpMethod(), getApiQueryString(), getApiBodyString()));
    }

    /**
     * This method will throw an IOException when having any problems with the httprequest including any non 200 http status code.
     * It does not check for network connection.
     * TODO: (bug) figure out what would happen if called without a connection?
     * @param method must be "GET" or "POST" or "DELETE"
     * @param myUrl the full url of the http request
     * @param myBody the json string of the body
     * @return the body of the http reply
     * @throws IOException
     */
    private String myHttpRequest(String method, String myUrl, String myBody) throws IOException {
        URL url = new URL(myUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(5000);
        conn.setConnectTimeout(10000);
        if(!(method.equals("GET") || method.equals("POST") || method.equals("DELETE"))) throw new RuntimeException(method + " http method not supported");
        conn.setRequestMethod(method);
        conn.setDoInput(true);
        Log.i(TAG, method + " " + myUrl);
        for (Pair<String,String> header : getExtraHeaders()) {
            conn.setRequestProperty(header.first, header.second);
            Log.i(TAG, "setting header: " + header.first + " : " + header.second);
        }
        if(method.equals("POST")) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            Log.i(TAG, "BODY = " + myBody);
            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
            osw.write(myBody);
            osw.close();
        }
        conn.connect();
        int response = conn.getResponseCode();
        if(response!=200) throw new IOException(String.format("HTTP RESPONSE CODE = %d", response));

        InputStream is = conn.getInputStream();
        try {
            return readInputStream(is);
        } finally {
            is.close();
            //conn.disconnect();    // This should be called once when no other calls to the server will be made in the near future (put it at the end of a sync routine maybe)
            // see http://docs.oracle.com/javase/7/docs/api/java/net/HttpURLConnection.html#disconnect%28%29
        }
    }

    /**
     * This method will read up to 16KB bytes from a stream of UTF-8 and return a string of it.
     * TODO: is it necessary to allocate 16K every time?
     * @param is inputStream
     * @return String
     * @throws IOException when more than 16KB was in the stream
     */
    private String readInputStream(InputStream is) throws IOException {
        final int bufferSize = 16 * 1024;
        Reader reader = new InputStreamReader(is, "UTF-8");
        char[] buffer = new char[bufferSize];
        int charsRead = reader.read(buffer,0,bufferSize);
        int length = charsRead;
        Log.i(TAG, "charsRead = " + charsRead + "   length = " + length);
        while((charsRead != -1) && (length < bufferSize)) {
            charsRead = reader.read(buffer, length, charsRead);
            if (charsRead > 0) length += charsRead;
            Log.i(TAG, "charsRead = " + charsRead + "   length = " + length);
        }
        if (charsRead != -1) { // this happens when the response size was longer or equal to the buffer size)
            Log.i(TAG, "stream = " + new String(buffer, 0, length));
            throw new IOException(String.format("api response longer than expected (buffersize = %d)", bufferSize));
        }
        return new String(buffer, 0, length);
    }
}
