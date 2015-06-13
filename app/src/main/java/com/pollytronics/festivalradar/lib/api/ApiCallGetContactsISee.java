package com.pollytronics.festivalradar.lib.api;

/**
 * Created by pollywog on 6/3/15.
 */
public class ApiCallGetContactsISee extends ApiCallGetContactsAbstract {
    @SuppressWarnings("unused")
    private final String TAG = "ApiCallGetContactsISee";

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId+"&mode=isee";
    }
}
