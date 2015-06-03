package com.pollytronics.festivalradar.lib.api;

import android.util.Log;

import com.pollytronics.festivalradar.RadarDatabase_Interface4RadarService;
import com.pollytronics.festivalradar.lib.RadarBlip;
import com.pollytronics.festivalradar.lib.RadarContact;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pollywog on 6/3/15.
 */
public class ApiCallSetMyBlip extends RadarApiCall {
    protected final String TAG = "ApiCallSetMyBlip";
    protected final String apiResourceName = "blips";
    private JSONObject selfBlipJSON = new JSONObject();
    private long selfId = 0;

    @Override
    public void collectData(RadarDatabase_Interface4RadarService db){
        Log.i(TAG, "collecting data for APICallSetMyBlip");
        RadarContact selfContact = db.getSelfContact();
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
    public void doTheWork(RadarDatabase_Interface4RadarService db) {}

    public String getApiBodyString(){
        return selfBlipJSON.toString();
    }

    public void parseContent(String content) {
        Log.i(TAG, "api reply = "+content);
    }
}
