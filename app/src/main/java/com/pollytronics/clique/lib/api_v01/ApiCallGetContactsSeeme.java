package com.pollytronics.clique.lib.api_v01;

/**
 * Created by pollywog on 6/3/15.
 */
public class ApiCallGetContactsSeeme extends ApiCallGetContactsAbstract {
    @SuppressWarnings("unused")
    private final String TAG = "ApiCallGetContactsSeeme";

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId+"&mode=seeme";
    }
}
