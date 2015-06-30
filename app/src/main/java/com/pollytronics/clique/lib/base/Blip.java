package com.pollytronics.clique.lib.base;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Extension of the Location class for use within Clique
 *
 * It contains an optional ownerId attribute that is used for identifying owners of blips provided by the api.
 * If the owner is set to 0 the owner is not known.
 */

public class Blip extends Location{

    private static final String TAG = "Blip";

    @SuppressWarnings("FieldCanBeLocal")
    private static final String PROVIDER = "Blip";

    private long ownerId = 0;

    /**
     * constructor made to work according to the api/v1 JSON specs
     * if the JSON contains a "userid" field, ownerId will be set accordingly
     * @param blipJSON
     * @throws JSONException
     */
    public Blip(JSONObject blipJSON) throws JSONException{
        super(PROVIDER);
        setLatitude(blipJSON.getDouble("lat"));
        setLongitude(blipJSON.getDouble("lon"));
        setTime((long)(blipJSON.getDouble("utc_s")*1000));    // Location class uses milliseconds, our class seconds
        try {
            this.ownerId = blipJSON.getLong("userid");
        } catch (JSONException e) {
            this.ownerId = 0;
        }
    }

    /**
     * Constructor that does not set the optional ownerId attribute
     * @param lat Latitude in degrees
     * @param lon Longitude in degrees
     * @param utc_s UTC in seconds since 1970
     */
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
        return String.format("%.6f lat %.6f lon", getLatitude(), getLongitude());
    }

    /**
     * @return time of blip expresses as seconds since 1970
     */
    public double getUtc_s() {
        return ((double)getTime() / 1000.0);
    }

    /**
     * compares the blip time value to the current clock on the device
     * @return age of the blip in seconds
     */
    public double getAge_s() {
        return (double)(System.currentTimeMillis()-getTime()) / 1000.0;
    }

    /**
     * returns the optional ownerId attribute
     * @return
     */
    public long getOwnerId() {
        return ownerId;
    }
}
