package com.pollytronics.clique.lib.api_v02;

import android.util.Log;
import android.util.Pair;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by pollywog on 9/7/15.
 * TODO: (bug) what if some of those methods such as setpinggetset are called twice?
 * TODO: (feature) remove the auto-accept behaviour and develop a request/accept flow.
 */
public class ApiCallSync extends CliqueApiCall {
    private static final String TAG = "ApiCallSync";

    private static final String apiResourceName = "api/v2/sync";

    private boolean fullyInitialized = false;

        private List<Pair<String, String>> headers = new ArrayList<>();

    private static class Request {
        private double lastSync = 0;
        private boolean lastSyncSet = false;
        private String nick = null;
        private Set<Long> canSeeMeAdds = new HashSet<>();
        private Set<Long> canSeeMeDels = new HashSet<>();
        private List<Blip> blips = new ArrayList<>();
        private boolean pingGet = false;
        private boolean pingSet = false;
    }

    private Request request = new Request();

    private static class Response {
        private boolean callSuccess = false;
        private boolean authSuccess = false;
        private String callMessage = null;
        private double newLastSync = 0;
        private List<Pair<Blip, Long>> newBlips = new ArrayList<>();
        private String newNickname = null;
        private List<Long> iCanSeeAdds = new ArrayList<>();
        private List<Long> iCanSeeDels = new ArrayList<>();
        private List<Long> canSeeMeAdds = new ArrayList<>();
        private List<Long> canSeeMeDels = new ArrayList<>();
        private List<Pair<Profile, Long>> newProfiles = new ArrayList<>();
        private List<Pair<Long, String>> newPings = new ArrayList<>();
    }

    private Response response = new Response();

    public ApiCallSync(String username, String key) throws JSONException {
        headers.add(new Pair<>("username", username));
        headers.add(new Pair<>("key", key));
        fullyInitialized = true;
    }

    public void setLastSync(double lastSync) throws JSONException {
        request.lastSync = lastSync;
        request.lastSyncSet = true;
    }

    public void setNickname(String nick) throws JSONException {
        request.nick = nick;
    }

    public void addCanSeeme(List<Long> adds) {
        request.canSeeMeAdds.addAll(adds);
    }

    public void delCanSeeme(List<Long> dels) {
        request.canSeeMeDels.addAll(dels);
    }

    public void addBlips(List<Blip> blips) {
        request.blips.addAll(blips);
    }

    public void setPingGetSet(boolean get, boolean set) {
        request.pingGet = get;
        request.pingSet = set;
    }

    @Override
    boolean isFullyInitialized() { return fullyInitialized; }

    @Override
    String getHttpMethod() { return "POST"; }

    @Override
    String getApiQueryString() { return baseUrl + apiResourceName; }

    @Override
    List<Pair<String, String>> getExtraHeaders() { return headers; }

    @Override
    String getApiBodyString() throws JSONException {
        JSONObject bodyJS = new JSONObject();
        if(request.lastSyncSet) bodyJS.put("lastsync", request.lastSync);
        if(request.nick != null) {
            JSONObject profileJS = new JSONObject().put("nick", request.nick);
            bodyJS.put("profile",profileJS);
        }

        JSONArray adds = null, dels = null;
        if(request.canSeeMeAdds.size() > 0) {
            adds = new JSONArray();
            for(Long id : request.canSeeMeAdds) {
                JSONObject entry = new JSONObject().put("id", id);
                adds.put(entry);
            }
        }
        if(request.canSeeMeDels.size() > 0) {
            dels = new JSONArray();
            for(Long id : request.canSeeMeDels) {
                JSONObject entry = new JSONObject().put("id", id);
                dels.put(entry);
            }
        }
        if((adds != null) || (dels != null)) {
            JSONObject contactsJS = new JSONObject();
            if(adds != null) contactsJS.put("add", adds);
            if(dels != null) contactsJS.put("delete", dels);
            bodyJS.put("contacts", contactsJS);
        }

        if(request.blips.size() > 0) {
            JSONArray blipsJS = new JSONArray();
            for(Blip blip : request.blips) {
                JSONObject newEntry = new JSONObject().put("lat",blip.getLatitude()).put("lon", blip.getLongitude()).put("utc_s", blip.getUtc_s());
                blipsJS.put(newEntry);
            }
            bodyJS.put("blips", blipsJS);
        }

        if(request.pingSet && request.pingGet) {
            JSONObject pingJS = new JSONObject();
            if(request.pingGet) pingJS.put("get", request.pingGet);
            if(request.pingSet) pingJS.put("set", request.pingSet);
            bodyJS.put("ping", pingJS);
        }
        return bodyJS.toString();
    }

    @Override
    void parseContent(String content) throws JSONException {
        JSONObject job = new JSONObject(content);
        if(job.has("auth")) response.authSuccess = job.getBoolean("auth");
        if(job.has("success")) response.callSuccess = job.getBoolean("success");
        if(job.has("message")) response.callMessage = job.getString("message");
        if(job.has("sync_time")) response.newLastSync = job.getDouble("sync_time");
        // new blips?
        if(job.has("blips")) {
            JSONArray blipsJS = job.getJSONArray("blips");
            for(int i=0; i < blipsJS.length(); i++) {
                JSONObject blipJS = blipsJS.getJSONObject(i);
                Log.i(TAG, "blipJS = " + blipJS.toString());
                Blip blip = new Blip(blipJS.getDouble("lat"), blipJS.getDouble("lon"), blipJS.getDouble("utc_s"));
                long id = blipJS.getLong("id");
                response.newBlips.add(new Pair<Blip, Long>(blip, id));
            }
        }
        // new nickname?
        if(job.has("profile")) {
            JSONObject profileJS = job.getJSONObject("profile");
            if(profileJS.has("nick")) response.newNickname = profileJS.getString("nick");
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
                        response.iCanSeeAdds.add(id);
                    }
                }
                if(icansee.has("delete")) {
                    JSONArray delsJS = icansee.getJSONArray("delete");
                    for(int i=0; i < delsJS.length(); i++) {
                        JSONObject delJS = delsJS.getJSONObject(i);
                        long id = delJS.getLong("id");
                        response.iCanSeeDels.add(id);
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
                        response.canSeeMeAdds.add(id);
                    }
                }
                if(canseeme.has("delete")) {
                    JSONArray delsJS = canseeme.getJSONArray("delete");
                    for(int i=0; i < delsJS.length(); i++) {
                        JSONObject delJS = delsJS.getJSONObject(i);
                        long id = delJS.getLong("id");
                        response.canSeeMeDels.add(id);
                    }
                }
            }
            if(contactJS.has("profiles")) {
                JSONArray contactProfilesJS = contactJS.getJSONArray("profiles");
                for(int i=0; i < contactProfilesJS.length(); i++) {
                    JSONObject profileJS = contactProfilesJS.getJSONObject(i);
                    long id = profileJS.getLong("id");
                    String nick = profileJS.getString("nick");
                    response.newProfiles.add(new Pair<Profile, Long>(new Profile(nick),id));
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
                response.newPings.add(new Pair<Long, String>(id, nick));
            }
        }
    }

    public double getNewLastSync() { return response.newLastSync; }
    public String getCallMessage() { return response.callMessage; }
    public List<Pair<Blip, Long>> getNewBlips() { return response.newBlips; }
    public String getNewNickname() { return response.newNickname; }
    public List<Long> getNewIcanSeeAdds() { return response.iCanSeeAdds; }
    public List<Long> getNewIcanSeeDels() { return response.iCanSeeDels; }
    public List<Long> getNewCanSeeMeAdds() { return response.canSeeMeAdds; }
    public List<Long> getNewCanSeeMeDels() { return response.canSeeMeDels; }
    public List<Pair<Profile, Long>> getNewProfiles() {return response.newProfiles; }
    public List<Pair<Long, String>> getNewPings() { return response.newPings; }
    public boolean isAuthSuccess() { return response.authSuccess; }
    public boolean isCallSuccess() { return response.callSuccess; }
}