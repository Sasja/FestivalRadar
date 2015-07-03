package com.pollytronics.clique.lib.gui_elements;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Pair;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;

/**
 * Helper class for RadarView to do all the canvas painting
 * TODO: tweak the color coding of age and consider involving zoomRadius in the calculation (10 minutes ago at 50km is much more relevant than at 1km)
 */
public class RadarView_Painter {
    public static final String TAG = "RadarView_Painter";

    static final double EARTH_RADIUS = 6371000.0;
    private final Paint paint = new Paint();
    private int width;
    private int height;
    private  Canvas canvas = null;
    private double zoomRadius = 1000.0;
    private double bearing = 0.0;
    private Blip centerLocation = null;

    public RadarView_Painter() {
        this.paint.setAntiAlias(true);
    }

    /**
     * Helper method to produce a user readable String for a distance expressed in meters
     * It uses m or km and only one decimal place max for km.
     * @param meters a distance in meters
     * @return a presentable string describing the distance
     */
    static private String metersToScaleString(int meters) {
        if (meters >= 1000) {
            int km = meters / 1000;
            double frac = meters / 1000.0 - km;
            if (frac > 0) return String.valueOf(km + frac) + "km";
            else return String.valueOf(km) + "km";
        } else {
            return String.valueOf(meters) + "m";
        }
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        this.width = canvas.getWidth();
        this.height = canvas.getHeight();
    }

    public void setZoomRadius(double zoomRadius) { this.zoomRadius = zoomRadius; }
    public void setBearing(double bearing) { this.bearing = bearing; }
    public void setCenterLocation(Blip centerLocation) { this.centerLocation = centerLocation; }

    private double cos(double degrees) { return Math.cos(Math.toRadians(degrees)); }
    private double sin(double degrees) { return Math.sin(Math.toRadians(degrees)); }

    /**
     * Helper method to calculate screen coordinates of blips
     * TODO: the new Pair<> seems to be allocation that is performed in the onDraw method...
     * @param blip blip to be displayed
     * @return x and y screen coordinates to draw the blip to (in same units as screenWidth and screenHeight eg. pixels)
     */
    private Pair<Float, Float> calcScreenXY(Blip blip) {
        double dLat = blip.getLatitude() - centerLocation.getLatitude();
        double dLon = blip.getLongitude() - centerLocation.getLongitude();
        double dLatMeters = Math.toRadians(dLat) * EARTH_RADIUS; // good enough as dLat << EARTH_RADIUS
        double dLonMeters = Math.toRadians(dLon) * EARTH_RADIUS * cos(centerLocation.getLatitude());
        double dXPixels = (width / 2.0 / zoomRadius * dLonMeters);
        double dYPixels = (width / 2.0 / zoomRadius * dLatMeters);
        return new Pair<>(
                (float) (width / 2 + cos(bearing) * dXPixels - sin(bearing) * dYPixels),
                (float) (height / 2 - sin(bearing) * dXPixels - cos(bearing) * dYPixels)
        );
    }

    /**
     * Helper method to calculate where to draw the sun on the screen
     * TODO: the new Pair<> seems to be allocation that is performed in the onDraw method...
     * @return x and y screen coordinate to draw the sun at
     */
    private Pair<Float, Float> calcSunXY(double sunAzimuth) {
        return new Pair<>(
                (float) (width / 2 - sin(bearing - sunAzimuth) * width / 2.1),
                (float) (height / 2 - cos(bearing - sunAzimuth) * width / 2.1)
        );
    }

    /**
     * Calculates a suitable color according to the age of the blip and the zoomRadius.
     * An old blip is way more relevant when zoomed out than when zoomed in
     * TODO: consider using distance from center instead of zoomRadius
     * @param age_s
     * @return
     */
    private int age2blipColor(double age_s) {
        final double WANDERSPEED = 1.0; // speed in m/s that a blip is assumed to move at
        final double COLOR_DECAY = 7.0; // 1.0 corresponds to 1/e after wandering one display radius
        final double OPACITY_DECAY = 2.0;
        final double MIN_OPACITY = 55.0;
        double zoomRadiussesWandered = WANDERSPEED * age_s / zoomRadius;
        double b = (255.0 * 2.0 / 3.0) * Math.exp(-zoomRadiussesWandered * COLOR_DECAY) + (255.0 / 3.0);
        double rg = (255.0 - b) / 2.0;
        double alpha = (255.0 - MIN_OPACITY) * Math.exp(-zoomRadiussesWandered * OPACITY_DECAY) + MIN_OPACITY;
        return Color.argb((int) alpha, (int) rg, (int) rg, (int) b);
    }

    /**
     * Draws crosshairs over the screen.
     * Make sure bearing and canvas is set appropriately.
     */
    public void crosshairs() {
        canvas.rotate(-(float) bearing, width / 2, height / 2); // make sure to restore!

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.argb(100, 200, 100, 100));

        canvas.drawLine(width / 2, -width / 2, width / 2, height / 2, paint);

        paint.setStrokeWidth(3);
        paint.setColor(Color.argb(100, 150, 150, 150));
        canvas.drawLine(width / 2, height / 2, width / 2, height - 1 + width / 2, paint);
        canvas.drawLine(-height / 2, height / 2, width - 1 + height / 2, height / 2, paint);

        canvas.restore();   // restoring the rotate operation
    }

    /**
     * Draws the scaleCircles on the screen.
     * It will calculate an appropriate step size before doing so using the CircleStepper in order to remember the result
     * Make sure zoomRadius and canvas is set.
     */
    public void scaleCircles() {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(width / 25);
        paint.setColor(Color.argb(100, 150, 150, 150));

        int circleStepMeter = CircleStepper.getCircleStep(zoomRadius);

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

    /**
     * Draws a blip to the RadarView Canvas with a color according to its age and the name of the corresponding Contact
     * @param blip Blip to draw
     * @param contact Contact used for its name
     */
    public void blip(Blip blip, Contact contact) {
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(width / 25);

//        double ageFactorColor = Math.exp(-blip.getAge_s() / 60.0);  // it takes about 1 min to loose color
//        double ageFactorOpacity = Math.exp(-blip.getAge_s() / 300.0);  // it takes about 5 minutes to loose opacity
//
//        int rg = (int) ((1-ageFactorColor) * 120);
//        int b = (int) (255 * ageFactorColor + (1 - ageFactorColor) * 120);
//        int alpha = (int) (200 * ageFactorOpacity + 55);
//
//        paint.setColor(Color.argb(alpha, rg, rg, b));
        paint.setColor(age2blipColor(blip.getAge_s()));

        Pair<Float, Float> xy = calcScreenXY(blip);
        canvas.drawCircle(xy.first, xy.second, width / 100, paint);
        canvas.drawText(contact.getName(), xy.first, xy.second + width / 25, paint);
    }

    /**
     * Draws the sun to the RadarView Canvas, the sunElevation will dictate its color or look
     * @param sunAzimuth azimuth of the sun in degrees
     * @param sunElevation elevation above horizon in degrees
     */
    public void sun(double sunAzimuth, double sunElevation) {
        paint.setStyle(Paint.Style.FILL);
        if(sunElevation < -3) {
            paint.setColor(Color.argb(20, 0, 0, 0));
        } else {
            int green = (int)(Math.max(0,(Math.min(sunElevation, 20) * 10)));   //200 max and declining to 0 from 20° above horizon
            paint.setColor(Color.argb(150, 200, green, green / 10));
        }
        Pair<Float, Float> sunXy = calcSunXY(sunAzimuth);
        canvas.drawCircle(sunXy.first, sunXy.second, width / 20, paint);
    }

    /**
     * Optimization class
     * This class provides a method to calculate the scale Circle step size from the zoomlevel
     * If the zoomlevel does not change it will just return the last value.
     */
    private static class CircleStepper {
        private static double lastZoomRadius = 0;
        private static int lastCircleStepMeter = 0;
        public static int getCircleStep(double zoomRadius) {
            if(zoomRadius == lastZoomRadius) return lastCircleStepMeter;
            else {
                int circleStepMeter = (int) (zoomRadius / 2.5);    // the quotient determines how many circles are drawn
                int nulls = (int) Math.floor(Math.log10(circleStepMeter));
                double expo = Math.log10(circleStepMeter) - nulls;
                if (expo < 0.15) {                                 // this will snap circleStepMeter to a sensible round value
                    circleStepMeter = (int) Math.pow(10, nulls);
                } else if (expo < 0.5) {
                    circleStepMeter = 2 * (int) Math.pow(10, nulls);
                } else if (expo < 0.85) {
                    circleStepMeter = 5 * (int) Math.pow(10, nulls);
                } else {
                    circleStepMeter = (int) Math.pow(10, nulls + 1);
                }
                lastZoomRadius = zoomRadius;
                lastCircleStepMeter = circleStepMeter;
                return circleStepMeter;
            }
        }
    }
}
