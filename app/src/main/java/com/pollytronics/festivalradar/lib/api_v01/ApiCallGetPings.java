package com.pollytronics.festivalradar.lib.api_v01;

import com.pollytronics.festivalradar.RadarDatabase_Interface;
import com.pollytronics.festivalradar.lib.RadarContact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pollywog on 6/19/15.
 */
public class ApiCallGetPings extends RadarApiCall {

    @SuppressWarnings("unused")
    protected final String TAG = "ApiCallGetPings";

    private final String apiResourceName = "pings";
    private JSONArray pings;
    private long selfId = 0;

    public void collectData(RadarDatabase_Interface db) {selfId = db.getSelfContact().getID(); }

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

    public List<RadarContact> getAllPingContacts() {
        List<RadarContact> pingContacts = new ArrayList<>();
        JSONObject pingJSON;
        for(int i = 0; i < pings.length(); i++) {
            try {
                pingJSON = pings.getJSONObject(i);
                RadarContact rc = new RadarContact();
                rc.setID(pingJSON.getLong("userid"));
                rc.setName(pingJSON.getString("nick"));
                pingContacts.add(rc);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return pingContacts;
    }
}
