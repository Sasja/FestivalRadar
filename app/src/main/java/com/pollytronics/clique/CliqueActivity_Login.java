package com.pollytronics.clique;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.pollytronics.clique.lib.CliqueActivity;

/**
 * TODO: when pressing "back" from sign in screen, shit happens
 */
public class CliqueActivity_Login extends CliqueActivity {

    private static final String TAG = "CliqueActivity_Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliqueactivity_login);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
