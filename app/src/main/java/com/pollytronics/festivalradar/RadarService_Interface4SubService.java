package com.pollytronics.festivalradar;

import android.content.Context;

/**
 * Created by pollywog on 9/22/14.
 * TODO: replace the sendStringToRa functionality with something specific eg. a way to trigger toasts or to message some specific events...
 */
interface RadarService_Interface4SubService {
    @SuppressWarnings("unused")
    void sendStringToRa(String text);

    void notifyNewData();

    Context getContext();
}
