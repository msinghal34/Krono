package com.example.leo.krono.misc;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.leo.krono.CalendarController;
import com.example.leo.krono.EventHandler;
import com.example.leo.krono.HolidayDatabase.DatabaseAccessHelper;

import java.util.Calendar;
import java.util.List;

import static android.provider.CalendarContract.Events.ALL_DAY;
import static android.provider.CalendarContract.Events.CALENDAR_ID;
import static android.provider.CalendarContract.Events.CONTENT_URI;
import static android.provider.CalendarContract.Events.EVENT_TIMEZONE;
import static android.provider.CalendarContract.Events.TITLE;
import static android.provider.CalendarContract.Reminders.EVENT_ID;
import static android.provider.CalendarContract.Reminders.METHOD;
import static android.provider.CalendarContract.Reminders.MINUTES;
import static com.example.leo.krono.CalendarController.ACCOUNT_NAME;
import static com.example.leo.krono.CalendarController.ACCOUNT_TYPE;
import static com.example.leo.krono.EventHandler.CalIDS;
import static com.example.leo.krono.EventHandler.populateIDS;
import static java.lang.Integer.parseInt;
import static java.lang.Thread.sleep;

/**
 * Created by guptaji on 2/10/17.
 */
//an empty activity that creates user account for the app and adds calendars on the first run, and disappears (although it doesn't 'appear' in the first place)
public class AddCalAct extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(!CalendarController.checkAccount(getBaseContext())) {
            addCalendars(getBaseContext());
        }
        finish();
    }
    private void addCalendars(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        //try adding calendars
        try {
            CalendarController.addCalendar(context,"Classes" ,sharedPref.getInt("color_classe",100), getContentResolver());
            CalendarController.addCalendar(context,"Tutorials" ,sharedPref.getInt("color_tutorial",100), getContentResolver());
            CalendarController.addCalendar(context,"Labs" , sharedPref.getInt("color_lab",100), getContentResolver());
            CalendarController.addCalendar(context,"Reminders" ,sharedPref.getInt("color_reminder",100), getContentResolver());
            CalendarController.addCalendar(context,"Deadlines" , sharedPref.getInt("color_deadline",100), getContentResolver());
            CalendarController.addCalendar(context,"Events" ,sharedPref.getInt("color_event",100), getContentResolver());
            CalendarController.addCalendar(context,"Holidays" , sharedPref.getInt("color_holiday",100), getContentResolver());
            CalendarController.addCalendar(context,"Exams" , sharedPref.getInt("color_exam",100), getContentResolver());
        } catch (Exception e) {
            showMessageAndFinish();
        }
        //wait 210 ms
        try {
            sleep(210);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //populate the id-calendar hashmap
        EventHandler.populateIDS(getBaseContext());
        //wait 210 ms
        try {
            sleep(210);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //now try to add holidays
        DatabaseAccessHelper databaseAccess = DatabaseAccessHelper.getInstance(this);
        databaseAccess.open();
        List<String[]> holidays= databaseAccess.getHolidays();
        databaseAccess.close();
        for(String[] h:holidays){
            populateIDS(getBaseContext());
            Uri uri=CONTENT_URI.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE).build();
            ContentResolver cr = getBaseContext().getContentResolver();
            ContentValues values = new ContentValues();
            Calendar cal = Calendar.getInstance();
            cal.set(parseInt(h[1].substring(6,10)), parseInt(h[1].substring(3,5)) - 1, parseInt(h[1].substring(0,2)));
            values.put(CalendarContract.Events.DTSTART,cal.getTimeInMillis());
            values.put(CALENDAR_ID, CalIDS.get("Holidays"));
            values.put(CalendarContract.Events._SYNC_ID,System.currentTimeMillis()+"");
            values.put(TITLE, h[0]);
            values.put(ALL_DAY, 1);
            values.put(EVENT_TIMEZONE, "Asia/Calcutta");
            Uri event = null;
            //permission check to make android studio happy
            if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                Toast toast = Toast.makeText(getBaseContext(),"Don't have access to calendar!",Toast.LENGTH_SHORT);
                toast.show();
            }
            else
                event = cr.insert(uri, values);
            // reminder insert
            values = new ContentValues();
            assert event != null;
            values.put( EVENT_ID, Long.parseLong(event.getLastPathSegment()));
            values.put( METHOD, 1);        //method=1 means that only notif will show up, set to 4 if you want an alarm instead of a notif
            values.put( MINUTES, 30 );   //set alert time to 11:30 PM on the previous day
            cr.insert(CalendarContract.Reminders.CONTENT_URI, values);
        }
        finish();
    }

    private void showMessageAndFinish() {
        Toast toast = Toast.makeText(getBaseContext(), "Failed to add calendars!", Toast.LENGTH_LONG);
        toast.show();
        finish();
    }
}