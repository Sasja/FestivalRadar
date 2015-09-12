package com.pollytronics.clique.lib.database.cliqueSQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * This Class has static members that describe and store the table and column names
 * It also provides The CliqueDbHelper Class that handles database creation and updating
 *
 * TODO: onUpgrade() and onDowngrade() simply discards all data at the moment
 */
public class DbStructure {
    private static final String TAG = "DbStructure";

    public static class ContactEntry implements BaseColumns {
        public static final String TABLE_NAME = "contacts";
        public static final String COLUMN_NAME_ID ="id";
        //public static final String COLUMN_NAME_SOFTDELETE="softdelete"; // is not used as can see me does this now
        public static final String COLUMN_NAME_DIRTYCOUNTER = "dirtyCounter";
        public static final String COLUMN_NAME_ICANSEE = "iCanSee";
        public static final String COLUMN_NAME_CANSEEME = "canSeeMe";
    }

    public static class ProfileEntry implements BaseColumns {
        public static final String TABLE_NAME = "profiles";
        public static final String COLUMN_NAME_NICK = "nick";
        public static final String COLUMN_NAME_ID = "id";
    }

    public static class SelfProfileEntry implements BaseColumns {
        public static final String TABLE_NAME = "selfProfile";
        public static final String COLUMN_NAME_NICK = "nick";
        public static final String COLUMN_NAME_DIRTYCOUNTER="dirtyCounter";
    }

    public static class BlipEntry implements BaseColumns {
        public static final String TABLE_NAME = "blips";
        public static final String COLUMN_NAME_ID ="id";
        public static final String COLUMN_NAME_LAT="lat";
        public static final String COLUMN_NAME_LON="lon";
        public static final String COLUMN_NAME_UTC_S="utc_s";
    }

    public static class SelfBlipEntry implements BaseColumns {
        public static final String TABLE_NAME = "selfBlips";
        public static final String COLUMN_NAME_LAT="lat";
        public static final String COLUMN_NAME_LON="lon";
        public static final String COLUMN_NAME_UTC_S="utc_s";
        public static final String COLUMN_NAME_DIRTYCOUNTER="dirtyCounter";
    }

    public static class SyncStatus implements BaseColumns{
        public static final String TABLE_NAME = "syncStatus";
        public static final String COLUMN_NAME_GLOBALDIRTYCOUNTER = "counter";
        public static final String COLUMN_NAME_LASTSYNC = "lastsync";
    }

    public static class CliqueDbHelper extends SQLiteOpenHelper {
        public CliqueDbHelper(Context context) {
            super(context, CliqueSQLite.DATABASE_NAME, null, CliqueSQLite.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            recreateCliqueDb(db);
        }

        //TODO: if upgrading to new db that has removed some tables they will not be dropped by dropAllTables, find better way to drop all tables!
        public static void recreateCliqueDb(SQLiteDatabase db) {
            Log.i(TAG,"WARNING: dropping all tables in local database.");
            db.execSQL("DROP TABLE IF EXISTS " + ContactEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + ProfileEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + BlipEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + SelfProfileEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + SelfBlipEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + SyncStatus.TABLE_NAME);

            Log.i(TAG, "recreating all tables in local database");
            db.execSQL("CREATE TABLE " + ContactEntry.TABLE_NAME + " ( " +
                    ContactEntry._ID +                      " INTEGER PRIMARY KEY," +
                    ContactEntry.COLUMN_NAME_ID +           " INTEGER, " +
                    //ContactEntry.COLUMN_NAME_SOFTDELETE +   " INTEGER DEFAULT 0, " +
                    ContactEntry.COLUMN_NAME_CANSEEME +     " INTEGER DEFAULT 0, " +
                    ContactEntry.COLUMN_NAME_ICANSEE +      " INTEGER DEFAULT 0, " +
                    ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " INTEGER DEFAULT 0)");
            db.execSQL("CREATE TABLE " + ProfileEntry.TABLE_NAME + " ( " +
                    ProfileEntry._ID +                      " INTEGER PRIMARY KEY, " +
                    ProfileEntry.COLUMN_NAME_ID +           " INTEGER, " +
                    ProfileEntry.COLUMN_NAME_NICK +         " TEXT )");
            db.execSQL("CREATE TABLE " + BlipEntry.TABLE_NAME + " ( " +
                    BlipEntry._ID +                         " INTEGER PRIMARY KEY," +
                    BlipEntry.COLUMN_NAME_ID +              " INTEGER, " +
                    BlipEntry.COLUMN_NAME_LAT +             " REAL, " +
                    BlipEntry.COLUMN_NAME_LON +             " REAL, " +
                    BlipEntry.COLUMN_NAME_UTC_S +           " REAL )");
            db.execSQL("CREATE TABLE " + SelfProfileEntry.TABLE_NAME + " ( " +
                    SelfProfileEntry._ID +                        " INTEGER PRIMARY KEY, " +
                    SelfProfileEntry.COLUMN_NAME_NICK +           " TEXT, " +
                    SelfProfileEntry.COLUMN_NAME_DIRTYCOUNTER +   " INTEGER )");
            db.execSQL("CREATE TABLE " + SelfBlipEntry.TABLE_NAME + " ( " +
                    SelfBlipEntry._ID +                      " INTEGER PRIMARY KEY," +
                    SelfBlipEntry.COLUMN_NAME_LAT +          " REAL, " +
                    SelfBlipEntry.COLUMN_NAME_LON +          " REAL, " +
                    SelfBlipEntry.COLUMN_NAME_UTC_S +        " REAL, " +
                    SelfBlipEntry.COLUMN_NAME_DIRTYCOUNTER + " INTEGER DEFAULT 0)");
            db.execSQL("CREATE TABLE " + SyncStatus.TABLE_NAME + " ( " +
                    SyncStatus.COLUMN_NAME_GLOBALDIRTYCOUNTER + " INTEGER, " +
                    SyncStatus.COLUMN_NAME_LASTSYNC +           " REAL )");

            // enter an initial selfContact
            ContentValues contentValues = new ContentValues();
            contentValues.put(SelfProfileEntry.COLUMN_NAME_NICK, "anon");
            contentValues.put(SelfProfileEntry.COLUMN_NAME_DIRTYCOUNTER, 0);
            db.insert(SelfProfileEntry.TABLE_NAME, null, contentValues);

            // set the globaldirtycounter to one and the lastsync to zero
            contentValues = new ContentValues();
            contentValues.put(SyncStatus.COLUMN_NAME_GLOBALDIRTYCOUNTER,1);
            contentValues.put(SyncStatus.COLUMN_NAME_LASTSYNC, 0);
            db.insert(SyncStatus.TABLE_NAME,null,contentValues);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG,"WARNING: onUpgrade(), discarding all data in local database. (dbversion "+oldVersion+"->"+newVersion+")");
            //simply discard all data and start over
            recreateCliqueDb(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG,"WARING: onDowngrade() calling onUpgrade");
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
