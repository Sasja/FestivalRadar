package com.pollytronics.clique.lib.api_v02;

import com.pollytronics.clique.lib.CliqueApiCall;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Create a new profile, setting the login, storing salts and keys
 *
 * Created by pollywog on 9/3/15.
 */
public class ApiCallPostAccounts extends CliqueApiCall {
    private final String TAG = "ApiCallPostAccounts";

    private final String apiResourceName = "api/v2/accounts";
    private boolean fullyInitialized = false;
    private String body;

    private boolean callSuccess = false;
    private String callMessage = null;
    private int accountId;

    public ApiCallPostAccounts(String username, String cs, String key, String cs2, String key2) throws JSONException {
        this.body = new JSONObject()
                .put("username",username)
                .put("cs",cs).put("key", key)
                .put("cs2",cs2).put("key2",key2)
                .toString();
        this.fullyInitialized = true;
    }

    @Override
    protected boolean isFullyInitialized() {
        return fullyInitialized;
    }

    @Override
    protected String getHttpMethod() { return "POST"; }

    @Override
    protected String getApiQueryString() { return baseUrl + apiResourceName; }

    @Override
    protected String getApiBodyString() {
        return body;
    }

    @Override
    protected void parseContent(String content) throws JSONException {
        JSONObject job = new JSONObject(content);
        this.callSuccess = job.getBoolean("success");
        if(job.has("message")) this.callMessage = job.getString("message");
        if(job.has("id"))      this.accountId =   job.getInt("id");
    }

    public boolean getCallSuccess() { return callSuccess; }
    public String getCallMessage() {return callMessage; }
    public int getAccountId() {return accountId; }
}
