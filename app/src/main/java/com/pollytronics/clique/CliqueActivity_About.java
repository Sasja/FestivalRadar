package com.pollytronics.clique;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.pollytronics.clique.lib.CliqueActivity;


public class CliqueActivity_About extends CliqueActivity {

    @SuppressWarnings("unused")
    private static final String TAG = "CliqueActivity_About";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliqueactivity_about);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);        // this will add the global actions
        getMenuInflater().inflate(R.menu.about, menu);
        return true;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
