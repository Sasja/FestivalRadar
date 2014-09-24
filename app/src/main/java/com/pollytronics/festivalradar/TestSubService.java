package com.pollytronics.festivalradar;

import android.util.Log;

/**
 * Created by pollywog on 9/20/14.
 * Example SubService that provides a sayYo method to the RadarService,
 * so it can say "YO!!!" to any RadarActivity that might be listening (=that is registered).
 */
public class TestSubService extends AbstractSubService {

    private final String TAG = "TestSubService";

    public TestSubService(RadarService rs) {
        super(rs);
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"onCreate");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy");
    }

    @Override
    protected void onRegister() {
        Log.i(TAG,"onRegister");
        getRadarService().sendStringToRa("well hello there, mr activity!\n");
    }

    @Override
    protected void onUnregister() {
        Log.i(TAG,"onUnregister");
    }

    @Override
    protected void onNewSettings() {

    }

    //-----

    /**
     * yooooooooooo motherfucker! just a test-method to show how to send a message to any listening activity from a subservice.
     * that message is then handled (or discarded) by its print() method.
     */
    public void sayYo() {
        Log.i(TAG,"YO!!!");
        getRadarService().sendStringToRa("YO\n");
    }
}
