package com.pollytronics.festivalradar.lib.service;

import android.os.Handler;
import android.os.Looper;

import com.pollytronics.festivalradar.lib.database.RadarDatabase_Interface;
import com.pollytronics.festivalradar.lib.preferences.RadarPreferences;

/**
 * Created by pollywog on 9/20/14.
 * abstract class for helper classes for the RadarService class
 * rs refers to the RadarService instance containing this and all other helper instances,
 * it provides appropriate interfaces through
 *
 * getRadarService()
 * getRadarDatabase()
 * getRadarPreferences()
 *
 * and it provides a Handler to post tasks on the main thread through
 *
 * getMainHandler()
 *
 * a few methods need to be overridden to handle some events.
 */
public abstract class SubService {

    @SuppressWarnings("unused")
    private final String TAG = "override this SubService TAG";

    private final RadarService rs;
    /**
     * this gets a handler for the main thread, use it to post Runnables to the main thread
     */
    private final Handler handler = new Handler(Looper.getMainLooper());

    protected SubService(RadarService rs){
        this.rs = rs;
    }

    protected RadarService_Interface4SubService getRadarService() { return rs; }

    protected RadarDatabase_Interface getRadarDatabase() { return rs.getRadarDataBase(); }

    protected RadarPreferences getRadarPreferences() {return RadarPreferences.getInstance(rs.getApplicationContext());}

    protected Handler getMainHandler(){
        return handler;
    }

    /*
        Override the following 5 methods to handle these events
        and add methods to handle the sub classes responsibilities
     */

    abstract public void onCreate();

    abstract public void onDestroy();

    abstract protected void onRegister();

    abstract protected void onUnregister();

    abstract protected void onNewSettings();
}
