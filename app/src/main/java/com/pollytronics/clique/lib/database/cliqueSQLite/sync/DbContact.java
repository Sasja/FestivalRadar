package com.pollytronics.clique.lib.database.cliqueSQLite.sync;

import android.content.ContentValues;
import android.database.Cursor;

import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.ContactEntry;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbInsert;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbQuery;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbUpdate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pollywog on 9/9/15.
 *
 * remember that the dirtycounter only is used for uploading canseeme,
 * icansee always blindly follows what the server says
 * TODO: think about cleaning up, when can i actually delete rows?
 */
public class DbContact {

    /**
     * Prevent class instantiation
     */
    private DbContact() {}

    /**
     * The only values that can be changed on the server are the canseeme values,
     * so those are the only values that are related to the dirtycounter also.
     * @param maxDirtyCounter
     * @return
     * @throws CliqueDbException
     */
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
        query.setSelection(ContactEntry.COLUMN_NAME_CANSEEME + " = 1 AND " +
                ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " > 0 AND " +
                ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter), null);
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
        query.setSelection(ContactEntry.COLUMN_NAME_CANSEEME + " = 0 AND " +
                           ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " > 0 AND " +
                           ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter),null);
        query.execute();
        return deleted;
    }

    /**
     * TODO: might want to clean up (delete) when both flags canseeme and ican see are 0?
     */
    public static void clearDirtyCounters(long maxDirtyCounter) throws CliqueDbException {
        CliqueDbUpdate update = new CliqueDbUpdate();
        update.setTable(ContactEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(ContactEntry.COLUMN_NAME_DIRTYCOUNTER, 0);
        update.setValues(content);
        update.setWhere(ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " > 0 AND " +
                ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter), null);
        update.execute();
    }

    public static void addIcanSee(long id) throws CliqueDbException{
        CliqueDbUpdate update = new CliqueDbUpdate();
        update.setTable(ContactEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(ContactEntry.COLUMN_NAME_ICANSEE, 1);
        update.setValues(content);
        update.setWhere(ContactEntry.COLUMN_NAME_ID + " = " + Long.toString(id), null);
        update.execute();

        if(update.getnUpdated() == 1) return;

        CliqueDbInsert insert = new CliqueDbInsert();
        insert.setTable(ContactEntry.TABLE_NAME);
        content.put(ContactEntry.COLUMN_NAME_ID, id);
        content.put(ContactEntry.COLUMN_NAME_ICANSEE, 1);
        // i'm relying on the default values of the columns here for dirtycounter and canseeme
        insert.setValues(content);
        insert.execute();
    }

    public static void removeIcanSee(long id) throws CliqueDbException {
        CliqueDbUpdate update = new CliqueDbUpdate();
        update.setTable(ContactEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(ContactEntry.COLUMN_NAME_ICANSEE, 0);
        update.setValues(content);
        update.setWhere(ContactEntry.COLUMN_NAME_ID + " = " + Long.toString(id), null);
        update.execute();
    }

    public static void addCanSeeMe(long id, long maxDirtyCounter) throws CliqueDbException {
        // first if the contact exists and was not modified recently (after maxDirtyCounter) -> update
        CliqueDbUpdate update = new CliqueDbUpdate();
        update.setTable(ContactEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(ContactEntry.COLUMN_NAME_CANSEEME, 1);
        content.put(ContactEntry.COLUMN_NAME_DIRTYCOUNTER, 0);
        update.setValues(content);
        update.setWhere(ContactEntry.COLUMN_NAME_ID + " = " + Long.toString(id) + " AND " +
                ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter), null);
        update.execute();

        if(update.getnUpdated() == 1) return;

        // if the contact exists (and thus modified recently) -> do nothing
        content = new ContentValues();
        content.put(ContactEntry.COLUMN_NAME_ID, id);   // hack to update nothing but just get count
        update.setValues(content);
        update.setWhere(ContactEntry.COLUMN_NAME_ID + " = " + Long.toString(id), null);
        update.execute();

        if (update.getnUpdated() == 1) return;

        // else the contact does not exist yet -> insert it
        CliqueDbInsert insert = new CliqueDbInsert();
        insert.setTable(ContactEntry.TABLE_NAME);
        content = new ContentValues();
        content.put(ContactEntry.COLUMN_NAME_ID, id);
        content.put(ContactEntry.COLUMN_NAME_CANSEEME, 1); // depending on db standard values here for icansee and dirtycounter
        insert.setValues(content);
        insert.execute();
    }

    public static void removeCanSeeMe(long id, long maxDirtyCounter) throws CliqueDbException {
        CliqueDbUpdate update = new CliqueDbUpdate();
        update.setTable(ContactEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(ContactEntry.COLUMN_NAME_CANSEEME, 0);
        content.put(ContactEntry.COLUMN_NAME_DIRTYCOUNTER, 0);
        update.setValues(content);
        update.setWhere(ContactEntry.COLUMN_NAME_ID + " = " + Long.toString(id) + " AND " +
                ContactEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter), null);
    }


}
