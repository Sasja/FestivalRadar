package com.pollytronics.clique.lib.gui_elements;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;

import java.util.HashMap;
import java.util.Map;

/**
 * This View subclass provides visualisation for blips/contacts.
 * It needs to be fed all the data through setters and then invalidated to update the visuals
 *
 * HINT: hardware accelleration is now enabled in manifest and disabled for this view due to a lack of compatibility. This is ok as long as performance is good enough.
 * TODO: (bug) compass blocked on jill's motoG: investigate
 * TODO: (optimize) try not to allocate anything in onDraw
 * TODO: (feature) allow pinching as well as single finger swiping
 */
public class RadarView extends View {
    static final String TAG = "RadarView";

    private static final double MIN_ZOOM_RADIUS = 10.0;                 // 10m
    private static final double MAX_ZOOM_RADIUS = 1000.0 * 1000.0;       // 1000km

    @SuppressLint("UseSparseArrays")
    private final Map<Long, Contact> contacts = new HashMap<>();
    private final Map<Long, Blip> lastBlips = new HashMap<>();
    private final RadarView_Painter painter = new RadarView_Painter();
    private Blip centerLocation;
    private double bearing = 0.0;
    private double sunAzimuth = 0.0;
    private double sunElevation = -90.0;
    private boolean sunIconEnabled = false;
    private double zoomRadius = 1000.0; // should be set through a setter on onResume of the activity
    // private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mScrollGestureDetector;

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
     * must be called from the constructors to disable hardware accereleration for this View (because of lack of compatibility with/without)
     * it also initializes the ScaleGestureDetector
     * @param context context
     */
    private void init(Context context){
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        mScaleGestureDetector = new ScaleGestureDetector(context, new MyScaleListener());
        mScrollGestureDetector = new GestureDetector(context, new MyScrollListener());
    }

    public void setCenterLocation(Blip centerLocation) { this.centerLocation = centerLocation; }
    public void setBearing(double bearing) { this.bearing = bearing; }
    public void setSunAzimuth(double sunAzimuth) {this.sunAzimuth = sunAzimuth; }
    public void setSunElevation(double sunElevation) {this.sunElevation = sunElevation; }
    public void setSunEnabled(boolean sunEnabled) { this.sunIconEnabled = sunEnabled; }
    public double getZoomRadius() { return zoomRadius; }
    public void setZoomRadius(double zoomRadius) { this.zoomRadius = zoomRadius; }



    /**
     * Draws all the elements of the radarview using the RadarView_Painter instance.
     * Make sure all necessary attributes are set before calling the drawing methods or you'll get nullPointerExceptions
     * @param canvas the canvas to draw on
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        painter.setCanvas(canvas);
        painter.setCenterLocation(centerLocation);
        painter.setZoomRadius(zoomRadius);
        painter.setBearing(bearing);

        painter.crosshairs();
        painter.scaleCircles();
        if(centerLocation != null) for(Contact c:contacts.values()){    // TODO: this will prevent null pointer exceptions, but see if it can be done more nicely
            Blip b = lastBlips.get(c.getGlobalId());
            if(b != null) {
                painter.blip(b, c);
            }
        }
        if(sunIconEnabled) painter.sun(sunAzimuth, sunElevation);
    }

    /**
     * Add or update a contact and its last Blip on the RadarView.
     * @param contact Contact instance of the owner of the Blip
     * @param lastBlip the Blip to display on the View
     */
    public void updateContact(Contact contact, Blip lastBlip) {
        contacts.put(contact.getGlobalId(), contact);
        lastBlips.put(contact.getGlobalId(), lastBlip);
    }

    /**
     * clear all contacts and blips remembered by the RadarView
     */
    public void removeAllContacts() { contacts.clear(); lastBlips.clear(); }

    /**
     * Intercepts all Motion Events on the view and passes them on to the ScaleGestureDetector or ScrollGestureDetector
     * @param ev event that is received
     * @return need to return true in order to receive more events related to the current event
     */
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        //mScaleGestureDetector.onTouchEvent(ev);
        mScrollGestureDetector.onTouchEvent(ev);
        return true; // give me all events
    }

//    private class MyScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
//        @Override
//        public boolean onScale(ScaleGestureDetector detector) {
//            // Log.i(TAG, String.format("scaling, factor = %f", detector.getScaleFactor()));
//            zoomRadius /= Math.pow(detector.getScaleFactor(), 2);    // increasing the exponent will make the zoom more sensitive.
//            zoomRadius = Math.max(MIN_ZOOM_RADIUS, Math.min(MAX_ZOOM_RADIUS, zoomRadius));
//            invalidate();
//            return true;
//        }
//    }

    private class MyScrollListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Log.i(TAG, String.format("scrolling, dY = %f", distanceY));
            zoomRadius /= Math.pow(0.995, distanceY);
            zoomRadius = Math.max(MIN_ZOOM_RADIUS, Math.min(MAX_ZOOM_RADIUS, zoomRadius));
            invalidate();
            return true;
        }
    }
}
