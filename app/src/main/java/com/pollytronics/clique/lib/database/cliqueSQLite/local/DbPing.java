package com.pollytronics.clique.lib.database.cliqueSQLite.local;

import android.database.Cursor;
import android.util.Pair;

import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.PingEntry;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbDelete;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pollywog on 9/13/15.
 */
public class DbPing {
    private static final String TAG = "DbPing";

    /**
     * Prevent class instantiation
     */
    private DbPing() {}

    public static List<Pair<Long, String>> getPings() throws CliqueDbException {
        final List<Pair<Long, String>> pings = new ArrayList<>();
        final CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            public void parseCursor(Cursor c) throws CliqueDbException {
                for(int i=0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    Long id = c.getLong(c.getColumnIndexOrThrow(PingEntry.COLUMN_NAME_ID));
                    String nick = c.getString(c.getColumnIndexOrThrow(PingEntry.COLUMN_NAME_NICK));
                    pings.add(new Pair<Long, String>(id, nick));
                }
            }
        };
        query.setTable(PingEntry.TABLE_NAME);
        query.setProjection(new String[]{PingEntry.COLUMN_NAME_ID, PingEntry.COLUMN_NAME_NICK});
        query.setOrderBy(PingEntry.COLUMN_NAME_DISTANCE);
        query.execute();
        return pings;
    }

    public static void remove(Long id) throws CliqueDbException {
        CliqueDbDelete delete = new CliqueDbDelete();
        delete.setTable(PingEntry.TABLE_NAME);
        delete.setWhere(PingEntry.COLUMN_NAME_ID + " = " + Long.toString(id), null);
        delete.execute();
    }

}
