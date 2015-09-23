package com.pollytronics.clique.lib.api_v02;

import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pollywog on 9/3/15.
 */
public class ApiCallGetSalts extends CliqueApiCall {
    private final String TAG = "ApiCallGetSalts";

    private final String apiResourceName = "api/v2/accounts/salts";
    private boolean fullyInitialized = false;

    private List<Pair<String,String>> headers = new ArrayList<Pair<String, String>>();

    private boolean callSuccess = false;
    private String callMessage = null;

    private String cs = null;
    private String cs2 = null;

    public ApiCallGetSalts(String username) {
        this.headers.add(new Pair<String, String>("username",username));
        this.fullyInitialized = true;
    }

    @Override
    protected boolean isFullyInitialized() { return fullyInitialized; }

    @Override
    protected String getHttpMethod() { return "GET"; }

    @Override
    protected String getApiQueryString() { return baseUrl + apiResourceName; }

    @Override
    protected List<Pair<String, String>> getExtraHeaders() { return headers; }

    @Override
    protected void parseContent(String content) throws JSONException {
        JSONObject job = new JSONObject(content);
        this.callSuccess = job.getBoolean("success");
        if(job.has("message")) this.callMessage = job.getString("message");
        if(job.has("cs"))      this.cs =          job.getString("cs");
        if(job.has("cs2"))     this.cs2 =         job.getString("cs2");
    }

    public boolean getCallSuccess() { return callSuccess; }
    public String getCallMessage() { return callMessage; }
    public String getCs() { return cs; }
    public String getCs2() { return cs2; }
}
