package com.pollytronics.clique.lib.database.cliqueSQLite.sync;

import android.content.ContentValues;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.BaseORM;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.BlipEntry;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbExecSQL;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbInsert;

/**
 * Created by pollywog on 9/9/15.
 */
public class DbBlip extends BaseORM {

    private final static String TAG = "DbBlip";

    public static void add(long id, Blip blip) throws CliqueDbException {
        CliqueDbInsert insert = new CliqueDbInsert();
        insert.setTable(BlipEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(BlipEntry.COLUMN_NAME_LAT, blip.getLatitude());
        content.put(BlipEntry.COLUMN_NAME_LON, blip.getLongitude());
        content.put(BlipEntry.COLUMN_NAME_UTC_S, blip.getUtc_s());
        content.put(BlipEntry.COLUMN_NAME_ID, id);
        insert.setValues(content);
        insert.execute();
    }

    public static void keepNEntriesForEachId(Integer n) throws CliqueDbException {
//        final Integer[] count = new Integer[1];
//        CliqueDbQuery query = new CliqueDbQuery() {
//            @Override
//            public void parseCursor(Cursor c) throws CliqueDbException {
//                c.moveToPosition(0);
//                count[0] = c.getInt(0);
//            }
//        };
//        query.setTable(BlipEntry.TABLE_NAME);
//        query.setProjection(new String[]{"COUNT(*)"});
//        query.execute();
//        Log.i(TAG, "total number of blips in local storage = " + count[0].toString());
//        Log.i(TAG, "now limiting amount of blips for each contact to n = " + n.toString());

        CliqueDbExecSQL execSQL = new CliqueDbExecSQL();
        // this is slow (and retarded) (n^2-timecomplexity) for large numbers but fine for small blip amounts. Sqlite doesn't support RANK()
        // SQLite also doesn't support {table name} AS {alias} which leads to the form below...
        // it seems to work :)
        execSQL.setSqlQuery(
                "DELETE FROM " + BlipEntry.TABLE_NAME + " " +
                "WHERE " + BlipEntry.TABLE_NAME + "." + BlipEntry._ID + " NOT IN ( " +
                    "SELECT b1_id FROM ( " +
                        "SELECT " + BlipEntry._ID + " AS b1_id, " + BlipEntry.COLUMN_NAME_ID + " AS b1_user_id FROM " + BlipEntry.TABLE_NAME + " " +
                        "WHERE b1_id IN ( " +
                            "SELECT " + BlipEntry._ID + " FROM " + BlipEntry.TABLE_NAME + " " +
                            "WHERE " + BlipEntry.TABLE_NAME + "." + BlipEntry.COLUMN_NAME_ID + " = b1_user_id " +
                            "ORDER BY " + BlipEntry.TABLE_NAME + "." + BlipEntry.COLUMN_NAME_UTC_S + " DESC " +
                            "LIMIT " + n.toString() + " " +
                        ") " +
                    ") " +
                ") ");
        execSQL.execute();
    }
}
