package com.pollytronics.clique.lib.database.cliqueSQLite.local;

import android.database.Cursor;
import android.util.Log;

import com.pollytronics.clique.lib.base.Profile;
import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.BaseORM;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.ProfileEntry;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbDelete;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbQuery;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pollywog on 9/9/15.
 */
public class DbProfile extends BaseORM {

    private static final String TAG = "DbProfile";

    /**
     * Prevent class instantiation
     */
    private DbProfile() {}

    public static Profile get(final long id) throws CliqueDbException {
        final Profile[] profile = new Profile[1];
        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            public void parseCursor(Cursor c) throws CliqueDbException {
                if(c.getCount() == 1) {
                    c.moveToPosition(0);
                    String name = c.getString(c.getColumnIndexOrThrow(ProfileEntry.COLUMN_NAME_NICK));
                    profile[0] = new Profile(name);
                } else if(c.getCount() == 0) {
                    profile[0] = null;
                } else {
                    Log.i(TAG, "WARNING: more than one Profile found on user_id " + Long.toString(id));
                    profile[0] = null;
                }
            }
        };
        query.setTable(ProfileEntry.TABLE_NAME);
        query.setProjection(new String[]{ProfileEntry.COLUMN_NAME_NICK});
        query.setSelection(ProfileEntry.COLUMN_NAME_ID + " = " + Long.toString(id), null);
        query.execute();
        return profile[0];
    }

    public static Map<Long, Profile> getAll() throws CliqueDbException {
        final Map<Long, Profile> allProfiles = new HashMap<>();
        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            public void parseCursor(Cursor c) throws CliqueDbException {
                for(int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    Long id = c.getLong(c.getColumnIndexOrThrow(ProfileEntry.COLUMN_NAME_ID));
                    String name = c.getString(c.getColumnIndexOrThrow(ProfileEntry.COLUMN_NAME_NICK));
                    allProfiles.put(id, new Profile(name));
                }
            }
        };
        query.setTable(ProfileEntry.TABLE_NAME);
        query.setProjection(new String[]{DbStructure.PingEntry.COLUMN_NAME_ID, ProfileEntry.COLUMN_NAME_NICK});
        query.execute();
        return allProfiles;
    }

    public static void remove(long id) throws CliqueDbException {
        CliqueDbDelete delete = new CliqueDbDelete();
        delete.setTable(ProfileEntry.TABLE_NAME);
        delete.setWhere(ProfileEntry.COLUMN_NAME_ID + " = " + Long.toString(id), null);
        delete.execute();
    }
}
