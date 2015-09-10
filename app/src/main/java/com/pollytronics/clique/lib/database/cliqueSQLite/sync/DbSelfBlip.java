package com.pollytronics.clique.lib.database.cliqueSQLite.sync;

import android.content.ContentValues;
import android.database.Cursor;

import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.BaseORM;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.SelfBlipEntry;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbQuery;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbUpdate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pollywog on 9/9/15.
 */
public class DbSelfBlip extends BaseORM {
    public static List<Blip> getNew(long maxDirtyCounter) throws CliqueDbException {
        final List<Blip> blips = new ArrayList<>();
        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            public void parseCursor(Cursor c) throws CliqueDbException {
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    double lat = c.getDouble(c.getColumnIndexOrThrow(SelfBlipEntry.COLUMN_NAME_LAT));
                    double lon = c.getDouble(c.getColumnIndexOrThrow(SelfBlipEntry.COLUMN_NAME_LON));
                    double utc_s = c.getDouble(c.getColumnIndexOrThrow(SelfBlipEntry.COLUMN_NAME_UTC_S));
                    blips.add(new Blip(lat, lon, utc_s));
                }
            }
        };
        query.setTable(SelfBlipEntry.TABLE_NAME);
        query.setProjection(new String[]{SelfBlipEntry.COLUMN_NAME_LAT, SelfBlipEntry.COLUMN_NAME_LON, SelfBlipEntry.COLUMN_NAME_UTC_S});
        query.setSelection(
                        SelfBlipEntry.COLUMN_NAME_DIRTYCOUNTER + " > 0 AND " +
                        SelfBlipEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter), null);
        query.execute();
        return blips;
    }

    public static void clearDirtyCounters(long maxDirtyCounter) throws CliqueDbException {
        CliqueDbUpdate update = new CliqueDbUpdate();
        update.setTable(SelfBlipEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(SelfBlipEntry.COLUMN_NAME_DIRTYCOUNTER, 0);
        update.setValues(content);
        update.setWhere(SelfBlipEntry.COLUMN_NAME_DIRTYCOUNTER + " > 0 AND " +
                        SelfBlipEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter),null);
        update.execute();
    }
}
