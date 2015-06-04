package com.pollytronics.festivalradar.lib.api;

import com.pollytronics.festivalradar.RadarDatabase_Interface4RadarActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO: this is superduplicate code with ApiCallGetContactsIsee
 * Created by pollywog on 6/3/15.
 */
public class ApiCallGetContactsSeeme extends RadarApiCall {
    protected final String TAG = "ApiCallGetContactsSeeme";
    protected final String apiResourceName = "contacts";
    private JSONArray contacts;
    private long selfId = 0;

    public void collectData(RadarDatabase_Interface4RadarActivity db) {
        selfId = db.getSelfContact().getID();
    }

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId+"&mode=seeme";
    }

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
