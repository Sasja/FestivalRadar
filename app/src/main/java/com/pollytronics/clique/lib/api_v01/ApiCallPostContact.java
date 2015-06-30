package com.pollytronics.clique.lib.api_v01;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * adds a link in the api that allows contactId to see you
 */
public class ApiCallPostContact extends CliqueApiCall {
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "ApiCallPostContact";

    @SuppressWarnings("FieldCanBeLocal")
    private final String apiResourceName = "contacts";

    private long selfId;
    private String body;
    private boolean fullyInitialized = false;

    public ApiCallPostContact(long selfId) {
        this.selfId = selfId;
    }

    public ApiCallPostContact(long selfId, long contactId) throws JSONException {
        this.selfId = selfId;
        setContactId(contactId);
    }

    public void setContactId(long contactId) throws JSONException {
        this.body = new JSONObject().put("id", contactId).toString();
        this.fullyInitialized = true;
    }

    @Override
    protected boolean isFullyInitialized() {
        return fullyInitialized;
    }

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId;
    }

    @Override
    protected String getApiBodyString() {return body;}

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    @Override
    protected void parseContent(String content) { Log.i(TAG, "api reply = " + content);}

}
