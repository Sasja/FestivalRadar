package com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.CliqueSQLite;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure;

/**
 * Created by pollywog on 9/10/15.
 */
public class CliqueDbRecreate {
    public void execute () throws CliqueDbException {
        SQLiteDatabase db = null;
        try {
            db = CliqueSQLite.getCliqueDbHelper().getWritableDatabase();
            DbStructure.CliqueDbHelper.recreateCliqueDb(db);
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new CliqueDbException();
        } finally {
            if (db != null) db.close();
        }
    }
}
