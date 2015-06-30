package com.pollytronics.clique.lib.api_v01;

import com.pollytronics.clique.lib.base.Blip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Simply requests all latest blips i have access to (api might give you more than that)
 * result can be obtained by getBlipList()
 * using this api call the response will contain "userid" fields that will be stored in the optional ownerId attribute of the Blips
 */
public class ApiCallGetBlips extends CliqueApiCall {
    @SuppressWarnings("unused")
    protected final String TAG = "ApiCallGetBlips";

    @SuppressWarnings("FieldCanBeLocal")
    private final String apiResourceName = "blips";
    private List<Blip> blips = new ArrayList<>();
    private long selfId;
    private boolean fullyInitialized = false;

    public ApiCallGetBlips(long selfId) {
        this.selfId = selfId;
        this.fullyInitialized = true;
    }

    @Override
    protected boolean isFullyInitialized() {
        return fullyInitialized;
    }

    @Override
    public String getHttpMethod() {
        return "GET";
    }

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId;
    }

    /**
     * fills the blips List with blips created from the api response
     * @param content
     * @throws JSONException
     */
    @Override
    protected void parseContent(String content) throws JSONException {
        JSONArray jsonBlipArray = (new JSONObject(content)).getJSONArray("blips");
        for(int i = 0; i< jsonBlipArray.length(); i++) {
            blips.add(new Blip(jsonBlipArray.getJSONObject(i)));
        }
    }

    /**
     * @return a list of Blip's retrieved from the api, it's up to the caller to decide what to do with it
     */
    public List<Blip> getBlipList() { return blips; }
}
