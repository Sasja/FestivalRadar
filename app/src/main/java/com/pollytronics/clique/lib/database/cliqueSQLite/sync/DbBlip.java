package com.pollytronics.clique.lib.database.cliqueSQLite.sync;

import android.content.ContentValues;
import android.util.Log;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.BaseORM;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.BlipEntry;
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

    public static void keepNEntriesForEachId(int n) throws CliqueDbException {
        Log.i(TAG, "WARNING: keepNEntriesForEachId() called but not implemented!, db will keep growing!");
    }
}
