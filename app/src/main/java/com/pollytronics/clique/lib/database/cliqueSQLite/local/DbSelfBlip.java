package com.pollytronics.clique.lib.database.cliqueSQLite.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.BaseORM;
import com.pollytronics.clique.lib.database.cliqueSQLite.CliqueSQLite;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.SelfBlipEntry;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbInsert;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbQuery;

/**
 * Created by pollywog on 9/9/15.
 */
public class DbSelfBlip extends BaseORM{
    private static final String TAG = "DbSelfBlip";

    public static void add(Blip blip) throws CliqueDbException {
        long currentDirtyCounter = CliqueSQLite.getGlobalDirtyCounter();
        CliqueDbInsert insert = new CliqueDbInsert();
        insert.setTable(SelfBlipEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(SelfBlipEntry.COLUMN_NAME_LAT, blip.getLatitude());
        content.put(SelfBlipEntry.COLUMN_NAME_LON, blip.getLongitude());
        content.put(SelfBlipEntry.COLUMN_NAME_UTC_S, blip.getUtc_s());
        content.put(SelfBlipEntry.COLUMN_NAME_DIRTYCOUNTER, currentDirtyCounter);
        insert.setValues(content);
        insert.execute();
    }

    public static Blip getLast() throws CliqueDbException {
        final Blip[] blip = new Blip[1];
        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            public void parseCursor(Cursor c) throws CliqueDbException {
                if(c.getCount() > 0) {
                    c.moveToPosition(0);
                    double lat = c.getDouble(c.getColumnIndexOrThrow(SelfBlipEntry.COLUMN_NAME_LAT));
                    double lon = c.getDouble(c.getColumnIndexOrThrow(SelfBlipEntry.COLUMN_NAME_LON));
                    double utc_s = c.getDouble(c.getColumnIndexOrThrow(SelfBlipEntry.COLUMN_NAME_UTC_S));
                    blip[0] = new Blip(lat, lon , utc_s);
                } else {
                    Log.i(TAG, "no self Blip found, cannot determine last location");
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

    public static void keepNEntries(int n) throws CliqueDbException {
        Log.i(TAG, "WARNING: keepNEntries not implemented yet so local database will keep growing!");
    }
}
