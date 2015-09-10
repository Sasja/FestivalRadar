package com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.CliqueSQLite;

public class CliqueDbDelete {
    private String table = null;
    private String where = null;
    private String[] whereArgs = null;
    private int nDeleted = 0;

    public void setTable(String table) { this.table = table; }
    public void setWhere(String where, String[] whereArgs) {
        this.where = where;
        this.whereArgs = whereArgs;
    }

    /**
     * executes the sql operation, translates exceptions and releases resources
     * because an SQLiteException is a RunTimeException it is not checked, Catching and rethrowing makes them checked.
     * @throws CliqueDbException
     */
    public void execute() throws CliqueDbException {
        SQLiteDatabase db = null;
        try {
            db = CliqueSQLite.getCliqueDbHelper().getWritableDatabase();
            nDeleted = db.delete(table, where, whereArgs);
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new CliqueDbException();
        } finally {
            if (db != null) db.close();
        }
    }

    public int getnDeleted() { return nDeleted; }
}
