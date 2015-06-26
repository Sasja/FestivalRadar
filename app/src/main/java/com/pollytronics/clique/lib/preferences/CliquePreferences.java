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

    public static final int UPDATE_RATE_LO_BAT = 0;
    public static final int UPDATE_RATE_BALANCED = 1;
    public static final int UPDATE_RATE_HI_PERFORMANCE = 2;
    @SuppressWarnings("unused")
    static final String TAG = "CliquePreferences";
    private static final String UPDATE_RATE = "updateRate";
    private static final int UPDATE_RATE_DEFAULT = UPDATE_RATE_BALANCED;

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
                return 30 * 1000;
            case UPDATE_RATE_BALANCED:
                return 15 * 1000;
            case UPDATE_RATE_HI_PERFORMANCE:
                return 1 * 1000;
            default:
                return 15 * 1000;
        }
    }
    public long getCloudUpdateTime_ms(){
        int setting = preferences.getInt(UPDATE_RATE, UPDATE_RATE_DEFAULT);
        switch (setting) {
            case UPDATE_RATE_LO_BAT:
                return 120 * 1000;
            case UPDATE_RATE_BALANCED:
                return 30 * 1000;
            case UPDATE_RATE_HI_PERFORMANCE:
                return 5 * 1000;
            default:
                return 30 * 1000;
        }
    }

    public int getUpdateRate() {
        return preferences.getInt(UPDATE_RATE, UPDATE_RATE_DEFAULT);
    }

    public void setUpdateRate(int setting) {
        Log.i(TAG, "setting update rate to " + String.valueOf(setting));
        preferences.edit().putInt(UPDATE_RATE, setting).apply();
    }
}
