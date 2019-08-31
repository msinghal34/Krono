package com.example.leo.krono.CourseDatabase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guptaji on 15/10/17.
 */

//helper class to access the stored course database
public class DatabaseAccessHelper {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccessHelper instance;

    /**
     * Private constructor to avoid object creation from outside classes.
     */
    private DatabaseAccessHelper(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    /**
     * Return a singleton instance of DatabaseAccessHelper.
     *
     * @param context the Context
     * @return the instance of DabaseAccessHelper
     */
    public static DatabaseAccessHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccessHelper(context);
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
     * Read all course from the database.
     *
     * @return a List of courses.
     */
    public List<String[]> getCourses() {
        List<String[]> list = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM courses", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String venue=(cursor.getString(2)==null)? "":cursor.getString(2).trim();
            String slots=(cursor.getString(3)==null)? "":cursor.getString(3).trim();
            list.add(new String[] {cursor.getString(0),cursor.getString(1),venue,slots});
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }
}