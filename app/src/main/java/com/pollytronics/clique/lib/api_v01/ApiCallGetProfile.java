package com.pollytronics.clique.lib.api_v01;

import android.util.Log;

import com.pollytronics.clique.lib.base.Contact;
import com.pollytronics.clique.lib.database.CliqueDb_Interface;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pollywog on 6/22/15.
 */
public class ApiCallGetProfile extends CliqueApiCall {
    protected final String TAG = "ApiCallGetProfile";

    private final String apiResourceName = "profiles";
    private JSONObject profileJSON;
    private long requestedID;

    @Override
    public String getHttpMethod() { return "GET"; }

    @Override
    protected String getApiQueryString() { return baseUrl+apiResourceName+"?userid="+requestedID; }

    //TODO: shouldn't this just throw instead of returning null?
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
    public void collectData(CliqueDb_Interface db) { }

    public void setRequestedId(long id) { this.requestedID = id; }

    public Contact getContact() throws JSONException {
        return new Contact(profileJSON);
    }
}
