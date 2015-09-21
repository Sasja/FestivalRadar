package com.pollytronics.clique.lib.database.cliqueSQLite.local;

import android.database.Cursor;
import android.util.Log;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.BaseORM;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.BlipEntry;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbQuery;

/**
 * Created by pollywog on 9/9/15.
 */
public class DbBlip extends BaseORM {

    private static final String TAG = "DbBlip";

    /**
     * Prevent class instantiation
     */
    private DbBlip() {}

    public static Blip getLast(final long id) throws CliqueDbException {
        final Blip[] blip = new Blip[1];
        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            public void parseCursor(Cursor c) throws CliqueDbException {
                if(c.getCount() > 0) {
                    c.moveToPosition(0);
                    double lat = c.getDouble(c.getColumnIndexOrThrow(BlipEntry.COLUMN_NAME_LAT));
                    double lon = c.getDouble(c.getColumnIndexOrThrow(BlipEntry.COLUMN_NAME_LON));
                    double utc_s = c.getDouble(c.getColumnIndexOrThrow(BlipEntry.COLUMN_NAME_UTC_S));
                    blip[0] = new Blip(lat, lon, utc_s);
                } else {
                    Log.i(TAG, "no blip found for user_id " + Long.toString(id));
                    blip[0] = null;
                }
            }
        };
        query.setTable(BlipEntry.TABLE_NAME);
        query.setProjection(new String[]{BlipEntry.COLUMN_NAME_LAT, BlipEntry.COLUMN_NAME_LON, BlipEntry.COLUMN_NAME_UTC_S});
        query.setSelection(BlipEntry.COLUMN_NAME_ID + " = " + Long.toString(id), null);
        query.setOrderBy(BlipEntry.COLUMN_NAME_UTC_S + " DESC");
        query.execute();
        return blip[0];
    }
}
