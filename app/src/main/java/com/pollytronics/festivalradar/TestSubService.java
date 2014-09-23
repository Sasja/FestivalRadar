package com.pollytronics.festivalradar;

import android.util.Log;

/**
 * Created by pollywog on 9/20/14.
 * Example SubService that counts and sends a string to the listening activity each second
 * it also provides a sayYo method to the RadarService, so it can say "YO!!!" to any RadarActivity that might be listening (registered).
 */
public class TestSubService extends AbstractSubService {

    private final String TAG = "TestSubService";

    public TestSubService(RadarService rs) {
        super(rs);
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"onCreate");
        getMainHandler().post(testLoop);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy");
        getMainHandler().removeCallbacks(testLoop);
    }

    @Override
    protected void onRegister() {
        Log.i(TAG,"onRegister");
    }

    @Override
    protected void onUnregister() {
        Log.i(TAG,"onUnregister");
    }

    @Override
    protected void onNewSettings() {

    }

    //-----

    private int cnt = 0;
    private final Runnable testLoop = new Runnable() {
        @Override
        public void run() {
            try{
                getRadarService().sendStringToRa(Integer.toString(cnt++) + " ");
                getMainHandler().postDelayed(testLoop,1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * yooooooooooo motherfucker! just a test-method.
     */
    public void sayYo() {
        Log.i(TAG,"YO!!!");
        getRadarService().sendStringToRa("\nYO\n");
    }
}
