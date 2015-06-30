package com.pollytronics.clique.lib.service;

import android.os.Handler;
import android.os.Looper;

import com.pollytronics.clique.lib.database.CliqueDb_Interface;
import com.pollytronics.clique.lib.preferences.CliquePreferences;

/**
 * Created by pollywog on 9/20/14.
 * abstract class for helper classes for the CliqueService class
 * rs refers to the CliqueService instance containing this and all other helper instances,
 * it provides appropriate interfaces through
 *
 * getCliqueService()
 * getCligueDb()
 * getCliquePreferences()
 *
 * and it provides a Handler to post tasks on the main thread through
 *
 * getMainHandler()
 *
 * a few methods need to be overridden to handle some events.
 */
public abstract class SubService {

    @SuppressWarnings("unused")
    private final String TAG = "override this SubService TAG";

    private final CliqueService rs;
    /**
     * this gets a handler for the main thread, use it to post Runnables to the main thread
     */
    private final Handler handler = new Handler(Looper.getMainLooper());

    protected SubService(CliqueService rs){
        this.rs = rs;
    }

    protected CliqueService_Interface4SubService getCliqueService() { return rs; }

    protected CliqueDb_Interface getCliqueDb() { return rs.getCliqueDb(); }

    protected CliquePreferences getCliquePreferences() {return CliquePreferences.getInstance(rs.getApplicationContext());}

    protected Handler getMainHandler(){
        return handler;
    }

    /*
        Override the following 5 methods to handle these events
        and add methods to handle the sub classes responsibilities
     */

    abstract public void onCreate();

    abstract public void onDestroy();

    abstract protected void onRegister();

    abstract protected void onUnregister();

    abstract protected void onNewSettings();
}
