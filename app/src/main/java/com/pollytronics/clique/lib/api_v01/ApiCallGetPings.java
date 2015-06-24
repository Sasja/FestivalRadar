package com.pollytronics.clique.lib.api_v01;

import com.pollytronics.clique.lib.base.Contact;
import com.pollytronics.clique.lib.database.CliqueDb_Interface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pollywog on 6/19/15.
 */
public class ApiCallGetPings extends CliqueApiCall {

    @SuppressWarnings("unused")
    protected final String TAG = "ApiCallGetPings";

    private final String apiResourceName = "pings";
    private JSONArray pings;
    private long selfId = 0;

    public void collectData(CliqueDb_Interface db) {selfId = db.getSelfContact().getGlobalId(); }

    @Override
    public String getHttpMethod() { return "GET"; }

    @Override
    protected String getApiQueryString() { return baseUrl+apiResourceName+"?userid="+selfId; }

    protected void parseContent(String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            pings = jsonObject.getJSONArray("pings");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Contact> getAllPingContacts() {
        List<Contact> pingContacts = new ArrayList<>();
        JSONObject pingJSON;
        for(int i = 0; i < pings.length(); i++) {
            try {
                pingJSON = pings.getJSONObject(i);
                Contact rc = new Contact(pingJSON);
                pingContacts.add(rc);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return pingContacts;
    }
}
