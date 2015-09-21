package com.pollytronics.clique.lib.tools;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by pollywog on 9/11/15.
 */
public class MyAssortedTools {

    /**
     * prevent instantiation with private constructor
     */
    private MyAssortedTools() {}

    // yuck! the following lines are to hide the keyboard (http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard)
    // yuck android, thanks rmirabelle
    public static void hide_keyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if(view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
