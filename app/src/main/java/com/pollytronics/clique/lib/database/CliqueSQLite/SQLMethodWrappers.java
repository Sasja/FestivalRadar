package com.pollytronics.clique.lib.database.CliqueSQLite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.CliqueSQLite.DbStructure.CliqueDbHelper;

/**
 * These wrappers make sure all database resources are released and translate (unchecked) SQLExceptions to (checked) CliqueDbExceptions.
 */
class SQLMethodWrappers {

    static abstract class CliqueDbQuery {

        private final CliqueDbHelper cliqueDbHelper;
        private String table = null;
        private String[] projection = null;
        private String selection = null;
        private String[] selectionArgs = null;
        private String groupBy = null;
        private String having = null;
        private String orderBy = null;

        public CliqueDbQuery(CliqueDbHelper cliqueDbHelper) {
            this.cliqueDbHelper = cliqueDbHelper;
        }

        public void setTable(String table) { this.table = table; }
        public void setProjection(String[] projection) { this.projection = projection; }
        public void setSelection(String selection) { this.selection = selection; }
        public void setSelectionArgs(String[] selectionArgs) { this.selectionArgs = selectionArgs; }
        public void setGroupBy(String groupBy) { this.groupBy = groupBy; }
        public void setHaving(String having) { this.having = having; }
        public void setOrderBy(String orderBy) { this.orderBy = orderBy; }

        /**
         * Implement this method to extract necessary data from your cursor, throw a CliqueDbException with a message when something unexpected happens
         * @param c Cursor instance returned by the database query.
         * @throws CliqueDbException
         */
        abstract void parseCursor(Cursor c) throws CliqueDbException;

        /**
         * executes the sql operation, extracts the results, translates exceptions and releases resources
         * because an SQLiteException is a RunTimeException it is not checked, Catching and rethrowing makes them checked.
         * @throws CliqueDbException
         */
        void execute() throws CliqueDbException {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                db = cliqueDbHelper.getReadableDatabase();
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

    static class CliqueDbDelete {
        private final CliqueDbHelper cliqueDbHelper;
        private String table = null;
        private String where = null;
        private String[] whereArgs = null;
        private int nDeleted = 0;

        public CliqueDbDelete(CliqueDbHelper cliqueDbHelper) {
            this.cliqueDbHelper = cliqueDbHelper;
        }

        public void setTable(String table) { this.table = table; }
        public void setWhere(String where) { this.where = where; }
        public void setWhereArgs(String[] whereArgs) { this.whereArgs = whereArgs; }

        /**
         * executes the sql operation, translates exceptions and releases resources
         * because an SQLiteException is a RunTimeException it is not checked, Catching and rethrowing makes them checked.
         * @throws CliqueDbException
         */
        void execute() throws CliqueDbException {
            SQLiteDatabase db = null;
            try {
                db = cliqueDbHelper.getWritableDatabase();
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

    static class CliqueDbInsert {
        private final CliqueDbHelper cliqueDbHelper;
        private String table = null;
        private ContentValues values = null;

        public CliqueDbInsert(CliqueDbHelper cliqueDbHelper) {
            this.cliqueDbHelper = cliqueDbHelper;
        }

        public void setTable(String table) { this.table = table; }
        public void setValues(ContentValues values) { this.values = values; }

        /**
         * executes the sql operation, translates exceptions and releases resources
         * because an SQLiteException is a RunTimeException it is not checked, Catching and rethrowing makes them checked.
         * @throws CliqueDbException
         */
        void execute() throws CliqueDbException {
            SQLiteDatabase db = null;
            try {
                db = cliqueDbHelper.getWritableDatabase();
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
}
