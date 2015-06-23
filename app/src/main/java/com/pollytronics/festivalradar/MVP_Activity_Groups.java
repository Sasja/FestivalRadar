package com.pollytronics.festivalradar;

import android.view.Menu;

import com.pollytronics.festivalradar.lib.RadarActivity_MyViewPagerAct;


public class MVP_Activity_Groups extends RadarActivity_MyViewPagerAct {

    @SuppressWarnings("unused")
    private static final String TAG = "ViewPagerAct_Groups";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewpageractivity_groups, menu);
        return true;
    }

    @Override
    protected void loadMyFragments() {
        addFragment(Fragment_Groups_Nearby.class,"NEARBY");
        addFragment(Fragment_Groups_MyGroups.class,"MY GROUPS");
        addFragment(Fragment_Groups_Remote.class,"REMOTE");
    }
}
