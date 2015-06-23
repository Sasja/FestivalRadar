package com.pollytronics.clique.lib.api_v01;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;
import com.pollytronics.clique.lib.database.CliqueDb_Interface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pollywog on 6/3/15.
 */
public class ApiCallGetBlips extends CliqueApiCall {
    @SuppressWarnings("unused")
    protected final String TAG = "ApiCallGetBlips";
    @SuppressWarnings("FieldCanBeLocal")
    private final String apiResourceName = "blips";
    private JSONArray blips;
    private long selfId = 0;

    public void collectData(CliqueDb_Interface db) {
        selfId = db.getSelfContact().getID();
    }

    @Override
    public String getHttpMethod() {
        return "GET";
    }

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId;
    }

    protected void parseContent(String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            blips = jsonObject.getJSONArray("blips");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void doTheWork(CliqueDb_Interface db) {
        JSONObject blipJSON;
        Long id, time;
        double lat, lon;
        Blip blip = new Blip();
        Contact contact;
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
}
