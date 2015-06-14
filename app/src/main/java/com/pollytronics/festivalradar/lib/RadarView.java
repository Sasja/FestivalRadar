package com.pollytronics.festivalradar.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pollywog on 10/2/14.
 * TODO: hardware accelleration is now enabled in manifest and disabled for this view due to a lack of compatibility with/without. This if fine as long performance is good enough.
 * TODO: make it prettier
 */
public class RadarView extends View {

    @SuppressWarnings("unused")
    static final String TAG = "RadarView";
    static final double earthRadius = 6371000.0;
    @SuppressLint("UseSparseArrays")
    private final Map<Long, RadarContact> contacts = new HashMap<Long, RadarContact>();
    private final Paint paint = new Paint();
    private RadarBlip centerLocation;
    private double bearing=0;
    private double zoomLevel = 1000.0;     // means its 200m to the left or right edge of screen

    public RadarView(Context context) {
        super(context);
        init();
    }

    public RadarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RadarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private Pair<Float, Float> calcScreenXY(RadarBlip blip, RadarBlip centerLocation, double screenWidth, double screenHeight, double bearing) {
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

    @Override
    // TODO: make this way more elegant and efficient and better in every imaginable way
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
        paint.setColor(Color.rgb(200,100,100));
        canvas.drawLine(width/2, -width/2, width/2, height/2,paint);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(200,200,200));
        canvas.drawLine(width/2, height/2, width/2, height-1+width/2,paint);
        canvas.drawLine(-height/2, height/2, width-1+height/2, height/2, paint);
        int radiusStep = width/8;
        for(int r = radiusStep; r < Math.sqrt(width*width+height*height)/2; r += radiusStep) {
            canvas.drawCircle(width/2, height/2, r, paint);
        }

        canvas.restore();   // calculate own rotation from now on

        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(150, 0, 0, 250));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(width / 25);
        for(RadarContact c:contacts.values()) {
            Pair<Float, Float> xy = calcScreenXY(c.getLastBlip(), centerLocation, width, height, bearing);
            canvas.drawCircle(xy.first, xy.second, width / 100
                    , paint);
            canvas.drawText(c.getName(), xy.first, xy.second + width/25, paint);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(false);
        paint.setColor(Color.rgb(0, 0, 0));
        paint.setStrokeWidth(1);
        canvas.drawRect(0, 0, width-1, height-1, paint);
    }

    private void init(){
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);   // Disables hardware acceleration for this view as theres no full compatibility with/without
    }

    public void addContact(RadarContact contact) {
        if(contacts.containsKey(contact.getID())) {
            throw new IllegalArgumentException("contact to add is allready present in RadarView");
        } else {
            contacts.put(contact.getID(),contact);
        }
    }

    @SuppressWarnings("unused")
    public void updateContact(RadarContact contact) {
        if(contacts.containsKey(contact.getID())) {
            throw new IllegalArgumentException("contact to update not present in RadarView!");
        } else {
            contacts.put(contact.getID(), contact);
        }

    }

    public void removeAllContacts() {
        contacts.clear();
    }

    public void setCenterLocation(RadarBlip centerLocation) {
        this.centerLocation = centerLocation;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public void setZoomLevel(double zoomLevel) { this.zoomLevel = zoomLevel; }
}
