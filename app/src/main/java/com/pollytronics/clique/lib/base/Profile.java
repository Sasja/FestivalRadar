package com.pollytronics.clique.lib.base;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pollywog on 9/8/15.
 */
public class Profile {
    private static final String TAG="Profile";

    private String name;

    private void setDefault() {
        this.name = "anon";
    }

    public Profile(JSONObject profileJSON) throws JSONException {
        if (profileJSON == null) throw new JSONException("null JSONObject passed to Profile constructor)");
        name = profileJSON.getString("nick");
    }

    public Profile(String name) {
        if(name != null) {
            this.name = name;
        } else {
            setDefault();
        }
    }

    public Profile() {
        setDefault();
    }

    public Profile(Profile profile) {
        if(profile != null) {
            this.name = profile.name;
        } else {
            setDefault();
        }
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Profile setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return name;
    }
}
