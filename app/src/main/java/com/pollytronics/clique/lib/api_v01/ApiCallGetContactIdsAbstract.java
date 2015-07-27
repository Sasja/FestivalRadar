package com.pollytronics.clique.lib.api_v01;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for ApiCallGetContacsSeeme and ApiCallGetContactIdsISee to prevent lots of duplicate code
 * only one attribute is different between the two subclasses in the api call
 * Note that only ids are transfered, not full contact info
 */
public abstract class ApiCallGetContactIdsAbstract extends CliqueApiCall {
    @SuppressWarnings("FieldCanBeLocal")
    final String apiResourceName = "contacts";
    final long selfId;
    @SuppressWarnings("unused")
    private final String TAG = "ApiCallGetContactIdsAbstract";
    private final List<Long> contactIds = new ArrayList<>();

    private boolean fullyInitialized = false;

    ApiCallGetContactIdsAbstract(long selfId) {
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
    protected abstract String getApiQueryString();

    protected void parseContent(String content) throws JSONException {
        JSONArray jsonContactArray = (new JSONObject(content)).getJSONArray("contacts");
        for(int i = 0; i < jsonContactArray.length(); i++) {
            contactIds.add(jsonContactArray.getJSONObject(i).getLong("id"));
        }
    }

    public List<Long> getContactIds() {
        return contactIds;
    }
}
