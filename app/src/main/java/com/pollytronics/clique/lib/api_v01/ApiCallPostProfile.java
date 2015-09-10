package com.pollytronics.clique.lib.api_v01;

import com.pollytronics.clique.lib.CliqueApiCall;
import com.pollytronics.clique.lib.base.Contact;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * create a new profile or update an existing one
 * when user id is set to 0 the api will create a new profile and return the profile with its appointed id
 *
 * TODO: (feature) post profile will only use the name and id at the moment and set other attributes to constants...
 */
@SuppressWarnings("FieldCanBeLocal")
@Deprecated
public class ApiCallPostProfile extends CliqueApiCall {
    private final String TAG = "ApiCallPostProfile";

    private final String apiResourceName = "api/v1/profiles";

    private final long selfId;
    private Contact profileReceive;
    private String body;
    private boolean fullyInitialized = false;

    public ApiCallPostProfile(Contact profile) throws JSONException {
        this.selfId = profile.getGlobalId();
        JSONObject markerJSON = new JSONObject().put("color","#808080").put("icon","dot");
        this.body = new JSONObject().put("nick", profile.getName()).put("marker", markerJSON).toString();
        this.fullyInitialized = true;
    }

    @Override
    protected boolean isFullyInitialized() {
        return fullyInitialized;
    }

    @Override
    protected String getApiQueryString() { return baseUrl+apiResourceName+"?userid="+selfId; }

    @Override
    public String getHttpMethod() { return "POST"; }

    @Override
    protected String getApiBodyString() { return body; }

    @Override
    protected void parseContent(String content) throws JSONException {
        this.profileReceive = new Contact(new JSONObject(content).getJSONObject("profiles"));
    }

    public Contact getContact() {
        return this.profileReceive;
    }
}
