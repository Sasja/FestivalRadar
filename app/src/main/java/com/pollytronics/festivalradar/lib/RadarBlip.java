package com.pollytronics.festivalradar.lib;

import android.os.SystemClock;
import android.provider.Settings;

import java.util.Random;

/**
 * Created by pollywog on 9/22/14.
 */
public class RadarBlip {
    private double x,y;
    private long t;

    public RadarBlip(){
        this.x = 0;
        this.y = 0;
        this.t = 0;
    }

    public RadarBlip(RadarBlip blip) {
        this.x = blip.x;
        this.y = blip.y;
        this.t = blip.t;
    }

    public RadarBlip(double x, double y) {
        this.x = x;
        this.y = y;
        this.t = System.currentTimeMillis();
    }

    public String toString() {
        if(t!=0) {
            return String.format("x=%.2f y=%.2f", x, y);
        } else {
            return "no data";
        }
    }

    public boolean after(RadarBlip blip) {
        return (this.t > blip.t);
    }

    public RadarBlip brownian(double distance){
        Random r = new Random();
        this.x += (r.nextDouble()-.5)*distance;
        this.y += (r.nextDouble()-.5)*distance;
        return this;
    }

    public RadarBlip reClock(){
        this.t = System.currentTimeMillis();
        return this;
    }
}
