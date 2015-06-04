package com.pollytronics.festivalradar.lib;

import android.location.Location;

/**
 * Created by pollywog on 9/22/14.
 * extension of the Location class for use within radar app
 */

public class RadarBlip extends Location{

    @SuppressWarnings("FieldCanBeLocal")
    private static final String PROVIDER = "RadarBlip";
    @SuppressWarnings("unused")
    private static String TAG = "RadarBlip";

    public RadarBlip(){
        super(PROVIDER);
        final double LAT = 51.072478;   //initialise there to facilitate testing, shouldn't matter
        final double LON = 3.709913;
        setLatitude(LAT);
        setLongitude(LON);
        setTime(0);
    }

    public RadarBlip(Location location) {
        super(location);
    }

    public RadarBlip(RadarBlip blip) {
        super(blip);
    }

    @Override
    public String toString() {
        if(getTime()!=0) {
            return String.format("%.6f lat %.6f lon", getLatitude(), getLongitude());
        } else {
            return "no data";
        }
    }
}
