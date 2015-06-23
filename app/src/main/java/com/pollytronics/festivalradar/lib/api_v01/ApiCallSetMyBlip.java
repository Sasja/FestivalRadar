package com.pollytronics.festivalradar.lib.api_v01;

import android.util.Log;

import com.pollytronics.festivalradar.lib.base.Blip;
import com.pollytronics.festivalradar.lib.database.CliqueDb_Interface;
import com.pollytronics.festivalradar.lib.base.Contact;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pollywog on 6/3/15.
 */
public class ApiCallSetMyBlip extends CliqueApiCall {
    private final String TAG = "ApiCallSetMyBlip";
    @SuppressWarnings("FieldCanBeLocal")
    private final String apiResourceName = "blips";
    private final JSONObject selfBlipJSON = new JSONObject();
    private long selfId = 0;

    @Override
    public void collectData(CliqueDb_Interface db){
        Log.i(TAG, "collecting data for APICallSetMyBlip");
        Contact selfContact = db.getSelfContact();
        selfId = selfContact.getID();
        Blip selfBlip = selfContact.getLastBlip();
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
