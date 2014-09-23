package com.pollytronics.festivalradar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;


public class MainRadarActivity extends RadarActivity {

    private static final String TAG = "MainRadarActivity";
    TextView logText;
    ToggleButton toggleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radaractivity_main);

        logText = (TextView) findViewById(R.id.text_view);

        final Button sayYoButton = (Button) findViewById(R.id.say_yo);
        sayYoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBoundToService()){
                    Log.i(TAG, "calling rs.sayYo()");
                    getBoundRadarService().sayYo();
                } else {
                    Log.i(TAG,"fuck");
                }
            }
        });

        toggleService = (ToggleButton) findViewById(R.id.toggle_service);
        toggleService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(toggleService.isChecked()) {
                    startAndBindRadarService();
                } else {
                    unbindAndStopRadarService();
                }
            }
        });
    }

    @Override
    protected void onRadarServiceDisconnected(){
        toggleService.setChecked(false);
    }

    @Override
    protected void onRadarServiceConnected(){
        toggleService.setChecked(true);
    }

    @Override
    public void print(String text) {
        logText.append(text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_radaractivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_main_dummy) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
