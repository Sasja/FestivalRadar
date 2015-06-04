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
 * TODO: figure out how canvas.getWidth() and such is supposed to work
 * TODO: make it prettier
 */
public class RadarView extends View {

    static final String TAG = "RadarView";

    private RadarBlip centerLocation;
    private double bearing=0;
    @SuppressLint("UseSparseArrays")
    private Map<Long, RadarContact> contacts = new HashMap<Long, RadarContact>();
    private Paint paint = new Paint();

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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //int width = canvas.getWidth();        //does not work on emulator
        //int height = canvas.getHeight();
        int width = MeasureSpec.getSize(getWidth());     // works, but how?
        int height = MeasureSpec.getSize(getHeight());

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.rgb(0,0,0));
        paint.setStrokeWidth(1);
        canvas.drawRect(1, 1, width, height, paint);

        canvas.rotate(-(float)bearing, (float)width/2, (float)height/2); //dont forget to restore!

        paint.setStyle(Paint.Style.STROKE);
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
        paint.setColor(Color.argb(150,0,0,250));
        for(RadarContact c:contacts.values()) {
            double dLat = c.getLastBlip().getLongitude() - centerLocation.getLongitude();
            double dLon = c.getLastBlip().getLatitude() - centerLocation.getLatitude();
            canvas.drawCircle((float)(width/2 + dLat/0.00001), (float)(height/2 - dLon/0.00001), 6, paint);
        }

        canvas.restore(); // don't draw after restore, as it does something with border offset or something

    }

    private void init(){

    }

    public void addContact(RadarContact contact) {
        if(contacts.containsKey(contact.getID())) {
            throw new IllegalArgumentException("contact to add is allready present in RadarView");
        } else {
            contacts.put(contact.getID(),contact);
        }
    }

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
