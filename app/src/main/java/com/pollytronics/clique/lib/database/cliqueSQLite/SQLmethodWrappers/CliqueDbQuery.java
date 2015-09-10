package com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.CliqueSQLite;

/**
 * These wrappers make sure all database resources are released and translate (unchecked) SQLExceptions to (checked) CliqueDbExceptions.
 * TODO: (optimize) check if it is ok to getReadableDatabase() and close() all the time, should it be open all the time and close once?
 */

public abstract class CliqueDbQuery {
    private String table = null;
    private String[] projection = null;
    private String selection = null;
    private String[] selectionArgs = null;
    private String groupBy = null;
    private String having = null;
    private String orderBy = null;

    public void setTable(String table) { this.table = table; }
    public void setProjection(String[] projection) { this.projection = projection; }
    public void setSelection(String selection, String[] selectionArgs) {
        this.selection = selection;
        this.selectionArgs = selectionArgs;
    }
    public void setGroupBy(String groupBy) { this.groupBy = groupBy; }
    public void setHaving(String having) { this.having = having; }
    public void setOrderBy(String orderBy) { this.orderBy = orderBy; }

    /**
     * Implement this method to extract necessary data from your cursor, throw a CliqueDbException with a message when something unexpected happens
     * @param c Cursor instance returned by the database query.
     * @throws CliqueDbException
     */
    public abstract void parseCursor(Cursor c) throws CliqueDbException;

    /**
     * executes the sql operation, extracts the results, translates exceptions and releases resources
     * because an SQLiteException is a RunTimeException it is not checked, Catching and rethrowing makes them checked.
     * @throws CliqueDbException
     */
    public void execute() throws CliqueDbException {
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = CliqueSQLite.getCliqueDbHelper().getReadableDatabase();
            c = db.query(table, projection, selection, selectionArgs, groupBy, having, orderBy);
            parseCursor(c);
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new CliqueDbException();
        } finally {
            if (c != null) c.close();
            if (db != null) db.close();
        }
    }
}
