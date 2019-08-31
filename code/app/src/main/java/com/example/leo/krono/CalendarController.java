package com.example.leo.krono;

/**
 * Created by guptaji on 2/10/17.
 */

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

//this class has methods for creating and updating calendars
//handle with extreme care, dealing with content providers is VERY prone to throw exceptions
public class CalendarController {
    public static final String ACCOUNT_NAME = "Krono";

    public static final String ACCOUNT_TYPE = "org.sufficientlysecure.localcalendar.account";//, this works, but look up naming conventions if you have time

    private static final Account ACCOUNT = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);

    private static final String INT_NAME_PREFIX = "local_";

    private static Uri buildCalUri() {  //builds a URI to the calendar table
        return CalendarContract.Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE).build();
    }

    private static ContentValues buildContentValues(String displayName, int color) {
        String intName = INT_NAME_PREFIX + displayName;
        final ContentValues cv = new ContentValues();
        cv.put(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME);
        cv.put(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE);
        cv.put(CalendarContract.Calendars.NAME, intName);
        cv.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, displayName);
        cv.put(CalendarContract.Calendars.CALENDAR_COLOR, color);
        cv.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        cv.put(CalendarContract.Calendars.OWNER_ACCOUNT, ACCOUNT_NAME);
        cv.put(CalendarContract.Calendars.VISIBLE, 1);
        cv.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        return cv;
    }

    /**
     * Add calendar with given name and color
     */
    public static void addCalendar(Context context, String displayName, int color,
                                   final ContentResolver cr) {
        if (displayName == null) {
            throw new IllegalArgumentException();
        }

        /*
         * On Android < 4.1 create an account for our calendars. Using ACCOUNT_TYPE_LOCAL would
         * cause these bugs:
         *
         * - On Android < 4.1: Selecting "Calendars to sync" in the calendar app it crashes with
         * NullPointerException. see http://code.google.com/p/android/issues/detail?id=27474
         *
         * - On Android <= 2.3: Opening the calendar app will ask to create an account first even
         * when local calendars are present
         */
        if(!checkAccount(context)) {
            if (addAccount(context)) {
//                Log.d(Constants.TAG, "Account was added!");

                // wait until account is added asynchronously
                try {
                    Thread.sleep(1500);
//                    Log.d(Constants.TAG, "after wait...");
                } catch (InterruptedException e) {
//                    Log.e(Constants.TAG, "InterruptedException", e);
                }
            } else {
//                Log.e(Constants.TAG, "There was a problem when trying to add the account!");
                return;
            }
        }

        // Add calendar
        final ContentValues cv = buildContentValues(displayName, color);
        Uri resultUri = cr.insert(buildCalUri(), cv);
        if (resultUri == null) {
            throw new IllegalArgumentException();
        }

        final String[] projection = {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.NAME
        };
        final String selection = CalendarContract.Calendars.NAME + " = ?";
        Cursor cursor = cr.query(buildCalUri(), projection, selection,
                new String[]{cv.getAsString(CalendarContract.Calendars.NAME)}, null);
        try {
            if (cursor == null || !cursor.moveToFirst()) {
//                Log.e(Constants.TAG, "Query is empty after insert! AppOps disallows access to read or write calendar?");
                throw new IllegalArgumentException();
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    /**
     * Add account to Android system
     */
    private static boolean addAccount(Context context) {
        if(checkAccount(context)) {
            //Log.i(Constants.TAG, "Account already exists!");
            return true;
        }
        else {
            //Log.d(Constants.TAG, "Adding account...");

            AccountManager am = AccountManager.get(context);
            if (am.addAccountExplicitly(CalendarController.ACCOUNT, null, null)) {
                //TODO: try to enable sync and see what happens
                //TODO: will need to do everything with sync adapters then
                // EXPLICITLY disable web sync
                ContentResolver.setSyncAutomatically(ACCOUNT, CalendarContract.AUTHORITY, false);
                ContentResolver.setIsSyncable(ACCOUNT, CalendarContract.AUTHORITY, 0);

                return true;
            } else {
                return false;
            }
        }
    }
    //check if account already exists on device
    public static boolean checkAccount(Context context) {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccounts();
        for (Account account : accounts) {
            if (account.name.equals(ACCOUNT_NAME))
                return true;
        }
        return false;
    }

    /**
     * Update values of existing calendar with id
     */
    public static void updateCalendar(long id, String displayName, int color, ContentResolver cr) {
        Uri calUri = ContentUris.withAppendedId(buildCalUri(), id);
        ContentValues cv = buildContentValues(displayName, color);
        cr.update(calUri, cv, null, null);
    }
}
