package com.pollytronics.clique.lib.tools;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by pollywog on 9/11/15.
 */
public class MyCrypto {

    private static final String TAG = "MyCrypto";

    /**
     * prevent instantiation with private constructor
     */
    private MyCrypto() {}

    public static String generateSaltb64() {
        byte[] salt = new byte[64];
        new SecureRandom().nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
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
