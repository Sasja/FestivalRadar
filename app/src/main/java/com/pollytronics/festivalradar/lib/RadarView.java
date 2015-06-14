package com.pollytronics.festivalradar.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
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
    @SuppressLint("UseSparseArrays")
    private final Map<Long, RadarContact> contacts = new HashMap<Long, RadarContact>();
    private final Paint paint = new Paint();
    private RadarBlip centerLocation;
    private double bearing=0;

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

        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(150, 0, 0, 250));
        for(RadarContact c:contacts.values()) {
            double dLat = c.getLastBlip().getLongitude() - centerLocation.getLongitude();
            double dLon = c.getLastBlip().getLatitude() - centerLocation.getLatitude();
            float x = (float) (width/2 + dLat/0.00001);     // TODO: this is laughable :)
            float y = (float) (height/2 - dLon/0.00001);
            canvas.drawCircle(x, y, 6, paint);
        }

        canvas.restore();   // calculate own rotation from now on

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(width / 25);
        for(RadarContact c:contacts.values()) {
            double dLat = c.getLastBlip().getLongitude() - centerLocation.getLongitude();
            double dLon = c.getLastBlip().getLatitude() - centerLocation.getLatitude();
            float xt = (float) (dLat/0.00001);
            float yt = (float) (dLon/0.00001);
            float bearingRads = (float) (bearing * 3.1415 / 180.0);
            float x = (float) (width/2 + Math.cos(bearingRads) * xt - Math.sin(bearingRads) * yt);
            float y = (float) (height/2 - Math.sin(bearingRads) * xt - Math.cos(bearingRads) * yt + width/25);
            canvas.drawText(c.getName(), x, y, paint);
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
}
