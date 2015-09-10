package com.pollytronics.clique.lib.database.cliqueSQLite.local;

import android.content.ContentValues;
import android.database.Cursor;

import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.BaseORM;
import com.pollytronics.clique.lib.database.cliqueSQLite.CliqueSQLite;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.ContactEntry;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbInsert;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbQuery;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbUpdate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pollywog on 9/9/15.
 */
public class DbContact extends BaseORM {

    private static final String TAG = "DbContact";

    public static List<Long> getIds() throws CliqueDbException {
        final List<Long> contactIds = new ArrayList<>();
        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            public void parseCursor(Cursor c) throws CliqueDbException {
                for(int i=0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    long id = c.getLong(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_ID));
                    contactIds.add(id);
                }
            }
        };
        query.setTable(ContactEntry.TABLE_NAME);
        query.setProjection(new String[]{ContactEntry.COLUMN_NAME_ID});
        query.setSelection(ContactEntry.COLUMN_NAME_SOFTDELETE + " = 0", null);
        query.execute();
        return contactIds;
    }

    public static void remove(long id) throws CliqueDbException {
        long currentDirtyCounter = CliqueSQLite.getGlobalDirtyCounter();
        CliqueDbUpdate update = new CliqueDbUpdate();
        update.setTable(ContactEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(ContactEntry.COLUMN_NAME_SOFTDELETE, 1);
        content.put(ContactEntry.COLUMN_NAME_DIRTYCOUNTER, currentDirtyCounter);
        update.setValues(content);
        update.setWhere(ContactEntry.COLUMN_NAME_ID + " = " + Long.toString(id), null);
        update.execute();
    }

    public static void add(long id) throws CliqueDbException {
        long currentDirtyCounter = CliqueSQLite.getGlobalDirtyCounter();
        CliqueDbUpdate update = new CliqueDbUpdate();
        update.setTable(ContactEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(ContactEntry.COLUMN_NAME_SOFTDELETE, 0);
        content.put(ContactEntry.COLUMN_NAME_DIRTYCOUNTER, currentDirtyCounter);
        update.setValues(content);
        update.setWhere(ContactEntry.COLUMN_NAME_ID + " = " + Long.toString(id), null);
        update.execute();

        if (update.getnUpdated() == 1) return;

        CliqueDbInsert insert = new CliqueDbInsert();
        insert.setTable(ContactEntry.TABLE_NAME);
        content = new ContentValues();
        content.put(ContactEntry.COLUMN_NAME_SOFTDELETE, 0);
        content.put(ContactEntry.COLUMN_NAME_DIRTYCOUNTER, currentDirtyCounter);
        insert.setValues(content);
        insert.execute();
    }

    public static boolean isContact(long id) throws CliqueDbException {
        final boolean[] result = new boolean[1];
        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            public void parseCursor(Cursor c) throws CliqueDbException {
                result[0] = c.getCount() >= 0;
            }
        };
        query.setTable(ContactEntry.TABLE_NAME);
        query.setSelection(ContactEntry.COLUMN_NAME_ID + " = " + Long.toString(id) +
                ContactEntry.COLUMN_NAME_SOFTDELETE + " = 0", null);
        query.execute();
        return result[0];
    }
}
