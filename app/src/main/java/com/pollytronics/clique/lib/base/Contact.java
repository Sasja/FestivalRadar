package com.pollytronics.clique.lib.base;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that holds information on a Contact and its Profile, is also used for own profile info.
 * Methods that change the object should return the object so calls can be chained.
 *
 * TODO: (feature) add marker icon and color stuff
 */
public class Contact{

    private static final String TAG="Contact";

    private String name;
    private long globalId;

    public Contact(JSONObject profileJSON) throws JSONException {
        if (profileJSON == null) throw new JSONException("null JSONObject passed to Contact constructor)");
        name = profileJSON.getString("nick");
        globalId = profileJSON.getLong("userid");
    }

    public Contact(long globalId, String name) {
        this.name = name;
        this.globalId = globalId;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Contact setName(String name) {
        this.name = name;
        return this;
    }

    public long getGlobalId() {
        return globalId;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Contact setGlobalId(long globalId) {
        Log.i(TAG, "WARNING: setGlobalId(), should only be called for testing purposes");
        this.globalId = globalId;
        return this;
    }

    @Override
    public String toString() {
        return name;
    }
}
