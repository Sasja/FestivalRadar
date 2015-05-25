package com.pollytronics.festivalradar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class RadarActivity_About extends RadarActivity {

    private static final String TAG = "RadarActivity_About";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radaractivity_about);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);        // this will add the global actions
        getMenuInflater().inflate(R.menu.about_radar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
