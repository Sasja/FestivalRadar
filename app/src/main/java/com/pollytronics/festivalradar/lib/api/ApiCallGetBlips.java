package com.pollytronics.festivalradar.lib.api;

import com.pollytronics.festivalradar.RadarDatabase_Interface4RadarService;
import com.pollytronics.festivalradar.lib.RadarBlip;
import com.pollytronics.festivalradar.lib.RadarContact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by pollywog on 6/3/15.
 */
public class ApiCallGetBlips extends RadarApiCall {
    protected final String TAG = "ApiCallGetBlips";
    protected final String apiResourceName = "blips";
    private JSONArray blips;
    private long selfId = 0;

    @Override
    public void collectData(RadarDatabase_Interface4RadarService db) {
        selfId = db.getSelfContact().getID();
    }

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId;
    }

    private void parseContent(String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            blips = jsonObject.getJSONArray("blips");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doTheWork(RadarDatabase_Interface4RadarService db) {
        JSONObject blipJSON;
        Long id, time;
        double lat, lon;
        RadarBlip blip = new RadarBlip();
        RadarContact contact;
        for (int i = 0; i < blips.length(); i++) {
            try {
                blipJSON = blips.getJSONObject(i);
                id = blipJSON.getLong("userid");
                lat = blipJSON.getDouble("lat");
                lon = blipJSON.getDouble("lon");
                time = blipJSON.getLong("time");
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
            blip.setLatitude(lat);
            blip.setLongitude(lon);
            blip.setTime(time);
            contact = db.getContact(id);
            if(contact != null) {   // check if contact is known locally on phone
                contact.addBlip(blip);
                db.updateContact(contact);
            }
        }
    }

    @Override
    public void callAndParse() throws IOException {
        parseContent(myHttpGet(getApiQueryString()));
    }
}
