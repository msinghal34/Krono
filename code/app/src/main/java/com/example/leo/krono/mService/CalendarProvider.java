package com.example.leo.krono.mService;

/**
 * Created by guptaji on 10/10/17.
 */

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Instances;
import android.support.v4.content.ContextCompat;

import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
//class similar to CalendarController, with methods that are used by the service
class CalendarProvider {

    private Context context;
    CalendarProvider(Context context) {
        this.context = context;
    }

    // Projection for event queries
    private static final String[] INSTANCE_PROJECTION = new String[] {
            Instances.TITLE,
            Instances.BEGIN,
            Instances.END,
    };

    private static final int INSTANCE_PROJECTION_TITLE_INDEX = 0;
    private static final int INSTANCE_PROJECTION_BEGIN_INDEX = 1;
    private static final int INSTANCE_PROJECTION_END_INDEX = 2;

    /**
     * Get the last calendars fetched by listCalendar
     * @return Calendars in memory
     */

    /**
     * List the user's calendars
     * @return All calendars of the user, null if they could not be read
     */


    private Uri getInstancesQueryUri() {
        // Event search window : from one month before to one month after, to be sure
        GregorianCalendar dateDebut = new GregorianCalendar();
        dateDebut.add(GregorianCalendar.MONTH, -1);
        GregorianCalendar dateFin = new GregorianCalendar();
        dateFin.add(GregorianCalendar.MONTH, 1);

        // search URI (contains the search window)
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, dateDebut.getTimeInMillis());
        ContentUris.appendId(builder, dateFin.getTimeInMillis());

        return builder.build();
    }

    /**
     * Get the current event in one of the calendars set in the preferences
     * @param currentTime Time at which the event should be searched
     * @return The first event found, or null if there is none
     */
    CalendarEvent getCurrentEvent(long currentTime) {
        ContentResolver cr = context.getContentResolver();

        // Make the calendar ID selection string
        String calIdsSelect = getEventCalendarIdsSelectString();

        if(calIdsSelect.equals("")) {
            return null;
        }

        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        // Selection must be inclusive on the start time, and eclusive on the end time.
        // This way when setting an alarm at the end of the event, this moment is considered outside of the event
        String selection ="(" + calIdsSelect + ") AND " +
                 Instances.BEGIN + " <= ? AND "
                + Instances.END + " > ? AND " + Instances.ALL_DAY + " = 0";

        String strCurrentTimeEarly = String.valueOf(currentTime);
        String strCurrentTimeDelay = String.valueOf(currentTime);
        String[] selectionArgs =  new String[] { strCurrentTimeEarly, strCurrentTimeDelay };

        Cursor cur = cr.query(getInstancesQueryUri(), INSTANCE_PROJECTION, selection, selectionArgs, Instances.END); // Take the event that ends first

        CalendarEvent res;
        if(cur != null && cur.moveToNext()) {
            res = new CalendarEvent(cur.getString(INSTANCE_PROJECTION_TITLE_INDEX),
                    cur.getLong(INSTANCE_PROJECTION_BEGIN_INDEX), cur.getLong(INSTANCE_PROJECTION_END_INDEX));
        }
        else {
            res = null;
        }

        if(cur != null) {
            cur.close();
        }
        return res;
    }

    /**
     * Get the next event in the calendars set in the preferences
     * @param currentTime Time to use to search for events
     * @return The first event found, or null if there is none
     */
    CalendarEvent getNextEvent(long currentTime) {
        ContentResolver cr = context.getContentResolver();

        // Make the calendar ID selection string
        String calIdsSelect = getEventCalendarIdsSelectString();
        if(calIdsSelect.equals(""))
            return null;

        // Selection is inclusive on event start time.
        // This way we are consistent wih getCurrentEvent
        String selection = "(" + calIdsSelect + ") AND " +
                Instances.BEGIN + " >= ? AND " + Instances.ALL_DAY + " = 0";


        String strCurrentTime = String.valueOf(currentTime);
        String[] selectionArgs =  new String[] { strCurrentTime };

        Cursor cur = cr.query(getInstancesQueryUri(), INSTANCE_PROJECTION, selection, selectionArgs, Instances.BEGIN); // Sort by start time to get the first event

        CalendarEvent res;
        if(cur != null && cur.moveToNext())
            res = new CalendarEvent(cur.getString(INSTANCE_PROJECTION_TITLE_INDEX),
                    cur.getLong(INSTANCE_PROJECTION_BEGIN_INDEX), cur.getLong(INSTANCE_PROJECTION_END_INDEX));
        else {
            res = null;
        }

        if(cur != null) {
            cur.close();
        }
        return res;
    }

    /**
     * Make a WHERE clause to filter selected calendars
     * @return generated WHERE clause, or en empty string if there is no calendar selected
     */
    private String getEventCalendarIdsSelectString() {
        LinkedHashMap<Long, Boolean> checkedCalendars = PreferencesManager.getCheckedCalendars(context);

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for(long idCalendar : checkedCalendars.keySet()) {
            if(first)
                first = false;
            else
                builder.append(" OR ");

            builder.append("(").append(Instances.CALENDAR_ID).append("=").append(idCalendar).append(")");
        }
        return builder.toString();
    }
}

