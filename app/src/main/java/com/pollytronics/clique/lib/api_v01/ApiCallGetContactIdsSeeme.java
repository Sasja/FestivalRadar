package com.pollytronics.clique.lib.api_v01;

/**
 * provides a list of contact-ids that are allowed to see me
 */
@Deprecated
public class ApiCallGetContactIdsSeeme extends ApiCallGetContactIdsAbstract {
    @SuppressWarnings("unused")
    private final String TAG = "ApiCallGetContactIdsSeeme";

    public ApiCallGetContactIdsSeeme(long selfId) {
        super(selfId);
    }

    @Override
    protected String getApiQueryString() {
        return baseUrl+apiResourceName+"?userid="+selfId+"&mode=seeme";
    }
}
