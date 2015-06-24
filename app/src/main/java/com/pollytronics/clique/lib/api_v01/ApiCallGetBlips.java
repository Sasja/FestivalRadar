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
        selfId = db.getSelfContact().getGlobalId();
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
        Long id;
        Blip blip;
        Contact contact;
        for (int i = 0; i < blips.length(); i++) {
            try {
                blipJSON = blips.getJSONObject(i);
                blip = new Blip(blipJSON);
                id = blipJSON.getLong("userid");
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
            contact = db.getContactById(id);
            if(contact != null) {   // check if contact is known locally on phone
                db.addBlip(blip,contact);
            }
        }
    }
}
