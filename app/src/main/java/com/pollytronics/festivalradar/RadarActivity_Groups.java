package com.pollytronics.festivalradar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * TODO: What does @Nullable mean???
 */
public class RadarActivity_Groups extends RadarActivity {

    private final String TAG = "RadarActivity_Groups";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radaractivity_groups);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);   // start in the middle
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_radar_activity__groups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public static class GroupsFragment extends Fragment {
        public GroupsFragment() {}

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.groupsfragment_dummy, container, false);
        }
    }

    public static class GroupsFragment_myGroups extends GroupsFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.groupsfragment_mygroups, container, false);
        }
    }

    public static class GroupsFragment_nearbyGroups extends GroupsFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.groupsfragment_nearbygroups, container, false);
        }
    }

    public static class GroupsFragment_privateGroups extends GroupsFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.groupsfragment_privategroups, container, false);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {super(fm);}

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch(position) {
                case 0:
                    fragment = new GroupsFragment_nearbyGroups();
                    break;
                case 1:
                    fragment = new GroupsFragment_myGroups();
                    break;
                case 2:
                    fragment = new GroupsFragment_privateGroups();
                    break;
                default:
                    fragment = new GroupsFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {return 3;}

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0:
                    return getString(R.string.groups_tab_nearby);
                case 1:
                    return getString(R.string.groups_tab_mygroups);
                case 2:
                    return getString(R.string.groups_tab_private);
                default:
                    return "dummy";
            }
        }
    }
}
