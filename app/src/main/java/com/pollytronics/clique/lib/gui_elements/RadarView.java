package com.pollytronics.clique.lib.gui_elements;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pollywog on 10/2/14.
 * TODO: hardware accelleration is now enabled in manifest and disabled for this view due to a lack
 * TODO: of compatibility with/without. This if fine as long performance is good enough.
 * TODO: make zoom value when app loads remembered in preferences or smth
 * TODO: make it prettier
 * TODO: compass blocked on jill's motoG: investigate, more logging
 */
public class RadarView extends View {

    @SuppressWarnings("unused")
    static final String TAG = "RadarView";
    static final double earthRadius = 6371000.0;
    @SuppressLint("UseSparseArrays")
    private final Map<Long, Contact> contacts = new HashMap<>();
    private final Map<Long, Blip> lastBlips = new HashMap<>();
    private final Paint paint = new Paint();
    private Blip centerLocation;
    private double bearing=0;
    private double sunAzimuth = 0;
    private double sunElevation = -90;
    private boolean sunIconEnabled = true;
    private double zoomLevel = 1000.0;     // means its that much meters to the left or right edge of screen
    private ScaleGestureDetector mScaleGestureDetector;

    // TODO: zoomlevel should not be initialised here but from some stored value in preferences or smth

    public RadarView(Context context) {
        super(context);
        init(context);
    }

    public RadarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RadarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
        // Disables hardware acceleration for this view as theres no full compatibility with/without
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mScaleGestureDetector.onTouchEvent(ev);
        return true;
    }

    private Pair<Float, Float> calcScreenXY(Blip blip,
                                            Blip centerLocation,
                                            double screenWidth,
                                            double screenHeight,
                                            double bearing) {
        double dLat = blip.getLatitude() - centerLocation.getLatitude();
        double dLon = blip.getLongitude() - centerLocation.getLongitude();
        double dLatMeters = dLat * 3.1415 / 180 * earthRadius;
        double dLonMeters = dLon * 3.1415 / 180 * earthRadius * Math.cos(centerLocation.getLatitude() * 3.1415 / 180);
        double dXPixels = (screenWidth / 2 / zoomLevel * dLonMeters);
        double dYPixels = (screenWidth / 2 / zoomLevel * dLatMeters);
        double bearingRad = (bearing * 3.1415 / 180.0);
        return new Pair<>(
                (float) (screenWidth/2 + Math.cos(bearingRad) * dXPixels - Math.sin(bearingRad) * dYPixels),
                (float) (screenHeight/2 - Math.sin(bearingRad) * dXPixels - Math.cos(bearingRad) * dYPixels)
        );
    }

    private Pair<Float, Float> calcSunXY(double width, double height, double bearing, double sunAzimuth) {
        return new Pair<>(
                (float) (width/2  - Math.sin((bearing-sunAzimuth)*3.1415/180.0) * width/2.1),
                (float) (height/2 - Math.cos((bearing-sunAzimuth)*3.1415/180.0) * width/2.1)
        );
    }

    private String intToScaleText(int meters) {
        if (meters >= 1000) {
            int km = meters/1000;
            double frac = meters/1000.0 - km;
            if(frac > 0) return String.valueOf(km + frac) + "km";
            else return String.valueOf(km) + "km";
        } else {
            return String.valueOf(meters) + "m";
        }
    }

    @Override
    // TODO: make this way more elegant and efficient and better in every imaginable way once you like the look and feel of it
    protected void onDraw(Canvas canvas) {
        //int width = canvas.getWidth();        //does not work on emulator
        //int height = canvas.getHeight();
        int width = MeasureSpec.getSize(getWidth());     // works, but how? and why not just getWidth?
        int height = MeasureSpec.getSize(getHeight());

        super.onDraw(canvas);

        canvas.rotate(-(float) bearing, (float) width / 2, (float) height / 2); //dont forget to restore!

        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);
        paint.setColor(Color.rgb(200, 100, 100));
        canvas.drawLine(width / 2, -width / 2, width / 2, height / 2, paint);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(200, 200, 200));
        canvas.drawLine(width / 2, height / 2, width / 2, height - 1 + width / 2, paint);
        canvas.drawLine(-height / 2, height / 2, width - 1 + height / 2, height / 2, paint);

        canvas.restore();   // calculate own rotation from now on

        int circleStepMeter = (int) (zoomLevel/2.5);    // the quotient will determine how many circles are drawn approx
        int nulls = (int) Math.floor(Math.log10(circleStepMeter));
        double expo = Math.log10(circleStepMeter)-nulls;
        if (expo < 0.15) {                              // this will snap to a sensible scale circle interval
            circleStepMeter = 1 * (int) Math.pow(10,nulls);
        } else if (expo < 0.5) {
            circleStepMeter = 2 * (int) Math.pow(10, nulls);
        } else if (expo < 0.85) {
            circleStepMeter = 5 * (int) Math.pow(10, nulls);
        } else {
            circleStepMeter = 1 * (int) Math.pow(10, nulls+1);
        }
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(width / 30);
        for (int i = 1; i*circleStepMeter < zoomLevel * 2; ++i) {
            float radius = (float) (i*circleStepMeter/zoomLevel*width/2);
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(width / 2, height / 2, radius, paint);
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(intToScaleText(circleStepMeter*i), width/2, height/2 + radius - width / 60, paint);
        }

        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(width / 25);
        for(Contact c:contacts.values()) {
            if(lastBlips.get(c.getGlobalId()) != null ) {
                Pair<Float, Float> xy = calcScreenXY(lastBlips.get(c.getGlobalId()), centerLocation, width, height, bearing);
                double ageFactor = Math.exp(-lastBlips.get(c.getGlobalId()).getAge_s() / 120.0);  // 120 sec oud is 1/e en exponentieel verval verder
                paint.setColor(Color.argb((int)(255 * ageFactor), 0, 0, 250));
                canvas.drawCircle(xy.first, xy.second, width / 100
                        , paint);
                canvas.drawText(c.getName(), xy.first, xy.second + width / 25, paint);
            }
        }

        if(sunIconEnabled) {
            if(sunElevation < -3) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(1);
            } else {
                paint.setStyle(Paint.Style.FILL);
                int green = (int)(Math.max(0,(Math.min(sunElevation, 20) * 10)));   //200 max and declining to 0 from 20° above horizon
                paint.setColor(Color.argb(150, 200, green, green/10));
            }
            Pair<Float, Float> sunXy = calcSunXY(width, height, bearing, sunAzimuth);
            canvas.drawCircle(sunXy.first, sunXy.second, width / 20, paint);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(false);
        paint.setColor(Color.rgb(0, 0, 0));
        paint.setStrokeWidth(1);
        canvas.drawRect(0, 0, width-1, height-1, paint);
    }



    public void addContact(Contact contact, Blip lastBlip) {
        if(contacts.containsKey(contact.getGlobalId())) {
            //throw new IllegalArgumentException("contact to add is allready present in RadarView");
            Log.i(TAG, "WARNING: contact ID is allready present in RaderView, duplicate ID's?");
        } else {
            contacts.put(contact.getGlobalId(),contact);
            lastBlips.put(contact.getGlobalId(), lastBlip);
        }
    }

    public void removeAllContacts() {
        contacts.clear();
    }

    public void setCenterLocation(Blip centerLocation) {
        this.centerLocation = centerLocation;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public void setSunAzimuth(double sunAzimuth) {this.sunAzimuth = sunAzimuth; }

    public void setSunElevation(double sunElevation) {this.sunElevation = sunElevation; }

    public void zoomPercent(double zoomPercent) {
        this.zoomLevel = Math.pow(10, 5-zoomPercent/25);  // 0 -> 100km and 100 -> 10m
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            zoomLevel /= Math.pow(detector.getScaleFactor(), 2);    // increasing the exponent will make the zoom more sensitive.
            zoomLevel = Math.max(10.0, Math.min(100000.0, zoomLevel));
            invalidate();
            return true;
        }
    }
}
