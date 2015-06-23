package com.pollytronics.festivalradar.lib.api_v01;

import android.util.Log;

import com.pollytronics.festivalradar.lib.database.CliqueDb_Interface;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pollywog on 6/3/15.
 */
public class ApiCallPostContact extends CliqueApiCall {
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "ApiCallPostContact";

    @SuppressWarnings("FieldCanBeLocal")
    private final String apiResourceName = "contacts";

    private long selfId = 0;
    private JSONObject contactJSON;

    public void collectData(CliqueDb_Interface db) {
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

    @Override
    protected String getApiBodyString() {return contactJSON.toString();}

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    @Override
    protected void parseContent(String content) { Log.i(TAG, "api reply = " + content);}

}
