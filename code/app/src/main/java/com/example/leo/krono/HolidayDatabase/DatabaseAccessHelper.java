package com.example.leo.krono.HolidayDatabase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 24/10/17.
 */
//helper class to access the stored holidays database
public class DatabaseAccessHelper {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static com.example.leo.krono.HolidayDatabase.DatabaseAccessHelper instance;

    /**
     * Private constructor to avoid object creation from outside classes.
     */
    private DatabaseAccessHelper(Context context) {
        this.openHelper = new com.example.leo.krono.HolidayDatabase.DatabaseOpenHelper(context);
    }

    /**
     * Return a singleton instance of DatabaseAccessHelper.
     *
     * @param context the Context
     * @return the instance of DatabaseAccessHelper
     */
    public static com.example.leo.krono.HolidayDatabase.DatabaseAccessHelper getInstance(Context context) {
        if (instance == null) {
            instance = new com.example.leo.krono.HolidayDatabase.DatabaseAccessHelper(context);
        }
        return instance;
    }

    /**
     * Open the database connection.
     */
    public void open() {
        this.database = openHelper.getWritableDatabase();
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }

    /**
     * Read all Holiday from the database.
     *
     * @return a List of Holidays.
     */
    public List<String[]> getHolidays() {
        List<String[]> list = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM holidays", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
           //add to list
            list.add(new String[] {cursor.getString(0).trim(),cursor.getString(1).trim()});
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }
}
