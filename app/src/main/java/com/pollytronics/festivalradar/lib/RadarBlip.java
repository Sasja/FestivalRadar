package com.pollytronics.festivalradar.lib;

import android.location.Location;

import java.util.Random;

/**
 * Created by pollywog on 9/22/14.
 * yup
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

    public RadarBlip(double latitude, double longitude) {
        super(PROVIDER);
        setLatitude(latitude);
        setLongitude(longitude);
    }

    @Override
    public String toString() {
        if(getTime()!=0) {
            return String.format("%.3f lat %.3f lon", getLatitude(), getLongitude());
        } else {
            return "no data";
        }
    }

    public boolean after(RadarBlip blip) {
        return (this.getTime() > blip.getTime());
    }

    public RadarBlip brownian(double degrees){
        Random r = new Random();
        setLatitude(getLatitude()+(r.nextDouble()-.5)*degrees);
        setLongitude(getLongitude()+(r.nextDouble()-.5)*degrees);
        return this;
    }

    public RadarBlip reClock(){
        setTime(System.currentTimeMillis());
        return this;
    }
}
