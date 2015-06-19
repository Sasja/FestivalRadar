package com.pollytronics.festivalradar;

import android.view.Menu;


public class ViewPagerActivity_Groups extends RadarActivity_MyViewPagerAct {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_viewpageractivity_groups, menu);
        return true;
    }

    @Override
    protected void loadMyFragments() {
        addFragment(Fragment_Groups_Nearby.class,"NEARBY");
        addFragment(Fragment_Groups_MyGroups.class,"MY GROUPS");
        addFragment(Fragment_Groups_Remote.class,"REMOTE");
    }
}
