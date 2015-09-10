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
public class Contact extends Profile {
    private static final String TAG="Contact";

    private long globalId;

    public Contact(JSONObject profileJSON) throws JSONException {
        super(profileJSON);
        globalId = profileJSON.getLong("userid");
    }

    @Deprecated
    public Contact(long globalId, String name) {
        super(name);
        this.globalId = globalId;
    }

    public Contact(long id, Profile profile) {
        super(profile);
        this.globalId = id;
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
}
