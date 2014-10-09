package com.pollytronics.festivalradar;

import android.content.Context;

/**
 * Created by pollywog on 9/22/14.
 */
public interface RadarService_Interface4SubService {
    void sendStringToRa(String text);

    void notifyNewData();

    Context getContext();
}
