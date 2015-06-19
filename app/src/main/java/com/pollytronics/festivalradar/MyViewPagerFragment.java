package com.pollytronics.festivalradar;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A Base class for Fragments that plug into my ViewPager class RadarActivity_MyViewPagerAct
 */
public class MyViewPagerFragment extends Fragment {

    public MyViewPagerFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_viewpagerdummy, container, false);
    }
}
