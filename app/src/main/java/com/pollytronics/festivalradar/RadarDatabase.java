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
 * TODO: make method updateContacts(Collection<RadarContact> contacts) and use it from within CloudSubService
 * TODO: check if it is ok to getReadableDatabase() and close() all the time, should it be open all the time and close once?
 * TODO: is it okay to do all this database stuff sync?
 */
public class RadarDatabase implements RadarDatabase_Interface4RadarService, RadarDatabase_Interface4RadarActivity {

    private static final String TAG="RadarDatabase";
    private static RadarDatabase instance=null;

    private RadarDbHelper radarDbHelper;

    private static abstract class ContactEntry implements BaseColumns {
        public static final String TABLE_NAME = "contacts";
        public static final String COLUMN_NAME_NAME="name";
        public static final String COLUMN_NAME_LAST_LON="lastLongitude";
        public static final String COLUMN_NAME_LAST_LAT="lastLatitude";
        public static final String COLUMN_NAME_LAST_TIME="lastTime";
        public static final String COLUMN_NAME_GLOBAL_ID="globalId";

        public static final String SELF_CONTACT_NAME_VALUE="SELF_CONTACT";
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
                    ContactEntry.COLUMN_NAME_GLOBAL_ID + " LONG, " +
                    ContactEntry.COLUMN_NAME_NAME + " TEXT, " +
                    ContactEntry.COLUMN_NAME_LAST_LAT + " DOUBLE, " +
                    ContactEntry.COLUMN_NAME_LAST_LON + " DOUBLE, " +
                    ContactEntry.COLUMN_NAME_LAST_TIME + " LONG )");


            ContentValues values = new ContentValues();
            values.put(ContactEntry.COLUMN_NAME_GLOBAL_ID, 0);
            values.put(ContactEntry.COLUMN_NAME_NAME, ContactEntry.SELF_CONTACT_NAME_VALUE);
            values.put(ContactEntry.COLUMN_NAME_LAST_LAT, 0.0);
            values.put(ContactEntry.COLUMN_NAME_LAST_LON, 0.0);
            values.put(ContactEntry.COLUMN_NAME_LAST_TIME, 0.0);
            db.insertOrThrow(ContactEntry.TABLE_NAME, null, values);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i2) {
            Log.i(TAG,"onUpgrade() discard all data");
            //simply discard all data and start over
            db.execSQL("DROP TABLE IF EXISTS " + ContactEntry.TABLE_NAME);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG,"onDowngrade() discard all data");
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private RadarDatabase(Context context){
        radarDbHelper = new RadarDbHelper(context);
        Log.i(TAG,"initialised radarDbHelper");
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
                ContactEntry.COLUMN_NAME_GLOBAL_ID,
                ContactEntry.COLUMN_NAME_NAME,
                ContactEntry.COLUMN_NAME_LAST_LAT,
                ContactEntry.COLUMN_NAME_LAST_LON,
                ContactEntry.COLUMN_NAME_LAST_TIME
        };
        String sortOrder = ContactEntry.COLUMN_NAME_NAME + " DESC";
        String selection = ContactEntry.COLUMN_NAME_NAME + "<>" + "'" + ContactEntry.SELF_CONTACT_NAME_VALUE + "'";
        Cursor c = db.query(
                ContactEntry.TABLE_NAME,
                projection,
                selection,
                null, null, null,
                sortOrder);
        for(int i = 0; i < c.getCount();i++){
            c.moveToPosition(i);
            long id = c.getLong(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_GLOBAL_ID));
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
    public Collection<Long> getAllContactIds() {
        Collection<Long> contactIds = new HashSet<Long>();
        SQLiteDatabase db = radarDbHelper.getReadableDatabase();
        String[] projection = {
                ContactEntry.COLUMN_NAME_GLOBAL_ID,
        };
        String selection = ContactEntry.COLUMN_NAME_NAME + "<>" + "'" + ContactEntry.SELF_CONTACT_NAME_VALUE + "'";
        Cursor c = db.query(
                ContactEntry.TABLE_NAME,
                projection,
                selection,
                null, null, null, null);
        for(int i = 0; i < c.getCount();i++){
            c.moveToPosition(i);
            Long id = c.getLong(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_GLOBAL_ID));
            contactIds.add(id);
        }
        c.close();
        db.close();
        return contactIds;
    }

    @Override
    public RadarContact getContact(Long id) {
        RadarContact contact;
        SQLiteDatabase db = radarDbHelper.getReadableDatabase();
        String[] projection = {
                ContactEntry.COLUMN_NAME_GLOBAL_ID,
                ContactEntry.COLUMN_NAME_NAME,
                ContactEntry.COLUMN_NAME_LAST_LAT,
                ContactEntry.COLUMN_NAME_LAST_LON,
                ContactEntry.COLUMN_NAME_LAST_TIME
        };
        String selection = ContactEntry.COLUMN_NAME_GLOBAL_ID + "=" + "'" + Long.toString(id) + "'";
        Cursor c = db.query(
                ContactEntry.TABLE_NAME,
                projection,
                selection,
                null, null, null, null);
        if(c.getCount() > 0) {
            c.moveToPosition(0);
            String name = c.getString(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_NAME));
            double lastLat = c.getDouble(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_LAST_LAT));
            double lastLon = c.getDouble(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_LAST_LON));
            long lastTime = c.getLong(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_LAST_TIME));
            RadarBlip blip = new RadarBlip();
            blip.setLatitude(lastLat);
            blip.setLongitude(lastLon);
            blip.setTime(lastTime);
            contact = new RadarContact().setID(id).setName(name).addBlip(blip);
        } else {
            contact = null;
        }
        c.close();
        db.close();
        return contact;
    }

    @Override
    public void removeContact(RadarContact contact) {
        if(contact.getName() != ContactEntry.SELF_CONTACT_NAME_VALUE) {
            SQLiteDatabase db = radarDbHelper.getWritableDatabase();
            String selection = ContactEntry.COLUMN_NAME_GLOBAL_ID + "=" + Long.toString(contact.getID());
            int n = db.delete(ContactEntry.TABLE_NAME, selection, null);
            Log.i(TAG, "removed " + Integer.toString(n) + " contacts from database");
            db.close();
        } else Log.i(TAG, "DO NOT USE THIS METHOD FOR THE SELF_CONTACT");
    }

    @Override
    public void updateContact(RadarContact contact) {               //TODO: this updates one contact at a time, should not be used over all contacts
        if(contact.getName() != ContactEntry.SELF_CONTACT_NAME_VALUE) {
            SQLiteDatabase db = radarDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ContactEntry.COLUMN_NAME_NAME, contact.getName());
            values.put(ContactEntry.COLUMN_NAME_LAST_LAT, contact.getLastBlip().getLatitude());
            values.put(ContactEntry.COLUMN_NAME_LAST_LON, contact.getLastBlip().getLongitude());
            values.put(ContactEntry.COLUMN_NAME_LAST_TIME, contact.getLastBlip().getTime());
            String selection = ContactEntry.COLUMN_NAME_GLOBAL_ID + "=" + Long.toString(contact.getID());
            int n = db.update(
                    ContactEntry.TABLE_NAME,
                    values,
                    selection, null
            );
            Log.i(TAG, "updated " + Integer.toString(n) + " contacts, better not use this for looping over all contacts");
            db.close();
        } else Log.i(TAG, "DO NOT USE THIS METHOD FOR THE SELF_CONTACT");
    }

    @Override
    public void addContactWithId(RadarContact contact) {
        if(contact.getName() != ContactEntry.SELF_CONTACT_NAME_VALUE) {
            SQLiteDatabase db = radarDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ContactEntry.COLUMN_NAME_GLOBAL_ID, contact.getID());
            values.put(ContactEntry.COLUMN_NAME_NAME, contact.getName());
            values.put(ContactEntry.COLUMN_NAME_LAST_LAT, contact.getLastBlip().getLatitude());
            values.put(ContactEntry.COLUMN_NAME_LAST_LON, contact.getLastBlip().getLongitude());
            values.put(ContactEntry.COLUMN_NAME_LAST_TIME, contact.getLastBlip().getTime());
            long newRowId;
            newRowId = db.insertOrThrow(ContactEntry.TABLE_NAME, null, values);
            Log.i(TAG, "inserted new contact in database in row_id " + Long.toString(newRowId));
            db.close();
        } else Log.i(TAG, "DO NOT USE THIS METHOD FOR THE SELF_CONTACT");
    }

    @Override
    public void addContact(RadarContact contact) {      //TODO:clean this shit up, this method should not exist
        int id = new Random().nextInt(1000)+666;
        contact.setID(id);
        Log.i(TAG,"CREATING RANDOM ID FOR NEW CONTACT, THIS SHOULDNT HAPPEN... but it did! :D");
        addContactWithId(contact);
    }

    @Override
    public RadarContact getSelfContact() {
        RadarContact selfContact = new RadarContact();
        SQLiteDatabase db = radarDbHelper.getReadableDatabase();
        String[] projection = {
                ContactEntry.COLUMN_NAME_GLOBAL_ID,
                ContactEntry.COLUMN_NAME_NAME,
                ContactEntry.COLUMN_NAME_LAST_LAT,
                ContactEntry.COLUMN_NAME_LAST_LON,
                ContactEntry.COLUMN_NAME_LAST_TIME
        };
        String selection = ContactEntry.COLUMN_NAME_NAME + "=" + "'" + ContactEntry.SELF_CONTACT_NAME_VALUE + "'";
        Cursor c = db.query(
                ContactEntry.TABLE_NAME,
                projection,
                selection,
                null, null, null, null
        );
        if (c.getCount() == 1){
            c.moveToFirst();
            long id = c.getLong(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_GLOBAL_ID));
            String name = c.getString(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_NAME));
            double lastLat = c.getDouble(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_LAST_LAT));
            double lastLon = c.getDouble(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_LAST_LON));
            long lastTime = c.getLong(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_LAST_TIME));
            RadarBlip blip = new RadarBlip();
            blip.setLatitude(lastLat);
            blip.setLongitude(lastLon);
            blip.setTime(lastTime);
            selfContact.setID(id).setName(name).addBlip(blip);
        } else {
            Log.i(TAG, "DID NOT FIND SELF_CONTACT! (or found more than one)");
            selfContact = null;
        }
        c.close();
        db.close();
        return selfContact;
    }

    @Override
    public void updateSelfContact(RadarContact newSelfContact) {
        SQLiteDatabase db = radarDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ContactEntry.COLUMN_NAME_GLOBAL_ID, newSelfContact.getID());
        values.put(ContactEntry.COLUMN_NAME_NAME, ContactEntry.SELF_CONTACT_NAME_VALUE);
        values.put(ContactEntry.COLUMN_NAME_LAST_LAT, newSelfContact.getLastBlip().getLatitude());
        values.put(ContactEntry.COLUMN_NAME_LAST_LON, newSelfContact.getLastBlip().getLongitude());
        values.put(ContactEntry.COLUMN_NAME_LAST_TIME, newSelfContact.getLastBlip().getTime());
        String selection = ContactEntry.COLUMN_NAME_NAME + "=" + "'" + ContactEntry.SELF_CONTACT_NAME_VALUE + "'";
        int n = db.update(
                ContactEntry.TABLE_NAME,
                values,
                selection,
                null
        );
        if (n == 1) {
            Log.i(TAG,"updated self-contact");
        } else {
            Log.i(TAG, "AMOUNT OF SELF CONTACTS UPDATED = " + Integer.toString(n));
        }
        db.close();
    }
}
