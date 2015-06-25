package com.pollytronics.clique.lib.tools;

/**
 * Created by pollywog on 6/25/15.
 * TODO: use resources for language
 */
public class TimeFormatting {
    public static String ageStringFromSeconds(double seconds) {
        int secs = (int) seconds;
        if (secs < 60) return String.valueOf(secs) + " sec";
        int mins = secs/60;
        if (mins < 60) return String.valueOf(mins) + " min";
        int hours = mins / 60;
        if (hours < 24) return String.valueOf(hours) + " hours";
        int days = hours/24;
        if (days < 7) return String.valueOf(hours) + " days";
        return "over a week";
    }
}
