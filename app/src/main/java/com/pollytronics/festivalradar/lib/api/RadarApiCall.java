package com.pollytronics.festivalradar.lib.api;

import com.pollytronics.festivalradar.RadarDatabase_Interface4RadarService;

/**
 * Created by pollywog on 6/3/15.
 *
 * APICall object implement REST API Calls, separating the work in:
 * 1) collecting all the data needed for the call
 * 2) setting extra parameters manually
 * 3) constructing the query url/headers/body
 * 4) parsing a response into member fields
 * 5) using the results
 *
 * only 1 and 5 should access data outside the object itself as all others might be called on another thread
 * TODO: check the error handling of this thing
 */
abstract public class RadarApiCall {
    protected final String TAG = "RadarApiCall";
    protected final String baseUrl = "http://festivalradarservice.herokuapp.com/api/v1/";
    //protected final String baseUrl = "http://192.168.0.5:8080/api/v1/";
    private boolean failed = false;

    public void setFailedFlag() { failed = true; }
    public boolean hasFailed() { return failed; }
    public abstract void collectData(RadarDatabase_Interface4RadarService db);
    public abstract String getApiQueryString();
    public abstract void doTheWork(RadarDatabase_Interface4RadarService db);
}
