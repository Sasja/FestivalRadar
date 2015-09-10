package com.pollytronics.clique.lib.database.cliqueSQLite.sync;

import android.content.ContentValues;
import android.database.Cursor;

import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.ContactEntry;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbDelete;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbInsert;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbQuery;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbUpdate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pollywog on 9/9/15.
 */
public class DbContact {

    public static List<Long> getAdded(long maxDirtyCounter) throws CliqueDbException {
        final List<Long> added = new ArrayList<>();
        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            public void parseCursor(Cursor c) throws CliqueDbException {
                for(int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    long id = c.getLong(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_ID));
                    added.add(id);
                }
            }
        };
        query.setTable(ContactEntry.TABLE_NAME);
        query.setProjection(new String[]{ContactEntry.COLUMN_NAME_ID});
        query.setSelection(ContactEntry.COLUMN_NAME_SOFTDELETE + " = 0 " +
                "AND " + ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " > 0 " +
                "AND " + ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter), null);
        query.execute();
        return added;
    }

    public static List<Long> getDeleted(long maxDirtyCounter) throws CliqueDbException {
        final List<Long> deleted = new ArrayList<>();
        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            public void parseCursor(Cursor c) throws CliqueDbException {
                for(int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    long id = c.getLong(c.getColumnIndexOrThrow(ContactEntry.COLUMN_NAME_ID));
                    deleted.add(id);
                }
            }
        };
        query.setTable(ContactEntry.TABLE_NAME);
        query.setProjection(new String[]{ContactEntry.COLUMN_NAME_ID});
        query.setSelection(ContactEntry.COLUMN_NAME_SOFTDELETE + " = 1 AND " +
                           ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " > 0 AND " +
                           ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter),null);
        query.execute();
        return deleted;
    }

    public static void clearDirtyCounters(long maxDirtyCounter) throws CliqueDbException {
        CliqueDbUpdate update = new CliqueDbUpdate();
        update.setTable(ContactEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(ContactEntry.COLUMN_NAME_DIRTYCOUNTER, 0);
        update.setValues(content);
        update.setWhere(ContactEntry.COLUMN_NAME_SOFTDELETE + " = 0 AND " +
                        ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " > 0 AND " +
                        ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter), null);
        update.execute();

        CliqueDbDelete delete = new CliqueDbDelete();
        delete.setTable(ContactEntry.TABLE_NAME);
        delete.setWhere(ContactEntry.COLUMN_NAME_SOFTDELETE + " = 1 AND " +
                ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " > 0 AND " +
                ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter), null);
        delete.execute();
    }

    public static void remove(long id, long maxDirtyCounter) throws CliqueDbException {
        CliqueDbDelete delete = new CliqueDbDelete();
        delete.setTable(ContactEntry.TABLE_NAME);
        delete.setWhere(ContactEntry.COLUMN_NAME_ID + " = " + Long.toString(id) + " AND " +
                ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter), null);
        delete.execute();
    }

    public static void add(long id, long maxDirtyCounter) throws CliqueDbException {
        // first, if the contact exits and was not modified recently (dirty) -> update
        CliqueDbUpdate update = new CliqueDbUpdate();
        update.setTable(ContactEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(ContactEntry.COLUMN_NAME_SOFTDELETE, 0);
        content.put(ContactEntry.COLUMN_NAME_DIRTYCOUNTER, 0);
        update.setValues(content);
        update.setWhere(ContactEntry.COLUMN_NAME_ID + " = " + Long.toString(id) + " AND " +
                        ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter), null);
        update.execute();

        if (update.getnUpdated() == 1) return;

        // if the contact exists, (and thus modified recently) -> do nothing
        content = new ContentValues();
        content.put(ContactEntry.COLUMN_NAME_ID, id);
        update.setValues(content);
        update.setWhere(ContactEntry.COLUMN_NAME_ID + " = " + Long.toString(id), null);
        update.execute();

        if (update.getnUpdated() == 1) return;

        // else insert it
        CliqueDbInsert insert = new CliqueDbInsert();
        insert.setTable(ContactEntry.TABLE_NAME);
        content = new ContentValues();
        content.put(ContactEntry.COLUMN_NAME_ID, id);
        content.put(ContactEntry.COLUMN_NAME_SOFTDELETE, 0);
        content.put(ContactEntry.COLUMN_NAME_DIRTYCOUNTER, 0);
        insert.setValues(content);
        insert.execute();
    }
}
