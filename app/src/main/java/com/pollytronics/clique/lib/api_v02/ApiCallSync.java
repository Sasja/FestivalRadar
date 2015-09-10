package com.pollytronics.clique.lib.api_v02;

import android.util.Pair;

import com.pollytronics.clique.lib.CliqueApiCall;
import com.pollytronics.clique.lib.base.Blip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pollywog on 9/7/15.
 */
public class ApiCallSync extends CliqueApiCall {
    private final String TAG = "ApiCallSync";

    private final String apiResourceName = "api/v2/sync";
    private boolean fullyInitialized = false;
    private JSONObject bodyJS = new JSONObject();
    private List<Pair<String, String>> headers = new ArrayList<>();

    private boolean callSuccess = false;
    private boolean authSuccess = false;
    private String callMessage = null;
    private double newLastSync = 0;
    private List<Pair<Blip, Long>> newBlips = new ArrayList<>();
    private String newNickname = null;
    private List<Long> newContactAdds = new ArrayList<>();
    private List<Long> newContactDels = new ArrayList<>();
    private Map<Long, String> newProfileNames = new HashMap<>();

    public ApiCallSync(String username, String key) throws JSONException {
        headers.add(new Pair<String, String>("username", username));
        headers.add(new Pair<String, String>("key", key));
        fullyInitialized = true;
    }

    public void setLastSync(double lastSync) throws JSONException {
        bodyJS.put("lastsync", lastSync);
    }

    public void setNickname(String nick) throws JSONException {
        JSONObject profileJS = new JSONObject().put("nick",nick);
        bodyJS.put("profile",profileJS);
    }

    //TODO: this is nor efficient or very elegant
    public void addContact(long id) throws JSONException {
        JSONObject newEntry = new JSONObject().put("id", id);
        JSONObject contactsJS = bodyJS.has("contacts") ? bodyJS.getJSONObject("contacts") : new JSONObject();
        JSONArray adds = contactsJS.has("add") ? contactsJS.getJSONArray("add") : new JSONArray();
        adds.put(newEntry);
        contactsJS.put("add", adds);
        bodyJS.put("contacts",contactsJS);
    }

    //TODO: this is duplicate code
    public void delContact(long id) throws JSONException {
        JSONObject newEntry = new JSONObject().put("id", id);
        JSONObject contactsJS = bodyJS.has("contacts") ? bodyJS.getJSONObject("contacts") : new JSONObject();
        JSONArray dels = contactsJS.has("del") ? contactsJS.getJSONArray("del") : new JSONArray();
        dels.put(newEntry);
        contactsJS.put("del", dels);
        bodyJS.put("contacts",contactsJS);
    }

    public void addBlip(Blip blip) throws JSONException {
        JSONObject newEntry = new JSONObject().put("lat",blip.getLatitude()).put("lon", blip.getLongitude()).put("utc_s", blip.getUtc_s());
        JSONArray blipsJS = bodyJS.has("blips") ? bodyJS.getJSONArray("blips") : new JSONArray();
        blipsJS.put(newEntry);
        bodyJS.put("blips", blipsJS);
    }

    @Override
    protected boolean isFullyInitialized() { return fullyInitialized; }

    @Override
    protected String getHttpMethod() { return "POST"; }

    @Override
    protected String getApiQueryString() { return baseUrl + apiResourceName; }

    @Override
    protected List<Pair<String, String>> getExtraHeaders() { return headers; }

    @Override
    protected String getApiBodyString() { return bodyJS.toString(); }

    @Override
    protected void parseContent(String content) throws JSONException {
        JSONObject job = new JSONObject(content);
        this.authSuccess = job.getBoolean("auth");
        this.callSuccess = job.getBoolean("success");
        if(job.has("message")) this.callMessage = job.getString("message");
        if(job.has("sync_time")) this.newLastSync = job.getDouble("sync_time");
        // new blips?
        if(job.has("blips")) {
            JSONArray blipsJS = job.getJSONArray("blips");
            for(int i=0; i < blipsJS.length(); i++) {
                JSONObject blipJS = blipsJS.getJSONObject(i);
                Blip blip = new Blip(blipJS.getDouble("lat"), blipJS.getDouble("lon"), blipJS.getDouble("utc_s"));
                long id = blipJS.getLong("id");
                newBlips.add(new Pair<Blip, Long>(blip, id));
            }
        }
        // new nickname?
        if(job.has("profile")) {
            JSONObject profileJS = job.getJSONObject("profile");
            if(profileJS.has("nick")) this.newNickname = profileJS.getString("nick");
        }

        // new contacts?
        if(job.has("contacts")) {
            JSONObject contactJS = job.getJSONObject("contacts");
            if(contactJS.has("add")) {
                JSONArray addsJS = contactJS.getJSONArray("add");
                for(int i=0; i < addsJS.length(); i++) {
                    JSONObject addJS = addsJS.getJSONObject(i);
                    long id = addJS.getLong("id");
                    newContactAdds.add(id);
                }
            }
            if(contactJS.has("del")) {
                JSONArray delsJS = contactJS.getJSONArray("del");
                for(int i=0; i < delsJS.length(); i++) {
                    JSONObject delJS = delsJS.getJSONObject(i);
                    long id = delJS.getLong("id");
                    newContactDels.add(id);
                }
            }
            if(contactJS.has("profiles")) {
                JSONArray contactProfilesJS = contactJS.getJSONArray("profiles");
                for(int i=0; i < contactProfilesJS.length(); i++) {
                    JSONObject profileJS = contactProfilesJS.getJSONObject(i);
                    long id = profileJS.getLong("id");
                    String nick = profileJS.getString("nick");
                    newProfileNames.put(id, nick);
                }
            }
        }

        // new profiles? TODO
        // ping TODO
        if(job.has("ping")) {
            JSONArray pingJS = job.getJSONArray("ping");
        }
    }

    public double getNewLastSync() { return newLastSync; }
    public String getCallMessage() { return callMessage; }
    public List<Pair<Blip, Long>> getNewBlips() { return newBlips; }
    public String getNewNickname() { return newNickname; }
    public List<Long> getNewContactAdds() { return newContactAdds; }
    public Map<Long, String> getNewProfileNames() { return newProfileNames; }
    public boolean isAuthSuccess() { return authSuccess; }
    public boolean isCallSuccess() { return callSuccess; }
}