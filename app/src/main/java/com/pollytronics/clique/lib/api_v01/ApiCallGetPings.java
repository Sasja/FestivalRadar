package com.pollytronics.clique.lib.api_v01;

import com.pollytronics.clique.lib.CliqueApiCall;
import com.pollytronics.clique.lib.base.Contact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * provides a list of contacts that pinged along with me
 */
@SuppressWarnings("FieldCanBeLocal")
@Deprecated
public class ApiCallGetPings extends CliqueApiCall {
    @SuppressWarnings("unused")
    protected final String TAG = "ApiCallGetPings";

    private final String apiResourceName = "api/v1/pings";

    private final List<Contact> pings = new ArrayList<>();
    private final long selfId;
    private boolean fullyInitialized = false;

    public ApiCallGetPings(long selfId) {
        this.selfId = selfId;
        this.fullyInitialized = true;
    }

    @Override
    protected boolean isFullyInitialized() {
        return fullyInitialized;
    }

    @Override
    public String getHttpMethod() { return "GET"; }

    @Override
    protected String getApiQueryString() { return baseUrl+apiResourceName+"?userid="+selfId; }

    protected void parseContent(String content) throws JSONException{
        JSONArray jsonPingsArray = (new JSONObject(content)).getJSONArray("pings");
        for(int i = 0; i < jsonPingsArray.length(); i++) {
            pings.add(new Contact(jsonPingsArray.getJSONObject(i)));
        }
    }

    public List<Contact> getAllPingContacts() {
        return pings;
    }
}
