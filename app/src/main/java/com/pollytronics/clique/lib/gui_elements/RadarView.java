package com.pollytronics.clique.lib.gui_elements;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;

import java.util.HashMap;
import java.util.Map;

/**
 * This View subclass provides visualisation for blips/contacts.
 * It needs to be fed all the data through setters and then invalidated to update the visuals
 *
 * TODO: hardware accelleration is now enabled in manifest and disabled for this view due to a lack of compatibility. This is ok as long as performance is good enough.
 * TODO: make zoom value when app loads remembered in preferences or smth
 * TODO: compass blocked on jill's motoG: investigate
 * TODO: try not to allocate anything in onDraw
 */
public class RadarView extends View {

    @SuppressWarnings("unused")
    static final String TAG = "RadarView";

    @SuppressLint("UseSparseArrays")
    private final Map<Long, Contact> contacts = new HashMap<>();
    private final Map<Long, Blip> lastBlips = new HashMap<>();
    private final Paint paint = new Paint();
    private Blip centerLocation;
    private double bearing = 0.0;
    private double sunAzimuth = 0.0;
    private double sunElevation = -90.0;
    private boolean sunIconEnabled = false;
    private double zoomRadius = 1000;
    private ScaleGestureDetector mScaleGestureDetector;

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

    /**
     * must be called from the constructors, to disable hardware accereleration for this View (because of lack of compatibility with/without)
     * it also initializes the ScaleGestureDetector
     * @param context
     */
    private void init(Context context){
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    /**
     * intercepts all Motion Events on the view and passes them on to the ScaleGestureDetector
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mScaleGestureDetector.onTouchEvent(ev);
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RadarView_Painter painter = new RadarView_Painter(canvas, centerLocation, zoomRadius, bearing);

        painter.crosshairs();
        painter.scaleCircles();
        for(Contact c:contacts.values()){
            Blip b = lastBlips.get(c.getGlobalId());
            if(b != null) {
                painter.blip(b, c);
            }
        }

        if(sunIconEnabled) painter.sun(sunAzimuth, sunElevation);
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

    public void setSunEnabled(boolean sunEnabled) {
        this.sunIconEnabled = sunEnabled;
    }

    public double getZoomRadius() { return zoomRadius; }

    public void setZoomRadius(double zoomRadius) { this.zoomRadius = zoomRadius; }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            zoomRadius /= Math.pow(detector.getScaleFactor(), 2);    // increasing the exponent will make the zoom more sensitive.
            zoomRadius = Math.max(10.0, Math.min(100000.0, zoomRadius));
            invalidate();
            return true;
        }
    }
}
