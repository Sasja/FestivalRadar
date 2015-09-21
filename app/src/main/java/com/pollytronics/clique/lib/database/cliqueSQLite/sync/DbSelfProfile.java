package com.pollytronics.clique.lib.database.cliqueSQLite.sync;

import android.content.ContentValues;
import android.database.Cursor;

import com.pollytronics.clique.lib.base.Profile;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.BaseORM;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.SelfProfileEntry;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbQuery;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbUpdate;

/**
 * Created by pollywog on 9/9/15.
 */
public class DbSelfProfile extends BaseORM {
    private static final String TAG = "DbSelfProfile";

    /**
     * Prevent class instantiation
     */
    private DbSelfProfile() {}

    public static Profile getChanged(long maxDirtyCounter) throws CliqueDbException {
        final Profile[] profile = new Profile[1];
        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            public void parseCursor(Cursor c) throws CliqueDbException {
                if (c.getCount() == 1) {
                    c.moveToPosition(0);
                    String nick = c.getString(c.getColumnIndexOrThrow(SelfProfileEntry.COLUMN_NAME_NICK));
                    profile[0] = new Profile(nick);
                } else profile[0] = null;
            }
        };
        query.setTable(SelfProfileEntry.TABLE_NAME);
        query.setProjection(new String[] {SelfProfileEntry.COLUMN_NAME_NICK});
        query.setSelection(SelfProfileEntry.COLUMN_NAME_DIRTYCOUNTER + " > 0 AND " +
                           SelfProfileEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter), null);
        query.execute();
        return profile[0];
    }

    public static void clearDirtyProfile(long maxDirtyCounter) throws CliqueDbException {
        CliqueDbUpdate update = new CliqueDbUpdate();
        update.setTable(SelfProfileEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(SelfProfileEntry.COLUMN_NAME_DIRTYCOUNTER, 0);
        update.setValues(content);
        update.setWhere(SelfProfileEntry.COLUMN_NAME_DIRTYCOUNTER + " > 0 AND " +
                SelfProfileEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter), null);
        update.execute();
    }

    public static void update(Profile profile, long maxDirtyCounter) throws CliqueDbException {
        CliqueDbUpdate update = new CliqueDbUpdate();
        update.setTable(SelfProfileEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(SelfProfileEntry.COLUMN_NAME_NICK, profile.getName());
        content.put(SelfProfileEntry.COLUMN_NAME_DIRTYCOUNTER, 0);
        update.setValues(content);
        update.setWhere(SelfProfileEntry.COLUMN_NAME_DIRTYCOUNTER + " < " + Long.toString(maxDirtyCounter), null);
        update.execute();
    }
}
