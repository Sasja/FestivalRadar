package com.pollytronics.festivalradar.lib;

import android.location.Location;

import java.util.Random;

/**
 * Created by pollywog on 9/22/14.
 * extension of the Location class for use within radar app
 */

public class RadarBlip extends Location{

    @SuppressWarnings("FieldCanBeLocal")
    private static final String PROVIDER = "RadarBlip";
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

    public boolean after(RadarBlip blip) {
        return (this.getTime() > blip.getTime());
    }

    /**
     * moves the location randomly over more or less n degrees
     * for testing only, it adapts the object and returns itself
     * @param degrees amount of degrees to move
     * @return itself
     */
    public RadarBlip brownian(double degrees){
        Random r = new Random();
        setLatitude(getLatitude()+(r.nextDouble()-.5)*degrees);
        setLongitude(getLongitude()+(r.nextDouble()-.5)*degrees);
        return this;
    }

    /**
     * set time to current system clock and return self
     * @return itself
     */
    public RadarBlip reClock(){
        setTime(System.currentTimeMillis());
        return this;
    }
}
