package com.pollytronics.clique;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pollytronics.clique.lib.MyViewPagerFragment;


public class Fragment_Groups_MyGroups extends MyViewPagerFragment {

    @SuppressWarnings("unused")
    private static final String TAG = "Frag_Groups_MyGroups";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_groups_mygroups, container, false);
    }

}