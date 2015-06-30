package com.pollytronics.clique.lib.api_v01;

import android.util.Log;

/**
 * requests deletion of the link between a user and his contact
 */
public class ApiCallDeleteContact extends CliqueApiCall {
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "ApiCallDeleteContact";

    @SuppressWarnings("FieldCanBeLocal")
    private final String apiResourceName = "contacts";
    boolean fullyInitialized = false;
    private long selfId;
    private long deleteId;

    public ApiCallDeleteContact(long selfId, long deleteId) {
        this.selfId = selfId;
        this.deleteId = deleteId;
        this.fullyInitialized = true;
    }

    @Override
    protected boolean isFullyInitialized() {
        return fullyInitialized;
    }

    @Override
    public String getHttpMethod() {
        return "DELETE";
    }

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId+"&contactid="+deleteId;
    }

    /**
     * does not have to do anything as there is only a "ok" response when succesfull. and callAndParse() throws an error when fails.
     * @param content
     */
    @Override
    protected void parseContent(String content) {
        Log.i(TAG, "api reply = " + content);
    }
}
