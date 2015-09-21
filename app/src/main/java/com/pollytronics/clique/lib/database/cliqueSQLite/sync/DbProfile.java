package com.pollytronics.clique.lib.database.cliqueSQLite.sync;

import android.content.ContentValues;

import com.pollytronics.clique.lib.base.Profile;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.BaseORM;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.ProfileEntry;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbDelete;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbInsert;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbUpdate;

/**
 * Created by pollywog on 9/9/15.
 */
public class DbProfile extends BaseORM {

    /**
     * Prevent class instantiation
     */
    private DbProfile() {}

    public static void add(long id, Profile profile) throws CliqueDbException {
        CliqueDbUpdate update = new CliqueDbUpdate();
        update.setTable(ProfileEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(ProfileEntry.COLUMN_NAME_NICK, profile.getName());
        update.setValues(content);
        update.setWhere(ProfileEntry.COLUMN_NAME_ID + " = " + Long.toString(id), null);
        update.execute();

        if (update.getnUpdated() == 1) return;

        CliqueDbInsert insert = new CliqueDbInsert();
        insert.setTable(ProfileEntry.TABLE_NAME);
        content = new ContentValues();
        content.put(ProfileEntry.COLUMN_NAME_ID, id);
        content.put(ProfileEntry.COLUMN_NAME_NICK, profile.getName());
        insert.setValues(content);
        insert.execute();
    }

    public static void remove(long id) throws CliqueDbException {
        CliqueDbDelete delete = new CliqueDbDelete();
        delete.setTable(ProfileEntry.TABLE_NAME);
        delete.setWhere(ProfileEntry.COLUMN_NAME_ID + " = " + Long.toString(id), null);
        delete.execute();
    }
}
