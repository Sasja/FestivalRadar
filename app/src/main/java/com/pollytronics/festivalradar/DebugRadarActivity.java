package com.pollytronics.festivalradar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * RadarActivity for debugging purposes
 * shows received calls
 */
public class DebugRadarActivity extends RadarActivity {

    private final String TAG = "DebugRadarActivity";

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
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void print(String text) {
        super.print(text);
        debugTextView.append("print(\""+text+"\")\n");
    }

    @Override
    public void notifyDatabaseUpdate() {
        super.notifyDatabaseUpdate();
        debugTextView.append("notifyDataBaseUpdate()\n");
    }

    @Override
    protected void onRadarServiceDisconnected() {
        super.onRadarServiceDisconnected();
        debugTextView.append("onRadarServiceDisconnected()\n");
    }

    @Override
    protected void onRadarServiceConnected() {
        super.onRadarServiceConnected();
        debugTextView.append("onRadarServiceConnected()\n");
    }
}
