package com.pollytronics.festivalradar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pollytronics.festivalradar.lib.RadarContact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class RadarActivity_ManageContacts extends RadarActivity {

    private static final String TAG = "ManageContactRadarAct";
    private DialogFragment mDialog;
    private RadarContact clickedContact;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radaractivity_manage_contacts);

        listView = (ListView) findViewById(R.id.listview_manage_contacts);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.i(TAG, "onItemClick i=" + position + " id=" + id);
                mDialog = new ContactActionDialogFragment();
                clickedContact = (RadarContact)adapterView.getAdapter().getItem(position);
                mDialog.show(getSupportFragmentManager(), "ContactActionDialog");
            }
        });
        updateContactListView();
    }

    public void onDelete(){
        Log.i(TAG, "deleting selected radar contact");
        getRadarDatabase().removeContact(clickedContact);
        notifyDatabaseUpdate();         // TODO: this shouldn't be called here but happen autamatically
    }

    public void onDeny(){
        Log.i(TAG, "canceled deleting radar contact");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.manage_contacts_radar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_contact_by_username) {
            startActivity(new Intent(this, RadarActivity_AddContactByUsername.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * set up the listView with data from the Database
     */
    private void updateContactListView(){
        ArrayList<RadarContact> contacts = new ArrayList<>(getRadarDatabase().getAllContacts());
        contacts.add(getRadarDatabase().getSelfContact());
        Collections.sort(contacts, new Comparator<RadarContact>() {
            @Override
            public int compare(RadarContact radarContact1, RadarContact radarContact2) {
                int result = radarContact1.getName().toUpperCase().compareTo(radarContact2.getName().toUpperCase());
                if (result == 0) {
                    result = ((Long)radarContact1.getID()).compareTo(radarContact2.getID());
                }
                return result;
            }
        });
        ArrayAdapter<RadarContact> adapter = (ArrayAdapter<RadarContact>) listView.getAdapter();
        if(adapter==null) {
            adapter = new ArrayAdapter<RadarContact>(this, R.layout.manage_contacs_list_item, contacts) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    RadarContact contact = getItem(position);
                    View view;
                    if (convertView == null) {
                        view = LayoutInflater.from(RadarActivity_ManageContacts.this).inflate(R.layout.manage_contacs_list_item, null);
                    } else {
                        view = convertView;
                    }
                    TextView tv_name = (TextView) view.findViewById(R.id.textview_contact_name);
                    tv_name.setText(contact.getName() + " (" + Long.toString(contact.getID()) + ")");
                    TextView tv_extra = (TextView) view.findViewById(R.id.textview_contact_extra);
                    tv_extra.setText(contact.getLastBlip().toString());
                    return view;
                }
            };
            listView.setAdapter(adapter);
        } else {
            //just clear everything and rebuild it
            adapter.clear();
            for(RadarContact c : contacts) adapter.add(c);
        }
    }

   /**
     * gets called by service when it has updated the database with new info
     */
    @Override
    public void notifyDatabaseUpdate() {
        super.notifyDatabaseUpdate();
        updateContactListView();
    }

    public static class ContactActionDialogFragment extends DialogFragment {

        RadarActivity_ManageContacts mRadarActivity;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mRadarActivity = (RadarActivity_ManageContacts) activity;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage("delete this contact?")
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mRadarActivity.onDeny();
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mRadarActivity.onDelete();
                        }
                    })
                    .create();
        }
    }

}
