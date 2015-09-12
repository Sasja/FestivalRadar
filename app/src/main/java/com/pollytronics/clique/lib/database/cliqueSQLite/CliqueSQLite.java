package com.pollytronics.clique.lib.database.cliqueSQLite;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.pollytronics.clique.lib.database.CliqueDbException;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.CliqueDbHelper;
import com.pollytronics.clique.lib.database.cliqueSQLite.DbStructure.SyncStatus;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbExecSQL;
import com.pollytronics.clique.lib.database.cliqueSQLite.SQLmethodWrappers.CliqueDbQuery;

/**
 * A class that implements the CliqueDb_Interface4Local using a SQLite database on the mobile device
 * This class should only be used directly for instantiation with getInstance(), use the interface for all other uses.
 * the static (Class) method getInstance will return the available instance or create it when necessary.
 * It uses the singleton design pattern: http://www.javaworld.com/article/2073352/core-java/simply-singleton.html
 *
 * it is not threadsafe so should only be accessed from one thread (the main thread or UI thread)
 *
 * TODO: (optimize) is it still okay to do all this database stuff sync on the main thread? not really according to developer.android.com but it works in practice
 * TODO: (errorhandling) i put try catch everywhere in the code where the interface methods are used to get it working for now, take a day to clean up and do proper error handling
 * TODO: (syncing) deleting a contact from the database should probably also get rid of the associated blips
 * TODO: (syncing) clean up the database now and then to maintain a maximum number of blips per user
 */
public final class CliqueSQLite {
    public static final String DATABASE_NAME = "Clique.db";
    public static final int DATABASE_VERSION = 14;   // increasing this will wipe all local databases on update
    private static final String TAG="CliqueSQLite";

    private static CliqueDbHelper cliqueDbHelper = null;
    private static Context context = null;

    public static CliqueDbHelper getCliqueDbHelper() {
        if(cliqueDbHelper == null) {
            throw new Error("CliqueSQLite class must be initialised before use using CliqueSQLite.init()");
        }
        return cliqueDbHelper;
    }

    public static void init(Context context) {
        CliqueSQLite.context = context;
        if(CliqueSQLite.cliqueDbHelper == null) CliqueSQLite.cliqueDbHelper = new CliqueDbHelper(context);
    }

    public static double getLastSync() throws CliqueDbException {
        final double[] lastsync = new double[1];
        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            public void parseCursor(Cursor c) throws CliqueDbException {
                if(c.getCount() != 1) {
                    Log.i(TAG, "WARNING: there is no, or more than one lastsync entry in the local database!!! (" + c.getCount() + ")");
                }
                c.moveToFirst();
                lastsync[0] = c.getDouble(c.getColumnIndexOrThrow(SyncStatus.COLUMN_NAME_LASTSYNC));
            }
        };
        query.setTable(SyncStatus.TABLE_NAME);
        query.setProjection(new String[]{SyncStatus.COLUMN_NAME_LASTSYNC,});
        query.execute();
        return lastsync[0];
    }

    public static void setLastSync(double lastSync) throws CliqueDbException {
        CliqueDbExecSQL sqlQuery = new CliqueDbExecSQL();
        sqlQuery.setSqlQuery("UPDATE " + SyncStatus.TABLE_NAME + " SET " +
                SyncStatus.COLUMN_NAME_LASTSYNC + " = " + lastSync);
        sqlQuery.execute();
    }

    public static void increaseGlobalDirtyCounter() throws CliqueDbException {
        CliqueDbExecSQL sqlQuery = new CliqueDbExecSQL();
        sqlQuery.setSqlQuery("UPDATE " + SyncStatus.TABLE_NAME + " SET " +
                SyncStatus.COLUMN_NAME_GLOBALDIRTYCOUNTER + " = " +
                SyncStatus.COLUMN_NAME_GLOBALDIRTYCOUNTER + " + 1");
        sqlQuery.execute();
    }

    public static long getGlobalDirtyCounter() throws CliqueDbException {
        final long[] counter = new long[1];
        CliqueDbQuery query = new CliqueDbQuery() {
            @Override
            public void parseCursor(Cursor c) throws CliqueDbException {
                if(c.getCount() != 1) {
                    Log.i(TAG, "WARNING: there is no, or more than one globaldirtycounter entry in the local database!!! (" + c.getCount() + ")");
                }
                c.moveToFirst();
                counter[0] = c.getLong(c.getColumnIndexOrThrow(SyncStatus.COLUMN_NAME_GLOBALDIRTYCOUNTER));
            }
        };
        query.setTable(SyncStatus.TABLE_NAME);
        query.setProjection(new String[]{SyncStatus.COLUMN_NAME_GLOBALDIRTYCOUNTER,});
        query.execute();
        return counter[0];
    }
}
