package com.pollytronics.festivalradar.lib.api;

import android.util.Log;

import com.pollytronics.festivalradar.RadarDatabase_Interface4RadarActivity;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by pollywog on 6/4/15.
 */
public class ApiCallDeleteContact extends RadarApiCall {
    protected final String apiResourceName = "contacts";
    private final String TAG = "ApiCallDeleteContact";
    private long selfId = 0;
    private long deleteId = 0;
    private JSONObject contactJSON;

    public void collectData(RadarDatabase_Interface4RadarActivity db) {
        selfId = db.getSelfContact().getID();
    }

    public void setContactId(long id) {
        deleteId = id;
    }

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId+"?contactid="+deleteId;
    }

    private void parseContent(String content) { Log.i(TAG, "api reply = " + content);}

    @Override
    public void callAndParse() throws IOException {
        parseContent(myHttpDelete(getApiQueryString()));
    }
}
