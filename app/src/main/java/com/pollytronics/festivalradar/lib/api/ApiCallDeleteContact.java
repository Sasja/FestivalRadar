package com.pollytronics.festivalradar.lib.api;

import android.util.Log;

import com.pollytronics.festivalradar.RadarDatabase_Interface;

import java.io.IOException;

/**
 * Created by pollywog on 6/4/15.
 */
public class ApiCallDeleteContact extends RadarApiCall {
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "ApiCallDeleteContact";

    @SuppressWarnings("FieldCanBeLocal")
    private final String apiResourceName = "contacts";
    private long selfId = 0;
    private long deleteId = 0;

    @Override
    public void collectData(RadarDatabase_Interface db) {
        selfId = db.getSelfContact().getID();
    }

    public void setContactId(long id) {
        deleteId = id;
    }

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId+"&contactid="+deleteId;
    }

    private void parseContent(String content) { Log.i(TAG, "api reply = " + content);}

    @Override
    public void callAndParse() throws IOException {
        parseContent(myHttpDelete(getApiQueryString()));
    }
}
