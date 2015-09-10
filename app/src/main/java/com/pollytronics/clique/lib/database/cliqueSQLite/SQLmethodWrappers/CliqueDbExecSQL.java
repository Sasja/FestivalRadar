package com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.CliqueSQLite;

public class CliqueDbExecSQL {

    private String sqlQuery = null;

    public void setSqlQuery(String sqlQuery) { this.sqlQuery = sqlQuery; }

    /**
     * executes the sql operation, extracts the results, translates exceptions and releases resources
     * because an SQLiteException is a RunTimeException it is not checked, Catching and rethrowing makes them checked.
     * @throws CliqueDbException
     */
    public void execute() throws CliqueDbException {
        SQLiteDatabase db = null;
        try {
            db = CliqueSQLite.getCliqueDbHelper().getWritableDatabase();
            db.execSQL(sqlQuery);
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new CliqueDbException();
        } finally {
            if (db != null) db.close();
        }
    }
}
