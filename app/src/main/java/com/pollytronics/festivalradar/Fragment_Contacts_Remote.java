package com.pollytronics.festivalradar;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pollytronics.festivalradar.lib.MyViewPagerFragment;


public class Fragment_Contacts_Remote extends MyViewPagerFragment {

    @SuppressWarnings("unused")
    private static final String TAG = "Frag_Contacts_Remote";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts_remote, container, false);
    }

}
