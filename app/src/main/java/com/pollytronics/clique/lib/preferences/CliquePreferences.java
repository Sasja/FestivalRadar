package com.pollytronics.clique.lib.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by pollywog on 9/23/14.
 * TODO: something is very wrong here with the instance always being null, and getInstance always returning a new object...
 */
public class CliquePreferences {
    public static final String COMPASS_ENABLED = "compassEnabled";
    public static final String SUN_ENABLED = "sunEnabled";
    public static final String ZOOM_RADIUS = "zoomRadius";
    public static final int UPDATE_RATE_LO_BAT = 0;
    public static final int UPDATE_RATE_BALANCED = 1;
    public static final int UPDATE_RATE_HI_PERFORMANCE = 2;
    @SuppressWarnings("unused")
    static final String TAG = "CliquePreferences";
    private static final String UPDATE_RATE = "updateRate";
    private static final boolean COMPASS_ENABLED_DEFAULT = true;
    private static final boolean SUN_ENABLED_DEFAULT = false;
    private static final int UPDATE_RATE_DEFAULT = UPDATE_RATE_BALANCED;
    private static final float ZOOM_RADIUS_DEFAULT = (float) 1000.0;

    @SuppressWarnings("CanBeFinal")
    private static CliquePreferences instance = null;
    private final SharedPreferences preferences;

    private CliquePreferences(Context context){
        preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static CliquePreferences getInstance(Context context) {
        if(instance==null) {
            return new CliquePreferences(context);
        } else {
            return instance;
        }
    }

    public long getLocalisationUpdateTime_ms(){
        int setting = preferences.getInt(UPDATE_RATE, UPDATE_RATE_DEFAULT);
        switch (setting) {
            case UPDATE_RATE_LO_BAT:
                return 60 * 1000;
            case UPDATE_RATE_BALANCED:
                return 15 * 1000;
            case UPDATE_RATE_HI_PERFORMANCE:
                return 1 * 1000;
            default:
                Log.i(TAG, "WARNING: unexpected UPDATE_RATE preference, returning the balanced value");
                return 15 * 1000;
        }
    }
    public long getCloudUpdateTime_ms(){
        int setting = preferences.getInt(UPDATE_RATE, UPDATE_RATE_DEFAULT);
        switch (setting) {
            case UPDATE_RATE_LO_BAT:
                return 300 * 1000;
            case UPDATE_RATE_BALANCED:
                return 60 * 1000;
            case UPDATE_RATE_HI_PERFORMANCE:
                return 10 * 1000;
            default:
                Log.i(TAG, "WARNING: unexpected UPDATE_RATE preference, returning the balanced value");
                return 60 * 1000;
        }
    }

    public int getUpdateRate() {
        return preferences.getInt(UPDATE_RATE, UPDATE_RATE_DEFAULT);
    }

    public void setUpdateRate(int setting) {
        Log.i(TAG, "setting update rate to " + String.valueOf(setting));
        preferences.edit().putInt(UPDATE_RATE, setting).apply();
    }

    public boolean getCompassEnabled() { return preferences.getBoolean(COMPASS_ENABLED, COMPASS_ENABLED_DEFAULT); }

    public void setCompassEnabled(Boolean enabled) { preferences.edit().putBoolean(COMPASS_ENABLED, enabled).apply(); }

    public boolean getSunEnabled() { return preferences.getBoolean(SUN_ENABLED, SUN_ENABLED_DEFAULT); }

    public void setSunEnabled(Boolean enabled) { preferences.edit().putBoolean(SUN_ENABLED, enabled).apply(); }

    public double  getZoomRadius() { return preferences.getFloat(ZOOM_RADIUS, ZOOM_RADIUS_DEFAULT); }

    public void setZoomRadius(double zoomRadius) { preferences.edit().putFloat(ZOOM_RADIUS, (float) zoomRadius).apply(); }
}
