package com.pollytronics.clique.lib.api_v02;

import android.util.Log;
import android.util.Pair;

import com.pollytronics.clique.lib.CliqueApiCall;
import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pollywog on 9/7/15.
 * TODO: (bug) what if some of those methods such as setpinggetset are called twice?
 * TODO: (feature) remove the auto-accept behaviour and develop a request/accept flow.
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
    private List<Long> newIcanSeeAdds = new ArrayList<>();
    private List<Long> newIcanSeeDels = new ArrayList<>();
    private List<Long> newCanSeeMeAdds = new ArrayList<>();
    private List<Long> newCanSeeMeDels = new ArrayList<>();
    private List<Pair<Profile, Long>> newProfiles = new ArrayList<>();
    private List<Pair<Long, String>> newPings = new ArrayList<>();

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

    //TODO: (code) this is nor efficient or very elegant
    public void addCanSeeme(long id) throws JSONException {
        JSONObject newEntry = new JSONObject().put("id", id);
        JSONObject contactsJS = bodyJS.has("contacts") ? bodyJS.getJSONObject("contacts") : new JSONObject();
        JSONArray adds = contactsJS.has("add") ? contactsJS.getJSONArray("add") : new JSONArray();
        adds.put(newEntry);
        contactsJS.put("add", adds);
        bodyJS.put("contacts",contactsJS);
    }

    //TODO: (code) this is duplicate code
    public void delCanSeeme(long id) throws JSONException {
        JSONObject newEntry = new JSONObject().put("id", id);
        JSONObject contactsJS = bodyJS.has("contacts") ? bodyJS.getJSONObject("contacts") : new JSONObject();
        JSONArray dels = contactsJS.has("delete") ? contactsJS.getJSONArray("delete") : new JSONArray();
        dels.put(newEntry);
        contactsJS.put("delete", dels);
        bodyJS.put("contacts",contactsJS);
    }

    //TODO: (code) this is nor efficient or very elegant
    public void addBlip(Blip blip) throws JSONException {
        JSONObject newEntry = new JSONObject().put("lat",blip.getLatitude()).put("lon", blip.getLongitude()).put("utc_s", blip.getUtc_s());
        JSONArray blipsJS = bodyJS.has("blips") ? bodyJS.getJSONArray("blips") : new JSONArray();
        blipsJS.put(newEntry);
        bodyJS.put("blips", blipsJS);
    }

    public void setPingGetSet(boolean get, boolean set) throws JSONException{
        JSONObject pingJS = new JSONObject().put("get", get).put("set", set);
        bodyJS.put("ping", pingJS);
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
                Log.i(TAG, "blipJS = " + blipJS.toString());
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
            if(contactJS.has("icansee")) {
                JSONObject icansee = contactJS.getJSONObject("icansee");
                if(icansee.has("add")) {
                    JSONArray addsJS = icansee.getJSONArray("add");
                    for(int i=0; i < addsJS.length(); i++) {
                        JSONObject addJS = addsJS.getJSONObject(i);
                        long id = addJS.getLong("id");
                        newIcanSeeAdds.add(id);
                    }
                }
                if(icansee.has("delete")) {
                    JSONArray delsJS = icansee.getJSONArray("delete");
                    for(int i=0; i < delsJS.length(); i++) {
                        JSONObject delJS = delsJS.getJSONObject(i);
                        long id = delJS.getLong("id");
                        newIcanSeeDels.add(id);
                    }
                }
            }
            if(contactJS.has("canseeme")) {
                JSONObject canseeme = contactJS.getJSONObject("canseeme");
                if(canseeme.has("add")) {
                    JSONArray addsJS = canseeme.getJSONArray("add");
                    for(int i=0; i < addsJS.length(); i++) {
                        JSONObject addJS = addsJS.getJSONObject(i);
                        long id = addJS.getLong("id");
                        newCanSeeMeAdds.add(id);
                    }
                }
                if(canseeme.has("delete")) {
                    JSONArray delsJS = canseeme.getJSONArray("delete");
                    for(int i=0; i < delsJS.length(); i++) {
                        JSONObject delJS = delsJS.getJSONObject(i);
                        long id = delJS.getLong("id");
                        newCanSeeMeDels.add(id);
                    }
                }
            }
            if(contactJS.has("profiles")) {
                JSONArray contactProfilesJS = contactJS.getJSONArray("profiles");
                for(int i=0; i < contactProfilesJS.length(); i++) {
                    JSONObject profileJS = contactProfilesJS.getJSONObject(i);
                    long id = profileJS.getLong("id");
                    String nick = profileJS.getString("nick");
                    newProfiles.add(new Pair<Profile, Long>(new Profile(nick),id));
                }
            }
        }

        // new pings?
        if(job.has("ping")) {
            JSONArray pingsJS = job.getJSONArray("ping");
            for(int i=0; i < pingsJS.length(); i++) {
                JSONObject pingJS = pingsJS.getJSONObject(i);
                long id = pingJS.getLong("id");
                String nick = pingJS.getString("nick");
                newPings.add(new Pair<Long, String>(id, nick));
            }
        }
    }

    public double getNewLastSync() { return newLastSync; }
    public String getCallMessage() { return callMessage; }
    public List<Pair<Blip, Long>> getNewBlips() { return newBlips; }
    public String getNewNickname() { return newNickname; }
    public List<Long> getNewIcanSeeAdds() { return newIcanSeeAdds; }
    public List<Long> getNewIcanSeeDels() { return newIcanSeeDels; }
    public List<Long> getNewCanSeeMeAdds() { return newCanSeeMeAdds; }
    public List<Long> getNewCanSeeMeDels() { return newCanSeeMeDels; }
    public List<Pair<Profile, Long>> getNewProfiles() {return newProfiles; }
    public List<Pair<Long, String>> getNewPings() { return newPings; }
    public boolean isAuthSuccess() { return authSuccess; }
    public boolean isCallSuccess() { return callSuccess; }
}