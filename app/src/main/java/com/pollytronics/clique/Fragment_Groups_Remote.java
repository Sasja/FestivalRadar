package com.pollytronics.clique;


import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pollytronics.clique.lib.MyViewPagerFragment;


public class Fragment_Groups_Remote extends MyViewPagerFragment {

    @SuppressWarnings("unused")
    private static final String TAG = "Frag_Groups_Remote";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_groups_remote, container, false);
    }

}
