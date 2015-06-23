package com.pollytronics.festivalradar.lib;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pollytronics.festivalradar.R;
import com.pollytronics.festivalradar.lib.database.CliqueDb_Interface;


/**
 * A Base class for Fragments that plug into my ViewPager class CliqueActivity_MyViewPagerAct
 */
public class MyViewPagerFragment extends Fragment {

    @SuppressWarnings("unused")
    private static final String TAG = "MyViewPagerFragment";

    public MyViewPagerFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_viewpagerdummy, container, false);
    }

    protected CliqueDb_Interface getCligueDb() {
        return ((CliqueActivity) getActivity()).getRadarDatabase();
    }

    public void notifyDatabaseUpdate() {
        Log.i(TAG, "received a notifyDatabaseUpdate");
    }
}
