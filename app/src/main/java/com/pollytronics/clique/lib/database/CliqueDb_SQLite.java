package com.pollytronics.clique.lib.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.base.Contact;

import java.util.ArrayList;
import java.util.List;

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
 * TODO: is it still okay to do all this database stuff sync on the main thread? not really according to developer.android.com but it works fine in practice
 * TODO: i put try catch everywhere in the code where the interface methods are used to get it working for now, take a day to clean up and do proper error handling
 * TODO: deleting a contact from the database should probably also get rid of the associated blips
 * TODO: clean up the database now and then to maintain a maximum number of blips per user
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
     * Note: Java doesn't allow static methods in interfaces (yet) so we cant put this in the interface sadly :(
     *
     * @param context
     * @return
     */
    public static CliqueDb_SQLite getInstance(Context context){
        if(instance == null) {
            Log.i(TAG, "creating a new instance of CliqueDb_SQLite");
            instance = new CliqueDb_SQLite(context);
        }
        return instance;
    }

    //----------------------------------- PUBLIC METHODS ----------------------------------------------------

    /**
     * @return all contacts in the contact table in no particular order
     * @throws CliqueDbException
     */
    @Override
    public List<Contact> getAllContacts() throws CliqueDbException{
        final List<Contact> contacts = new ArrayList<>();

        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            void parseCursor(Cursor c) {
                for(int i=0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    long id = c.getLong(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_GLOBAL_ID));
                    String name = c.getString(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_NAME));
                    contacts.add(new Contact(id, name));
                }
            }
        };
        query.setTable(ContactEntry.TABLE_NAME);
        query.setProjection(new String[]{ContactEntry.COLUMN_NAME_GLOBAL_ID, ContactEntry.COLUMN_NAME_NAME});
        query.execute();
        return contacts;
    }

    /**
     * returns a contact with that global id or null if none is found
     * @param id the global id of the contact
     * @return null or the contact if it is present
     * @throws CliqueDbException throws an error when more than one entry is found on that global id
     */
    @Override
    public Contact getContactById(final Long id) throws CliqueDbException {
        final Contact[] contact = new Contact[1];   // hack to allow access from CliqueDbQuery object

        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            void parseCursor(Cursor c) throws CliqueDbException{
                if(c.getCount() == 1) {
                    c.moveToPosition(0);
                    String name = c.getString(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_NAME));
                    contact[0] = new Contact(id, name); // hack to allow this object to write to calling method attribute
                } else if(c.getCount() == 0) {
                    contact[0] = null;
                } else if(c.getCount() > 1) {
                    throw new CliqueDbException("more than one contact found on global id!");
                }
            }
        };
        query.setTable(ContactEntry.TABLE_NAME);
        query.setProjection(new String[]{ContactEntry.COLUMN_NAME_GLOBAL_ID, ContactEntry.COLUMN_NAME_NAME});
        query.setSelection(ContactEntry.COLUMN_NAME_GLOBAL_ID + "=" + "'" + Long.toString(id) + "'");
        query.execute();
        return contact[0];
    }

    /**
     * removes a contact from the database, it basically just calls removes entries that have the same global id as contact
     * @param contact contact to remove from db, it just looks at the global id
     * @throws CliqueDbException
     */
    @Override
    public void removeContact(Contact contact) throws CliqueDbException {
        removeContactById(contact.getGlobalId());
    }

    @Override
    public void removeContactById(long id) throws CliqueDbException {
        CliqueDbDelete delete = new CliqueDbDelete();
        delete.setTable(ContactEntry.TABLE_NAME);
        delete.setWhere(ContactEntry.COLUMN_NAME_GLOBAL_ID + "=" + Long.toString(id));
        delete.execute();
        Log.i(TAG, String.format("deleted %d contact(s) from local contacts table", delete.getnDeleted()));
    }

    /**
     * If the contact (or a contact with the same global id) is not present allready adds the contact to the db.
     * It's okay to do it in two sql operations as this database is accessed from one thread only
     * @param contact contact to be added to the database
     * @throws CliqueDbException*
     */
    @Override
    public void addContact(Contact contact) throws CliqueDbException {
        if (getContactById(contact.getGlobalId()) != null) {
            Log.i(TAG, "WARNING: addContact() called with contact with allready used global id, not doing anything");
            return;
        }
        CliqueDbInsert insert = new CliqueDbInsert();
        insert.setTable(ContactEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(ContactEntry.COLUMN_NAME_GLOBAL_ID, contact.getGlobalId());
        content.put(ContactEntry.COLUMN_NAME_NAME, contact.getName());
        insert.setValues(content);
        insert.execute();
        Log.i(TAG, "new contact inserted into the local database");
    }

    /**
     * this method returns the self contact that is stored in a table of its own. It will throw CliqueDbExceptions
     * if there is more than one entry in that table. If there is no entry it will return null.
     * @return the self contact or null
     * @throws CliqueDbException
     */
    @Override
    public Contact getSelfContact() throws CliqueDbException {
        final Contact[] selfContact = new Contact[1]; // hack to allow access from CliqueDbQuery object

        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            void parseCursor(Cursor c) throws CliqueDbException {
                int nFound = c.getCount();
                if (nFound == 1) {
                    c.moveToPosition(0);
                    long id = c.getLong(c.getColumnIndexOrThrow(SelfContactEntry.COLUMN_NAME_GLOBAL_ID));
                    String name = c.getString(c.getColumnIndexOrThrow(SelfContactEntry.COLUMN_NAME_NAME));
                    selfContact[0] = new Contact(id, name);
                } else if (nFound == 0) {
                    Log.i(TAG, "no selfcontact was found in the database on getSelfContact(), returning null");
                    selfContact[0] = null;
                } else {
                    throw new CliqueDbException(String.format("more than one selfconact was found in the database!? (n=%d)",nFound));
                }
            }
        };
        query.setTable(SelfContactEntry.TABLE_NAME);
        query.setProjection(new String[]{SelfContactEntry.COLUMN_NAME_GLOBAL_ID, SelfContactEntry.COLUMN_NAME_NAME});
        query.execute();
        return selfContact[0];
    }

    /**
     * removes the selfContact if any and inserts a new one in it's dedicated table
     * this is done in two steps so in theory the database could end up without a selfContact
     * if no SelfContact is present it will just create one
     * TODO: figure out how to do this in one step to prevent erasure of the selfContact
     * @param newSelfContact the new self contact
     * @throws CliqueDbException
     */
    @Override
    public void updateSelfContact(Contact newSelfContact) throws CliqueDbException {
        Log.i(TAG, "updating selfContact");
        CliqueDbDelete delete = new CliqueDbDelete();
        delete.setTable(SelfContactEntry.TABLE_NAME);
        delete.execute();
        CliqueDbInsert insert = new CliqueDbInsert();
        insert.setTable(SelfContactEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(SelfContactEntry.COLUMN_NAME_GLOBAL_ID, newSelfContact.getGlobalId());
        content.put(SelfContactEntry.COLUMN_NAME_NAME, newSelfContact.getName());
        insert.setValues(content);
        insert.execute();
    }

    /**
     * @param contact is only used for it's global id
     * @return the latest Blip corresponding to the global_id of contact
     */
    @Override
    public Blip getLastBlip(Contact contact) throws CliqueDbException {
        final Blip[] blip = new Blip[1];
        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            void parseCursor(Cursor c) throws CliqueDbException {
                if(c.getCount() >= 1) {
                    c.moveToPosition(0);
                    Double lat = c.getDouble(c.getColumnIndexOrThrow(BlipEntry.COLUMN_NAME_LAT));
                    Double lon = c.getDouble(c.getColumnIndexOrThrow(BlipEntry.COLUMN_NAME_LON));
                    Double utc_s = c.getDouble(c.getColumnIndexOrThrow(BlipEntry.COLUMN_NAME_UTC_S));
                    blip[0] = new Blip(lat, lon, utc_s);
                } else {
                    blip[0] = null;
                }
            }
        };
        query.setTable(BlipEntry.TABLE_NAME);
        query.setProjection(new String[]{BlipEntry.COLUMN_NAME_LAT, BlipEntry.COLUMN_NAME_LON, BlipEntry.COLUMN_NAME_UTC_S});
        query.setSelection(BlipEntry.COLUMN_NAME_GLOBAL_ID + "=" + "'" + Long.toString(contact.getGlobalId()) + "'");
        query.setOrderBy(BlipEntry.COLUMN_NAME_UTC_S + " DESC");
        query.execute();
        return blip[0];
    }

    /**
     * Adds a blip to the local database. The table has a column to store the owners id
     * TODO: check if it is there allready to prevent duplicates
     * @param blip the blip to add, duh
     * @param contact contact used for the global_id that the blip belongs to
     */
    @Override
    public void addBlip(Blip blip, Contact contact) throws CliqueDbException {
        Log.i(TAG, "inserting new blip in database");
        CliqueDbInsert insert = new CliqueDbInsert();
        insert.setTable(BlipEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(BlipEntry.COLUMN_NAME_GLOBAL_ID, contact.getGlobalId());
        content.put(BlipEntry.COLUMN_NAME_LAT, blip.getLatitude());
        content.put(BlipEntry.COLUMN_NAME_LON, blip.getLongitude());
        content.put(BlipEntry.COLUMN_NAME_UTC_S, blip.getUtc_s());
        insert.setValues(content);
        insert.execute();
    }

    /**
     * thanks to Alex Barret
     * http://stackoverflow.com/questions/578867/sql-query-delete-all-records-from-the-table-except-latest-n
     * (see answer of NickC for optimisation for large tables)
     * TODO: implement it right, make sure to keep n entries per contact, use some kind of wrapper as above
     * TODO: apply it right, when should this cleaning be performed?
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
    public void addSelfBlip(Blip blip) throws CliqueDbException {
        Log.i(TAG, "inserting a new selfblip in database");
        CliqueDbInsert insert = new CliqueDbInsert();
        insert.setTable(SelfBlipEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(SelfBlipEntry.COLUMN_NAME_LAT, blip.getLatitude());
        content.put(SelfBlipEntry.COLUMN_NAME_LON, blip.getLongitude());
        content.put(SelfBlipEntry.COLUMN_NAME_UTC_S, blip.getUtc_s());
        // notice there is no GLOBAL_ID in column in this table
        insert.setValues(content);
        insert.execute();
    }

    @Override
    public Blip getLastSelfBlip() throws CliqueDbException {
        final Blip[] blip = new Blip[1];
        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            void parseCursor(Cursor c) throws CliqueDbException {
                if(c.getCount() >= 1) {
                    c.moveToPosition(0);
                    Double lat = c.getDouble(c.getColumnIndexOrThrow(SelfBlipEntry.COLUMN_NAME_LAT));
                    Double lon = c.getDouble(c.getColumnIndexOrThrow(SelfBlipEntry.COLUMN_NAME_LON));
                    Double utc_s = c.getDouble(c.getColumnIndexOrThrow(SelfBlipEntry.COLUMN_NAME_UTC_S));
                    blip[0] = new Blip(lat, lon, utc_s);
                } else {
                    blip[0] = null;
                }
            }
        };
        query.setTable(SelfBlipEntry.TABLE_NAME);
        query.setProjection(new String[]{SelfBlipEntry.COLUMN_NAME_LAT, SelfBlipEntry.COLUMN_NAME_LON, SelfBlipEntry.COLUMN_NAME_UTC_S});
        query.setOrderBy(SelfBlipEntry.COLUMN_NAME_UTC_S + " DESC");
        query.execute();
        return blip[0];
    }

    //------------------------------- SQL METHOD WRAPPERS ----------------------------------------------------------

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

    //-------------------------------- DATABASE STRUCTURE -------------------------------------------------

    /**
     * this abstract class wraps database queries to the database in order to make sure SQLExceptions are checked and handled and resources are released
     */
    private abstract class CliqueDbQuery {

        private String table = null;
        private String[] projection = null;
        private String selection = null;
        private String[] selectionArgs = null;
        private String groupBy = null;
        private String having = null;
        private String orderBy = null;

        public CliqueDbQuery() {}

        public void setTable(String table) { this.table = table; }
        public void setProjection(String[] projection) { this.projection = projection; }
        public void setSelection(String selection) { this.selection = selection; }
        public void setSelectionArgs(String[] selectionArgs) { this.selectionArgs = selectionArgs; }
        public void setGroupBy(String groupBy) { this.groupBy = groupBy; }
        public void setHaving(String having) { this.having = having; }
        public void setOrderBy(String orderBy) { this.orderBy = orderBy; }

        /**
         * Implement this method to extract necessary data from your cursor, throw a CliqueDbException with a message when something unexpected happens
         * @param c Cursor instance returned by the database query.
         * @throws CliqueDbException
         */
        abstract void parseCursor(Cursor c) throws CliqueDbException;

        /**
         * executes the sql operation, extracts the results, translates errors and releases resources
         * because an SQLiteException is a RunTimeException it is not checked, Catching and rethrowing makes them checked.
         * @throws CliqueDbException
         */
        void execute() throws CliqueDbException {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                db = cliqueDbHelper.getReadableDatabase();
                c = db.query(table, projection, selection, selectionArgs, groupBy, having, orderBy);
                parseCursor(c);
            } catch (SQLiteException e) {
                e.printStackTrace();
                throw new CliqueDbException();
            } finally {
                if (c != null) c.close();
                if (db != null) db.close();
            }
        }
    }

    /**
     * this class wraps database deletes in order to make sure SQLExceptions are checked and handled and resources are released
     */
    private class CliqueDbDelete {
        private String table = null;
        private String where = null;
        private String[] whereArgs = null;

        private int nDeleted = 0;

        public CliqueDbDelete() {}

        public void setTable(String table) { this.table = table; }
        public void setWhere(String where) { this.where = where; }
        public void setWhereArgs(String[] whereArgs) { this.whereArgs = whereArgs; }

        /**
         * executes the sql operation translates errors and releases resources
         * because an SQLiteException is a RunTimeException it is not checked, Catching and rethrowing makes them checked.
         * @throws CliqueDbException
         */
        void execute() throws CliqueDbException {
            SQLiteDatabase db = null;
            try {
                db = cliqueDbHelper.getWritableDatabase();
                nDeleted = db.delete(table, where, whereArgs);
            } catch (SQLiteException e) {
                e.printStackTrace();
                throw new CliqueDbException();
            } finally {
                if (db != null) db.close();
            }
        }

        public int getnDeleted() { return nDeleted; }
    }

    /**
     * this class wraps database inserts in order to make sure SQLExceptions are checked and handled and resources are released
     */
    private class CliqueDbInsert {
        private String table = null;
        private ContentValues values = null;

        public CliqueDbInsert() {}

        public void setTable(String table) { this.table = table; }
        public void setValues(ContentValues values) { this.values = values; }

        void execute() throws CliqueDbException {
            SQLiteDatabase db = null;
            try {
                db = cliqueDbHelper.getWritableDatabase();
                long result = db.insertOrThrow(table, null, values);
                if (result == -1) throw new CliqueDbException("insertOrThrow returned -1");
            } catch (SQLiteException e) {
                e.printStackTrace();
                throw new CliqueDbException();
            } finally {
                if (db != null) db.close();
            }
        }
    }

    private class CliqueDbHelper extends SQLiteOpenHelper {
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

    public class CliqueDbException extends Exception {
        public CliqueDbException() {
            super();
        }
        public CliqueDbException(String detailMessage) { super(detailMessage); }
    }


}
