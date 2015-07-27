package com.pollytronics.clique;

import android.view.Menu;

import com.pollytronics.clique.lib.CliqueActivity_MyViewPagerAct;


public class MVP_Activity_Groups extends CliqueActivity_MyViewPagerAct {

    @SuppressWarnings("unused")
    private static final String TAG = "ViewPagerAct_Groups";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.groups, menu);
        return true;
    }

    @Override
    protected void loadMyFragments() {
        addFragment(Fragment_Groups_Nearby.class, getString(R.string.groups_tab_nearby));
        addFragment(Fragment_Groups_MyGroups.class, getString(R.string.groups_tab_mygroups));
        addFragment(Fragment_Groups_Private.class, getString(R.string.groups_tab_private));
    }
}
