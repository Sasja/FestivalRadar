package com.pollytronics.festivalradar.lib.api_v01;

import android.util.Log;

import com.pollytronics.festivalradar.lib.database.RadarDatabase_Interface;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pollywog on 6/23/15.
 */
public class ApiCallPostProfile extends RadarApiCall {
    private final String TAG = "ApiCallPostProfile";

    private final String apiResourceName = "profiles";

    private long selfId = 0;
    private JSONObject profileJSON;

    public void collectData(RadarDatabase_Interface db) { selfId = db.getSelfContact().getID();}

    public void setName(String name) {
        try {
            JSONObject markerJSON = new JSONObject().put("color","#808080").put("icon","dot");
            profileJSON = new JSONObject().put("nick", name).put("marker",markerJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getApiQueryString() { return baseUrl+apiResourceName+"?userid="+selfId; }

    @Override
    public String getHttpMethod() { return "POST"; }

    @Override
    protected String getApiBodyString() { return profileJSON.toString(); }

    @Override
    protected void parseContent(String content) { Log.i(TAG, "api reply = " + content); }
}
