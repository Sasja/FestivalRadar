package com.pollytronics.clique.lib.api_v01;

import android.util.Log;

import com.pollytronics.clique.lib.CliqueApiCall;
import com.pollytronics.clique.lib.base.Blip;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * posts your last blip to the api
 */
@SuppressWarnings("FieldCanBeLocal")
@Deprecated
public class ApiCallSetMyBlip extends CliqueApiCall {
    private final String TAG = "ApiCallSetMyBlip";

    private final String apiResourceName = "api/v1/blips";
    private final long selfId;
    private String body;
    private boolean fullyInitialized = false;

    public ApiCallSetMyBlip(Blip selfBlip, long selfId) throws JSONException {
        this.selfId = selfId;
        this.body = new JSONObject().put("lat", selfBlip.getLatitude()).put("lon", selfBlip.getLongitude()).toString();
        this.fullyInitialized = true;
    }

    @Override
    protected boolean isFullyInitialized() {
        return fullyInitialized;
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId;
    }

    @Override
    protected String getApiBodyString(){
        return body;
    }

    @Override
    protected void parseContent(String content) {
        Log.i(TAG, "api reply = " + content);
    }
}
