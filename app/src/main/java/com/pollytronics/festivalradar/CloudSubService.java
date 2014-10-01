package com.pollytronics.festivalradar;

import android.util.Log;

import com.pollytronics.festivalradar.lib.RadarBlip;
import com.pollytronics.festivalradar.lib.RadarContact;

import java.util.Collection;

/**
 * Mock Cloud SubService that periodically gets all contacts and randomly updates their last blip a bit
 * for testing purposes only
 * Created by pollywog on 9/23/14.
 */
public class CloudSubService extends AbstractSubService {

    private final String TAG = "CloudSubService";
    private int updateTime_ms;

    public CloudSubService(RadarService rs) {
        super(rs);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        updateTime_ms = (int) getRadarPreferences().getCloudUpdateTime_ms();
        Log.i(TAG, "set updateTime to (ms) "+Integer.toString(updateTime_ms));
        getMainHandler().post(cloudLoop);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy");
        getMainHandler().removeCallbacks(cloudLoop);
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
        getMainHandler().removeCallbacks(cloudLoop);
        updateTime_ms = (int) getRadarPreferences().getCloudUpdateTime_ms();
        Log.i(TAG, "set updateTime to (ms) "+Integer.toString(updateTime_ms));
        getMainHandler().post(cloudLoop);
    }

    private final Runnable cloudLoop = new Runnable() {
        @Override
        public void run() {
            try{
                updateAllContactsInDatabase();
                getRadarService().notifyNewData();
                getMainHandler().postDelayed(cloudLoop,updateTime_ms);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * mock method to update locations of all contacts a meter or so
     */
    public void updateAllContactsInDatabase(){
        Collection<RadarContact> contacts = getRadarDatabase().getAllContacts();
        for(RadarContact c : contacts){
            c.addBlip(new RadarBlip(c.getLastBlip().brownian(0.0001).reClock()));
            getRadarDatabase().updateContact(c);
        }
    }
}
