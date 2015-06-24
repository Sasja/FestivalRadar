package com.pollytronics.clique.lib.base;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that holds information on a Contact and its Profile, is also used for own profile info
 * methods that change the object should return themselves so calls can be chained.
 * Created by pollywog on 9/22/14.
 * TODO: remove that Comparable interface, it doesn't belong here
 * TODO: add marker icon and color stuff
 * Updated on 23/6/15
 */
public class Contact implements Comparable<Contact>{

    private static final String TAG="Contact";

    private String name;
    private long globalId;

    public Contact(JSONObject profileJSON) throws JSONException {
        if (profileJSON==null) throw new JSONException("null JSONObject passed to Contact(JSONObject constructor)");
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

    public Contact setName(String name) {
        this.name = name;
        return this;
    }

    public long getGlobalId() {
        return globalId;
    }

    public Contact setGlobalId(long globalId) {
        Log.i(TAG, "WARNING: setGlobalId(), should only be called for testing purposes");
        this.globalId = globalId;
        return this;
    }

    @Override
    public int compareTo(Contact another) {
        int result = getName().toUpperCase().compareTo(another.getName().toUpperCase());
        if (result == 0) {
            result = ((Long) getGlobalId()).compareTo(another.getGlobalId());
        }
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
