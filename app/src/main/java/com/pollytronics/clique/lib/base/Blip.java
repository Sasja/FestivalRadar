package com.pollytronics.clique.lib.base;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pollywog on 9/22/14.
 * extension of the Location class for use within clique
 */

public class Blip extends Location{

    private static final String TAG = "Blip";

    @SuppressWarnings("FieldCanBeLocal")
    private static final String PROVIDER = "Blip";

    /**
     * contstructor made to work according to the api/v1 JSON specs
     * @param blipJSON
     * @throws JSONException
     */
    public Blip(JSONObject blipJSON) throws JSONException{
        super(PROVIDER);
        setLatitude(blipJSON.getDouble("lat"));
        setLongitude(blipJSON.getDouble("lon"));
        setTime((long)(blipJSON.getDouble("utc_s")*1000));    // Location class uses milliseconds
    }

    public Blip(double lat, double lon, double utc_s) {
        super(PROVIDER);
        setLatitude(lat);
        setLongitude(lon);
        setTime((long)(utc_s * 1000));      // Location class uses milliseconds
    }

    public Blip(Location location) {
        super(location);
    }

    @Override
    public String toString() {
        if(getTime()!=0) {
            return String.format("%.6f lat %.6f lon", getLatitude(), getLongitude());
        } else {
            return "no data";
        }
    }

    public double getUtc_s() {
        return ((double)getTime() / 1000.0);
    }

    public double getAge_s() {
        return (double)(System.currentTimeMillis()-getTime()) / 1000.0;
    }
}
