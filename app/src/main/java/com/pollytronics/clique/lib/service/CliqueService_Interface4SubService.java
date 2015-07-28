package com.pollytronics.clique.lib.service;

import android.content.Context;

/**
 * Created by pollywog on 9/22/14.
 */
public interface CliqueService_Interface4SubService {
    @SuppressWarnings("unused")
    void sendStringToRa(String text);

    void notifyNewData();

    Context getContext();
}
