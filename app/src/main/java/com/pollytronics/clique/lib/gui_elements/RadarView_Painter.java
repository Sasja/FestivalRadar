package com.pollytronics.clique.lib.gui_elements;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Pair;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;

/**
 * Helper class for RadarView to do all the canvas painting
 */
public class RadarView_Painter {
    static final double earthRadius = 6371000.0;

    private final int width;
    private final int height;
    private final Canvas canvas;

    private final double zoomRadius;
    private final double bearing;
    private final Paint paint = new Paint();
    private final Blip centerLocation;

    public RadarView_Painter(Canvas canvas, Blip centerLocation, double zoomRadius, double bearing) {
        this.width = canvas.getWidth();
        this.height = canvas.getHeight();
        this.canvas = canvas;
        this.centerLocation = centerLocation;
        this.zoomRadius = zoomRadius;
        this.bearing = bearing;
        this.paint.setAntiAlias(true);
    }

    /**
     * Helper method to produce a user readable String for a distance expressed in meters
     * It uses m or km and only one decimal place max for km.
     * @param meters
     * @return a presentable string describing the distance
     */
    static private String metersToScaleString(int meters) {
        if (meters >= 1000) {
            int km = meters/1000;
            double frac = meters/1000.0 - km;
            if(frac > 0) return String.valueOf(km + frac) + "km";
            else return String.valueOf(km) + "km";
        } else {
            return String.valueOf(meters) + "m";
        }
    }

    private double cos(double degrees) { return Math.cos(Math.toRadians(degrees)); }

    private double sin(double degrees) { return Math.sin(Math.toRadians(degrees)); }

    /**
     * Helper method to calculate screen coordinates of blips
     * @param blip blip to be displayed
     * @return x and y screen coordinates to draw the blip to in same units as screenWidth and screenHeigt
     */
    private Pair<Float, Float> calcScreenXY(Blip blip) {
        double dLat = blip.getLatitude() - centerLocation.getLatitude();
        double dLon = blip.getLongitude() - centerLocation.getLongitude();
        double dLatMeters = Math.toRadians(dLat) * earthRadius; // good enough as lLat << earthRadius
        double dLonMeters = Math.toRadians(dLon) * earthRadius * cos(centerLocation.getLatitude());
        double dXPixels = (width / 2.0 / zoomRadius * dLonMeters);
        double dYPixels = (width / 2.0 / zoomRadius * dLatMeters);
        return new Pair<>(
                (float) (width / 2 + cos(bearing) * dXPixels - sin(bearing) * dYPixels),
                (float) (height / 2 - sin(bearing) * dXPixels - cos(bearing) * dYPixels)
        );
    }

    /**
     * Helper method to calculate where to draw the sun on the screen
     * @return x and y screen coordinate to draw the sun at
     */
    private Pair<Float, Float> calcSunXY(double sunAzimuth) {
        return new Pair<>(
                (float) (width/2  - sin(bearing-sunAzimuth) * width/2.1),
                (float) (height/2 - cos(bearing - sunAzimuth) * width/2.1)
        );
    }

    public void crosshairs() {
        canvas.rotate(-(float) bearing, width / 2, height / 2); // make sure to restore!

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.argb(150, 200, 100, 100));

        canvas.drawLine(width / 2, -width / 2, width / 2, height / 2, paint);

        paint.setStrokeWidth(3);
        paint.setColor(Color.argb(100, 150, 150, 150));
        canvas.drawLine(width / 2, height / 2, width / 2, height - 1 + width / 2, paint);
        canvas.drawLine(-height / 2, height / 2, width - 1 + height / 2, height / 2, paint);

        canvas.restore();   // restoring the rotate operation
    }

    public void scaleCircles() {
        // first calculate what scale circles step to use based on zoomRadius
        int circleStepMeter = (int) (zoomRadius / 2.5);    // the quotient will determine how many circles are drawn approx
        int nulls = (int) Math.floor(Math.log10(circleStepMeter));
        double expo = Math.log10(circleStepMeter) - nulls;
        if (expo < 0.15) {                              // this will snap to a sensible scale circle interval
            circleStepMeter = (int) Math.pow(10, nulls);
        } else if (expo < 0.5) {
            circleStepMeter = 2 * (int) Math.pow(10, nulls);
        } else if (expo < 0.85) {
            circleStepMeter = 5 * (int) Math.pow(10, nulls);
        } else {
            circleStepMeter = (int) Math.pow(10, nulls + 1);
        }

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(width / 25);
        paint.setColor(Color.argb(100, 150, 150, 150));

        for (int i = 1; i * circleStepMeter < zoomRadius * 2; ++i) {
            float radius = (float) (i * circleStepMeter / zoomRadius * width / 2);

            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(width / 2, height / 2, radius, paint);

            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(metersToScaleString(circleStepMeter * i), width / 2, height / 2 + radius - width / 60, paint);
        }
    }

    public void blip(Blip blip, Contact contact) {
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(width / 25);

        Pair<Float, Float> xy = calcScreenXY(blip);

        double ageFactorColor = Math.exp(-blip.getAge_s() / 60.0);  // it takes about 1 min to loose color
        double ageFactorOpacity = Math.exp(-blip.getAge_s() / 300.0);  // it takes about 5 minutes to loose opacity
        int rg = (int) ((1-ageFactorColor) * 120);
        int b = (int) (255 * ageFactorColor + (1-ageFactorColor)* 120);
        int alpha = (int) (200 * ageFactorOpacity + 55);
        paint.setColor(Color.argb(alpha, rg, rg, b));

        canvas.drawCircle(xy.first, xy.second, width / 100, paint);
        canvas.drawText(contact.getName(), xy.first, xy.second + width / 25, paint);
    }

    public void sun(double sunAzimuth, double sunElevation) {
        paint.setStyle(Paint.Style.FILL);
        if(sunElevation < -3) {
            paint.setColor(Color.argb(20, 0, 0, 0));
        } else {
            int green = (int)(Math.max(0,(Math.min(sunElevation, 20) * 10)));   //200 max and declining to 0 from 20° above horizon
            paint.setColor(Color.argb(150, 200, green, green/10));
        }
        Pair<Float, Float> sunXy = calcSunXY(sunAzimuth);
        canvas.drawCircle(sunXy.first, sunXy.second, width / 20, paint);
    }
}
