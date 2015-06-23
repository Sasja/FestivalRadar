package com.pollytronics.festivalradar.lib;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.pollytronics.festivalradar.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is meant to be used as a base class for group and contact management.
 * it works like this:
 *      override loadMyFragments to initialise the fragments and fragmentTitles variables with the classes and titles of the fragments you want slide between
 *
 * TODO: if i need to call fragment methods from the activity i need an extra method to get a ref of the fragments as stored in fragments
 * TODO: if calling methods on fragments, make sure they are alive (some parameter, or check for it) to prevent nullpointerexceptions
 */
public class RadarActivity_MyViewPagerAct extends RadarActivity {

    private static final String TAG = "MyViewPagerAct";

    ViewPager viewPager;
    FragmentPagerAdapter fragmentPagerAdapter;
    // for the following, see:  http://stackoverflow.com/questions/7951730/viewpager-and-fragments-whats-the-right-way-to-store-fragments-state
    // TODO: should this use a hashmap??
    Map<Integer, MyViewPagerFragment> myPagerFragments = new HashMap<>();  // funky trick, keep ref of all the Fragments of the viewPager by overloading instantiateItem in the FragmentPagerAdapter

    private List<Class> fragments = new ArrayList<>();
    private List<String> fragmentTitles = new ArrayList<>();

    public RadarActivity_MyViewPagerAct() {
        loadMyFragments();
    }

    /**
     * Override this baby to put the right fragments into the viewPager
     */
    protected void loadMyFragments() {
        addFragment(MyViewPagerFragment.class, "DUMMY1");
        addFragment(MyViewPagerFragment.class, "DUMMY2");
        addFragment(MyViewPagerFragment.class, "DUMMY3");
    }

    protected void addFragment(Class fragmentClass, String title) {
        fragments.add(fragmentClass);
        fragmentTitles.add(title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myviewpager_base);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(2);     // this will prevent destruction of first fragment if last is shown (in case of three frags
        fragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setCurrentItem(1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public MyViewPagerFragment getFragmentByNr(int nr) {
        return myPagerFragments.get(nr);
    }

    @Override
    public void notifyDatabaseUpdate() {
        super.notifyDatabaseUpdate();
        for(MyViewPagerFragment f : myPagerFragments.values()) f.notifyDatabaseUpdate();
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        public MyFragmentPagerAdapter(FragmentManager fm) { super(fm); }

        @Override
        public Object instantiateItem(ViewGroup container, int position) { // funky trick from  http://stackoverflow.com/questions/7951730/viewpager-and-fragments-whats-the-right-way-to-store-fragments-state
            Object result = super.instantiateItem(container, position);
            myPagerFragments.put(position, (MyViewPagerFragment) result);
            return result;
        }

        /**
         * if i understand well this will be called only once for every i. (when fragment is about to be displayed next)
         * when activity gets recreated on tilt, fragment objects are recreated but not through this method.
         *
         * @param position
         * @return
         */
        @Override
        public Fragment getItem(int position) {
            try {
                return (Fragment) (fragments.get(position)).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
