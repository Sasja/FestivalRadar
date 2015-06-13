package com.pollytronics.festivalradar.lib.api_v01;

import android.util.Log;

import com.pollytronics.festivalradar.RadarDatabase_Interface;
import com.pollytronics.festivalradar.lib.RadarBlip;
import com.pollytronics.festivalradar.lib.RadarContact;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pollywog on 6/3/15.
 */
public class ApiCallSetMyBlip extends RadarApiCall {
    private final String TAG = "ApiCallSetMyBlip";
    @SuppressWarnings("FieldCanBeLocal")
    private final String apiResourceName = "blips";
    private final JSONObject selfBlipJSON = new JSONObject();
    private long selfId = 0;

    @Override
    public void collectData(RadarDatabase_Interface db){
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
    public String getHttpMethod() {
        return "POST";
    }

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId;
    }

    @Override
    protected String getApiBodyString(){
        return selfBlipJSON.toString();
    }

    @Override
    protected void parseContent(String content) {
        Log.i(TAG, "api reply = " + content);
    }


}
