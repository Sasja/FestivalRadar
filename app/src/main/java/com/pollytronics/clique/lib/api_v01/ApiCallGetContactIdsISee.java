package com.pollytronics.clique.lib.api_v01;

/**
 * provides a list of global id's that i am allowed to see
 */
@Deprecated
public class ApiCallGetContactIdsISee extends ApiCallGetContactIdsAbstract {
    @SuppressWarnings("unused")
    private final String TAG = "ApiCallGetContactIdsISee";

    public ApiCallGetContactIdsISee(long selfId) {
        super(selfId);
    }

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId+"&mode=isee";
    }
}
