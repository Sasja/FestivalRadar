package com.pollytronics.clique;

import com.pollytronics.clique.lib.MyViewPagerFragment;

/**
 * Base fragment class for the contact fragments
 * Created by pollywog on 6/22/15.
 */
public class MVP_Fragment_Contacts extends MyViewPagerFragment {
    MVP_Activity_Contacts getContactActivity() {
        return (MVP_Activity_Contacts) getActivity();
    }
}
