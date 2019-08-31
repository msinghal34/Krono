package com.example.leo.krono;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.alamkanak.weekview.WeekViewEvent;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.provider.BaseColumns._ID;
import static android.provider.CalendarContract.Events.ALL_DAY;
import static android.provider.CalendarContract.Events.CALENDAR_ID;
import static android.provider.CalendarContract.Events.CONTENT_URI;
import static android.provider.CalendarContract.Events.DESCRIPTION;
import static android.provider.CalendarContract.Events.DTSTART;
import static android.provider.CalendarContract.Events.EVENT_LOCATION;
import static android.provider.CalendarContract.Events.EVENT_TIMEZONE;
import static android.provider.CalendarContract.Events.HAS_ALARM;
import static android.provider.CalendarContract.Events.TITLE;
import static android.provider.CalendarContract.Reminders.EVENT_ID;
import static android.provider.CalendarContract.Reminders.METHOD;
import static android.provider.CalendarContract.Reminders.MINUTES;
import static com.example.leo.krono.CalendarController.ACCOUNT_NAME;
import static com.example.leo.krono.CalendarController.ACCOUNT_TYPE;
import static java.lang.Integer.parseInt;

/**
 * Created by guptaji on 3/10/17.
 */
//put methods for adding events, reminders and other stuff in here
//NOTE: month number in the event must be passed one less than the actual month number
//so january becomes 0, february becomes 1,...etc
public class EventHandler{
    //method that converts a duration in milliseconds to a string in rfc2445 format
    public static String getDuration(long timeInMilliSeconds){
        long seconds = timeInMilliSeconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        String d=(days<10) ? "0"+days : ""+days;
        String h=(hours%24<10) ? "0"+hours %24: ""+hours%24;
        String m=(minutes%60<10) ? "0"+minutes%60: ""+minutes%60;
        String s=(seconds%60<10) ? "0"+seconds%60: ""+seconds%60;
        return"P"+ d+ "DT" +h + "H" + m+ "M" + s+"S";
    }
        private static final String[] INSTANCE_PROJECTION = new String[] {
                CalendarContract.Instances.EVENT_ID,      // 0
                CalendarContract.Instances.TITLE,          // 1
                CalendarContract.Instances.BEGIN,         // 2
                CalendarContract.Instances.END,            // 3
        };
        //class to store event details for use by getDetails() function
        private static  class Items{
            public int color;
            public String venue;
            String desc;
            Items(int c, String v, String d){
                color=c;
                venue=v;
                desc=d;
            }
        }
    public static HashMap<String, Long> CalIDS = new HashMap<>(); //Hashmap for storing Calendar names against Calendar IDs

    @SuppressLint("ObsoleteSdkInt")
    public static void populateIDS(Context context) { //updates CalIDS, call this in the beginning of any method that needs CalIDs
        Cursor cursor;                              // otherwise your method will probably throw an exception
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(context,"Don't have access to calendar!",Toast.LENGTH_SHORT);
            toast.show();
            return;
        }   
        if (android.os.Build.VERSION.SDK_INT <= 7) {
            cursor = context.getContentResolver().query(Uri.parse("content://calendar/calendars"), new String[]{"_id", "displayName"}, CalendarContract.Calendars.ACCOUNT_TYPE+"=?",
                    new String [] {ACCOUNT_TYPE}, null);

        } else if (android.os.Build.VERSION.SDK_INT <= 14) {
            cursor = context.getContentResolver().query(Uri.parse("content://com.android.calendar/calendars"),
                    new String[]{"_id", "displayName"}, CalendarContract.Calendars.ACCOUNT_TYPE+"=?",
                    new String [] {ACCOUNT_TYPE}, null);
        } else {
            cursor = context.getContentResolver().query(Uri.parse("content://com.android.calendar/calendars"),
                    new String[]{"_id", "calendar_displayName"}, CalendarContract.Calendars.ACCOUNT_TYPE+"=?",
                    new String [] {ACCOUNT_TYPE}, null);

        }

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    EventHandler.CalIDS.put(cursor.getString(1), cursor.getLong(0));
                    cursor.moveToNext();
                }
            }
        }
        try{
            if (cursor != null) {
                cursor.close();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    // general method to add an event and its associated reminder
    public static boolean addReminder(Context context, String title, String desc, String venue, String start,String end, String rule, String CAL, int minutes) {
        //call populate ids
        populateIDS(context);
        //build the content uri as a sync adapter
        Uri uri=CONTENT_URI.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE).build();
        if (start == null || start.equals("") || CAL == null || CAL.equals(""))
            throw new IllegalArgumentException();
        Calendar cal = Calendar.getInstance();
        ContentResolver cr = context.getContentResolver();
        cal.set(parseInt(start.substring(0, 4)), parseInt(start.substring(5, 7)) - 1, parseInt(start.substring(8, 10)),
                                    parseInt(start.substring(10, 12)), parseInt(start.substring(13, 15)));

        // event insert
        ContentValues values = new ContentValues();
        values.put(CALENDAR_ID, CalIDS.get(CAL));
        values.put(CalendarContract.Events._SYNC_ID,System.currentTimeMillis()+"");
        values.put(TITLE, title);
        values.put(ALL_DAY, 0);
        values.put(EVENT_TIMEZONE, "Asia/Calcutta");
        values.put(DTSTART, cal.getTimeInMillis());
        Calendar cal2=Calendar.getInstance();
        if(end==null||end.equals("")) {
            cal2.set(parseInt(start.substring(0, 4)), parseInt(start.substring(5, 7)) - 1, parseInt(start.substring(8, 10)),
                    parseInt(start.substring(10, 12)), parseInt(start.substring(13, 15)) + 30);
        }
        else {
            cal2.set(parseInt(end.substring(0, 4)), parseInt(end.substring(5, 7)) - 1, parseInt(end.substring(8, 10)),
                    parseInt(end.substring(10, 12)), parseInt(end.substring(13, 15)));
        }
        if(rule!=null&&!rule.equals("")) {
            String duration = getDuration(cal2.getTimeInMillis() - cal.getTimeInMillis());
            values.put(CalendarContract.Events.DURATION, duration);
        }
        else
            values.put(CalendarContract.Events.DTEND,cal2.getTimeInMillis());
        if(desc!=null&&!desc.equals(""))
            values.put(DESCRIPTION, desc);
        if(rule!=null)
            values.put(CalendarContract.Events.RRULE, rule);
        if(venue!=null&&!venue.equals(""))
            values.put(EVENT_LOCATION,venue);
        values.put(HAS_ALARM, 1);
        Uri event = null;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(context,"Don't have access to calendar!",Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        else
            event = cr.insert(uri, values);

        // reminder insert
        values = new ContentValues();
        assert event != null;
        values.put( EVENT_ID, Long.parseLong(event.getLastPathSegment()));
        values.put( METHOD, 1);        //method=1 means that only notif will show up, set to 4 if you want an alarm instead of a notif
        values.put( MINUTES, minutes );
        cr.insert(CalendarContract.Reminders.CONTENT_URI, values);
        return true;
    }
    //method to set up a deadline with multiple reminders
    public static boolean makeDeadline(Context context, String title, String desc, String start) {
        populateIDS(context);
        Uri uri=CONTENT_URI.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE).build();
        if (start == null || start.equals(""))
            throw new IllegalArgumentException();
        Calendar cal = Calendar.getInstance();
        ContentResolver cr = context.getContentResolver();
        cal.set(parseInt(start.substring(0, 4)), parseInt(start.substring(5, 7)) - 1, parseInt(start.substring(8, 10)),
                parseInt(start.substring(10, 12)), parseInt(start.substring(13, 15)));
        // event insert

        ContentValues values = new ContentValues();
        values.put(CALENDAR_ID, CalIDS.get("Deadlines"));
        values.put(CalendarContract.Events._SYNC_ID,System.currentTimeMillis()+"");
        values.put(TITLE, title);
        values.put(ALL_DAY, 0);
        values.put(EVENT_TIMEZONE, "Asia/Calcutta");
        values.put(DTSTART, cal.getTimeInMillis());
        Calendar cal2=Calendar.getInstance();
        cal2.set(parseInt(start.substring(0, 4)), parseInt(start.substring(5, 7)) - 1, parseInt(start.substring(8, 10)),
                    parseInt(start.substring(10, 12)), parseInt(start.substring(13, 15)) + 30);
        values.put(CalendarContract.Events.DTEND,cal2.getTimeInMillis());
        if(desc!=null&&!desc.equals(""))
            values.put(DESCRIPTION, desc);
        values.put(HAS_ALARM, 1);
        Uri event = null;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(context,"Don't have access to calendar!",Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        else
            event = cr.insert(uri, values);
        for(int minute: new int[]{1440,720,180,60}) {
            // reminders insert
            values = new ContentValues();
            assert event != null;
            values.put(EVENT_ID, Long.parseLong(event.getLastPathSegment()));
            values.put(METHOD, 1);        //method=1 means that only notif will show up, set to 4 if you want an alarm instead of a notif
            values.put(MINUTES, minute);
            cr.insert(CalendarContract.Reminders.CONTENT_URI, values);
        }
        return true;
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    //method to populate a weekview list with instances of all event in the specified time range
    public static void getInstances(Context context, Calendar start, Calendar end, ArrayList<WeekViewEvent> e){
        populateIDS(context);
        Uri instanceUri = CalendarContract.Instances.CONTENT_URI
                .buildUpon()
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CalendarController.ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
                .build();
        instanceUri= ContentUris.withAppendedId(instanceUri, start.getTimeInMillis());
        instanceUri= ContentUris.withAppendedId(instanceUri, end.getTimeInMillis());
        Cursor cursor=null;
        try{
            cursor=context.getContentResolver().query(instanceUri,INSTANCE_PROJECTION,null,null,null);
        }
        catch (Exception exc){
            getInstances(context, start, end, e);
        }
        if(cursor!=null){
            while(cursor.moveToNext()) {
                long eventid=cursor.getLong(0);
                Items i=getDetails(context,eventid);
                if(i!=null) {
                    String title = cursor.getString(1);
                    long stime = cursor.getLong(2);
                    long etime = cursor.getLong(3);
                    Calendar s = Calendar.getInstance();
                    s.setTimeInMillis(stime);
                    Calendar en = Calendar.getInstance();
                    en.setTimeInMillis(etime);
//                    Log.i(Constants.TAG,etime-stime+"");
                    WeekViewEvent w = new WeekViewEvent(eventid, title,i.venue, s, en);
                    w.setColor(i.color);
                    e.add(w);
                }
            }
            cursor.close();
            return;
        }
        else{
            return;
        }
    }
    @SuppressWarnings("UnnecessaryReturnStatement")
    //method to populate a agendaview list list with instances of all event in the specified time range
    public static void getSInstances(Context context, Calendar start, Calendar end, ArrayList<CalendarEvent> e) {
        populateIDS(context);
        Uri instanceUri = CalendarContract.Instances.CONTENT_URI
                .buildUpon()
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CalendarController.ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
                .build();
        instanceUri= ContentUris.withAppendedId(instanceUri, start.getTimeInMillis());
        instanceUri= ContentUris.withAppendedId(instanceUri, end.getTimeInMillis());
        Cursor cursor=null;
        try{
            cursor=context.getContentResolver().query(instanceUri,INSTANCE_PROJECTION,null,null,null);
        }
        catch(Exception exc){
            getSInstances(context, start, end, e);
        }
        if(cursor!=null) {
            while(cursor.moveToNext()) {
                long eventid = cursor.getLong(0);
                Items i = getDetails(context, eventid);
                if (i!= null) {
                    String title = cursor.getString(1);
                    long stime = cursor.getLong(2);
                    long etime = cursor.getLong(3);
                    Calendar s = Calendar.getInstance();
                    s.setTimeInMillis(stime);
                    Calendar en = Calendar.getInstance();
                    en.setTimeInMillis(etime);
                    CalendarEvent b = new BaseCalendarEvent(eventid,i.color,title,i.desc,i.venue,s.getTimeInMillis(),en.getTimeInMillis(),0,"");
                    e.add(b);
                }
            }
            cursor.close();
            return;
        }
        else {
            return;
        }
    }
        //method that returns the key of value from a hashmap, works fast enough because our hashmap is small (less than 20 values)
        public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }
    //get details of an event by specifying its ID
    private static Items getDetails(Context context,long id){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Cursor cursor=null;
        try{
            cursor = context.getContentResolver()
                    .query(
                            CalendarContract.Events.CONTENT_URI,
                            new String[] {CALENDAR_ID,DESCRIPTION,EVENT_LOCATION}, _ID+"=?",
                            new String[] {Long.toString(id)}, null);
        }
        catch(SQLiteException e){
            return getDetails(context, id);
        }
        catch(SecurityException e){
            e.printStackTrace();
        }
        if(cursor==null) {
            return null;
        }
        else if(cursor.getCount()==0){
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        long calid=cursor.getLong(0);
        if(CalIDS.containsValue(calid)){
            String calname = (String)getKeyFromValue(CalIDS, calid);
            String colorkey= null;
            if (calname != null) {
                colorkey = "color_"+calname.toLowerCase().substring(0,calname.length()-1);
            }
            int color = sharedPref.getInt(colorkey, 100);
            String desc=cursor.getString(1);
            String venue=cursor.getString(2);
            cursor.close();
            if(desc==null)
                desc="";
            if(venue==null)
                venue="";
            return new Items(color,venue,desc);
        }
        else
            cursor.close();
            return null;
    }
}
