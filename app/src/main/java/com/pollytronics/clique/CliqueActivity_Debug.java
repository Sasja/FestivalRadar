package com.pollytronics.clique;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.pollytronics.clique.lib.CliqueActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * CliqueActivity for debugging purposes
 * shows received calls
 * TODO: have a look at the SlidingsTabBasic demo for some ideas for logging
 * TODO: doesnt work on api 15 cheapo tablet, no text visible
 */
public class CliqueActivity_Debug extends CliqueActivity {

    @SuppressWarnings("unused")
    private final String TAG = "CliqueActivity_Debug";

    private TextView debugTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliqueactivity_debug);
        debugTextView = (TextView) findViewById(R.id.textview_debug);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.debug, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void notifyDatabaseUpdate() {
        super.notifyDatabaseUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<String> logCatLog = getLogCat();
        for(String line: logCatLog) {
            debugTextView.append(line+"\n");
        }
    }

    @Override
    protected void onCliqueServiceDisconnected() {
        super.onCliqueServiceDisconnected();
    }

    @Override
    protected void onCliqueServiceConnected() {
        super.onCliqueServiceConnected();
    }

    private ArrayList<String> getLogCat() {
        ArrayList<String> logCatLog = new ArrayList<>();
        try {
            Process logCatProcess = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(logCatProcess.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                logCatLog.add(line);
            }
        } catch (IOException e) {
            logCatLog.add("ERROR OCCURED WHILE READING LOG");
        }
        return logCatLog;
    }

}
