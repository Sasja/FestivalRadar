package com.pollytronics.festivalradar;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class Fragment_Groups_Nearby extends MyViewPagerFragment {

    @SuppressWarnings("unused")
    private static final String TAG = "Frag_Groups_Nearby";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_groups_nearby, container, false);
    }
}
