package com.pollytronics.clique.lib.database.cliqueSQLite.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.pollytronics.clique.lib.base.Profile;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.BaseORM;
import com.pollytronics.clique.lib.database.cliqueSQLite.CliqueSQLite;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.SelfProfileEntry;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbQuery;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbUpdate;

/**
 * Created by pollywog on 9/9/15.
 */
public class DbSelfProfile extends BaseORM {
    private final static String TAG = "DbSelfProfile";

    public static Profile get() throws CliqueDbException {
        final Profile[] profile = new Profile[1];
        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            public void parseCursor(Cursor c) throws CliqueDbException {
                if (c.getCount() != 1) Log.i(TAG, "WARNING: more or less than 1 SelfProfile found! get ready to crash!");
                c.moveToPosition(0);
                String nick = c.getString(c.getColumnIndexOrThrow(SelfProfileEntry.COLUMN_NAME_NICK));
                profile[0] = new Profile(nick);
            }
        };
        query.setTable(SelfProfileEntry.TABLE_NAME);
        query.setProjection(new String[] {SelfProfileEntry.COLUMN_NAME_NICK});
        query.execute();
        return profile[0];
    }

    public static void set(Profile profile) throws CliqueDbException {
        long currentDirtyCounter = CliqueSQLite.getGlobalDirtyCounter();
        CliqueDbUpdate update = new CliqueDbUpdate();
        update.setTable(SelfProfileEntry.TABLE_NAME);
        ContentValues content = new ContentValues();
        content.put(SelfProfileEntry.COLUMN_NAME_NICK, profile.getName());
        content.put(SelfProfileEntry.COLUMN_NAME_DIRTYCOUNTER, currentDirtyCounter);
        update.setValues(content);
        update.execute();
    }
}
