package com.pollytronics.festivalradar.lib.api;

import com.pollytronics.festivalradar.RadarDatabase_Interface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract class for get contacts seeme and isee to prevent lots of duplicate code
 * only one parameter is different in the rest api call
 * Created by pollywog on 6/13/15.
 */
public abstract class ApiCallGetContactsAbstract extends RadarApiCall {
    @SuppressWarnings("FieldCanBeLocal")
    protected final String apiResourceName = "contacts";
    @SuppressWarnings("unused")
    private final String TAG = "ApiCallGetContactsAbstract";
    protected long selfId = 0;
    private JSONArray contacts;

    public void collectData(RadarDatabase_Interface db) {
        selfId = db.getSelfContact().getID();
    }

    @Override
    protected abstract String getApiQueryString();

    private void parseContent(String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            contacts = jsonObject.getJSONArray("contacts");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void callAndParse() throws IOException {
        parseContent(myHttpGet(getApiQueryString()));
    }

    public Set<Long> getCollection() {
        Set<Long> collection = new HashSet<Long>();        //TODO: not sure what best data type is
        for (int i=0; i<contacts.length(); i++) {
            try {
                collection.add(contacts.getJSONObject(i).getLong("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return collection;
    }
}
