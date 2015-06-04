package com.pollytronics.festivalradar.lib.api;

import android.util.Log;

import com.pollytronics.festivalradar.RadarDatabase_Interface4RadarActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by pollywog on 6/3/15.
 */
public class ApiCallPostContact extends RadarApiCall {
    protected final String apiResourceName = "contacts";
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "ApiCallPostContact";
    private long selfId = 0;
    private JSONObject contactJSON;

    public void collectData(RadarDatabase_Interface4RadarActivity db) {
        selfId = db.getSelfContact().getID();
    }

    public void setContactId(long id) {
        try {
            contactJSON = new JSONObject().put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId;
    }

    private String getApiBodyString() {return contactJSON.toString();}

    private void parseContent(String content) { Log.i(TAG, "api reply = "+content);}

    @Override
    public void callAndParse() throws IOException {
        parseContent(myHttpPost(getApiQueryString(),getApiBodyString()));
    }
}
