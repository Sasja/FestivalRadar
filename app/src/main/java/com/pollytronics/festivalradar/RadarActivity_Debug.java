package com.pollytronics.festivalradar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * RadarActivity for debugging purposes
 * shows received calls
 * TODO: have a look at the SlidingsTabBasic demo for some ideas for logging
 */
public class RadarActivity_Debug extends RadarActivity {

    private final String TAG = "RadarActivity_Debug";

    TextView debugTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radaractivity_debug);
        debugTextView = (TextView) findViewById(R.id.textview_debug);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.debug_radar, menu);
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
    protected void onRadarServiceDisconnected() {
        super.onRadarServiceDisconnected();
    }

    @Override
    protected void onRadarServiceConnected() {
        super.onRadarServiceConnected();
    }

    ArrayList<String> getLogCat() {
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
