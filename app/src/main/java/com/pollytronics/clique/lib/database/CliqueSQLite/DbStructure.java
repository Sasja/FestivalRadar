package com.pollytronics.clique.lib.database.CliqueSQLite;

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
class DbStructure {
    public static final String TAG = "DbStructure";

    static class ContactEntry implements BaseColumns {
        public static final String TABLE_NAME = "contacts";
        public static final String COLUMN_NAME_NAME="name";
        public static final String COLUMN_NAME_GLOBAL_ID="globalId";
    }

    static class SelfContactEntry extends ContactEntry {
        public static final String TABLE_NAME = "selfContact";
    }

    static class BlipEntry implements BaseColumns {
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

    static class SelfBlipEntry extends BlipEntry {
        public static final String TABLE_NAME = "selfBlips";
    }

    public static class CliqueDbHelper extends SQLiteOpenHelper {
        public CliqueDbHelper(Context context) {
            super(context, CliqueSQLite.DATABASE_NAME, null, CliqueSQLite.DATABASE_VERSION);
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
