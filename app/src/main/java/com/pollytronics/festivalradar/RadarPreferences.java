package com.pollytronics.festivalradar;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by pollywog on 9/23/14.
 * TODO: something is very wrong here with the instance always being null, and getInstance always returning a new object...
 */
public class RadarPreferences {

    static final String TAG = "RadarPreferences";
    private static final String LOCALISATION_UPDATE_PCT = "localisationUpdateTime_percent";
    private static final String CLOUD_UPDATE_PCT = "cloudUpdateTime_percent";
    private static final int LOCALISATION_UPDATE_PCT_INIT = 75;
    private static final int CLOUD_UPDATE_PCT_INIT = 25;
    private static RadarPreferences instance = null;
    private final SharedPreferences preferences;

    private RadarPreferences(Context context){
        preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static RadarPreferences getInstance(Context context) {
        if(instance==null) {
            return new RadarPreferences(context);
        } else {
            return instance;
        }
    }

    public long getLocalisationUpdateTime_ms(){
        double pct = preferences.getInt(LOCALISATION_UPDATE_PCT,LOCALISATION_UPDATE_PCT_INIT);
        double min_ms = 1000;
        double max_ms = 60000;
        return (long) (min_ms * Math.pow((max_ms/min_ms),(1.0-pct/100.0)));
    }
    public long getCloudUpdateTime_ms(){
        double pct = preferences.getInt(CLOUD_UPDATE_PCT,CLOUD_UPDATE_PCT_INIT);
        double min_ms = 1000;
        double max_ms = 60000;
        return (long) (min_ms * Math.pow((max_ms/min_ms),(1.0-pct/100.0)));
    }
    public int getLocalisationUpdateTime_percent(){
        return preferences.getInt(LOCALISATION_UPDATE_PCT, LOCALISATION_UPDATE_PCT_INIT);
    }
    public int getCloudUpdateTime_percent(){
        return preferences.getInt(CLOUD_UPDATE_PCT,CLOUD_UPDATE_PCT_INIT);
    }
    public void setLocalisationUpdateRate_percent(double percent){
        preferences.edit().putInt(LOCALISATION_UPDATE_PCT, (int) percent).apply();
    }
    public void setCloudUpdateRate_percent(double percent){
        preferences.edit().putInt(CLOUD_UPDATE_PCT, (int) percent).apply();
    }
}
