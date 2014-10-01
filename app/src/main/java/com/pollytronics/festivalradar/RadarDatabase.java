package com.pollytronics.festivalradar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.util.Log;

import com.pollytronics.festivalradar.lib.RadarBlip;
import com.pollytronics.festivalradar.lib.RadarContact;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by pollywog on 9/22/14.
 * This class should provide methods to store and retrieve data on...
 * A) contacts and metadata
 * B) location data
 * it is used by and has an interface for the RadarService and for RadarActivities
 *
 * TODO: onUpgrade() and onDowngrade just discards all data at the moment
 * TODO: pull selfContact into database also
 * TODO: make method updateContacts(Collection<RadarContact> contacts) and use it from within CloudSubService
 */
public class RadarDatabase implements RadarDatabase_Interface4RadarService, RadarDatabase_Interface4RadarActivity {

    private static final String TAG="RadarDatabase";
    private static RadarDatabase instance=null;

    private RadarContact selfContact;

    private RadarDbHelper radarDbHelper;

    private static abstract class ContactEntry implements BaseColumns {
        public static final String TABLE_NAME = "contacts";
        public static final String COLUMN_NAME_NAME="name";
        public static final String COLUMN_NAME_LAST_LON="lastLongitude";
        public static final String COLUMN_NAME_LAST_LAT="lastLatitude";
        public static final String COLUMN_NAME_LAST_TIME="lastTime";
    }

    private class RadarDbHelper extends SQLiteOpenHelper{
        public static final String DATABASE_NAME = "FestivalRadarContacts.db";
        public static final int DATABASE_VERSION = 1;

        public RadarDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG, "onCreate()");
            db.execSQL("CREATE TABLE " + ContactEntry.TABLE_NAME + " ( " +
                    ContactEntry._ID + " INTEGER PRIMARY KEY," +
                    ContactEntry.COLUMN_NAME_NAME + " TEXT, " +
                    ContactEntry.COLUMN_NAME_LAST_LAT + " DOUBLE, " +
                    ContactEntry.COLUMN_NAME_LAST_LON + " DOUBLE, " +
                    ContactEntry.COLUMN_NAME_LAST_TIME + " LONG )");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i2) {
            Log.i(TAG,"onUpgrade()");
            //simply discard all data and start over
            db.execSQL("DROP TABLE IF EXISTS " + ContactEntry.TABLE_NAME);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG,"onDowngrade()");
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private RadarDatabase(Context context){
        radarDbHelper = new RadarDbHelper(context);
        Log.i(TAG,"initialised radarDbHelper");

//        /**
//         * temporary stuff to generate some random blips for testing
//         */
        final double LAT = 51.072478;
        final double LON = 3.709913;
        final double LATRANGE = 0.003;
        final double LONRANGE = 0.002;
        class VanbeverBlip extends RadarBlip{
            VanbeverBlip(){
                super();
                Random rnd = new Random();
                setLatitude(LAT+LATRANGE*(rnd.nextDouble()-0.5));
                setLongitude(LON+LONRANGE*(rnd.nextDouble()-0.5));
                setTime(SystemClock.currentThreadTimeMillis());
            }
        }
        selfContact = (new RadarContact()).setName("self").addBlip(new VanbeverBlip());
    }

    public static RadarDatabase getInstance(Context context){
        if(instance==null){
            instance = new RadarDatabase(context);
        }
        return instance;
    }

    //-------------------------------

    /**
     * return an Iterable of contacts
     */
    @Override
    public Collection<RadarContact> getAllContacts() {
        Collection<RadarContact> contacts = new HashSet<RadarContact>();
        SQLiteDatabase db = radarDbHelper.getReadableDatabase();
        String[] projection = {
                ContactEntry._ID,
                ContactEntry.COLUMN_NAME_NAME,
                ContactEntry.COLUMN_NAME_LAST_LAT,
                ContactEntry.COLUMN_NAME_LAST_LON,
                ContactEntry.COLUMN_NAME_LAST_TIME
        };
        String sortOrder = ContactEntry.COLUMN_NAME_NAME + " DESC";
        Cursor c = db.query(
                ContactEntry.TABLE_NAME,
                projection,
                null, null, null, null,
                sortOrder);
        for(int i = 0; i < c.getCount();i++){
            c.moveToPosition(i);
            long id = c.getLong(c.getColumnIndexOrThrow(ContactEntry._ID));
            String name = c.getString(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_NAME));
            double lastLat = c.getDouble(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_LAST_LAT));
            double lastLon = c.getDouble(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_LAST_LON));
            long lastTime = c.getLong(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_LAST_TIME));
            RadarBlip blip = new RadarBlip();
            blip.setLatitude(lastLat);
            blip.setLongitude(lastLon);
            blip.setTime(lastTime);
            RadarContact aContact = new RadarContact().setID(id).setName(name).addBlip(blip);
            contacts.add(aContact);
        }
        c.close();
        db.close();
        return contacts;
    }

    @Override
    public void removeContact(RadarContact contact) {
        SQLiteDatabase db = radarDbHelper.getWritableDatabase();
        String selection = ContactEntry._ID + "=" + Long.toString(contact.getID());
        int n = db.delete(ContactEntry.TABLE_NAME, selection, null);
        Log.i(TAG, "removed " + Integer.toString(n) + " contacts from database");
        db.close();
    }

    @Override
    public void updateContact(RadarContact contact) {               //TODO: this updates one contact at a time, should not be used over all contacts
        SQLiteDatabase db = radarDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ContactEntry.COLUMN_NAME_NAME,contact.getName());
        values.put(ContactEntry.COLUMN_NAME_LAST_LAT,contact.getLastBlip().getLatitude());
        values.put(ContactEntry.COLUMN_NAME_LAST_LON,contact.getLastBlip().getLongitude());
        values.put(ContactEntry.COLUMN_NAME_LAST_TIME,contact.getLastBlip().getTime());
        String selection = ContactEntry._ID + "=" + Long.toString(contact.getID());
        int n = db.update(
                ContactEntry.TABLE_NAME,
                values,
                selection, null
        );
        Log.i(TAG,"updated " + Integer.toString(n) + " contacts, better not use this for looping over all contacts");
        db.close();
    }

    @Override
    public void addContact(RadarContact contact) {
        SQLiteDatabase db = radarDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ContactEntry.COLUMN_NAME_NAME,contact.getName());
        values.put(ContactEntry.COLUMN_NAME_LAST_LAT,contact.getLastBlip().getLatitude());
        values.put(ContactEntry.COLUMN_NAME_LAST_LON,contact.getLastBlip().getLongitude());
        values.put(ContactEntry.COLUMN_NAME_LAST_TIME,contact.getLastBlip().getTime());
        long newRowId;
        newRowId = db.insertOrThrow(ContactEntry.TABLE_NAME, null, values);
        Log.i(TAG, "inserted new contact in database in row_id "+ Long.toString(newRowId));
        db.close();
    }

    @Override
    public RadarContact getSelfContact() {
        return new RadarContact(selfContact);
    }

    @Override
    public void updateSelfContact(RadarContact newSelfContact) {
        selfContact = new RadarContact(newSelfContact);
    }


}
