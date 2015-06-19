package com.pollytronics.festivalradar;

import android.view.Menu;


public class ViewPagerActivity_Contacts extends RadarActivity_MyViewPagerAct {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewpageractivity_contacts, menu);
        return true;
    }

    @Override
    protected void loadMyFragments() {
        addFragment(Fragment_Contacts_Ping.class, "PING");
        addFragment(Fragment_Contacts_MyContacts.class, "MY CONTACTS");
        addFragment(Fragment_Contacts_Remote.class, "REMOTE");
    }
}
