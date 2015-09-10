package com.pollytronics.clique.lib.api_v01;

import android.support.annotation.Nullable;

import com.pollytronics.clique.lib.CliqueApiCall;
import com.pollytronics.clique.lib.base.Contact;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * retrieves a profile from the api for a specified global id
 */
@SuppressWarnings("FieldCanBeLocal")
@Deprecated
public class ApiCallGetProfile extends CliqueApiCall {
    protected final String TAG = "ApiCallGetProfile";

    private final String apiResourceName = "api/v1/profiles";
    private final long requestedID;
    private Contact profile;
    private boolean fullyInitialized = false;

    public ApiCallGetProfile(long requestedID) {
        this.requestedID = requestedID;
        this.fullyInitialized = true;
    }

    @Override
    protected boolean isFullyInitialized() {
        return fullyInitialized;
    }

    @Override
    public String getHttpMethod() { return "GET"; }

    @Override
    protected String getApiQueryString() { return baseUrl+apiResourceName+"?userid="+requestedID; }

    @Override
    protected void parseContent(String content) throws JSONException {
        profile = new Contact((new JSONObject(content)).getJSONObject("profiles"));
    }

    @Nullable
    public Contact getContact() {
        return profile;
    }
}
