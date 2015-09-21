package com.pollytronics.clique.lib.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Class that wraps the sharedPreferences.
 * It uses the singleton design pattern: http://www.javaworld.com/article/2073352/core-java/simply-singleton.html
 *
 * The instance should always be obtained through getInstance(), convenience methods such as getCliquePreferences() are provided for this in a few base classes.
 * Not threadsafe.
 * TODO: if the singleton pattern is necessary, consider using the sigle-element enumn type pattern (cfr Effective Java 2nd Edition, item 3, page 18)
 */
public class CliquePreferences {
    private static final String TAG = "CliquePreferences";

    private static final String COMPASS_ENABLED = "compassEnabled";
    private static final String SUN_ENABLED = "sunEnabled";
    private static final String ZOOM_RADIUS = "zoomRadius";
    private static final int UPDATE_RATE_LO_BAT = 0;
    private static final int UPDATE_RATE_BALANCED = 1;
    private static final int UPDATE_RATE_HI_PERFORMANCE = 2;
    private static final String UPDATE_RATE = "updateRate";
    private static final String ACCOUNT_LOGIN = "accountLogin";
    private static final String ACCOUNT_KEYB64 = "accountKeyB64";
    private static final String ACCOUNT_ID = "accountId";

    private static final boolean COMPASS_ENABLED_DEFAULT = true;
    private static final boolean SUN_ENABLED_DEFAULT = false;
    private static final int UPDATE_RATE_DEFAULT = UPDATE_RATE_BALANCED;
    private static final float ZOOM_RADIUS_DEFAULT = (float) 1000.0;

    @SuppressWarnings("CanBeFinal")
    private static CliquePreferences instance = null;
    private final SharedPreferences preferences;

    /**
     * private constructor to make sure it instances are only created through getInstance
     * @param context
     */
    private CliquePreferences(Context context){
        preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static CliquePreferences getInstance(Context context) {
        if(instance == null) {
            Log.i(TAG, "creating a new instance of CliquePreferences");
            instance = new CliquePreferences(context);
        }
        return instance;
    }

    /**
     * @return the desired localisation update interval in milliseconds
     */
    public long getLocalisationUpdateTime_ms(){
        switch (getUpdateRate()) {
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

    /**
     * @return the desired cloud connection rate in milliseconds
     */
    public long getCloudUpdateTime_ms(){
        switch (getUpdateRate()) {
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

    /**
     * @return the set value for the update rate, use the constants to decode
     */
    public int getUpdateRate() {
        return preferences.getInt(UPDATE_RATE, UPDATE_RATE_DEFAULT);
    }

    /**
     * @param setting use the class constants to set this value
     */
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

    public String getAccountLogin() { return preferences.getString(ACCOUNT_LOGIN, null); }
    public void setAccountLogin(String login) { preferences.edit().putString(ACCOUNT_LOGIN, login).apply(); }

    public String getAccountKeyb64() { return preferences.getString(ACCOUNT_KEYB64, null); }
    public void setAccountKeyb64(String key)  { preferences.edit().putString(ACCOUNT_KEYB64, key).apply(); }

    public int getAccountId() { return preferences.getInt(ACCOUNT_ID, 0); }
    public void setAccountId(int id) { preferences.edit().putInt(ACCOUNT_ID, id).apply(); }
}
