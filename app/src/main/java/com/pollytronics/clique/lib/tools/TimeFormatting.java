package com.pollytronics.clique.lib.tools;

import android.content.Context;
import android.content.res.Resources;

import com.pollytronics.clique.R;

public class TimeFormatting {
    /**
     * Turns an amount of seconds in a descriptive string.
     *
     * @param seconds amount of seconds
     * @param context the context to obtain the resource strings from
     * @return a string describing an age such as "18 hours" or "an hour"
     */
    public static String ageStringFromSeconds(double seconds, Context context) {
        final Resources resources = context.getResources();
        int secs = (int) seconds;
        if (secs < 60) return resources.getQuantityString(R.plurals.timeformatting_sec, secs, secs);

        int mins = secs/60;
        if (mins == 1) return resources.getString(R.string.timeformatting_one_min); // bit silly to catch the 'one' cases and then use plurals anyway... oh well it works fine
        if (mins < 60) return resources.getQuantityString(R.plurals.timeformatting_min, mins, mins);

        int hours = mins / 60;
        if (hours == 1) return resources.getString(R.string.timeformatting_one_hour);
        if (hours < 24) return resources.getQuantityString(R.plurals.timeformatting_hour, hours, hours);

        int days = hours/24;
        if (days == 1) return resources.getString(R.string.timeformatting_one_day);
        if (days < 7) return resources.getQuantityString(R.plurals.timeformatting_day, days, days);
        return context.getString(R.string.timeformatting_over_a_week);
    }
}
