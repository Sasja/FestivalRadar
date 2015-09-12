package com.pollytronics.clique.lib.tools;

import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by pollywog on 9/11/15.
 * TODO: make the generate salt actually generate a salt instead of using a constant
 * FIXME: really
 */
public class MyCrypto {

    private static final String TAG = "MyCrypto";

    public static String generateSaltb64() {
        Log.i(TAG, "WARNING GENERATING A RANDOM SALT USING A CONSTANT!!!!!!");
        return "VX4IAeia5bX/jwe15x0s5SwpfgIB5mXbaea5hVDVDjfNBMmf+HUYzqfFCQE8dqoOEK5SowWRo+IjTnrOwvH4Lg==";
    }

    public static String calcKey64(String salt64, String pass) {
        byte[] salt = Base64.decode(salt64, Base64.DEFAULT);
        byte[] passbytes = pass.getBytes();
        byte[] concat = new byte[salt.length + passbytes.length];
        byte[] key = null;
        System.arraycopy(salt, 0, concat, 0, salt.length);
        System.arraycopy(passbytes, 0, concat, salt.length, passbytes.length);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            key = md.digest(concat);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(key, Base64.NO_WRAP);
    }
}
