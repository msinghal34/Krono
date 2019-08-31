package com.example.leo.krono.activities;

import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekViewEvent;
import com.example.leo.krono.EventHandler;
import com.example.leo.krono.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by guptaji on 10/10/17.
 */
//activity to show the week overview screen
public class WeekView extends AppCompatActivity
        implements MonthLoader.MonthChangeListener,
            com.alamkanak.weekview.WeekView.EventClickListener,
                    com.alamkanak.weekview.WeekView.EventLongPressListener{
    //interprets dates and displays small dates and day of week in the header
    private DateTimeInterpreter dateint = new DateTimeInterpreter() {
        @Override
        public String interpretDate(Calendar date) {
            SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);
            String weekday = weekdayNameFormat.format(date.getTime());
            SimpleDateFormat format = new SimpleDateFormat(" d/M", Locale.ENGLISH);
            weekday = String.valueOf(weekday.charAt(0));
            return weekday.toUpperCase() + format.format(date.getTime());
        }

        @Override
        public String interpretTime(int hour) {
            return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.week_view);
        }
    @Override
    public void onResume(){
        super.onResume();
        //on resume, notify the view that event data might have changed, so refresh
        ((com.alamkanak.weekview.WeekView)findViewById(R.id.weeeek)).notifyDatasetChanged();
        onRestart();
    }
    @Override
    public void onRestart(){
        super.onRestart();
        //set listeners on the view
        com.alamkanak.weekview.WeekView mWeekView = (com.alamkanak.weekview.WeekView) findViewById(R.id.weeeek);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setMonthChangeListener(this);
        mWeekView.setEventLongPressListener(this);
        mWeekView.setDateTimeInterpreter(dateint);
    }
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        //call view event activity on clicking an event
        Intent intent = new Intent(getBaseContext(), ViewEvent.class);
        intent.putExtra("ID", event.getId());
        intent.putExtra("Ti", event.getName());
        intent.putExtra("Ve", event.getLocation());
        intent.putExtra("St", event.getStartTime());
        intent.putExtra("En", event.getEndTime());
        startActivity(intent);
    }
    //shows event title in a toast on long pressing
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(getBaseContext(), event.getName(), Toast.LENGTH_SHORT).show();
    }
    //this method loads event instances of 3 months into the view
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        ArrayList<WeekViewEvent> eventslist = new ArrayList<>();
        Calendar start=Calendar.getInstance();
        Calendar end=Calendar.getInstance();
        //gets events from start of previous month to end of next month
        start.set(newYear,newMonth-1,1,0,0,0);
        end.set(newYear,newMonth-1,numberOfDaysInMonth(newMonth-1,newYear),23,59,59);
        EventHandler.getInstances(getBaseContext(),start,end,eventslist);
        return eventslist;
    }
    //method to return number of days in a month
    public static int numberOfDaysInMonth(int month, int year) {
        Calendar monthStart = new GregorianCalendar(year, month, 1);
        return monthStart.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}