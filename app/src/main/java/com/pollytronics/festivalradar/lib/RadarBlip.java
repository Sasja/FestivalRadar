package com.pollytronics.festivalradar.lib;

import android.location.Location;

import java.util.Random;

/**
 * Created by pollywog on 9/22/14.
 * extension of the Location class for use within radar app
 */
public class RadarBlip extends Location{

    private static String TAG = "RadarBlip";

    private static String PROVIDER = "RadarBlip";

    public RadarBlip(){
        super(PROVIDER);
        setLatitude(0);
        setLongitude(0);
        setTime(0);
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
