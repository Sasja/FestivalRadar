package com.pollytronics.clique.lib.api_v02;

import android.util.Pair;

import com.pollytronics.clique.lib.CliqueApiCall;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pollywog on 9/3/15.
 */
public class ApiCallGetValidatekey extends CliqueApiCall {
    private final String TAG = "ApiCallGetValidatekey";

    private final String apiResourceName = "api/v2/accounts/validatekey";
    private boolean fullyInitialized = false;
    private List<Pair<String,String>> headers = new ArrayList<Pair<String,String>>();
    private boolean callSuccess;
    private String callMessage;
    private int accountId;

    public ApiCallGetValidatekey(String username, String key) {
        this.headers.add(new Pair<String, String>("username",username));
        this.headers.add(new Pair<String, String>("key", key));
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
        if(job.has("id"))      this.accountId =   job.getInt("id");
    }

    public boolean isCallSuccess() { return callSuccess; }
    public String getCallMessage() { return callMessage; }
    public int getAccountId() { return accountId; }
}
