package com.pollytronics.clique.lib.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;

import java.util.Collection;
import java.util.HashSet;

/**
 * A class that implements the CliqueDb_Interface using a SQLite database on the mobile device
 * This class should only be used directly for instantiation with getInstance(), use the interface for all other uses.
 * the static (Class) method getInstance will return the available instance or create it when necessary.
 * It uses the singleton design pattern: http://www.javaworld.com/article/2073352/core-java/simply-singleton.html
 *
 * it is not threadsafe so should only be accessed from one thread (the main thread or UI thread)
 *
 * TODO: onUpgrade() and onDowngrade() simply discards all data at the moment
 * TODO: make method updateContacts(Collection<Contact> contacts) and use it from within SubService_Cloud_2
 * TODO: check if it is ok to getReadableDatabase() and close() all the time, should it be open all the time and close once?
 * TODO: is it still okay to do all this database stuff sync on the main thread?
 */
public final class CliqueDb_SQLite implements CliqueDb_Interface {
    public static final String DATABASE_NAME = "Clique.db";
    public static final int DATABASE_VERSION = 2;   // increasing this will wipe all local databases on update
    private static final String TAG="CliqueDb_SQLite";
    private static CliqueDb_SQLite instance = null;
    private final CliqueDbHelper cliqueDbHelper;

    /**
     * private constructor to make sure only one object is ever created, the object should be obtained through getInstance instead.
     * @param context
     */
    private CliqueDb_SQLite(Context context){
        Log.i(TAG, "instanciating CliqueDb_SQLite object");
        cliqueDbHelper = new CliqueDbHelper(context);
    }

    /**
     * This is used instead of a constructor to ensure only one instance of this class is ever created (singleton design pattern)
     *
     * Note: Java doesn't allow static methods in interfaces (yet) so we cant just
     *
     * @param context
     * @return
     */
    public static CliqueDb_SQLite getInstance(Context context){
        if(instance == null) {
            instance = new CliqueDb_SQLite(context);
        }
        return instance;
    }

    @Override
    public Collection<Contact> getAllContacts() {
        Collection<Contact> contacts = new HashSet<>();
        SQLiteDatabase db = cliqueDbHelper.getReadableDatabase();
        String[] projection = {
                ContactEntry.COLUMN_NAME_GLOBAL_ID,
                ContactEntry.COLUMN_NAME_NAME,
        };
        String sortOrder = ContactEntry.COLUMN_NAME_NAME + " DESC";
        Cursor c = db.query(
                ContactEntry.TABLE_NAME,    // table
                projection,                 // columns
                null,                       // selection
                null,                       // selectionArgs
                null,                       // groupBy
                null,                       // having
                sortOrder                   // orderBy
        );
        for(int i = 0; i < c.getCount();i++){
            c.moveToPosition(i);
            long id = c.getLong(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_GLOBAL_ID));
            String name = c.getString(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_NAME));
            Contact aContact = new Contact(id, name);
            contacts.add(aContact);
        }
        c.close();
        db.close();
        return contacts;
    }

    @Override
    public Contact getContactById(Long id) {
        Contact contact;
        SQLiteDatabase db = cliqueDbHelper.getReadableDatabase();
        String[] projection = {
                ContactEntry.COLUMN_NAME_GLOBAL_ID,
                ContactEntry.COLUMN_NAME_NAME,
        };
        String selection = ContactEntry.COLUMN_NAME_GLOBAL_ID + "=" + "'" + Long.toString(id) + "'";
        Cursor c = db.query(
                ContactEntry.TABLE_NAME,
                projection,
                selection,
                null,
                null,
                null,
                null
        );
        if(c.getCount() > 0) {
            c.moveToPosition(0);
            String name = c.getString(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_NAME));
            contact = new Contact(id,name);
        } else {
            contact = null;
        }
        c.close();
        db.close();
        return contact;
    }

    @Override
    public void removeContact(Contact contact) {
        removeContactById(contact.getGlobalId());
    }

    @Override
    public void removeContactById(long id) {
        SQLiteDatabase db = cliqueDbHelper.getWritableDatabase();
        String selection = ContactEntry.COLUMN_NAME_GLOBAL_ID + "=" + Long.toString(id);
        int n = db.delete(
                ContactEntry.TABLE_NAME,
                selection,
                null
        );
        Log.i(TAG, "removed " + Integer.toString(n) + " contact(s) from database");
        db.close();
    }

    @Override
    public void updateContact(Contact contact) {               //TODO: this updates one contact at a time, should not be used over all contacts
        SQLiteDatabase db = cliqueDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ContactEntry.COLUMN_NAME_NAME, contact.getName());

        String selection = ContactEntry.COLUMN_NAME_GLOBAL_ID + "=" + Long.toString(contact.getGlobalId());
        int n = db.update(
                ContactEntry.TABLE_NAME,        // table
                values,                         // values
                selection,                      // whereClause
                null                            // whereArgs
        );
        Log.i(TAG, "updated " + Integer.toString(n) + " contact(s), better not use this for looping over all contacts");
        db.close();
    }

    /**
     * TODO: shouldn't this be atomic?
     * @param contact
     */
    @Override
    public void addContact(Contact contact) {
        if (getContactById(contact.getGlobalId()) != null) {
            Log.i(TAG, "addContact() called with an allready known user id, not doing anything!");
            return;
        }
        SQLiteDatabase db = cliqueDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ContactEntry.COLUMN_NAME_GLOBAL_ID, contact.getGlobalId());
        values.put(ContactEntry.COLUMN_NAME_NAME, contact.getName());
        long newRowId = db.insertOrThrow(
                ContactEntry.TABLE_NAME,
                null,
                values
        );
        Log.i(TAG, "inserted new contact in database in row_id " + Long.toString(newRowId));
        db.close();
    }

    @Override
    public Contact getSelfContact() {
        Contact selfContact;
        SQLiteDatabase db = cliqueDbHelper.getReadableDatabase();
        String[] projection = {
                SelfContactEntry.COLUMN_NAME_GLOBAL_ID,
                SelfContactEntry.COLUMN_NAME_NAME,
        };
        Cursor c = db.query(
                SelfContactEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        if(c.getCount() > 0) {
            c.moveToPosition(0);
            String name = c.getString(c.getColumnIndexOrThrow(SelfContactEntry.COLUMN_NAME_NAME));
            long id = c.getLong(c.getColumnIndexOrThrow(SelfContactEntry.COLUMN_NAME_GLOBAL_ID));
            selfContact = new Contact(id, name);
        } else {
            selfContact = null;
        }
        c.close();
        db.close();
//        if (selfContact==null) selfContact = insertRandomSelfContact(); //TODO: remove this shit, it inserts a random contact in db and returns it also
        return selfContact;
    }

    @Override
    public void updateSelfContact(Contact newSelfContact) {
        SQLiteDatabase db = cliqueDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SelfContactEntry.COLUMN_NAME_NAME, newSelfContact.getName());
        values.put(SelfContactEntry.COLUMN_NAME_GLOBAL_ID, newSelfContact.getGlobalId());
        Log.i(TAG, "updating selfContact");
        db.delete(
                SelfContactEntry.TABLE_NAME,
                null,
                null
        );
        db.insertOrThrow(
                    SelfContactEntry.TABLE_NAME,
                    null,
                    values
        );
        db.close();
    }

    @Override
    public Blip getLastBlip(Contact contact) {
        Blip lastBlip;
        SQLiteDatabase db = cliqueDbHelper.getReadableDatabase();
        String[] projection = {
                BlipEntry.COLUMN_NAME_LAT,
                BlipEntry.COLUMN_NAME_LON,
                BlipEntry.COLUMN_NAME_UTC_S
        };
        String selection = BlipEntry.COLUMN_NAME_GLOBAL_ID + "=" + "'" + Long.toString(contact.getGlobalId()) + "'";
        String sortOrder = BlipEntry.COLUMN_NAME_UTC_S + " DESC";
        Cursor c = db.query(
                BlipEntry.TABLE_NAME,   // table
                projection,             // columns
                selection,              // selection
                null,                   // selectionArgs
                null,                   // groupBy
                null,                   // having
                sortOrder               // orderBy
        );
        if(c.getCount() > 0) {
            c.moveToPosition(0);
            Double lat = c.getDouble(c.getColumnIndexOrThrow(BlipEntry.COLUMN_NAME_LAT));
            Double lon = c.getDouble(c.getColumnIndexOrThrow(BlipEntry.COLUMN_NAME_LON));
            Double utc_s = c.getDouble(c.getColumnIndexOrThrow(BlipEntry.COLUMN_NAME_UTC_S));
            lastBlip = new Blip(lat, lon, utc_s);
        } else {
            lastBlip = null;
        }
        c.close();
        db.close();
        return lastBlip;
    }

    @Override
    public void addBlip(Blip blip, Contact contact) {
        SQLiteDatabase db = cliqueDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BlipEntry.COLUMN_NAME_GLOBAL_ID, contact.getGlobalId());
        values.put(BlipEntry.COLUMN_NAME_LAT, blip.getLatitude());
        values.put(BlipEntry.COLUMN_NAME_LON, blip.getLongitude());
        values.put(BlipEntry.COLUMN_NAME_UTC_S, blip.getUtc_s());
        long newRowId = db.insertOrThrow(
                BlipEntry.TABLE_NAME,
                null,
                values
        );
        Log.i(TAG, "inserted new blip in database in row_id " + Long.toString(newRowId));
        db.close();
        keepNEntries(BlipEntry.TABLE_NAME, BlipEntry.COLUMN_NAME_UTC_S, 10);
    }

    /**
     * thanks to Alex Barret
     * http://stackoverflow.com/questions/578867/sql-query-delete-all-records-from-the-table-except-latest-n
     * (see answer of NickC for optimisation for large tables)
     * TODO: addapt code to delete from one contact only
     * @param tableName
     * @param orderColumnName
     * @param nEntries
     */
    private void keepNEntries(String tableName, String orderColumnName, int nEntries) {
        Log.i(TAG, "keepNEntries(" + tableName + ", " + orderColumnName + ", " + nEntries + ") called");
        Log.i(TAG, "WARNING: keepNEntries is not implemented so database will keep growing");
//        SQLiteDatabase db = cliqueDbHelper.getWritableDatabase();
//        db.execSQL(
//                "DELETE FROM " + tableName + " " +
//                        "WHERE " + BaseColumns._ID + " NOT in ( " +
//                        "SELECT " + BaseColumns._ID + " FROM ( " +
//                        "SELECT " + BaseColumns._ID + " FROM " + tableName + " " +
//                        "ORDER BY " + orderColumnName + " DESC " +
//                        "LIMIT " + nEntries + " " +
//                        ") foo " +
//                        ")"
//        );
//        db.close();
    }

    @Override
    public void addSelfBlip(Blip blip) {
        SQLiteDatabase db = cliqueDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SelfBlipEntry.COLUMN_NAME_LAT, blip.getLatitude());      // There is no GLOBAL_ID in this table
        values.put(SelfBlipEntry.COLUMN_NAME_LON, blip.getLongitude());
        values.put(SelfBlipEntry.COLUMN_NAME_UTC_S, blip.getUtc_s());
        long newRowId = db.insertOrThrow(
                SelfBlipEntry.TABLE_NAME,
                null,
                values
        );
        Log.i(TAG, "inserted new selfBlip in database in row_id " + Long.toString(newRowId));
        db.close();
        keepNEntries(SelfBlipEntry.TABLE_NAME, SelfBlipEntry.COLUMN_NAME_UTC_S, 10);
    }

    @Override
    public Blip getLastSelfBlip() {
        Blip lastSelfBlip;
        SQLiteDatabase db = cliqueDbHelper.getReadableDatabase();
        String[] projection = {
                SelfBlipEntry.COLUMN_NAME_LAT,
                SelfBlipEntry.COLUMN_NAME_LON,
                SelfBlipEntry.COLUMN_NAME_UTC_S
        };
        String sortOrder = SelfBlipEntry.COLUMN_NAME_UTC_S + " DESC";
        Cursor c = db.query(
                SelfBlipEntry.TABLE_NAME,   // table
                projection,                 // columns
                null,                       // selection
                null,                       // selectionArgs
                null,                       // groupBy
                null,                       // having
                sortOrder                   // orderBy
        );
        if(c.getCount() > 0) {
            c.moveToPosition(0);
            Double lat = c.getDouble(c.getColumnIndexOrThrow(SelfBlipEntry.COLUMN_NAME_LAT));
            Double lon = c.getDouble(c.getColumnIndexOrThrow(SelfBlipEntry.COLUMN_NAME_LON));
            Double utc_s = c.getDouble(c.getColumnIndexOrThrow(SelfBlipEntry.COLUMN_NAME_UTC_S));
            lastSelfBlip = new Blip(lat, lon, utc_s);
        } else {
            lastSelfBlip = null;
        }
        c.close();
        db.close();
        return lastSelfBlip;
    }

    private static class ContactEntry implements BaseColumns {
        public static final String TABLE_NAME = "contacts";
        public static final String COLUMN_NAME_NAME="name";
        public static final String COLUMN_NAME_GLOBAL_ID="globalId";
    }

    private static class SelfContactEntry extends ContactEntry {
        public static final String TABLE_NAME = "selfContact";
    }

    private static class BlipEntry implements BaseColumns {
        public static final String TABLE_NAME = "blips";
        public static final String COLUMN_NAME_GLOBAL_ID="globalId";
        public static final String COLUMN_NAME_LAT="lat";
        public static final String COLUMN_NAME_LON="lon";
        public static final String COLUMN_NAME_UTC_S="utc_s";
        public static final String COLUMN_NAME_DLAT="dLat";
        public static final String COLUMN_NAME_DLON="dLon";
        public static final String COLUMN_NAME_VLAT="vLat";
        public static final String COLUMN_NAME_VLON="vLon";
    }

    private static class SelfBlipEntry extends BlipEntry {
        public static final String TABLE_NAME = "selfBlips";
    }

    private class CliqueDbHelper extends SQLiteOpenHelper{
        public CliqueDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG, "onCreate()");
            db.execSQL("CREATE TABLE " + ContactEntry.TABLE_NAME + " ( " +
                    ContactEntry._ID +                      " INTEGER PRIMARY KEY," +
                    ContactEntry.COLUMN_NAME_GLOBAL_ID +    " LONG, " +
                    ContactEntry.COLUMN_NAME_NAME +         " TEXT )");
            db.execSQL("CREATE TABLE " + BlipEntry.TABLE_NAME + " ( " +
                    BlipEntry._ID +                         " INTEGER PRIMARY KEY," +
                    BlipEntry.COLUMN_NAME_GLOBAL_ID +       " LONG, " +
                    BlipEntry.COLUMN_NAME_LAT +             " DOUBLE, " +
                    BlipEntry.COLUMN_NAME_LON +             " DOUBLE, " +
                    BlipEntry.COLUMN_NAME_UTC_S +           " DOUBLE, " +
                    BlipEntry.COLUMN_NAME_DLAT +            " DOUBLE, " +
                    BlipEntry.COLUMN_NAME_DLON +            " DOUBLE, " +
                    BlipEntry.COLUMN_NAME_VLAT +            " DOUBLE, " +
                    BlipEntry.COLUMN_NAME_VLON +            " DOUBLE )");
            db.execSQL("CREATE TABLE " + SelfContactEntry.TABLE_NAME + " ( " +
                    SelfContactEntry._ID +                  "INTEGER PRIMARY KEY," +
                    SelfContactEntry.COLUMN_NAME_GLOBAL_ID +" LONG, " +
                    SelfContactEntry.COLUMN_NAME_NAME +     " TEXT )");
            db.execSQL("CREATE TABLE " + SelfBlipEntry.TABLE_NAME + " ( " +
                    BlipEntry._ID +                         " INTEGER PRIMARY KEY," +
                    //BlipEntry.COLUMN_NAME_GLOBAL_ID + " LONG, " +         // Threre is no global id column in this table
                    SelfBlipEntry.COLUMN_NAME_LAT +         " DOUBLE, " +
                    SelfBlipEntry.COLUMN_NAME_LON +         " DOUBLE, " +
                    SelfBlipEntry.COLUMN_NAME_UTC_S +       " DOUBLE, " +
                    SelfBlipEntry.COLUMN_NAME_DLAT +        " DOUBLE, " +
                    SelfBlipEntry.COLUMN_NAME_DLON +        " DOUBLE, " +
                    SelfBlipEntry.COLUMN_NAME_VLAT +        " DOUBLE, " +
                    SelfBlipEntry.COLUMN_NAME_VLON +        " DOUBLE )");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG,"WARNING: onUpgrade(), discarding all data in local database. (dbversion "+oldVersion+"->"+newVersion+")");
            //simply discard all data and start over
            db.execSQL("DROP TABLE IF EXISTS " + ContactEntry.TABLE_NAME);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG,"WARING: onDowngrade() calling onUpgrade");
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
