package com.pollytronics.clique.lib.database.cliqueSQLite.sync;

import android.content.ContentValues;

import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.PingEntry;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbDelete;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbInsert;

/**
 * Created by pollywog on 9/13/15.
 */
public class DbPing {

    /**
     * Prevent class instantiation
     */
    private DbPing() {}

    public static void flush() throws CliqueDbException {
        CliqueDbDelete delete = new CliqueDbDelete();
        delete.setTable(PingEntry.TABLE_NAME);
        delete.execute();
    }

    public static void add(Long id, String nick, Double distance) throws CliqueDbException {
        CliqueDbInsert insert = new CliqueDbInsert();
        insert.setTable(PingEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(PingEntry.COLUMN_NAME_ID, id);
        content.put(PingEntry.COLUMN_NAME_NICK, nick);
        content.put(PingEntry.COLUMN_NAME_DISTANCE, distance);
        insert.setValues(content);
        insert.execute();
    }

}
