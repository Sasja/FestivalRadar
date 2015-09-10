package com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.CliqueSQLite;

public class CliqueDbInsert {
    private String table = null;
    private ContentValues values = null;

    public void setTable(String table) { this.table = table; }
    public void setValues(ContentValues values) { this.values = values; }

    /**
     * executes the sql operation, translates exceptions and releases resources
     * because an SQLiteException is a RunTimeException it is not checked, Catching and rethrowing makes them checked.
     * @throws CliqueDbException
     */
    public void execute() throws CliqueDbException {
        SQLiteDatabase db = null;
        try {
            db = CliqueSQLite.getCliqueDbHelper().getWritableDatabase();
            long result = db.insertOrThrow(table, null, values);
            if (result == -1) throw new CliqueDbException("insertOrThrow returned -1");
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new CliqueDbException();
        } finally {
            if (db != null) db.close();
        }
    }
}
