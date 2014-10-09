package com.pollytronics.festivalradar;

import android.util.Log;

import com.pollytronics.festivalradar.lib.RadarContact;

/**
 * Mock SubService to spoof own location lookup
 * it periodically updates the self-contact in the database
 * Created by pollywog on 9/23/14.
 */
public class LocalisationSubService extends AbstractSubService {

    private final String TAG = "LocalisationSubService";
    private int updateTime_ms;

    public LocalisationSubService(RadarService rs) {
        super(rs);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        updateTime_ms = (int) getRadarPreferences().getLocalisationUpdateTime_ms();
        getMainHandler().post(localiseLoop);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy");
        getMainHandler().removeCallbacks(localiseLoop);
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
        getMainHandler().removeCallbacks(localiseLoop);
        updateTime_ms = (int) getRadarPreferences().getLocalisationUpdateTime_ms();
        Log.i(TAG, "set updateTime to (ms) "+Integer.toString(updateTime_ms));
        getMainHandler().post(localiseLoop);
    }

    //-------------------------------------------------

    private final Runnable localiseLoop = new Runnable() {
        @Override
        public void run() {
            try{
                RadarContact selfContact = getRadarDatabase().getSelfContact();
                selfContact.addBlip(selfContact.getLastBlip().brownian(0.00005).reClock());
                getRadarDatabase().updateSelfContact(selfContact);
                getRadarService().notifyNewData();
                getMainHandler().postDelayed(localiseLoop,updateTime_ms);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
