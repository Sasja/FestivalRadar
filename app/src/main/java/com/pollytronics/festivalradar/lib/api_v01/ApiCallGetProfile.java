package com.pollytronics.festivalradar.lib.api_v01;

import android.util.Log;

import com.pollytronics.festivalradar.RadarDatabase_Interface;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pollywog on 6/22/15.
 */
public class ApiCallGetProfile extends RadarApiCall {
    protected final String TAG = "ApiCallGetProfile";

    private final String apiResourceName = "profiles";
    private JSONObject profileJSON;
    private long requestedID;

    @Override
    public String getHttpMethod() { return "GET"; }

    @Override
    protected String getApiQueryString() { return baseUrl+apiResourceName+"?userid="+requestedID; }

    protected void parseContent(String content) {
        try {
            Log.i(TAG, content);
            profileJSON = new JSONObject(content).getJSONObject("profiles");
        } catch (JSONException e) {
            e.printStackTrace();
            profileJSON = null;
        }
    }

    @Override
    public void collectData(RadarDatabase_Interface db) { }

    public void setRequestedId(long id) { this.requestedID = id; }

    public String getName() {
        String name = "anon";
        try {
            name = profileJSON.getString("nick");
        } catch (JSONException e) {
            e.printStackTrace();
            name = "anon";
        } catch (NullPointerException e) {
            name = "anon";
        }
        return name;
    }
}
