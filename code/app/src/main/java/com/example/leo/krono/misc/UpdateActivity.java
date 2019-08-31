package com.example.leo.krono.misc;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.leo.krono.CalendarController;
import com.example.leo.krono.EventHandler;

import java.util.HashMap;

//activity that updates calendars with new colors
public class UpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        EventHandler.populateIDS(getBaseContext());
        HashMap<String, Long> ids = EventHandler.CalIDS;
        CalendarController.updateCalendar(ids.get("Classes"),"Classes",sharedPref.getInt("color_classe",100),getContentResolver());
        CalendarController.updateCalendar(ids.get("Labs"),"Labs",sharedPref.getInt("color_lab",100),getContentResolver());
        CalendarController.updateCalendar(ids.get("Exams"),"Exams",sharedPref.getInt("color_exam",100),getContentResolver());
        CalendarController.updateCalendar(ids.get("Tutorials"),"Tutorials",sharedPref.getInt("color_tutorial",100),getContentResolver());
        CalendarController.updateCalendar(ids.get("Reminders"),"Reminders",sharedPref.getInt("color_reminder",100),getContentResolver());
        CalendarController.updateCalendar(ids.get("Deadlines"),"Deadlines",sharedPref.getInt("color_deadline",100),getContentResolver());
        CalendarController.updateCalendar(ids.get("Holidays"),"Holidays",sharedPref.getInt("color_holiday",100),getContentResolver());
        CalendarController.updateCalendar(ids.get("Events"),"Events",sharedPref.getInt("color_event",100),getContentResolver());
        Toast toast = Toast.makeText(getBaseContext(), "Updated calendar colors!", Toast.LENGTH_SHORT);
        toast.show();
        finish();
    }
}
