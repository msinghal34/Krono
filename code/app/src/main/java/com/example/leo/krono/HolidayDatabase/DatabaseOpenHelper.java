package com.example.leo.krono.HolidayDatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by root on 24/10/17.
 */

//class to open a connection to the database (derives from SQLiteAssetHelper)
public class DatabaseOpenHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "hol.db"; //name of database
    private static final int DATABASE_VERSION = 1; //database version, update it when the database is changed otherwise android won't reload it

    @Override
    public void setForcedUpgrade() {
        super.setForcedUpgrade();
    }
    //since this is a read-only database, I'll force android to completely overwrite when a new version of the DB is encountered
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        setForcedUpgrade();
    }

    DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}