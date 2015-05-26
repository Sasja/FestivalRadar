package com.pollytronics.festivalradar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.pollytronics.festivalradar.lib.RadarBlip;
import com.pollytronics.festivalradar.lib.RadarContact;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

/**
 * Cloud SubService 1
 *
 * periodically pulls and pushes data from server and updates the local database
 *
 * TODO: migrate to version 2 (SubService_Cloud_2) i deprecate this :P
 *
 * Created by pollywog on 9/23/14.
 */
public class SubService_Cloud_1 extends SubService {

    private final String TAG = "SubService_Cloud_1";
    private int updateTime_ms;
    private boolean cleaningUp = false;     //a flag so the network pull loop will stop posting itself
    private final Runnable cloudLoop = new Runnable() {
        @Override
        public void run() {
            try{
                ConnectivityManager connMgr = (ConnectivityManager) getRadarService().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    new PushSelfContactTask().execute();
                    new PullAllContactsTask() {
                        @Override
                        protected void onPostExecute(String response) {
                            super.onPostExecute(response);
                            getMainHandler().removeCallbacks(cloudLoop);    //make sure we dont have 2 loops
                            if(!cleaningUp) getMainHandler().postDelayed(cloudLoop,updateTime_ms);
                        }
                    }.execute();
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

    public SubService_Cloud_1(RadarService rs) {
        super(rs);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        updateTime_ms = (int) getRadarPreferences().getCloudUpdateTime_ms();
        Log.i(TAG, "set updateTime to (ms) "+Integer.toString(updateTime_ms));
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
        Log.i(TAG, "set updateTime to (ms) "+Integer.toString(updateTime_ms));
        getMainHandler().post(cloudLoop);
    }

    private class PushSelfContactTask extends AsyncTask<Void, Void, String> {
        String queryString = "";

        @Override
        protected void onPreExecute() {
            RadarContact selfContact =  getRadarDatabase().getSelfContact();
            RadarBlip selfBlip = selfContact.getLastBlip();
            JSONObject queryJSON = new JSONObject();
            JSONObject blipJSON = new JSONObject();
            try {
                blipJSON.put("lat", selfBlip.getLatitude());
                blipJSON.put("lon", selfBlip.getLongitude());
                blipJSON.put("time", selfBlip.getTime());
                queryJSON.put("userId",selfContact.getID());
                queryJSON.put("radarBlip", blipJSON);
                queryString = queryJSON.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            Log.i(TAG,"PUSH: will do request with JSONdata="+queryString);
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://festivalradarservice.herokuapp.com/webservice/setMyBlip");     // TODO: domain should be defined elsewhere and not repeated
            try {
                httpPost.setEntity(new StringEntity(queryString));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            httpPost.setHeader("Content-Type", "application/json");
            String response = "";
            try {
                response = httpClient.execute(httpPost, new BasicResponseHandler());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            Log.i(TAG, "PushSelfContactTask.onPostExecute() with server response="+response);
        }
    }

    private class PullAllContactsTask extends AsyncTask<Void, Void, String> {
        String queryString = "";

        @Override
        protected void onPreExecute() {
            Collection<Long> contact_ids = getRadarDatabase().getAllContactIds();
            JSONArray idListJSON = new JSONArray(contact_ids);
            JSONObject queryJSON = new JSONObject();
            try {
                queryJSON.put("idList", idListJSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            queryString = queryJSON.toString();
            Log.i(TAG, "lets query this: " + queryString);
        }

        @Override
        protected String doInBackground(Void... voids) {
            Log.i(TAG, "PULL: will do request with JSONdata=" + queryString);
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://festivalradarservice.herokuapp.com/webservice/getBlips");
            try {
                httpPost.setEntity(new StringEntity(queryString));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            httpPost.setHeader("Content-Type", "application/json");
            String response = "";
            try {
                response = httpClient.execute(httpPost, new BasicResponseHandler());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            Log.i(TAG, "PullAllContactsTask.onPostExecute() with response=" + response);
            Long id, time;
            double lat, lon;
            JSONArray newBlips;
            JSONObject blipJSON;
            RadarBlip blip = new RadarBlip();
            RadarContact contact;

            try {
                JSONObject responseJSON = new JSONObject(response);
                newBlips = responseJSON.getJSONArray("lastBlips");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            for (int i = 0; i < newBlips.length(); i++) {
                try {
                    blipJSON = newBlips.getJSONObject(i);
                    id = blipJSON.getLong("userId");
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
                contact.addBlip(blip);
                getRadarDatabase().updateContact(contact);
            }
            getRadarService().notifyNewData();

        }
    }
}
