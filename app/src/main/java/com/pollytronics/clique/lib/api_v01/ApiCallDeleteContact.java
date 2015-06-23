package com.pollytronics.clique.lib.api_v01;

import android.util.Log;

import com.pollytronics.clique.lib.database.CliqueDb_Interface;

/**
 * Created by pollywog on 6/4/15.
 */
public class ApiCallDeleteContact extends CliqueApiCall {
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "ApiCallDeleteContact";

    @SuppressWarnings("FieldCanBeLocal")
    private final String apiResourceName = "contacts";
    private long selfId = 0;
    private long deleteId = 0;

    @Override
    public void collectData(CliqueDb_Interface db) {
        selfId = db.getSelfContact().getID();
    }

    public void setContactId(long id) {
        deleteId = id;
    }

    @Override
    public String getHttpMethod() {
        return "DELETE";
    }

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId+"&contactid="+deleteId;
    }

    protected void parseContent(String content) { Log.i(TAG, "api reply = " + content);}
}
