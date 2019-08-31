package com.example.leo.krono.activities;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.recurrencepicker.EventRecurrence;
import com.codetroopers.betterpickers.recurrencepicker.EventRecurrenceFormatter;
import com.example.leo.krono.CalendarController;
import com.example.leo.krono.R;
import com.pepperonas.materialdialog.MaterialDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.provider.BaseColumns._ID;
import static android.provider.CalendarContract.Events.CALENDAR_ID;
import static android.provider.CalendarContract.Events.CONTENT_URI;
import static android.provider.CalendarContract.Events.DTSTART;
import static android.provider.CalendarContract.Events.ORIGINAL_INSTANCE_TIME;
import static android.provider.CalendarContract.Events.RRULE;
import static android.provider.CalendarContract.Events.STATUS;
import static android.provider.CalendarContract.Events.STATUS_CANCELED;
import static com.example.leo.krono.EventHandler.CalIDS;
import static com.example.leo.krono.EventHandler.getKeyFromValue;
import static java.lang.Math.abs;

//TODO: deleting weekly should delete by name??
//TODO: delete as sync adapter?
//activity that shows the details associated with a specific event
public class ViewEvent extends AppCompatActivity {
    //choices for deleting recurring events
    final String[] delete_options=new String[] {"This event", "This and following events", "All events"};
    //various other variable declarations
    private final SimpleDateFormat spf=new SimpleDateFormat("EEE, d MMM yyyy 'at' HH:mm" , Locale.ENGLISH);
    private long id;
    private String title="";
    private String desc="";
    private String syncid;
    public static int choice;
    private int min;
    private long dtstart;
    private String cal="";
    private String venue="";
    private String rule="";
    Calendar start=null;
    Calendar end=null;
    private long calid=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set default choice to deleting all events
        choice=2;
        super.onCreate(savedInstanceState);
        //get data items from calling intent
        Intent i=getIntent();
        id = i.getLongExtra("ID", -1);
        if(id==-1) {
            finish();
        }
        setContentView(R.layout.activity_view_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            title = i.getStringExtra("Ti");
            venue = i.getStringExtra("Ve");
            start = (Calendar) i.getSerializableExtra("St");
            end = (Calendar) i.getSerializableExtra("En");
            if(getSupportActionBar()!=null) {
                getSupportActionBar().setTitle(title);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //query the event table for additional information which was not present in the intent
        Cursor cursor=null;
        try{
            cursor = getBaseContext().getContentResolver()
                    .query(
                            CalendarContract.Events.CONTENT_URI,
                            new String[] {RRULE,CALENDAR_ID, CalendarContract.Events._SYNC_ID,DTSTART,
                                    CalendarContract.Events.DESCRIPTION}, _ID+"=?",
                            new String[] {Long.toString(id)}, null);
        }
        catch(SecurityException e){
            e.printStackTrace();
        }
        if(cursor==null) {
        }
        else if (cursor.getCount()==0) {
            cursor.close();
        }
        else{
            cursor.moveToFirst();
            rule=(cursor.getString(0)==null) ? "" : cursor.getString(0);
            calid=cursor.getLong(1);
            syncid=cursor.getString(2);
            dtstart=cursor.getLong(3);
            desc=(cursor.getString(4)==null) ? "" : cursor.getString(4);
            cal=(String) getKeyFromValue(CalIDS,calid);
            min=Integer.parseInt(sharedPref.getString("minute_"+cal.toLowerCase().substring(0,cal.length()-1),"-1"));
            cursor.close();
        }

        //set various views and their visibility according to availability of data
        if(!desc.equals("")) {
            findViewById(R.id.eview_desc).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView4).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.eview_desc)).setText(desc);
        }
        else{
            findViewById(R.id.eview_desc).setVisibility(View.GONE);
            findViewById(R.id.imageView4).setVisibility(View.GONE);
        }
        if(!venue.equals("")) {

            findViewById(R.id.eview_venue).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView5).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.eview_venue)).setText(venue);
        }
        else{
            findViewById(R.id.eview_venue).setVisibility(View.GONE);
            findViewById(R.id.imageView5).setVisibility(View.GONE);
        }
        ((TextView)findViewById(R.id.eview_start)).setText(spf.format(start.getTime()));
        if(cal.equals("Reminders")||cal.equals("Deadlines")){
            findViewById(R.id.eview_end).setVisibility(View.GONE);
            findViewById(R.id.imageView7).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.eview_end).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView7).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.eview_end)).setText(spf.format(end.getTime()));
        }
        if(min!=-1) {
            findViewById(R.id.eview_min).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView10).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.eview_min)).setText(min + " minutes before");
        }
        else{
            findViewById(R.id.eview_min).setVisibility(View.GONE);
            findViewById(R.id.imageView10).setVisibility(View.GONE);
        }
        ((TextView)findViewById(R.id.eview_cal)).setText(cal);
        if(!rule.equals("")) {
            EventRecurrence r = new EventRecurrence();
            r.parse(rule);
            String repeatString = EventRecurrenceFormatter.getRepeatString(this, getResources(), r, true);
            findViewById(R.id.eview_repeat).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView6).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.eview_repeat)).setText("Repeats "+repeatString);
        }
        else{
            findViewById(R.id.eview_repeat).setVisibility(View.GONE);
            findViewById(R.id.imageView6).setVisibility(View.GONE);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.eview, menu);
        return true;
    }
    //listen to action bar item selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.eview_delete:
                if(rule==null||rule.equals("")){
                    //show dialog with choices for deleting non-recurring event
                    new MaterialDialog.Builder(this)
                            .message("Are you sure you want to delete this?")
                            .positiveText("YES")
                            .dim(50)
                            .negativeText("NO")
                            .positiveColor(R.color.green_700)
                            .neutralColor(R.color.yellow_700)
                            .negativeColor(R.color.pink_700)
                            .showListener(new MaterialDialog.ShowListener() {
                                @Override
                                public void onShow(AlertDialog d) {
                                    super.onShow(d);
                                }
                            })
                            .dismissListener(new MaterialDialog.DismissListener() {
                                @Override
                                public void onDismiss() {
                                    super.onDismiss();
                                }
                            })
                            .buttonCallback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    //delete event if user confirms
                                    Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id);
                                    getContentResolver().delete(deleteUri, null, null);
                                    finish();
                                    Toast.makeText(ViewEvent.this,"Deleting...", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                    //do nothing if user says no
                                }
                            })
                            .show();
                }
                else {
                    //show choices for deleting recurring event
                    new MaterialDialog.Builder(this)
                            .title("Delete Recurring Event")
                            .message(null)
                            .dim(50)
                            .positiveText("DELETE")
                            .negativeText("CANCEL")
                            .positiveColor(R.color.green_700)
                            .negativeColor(R.color.pink_700)
                            .listItemsSingleSelection(false, delete_options)
                            .selection(2)
                            .itemClickListener(new MaterialDialog.ItemClickListener() {
                                @Override
                                public void onClick(View v, int position, long id) {
                                    super.onClick(v, position, id);
                                    //set choice according to user input
                                    switch(position){
                                        case 0:
                                            choice=0;
                                            break;
                                        case 1:
                                            choice=1;
                                            break;
                                        case 2:
                                            choice=2;
                                            break;
                                    }
                                }
                            })
                            .buttonCallback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    switch(choice){
                                        case 0:
                                            //create an exception event if only one instance is to be deleted
                                            //exception event is set as 'cancelled' and thus overrides the specified instance
                                            ContentValues values=new ContentValues();
                                            values.put(ORIGINAL_INSTANCE_TIME,start.getTimeInMillis());
                                            values.put(STATUS,STATUS_CANCELED);
                                            values.put(CalendarContract.Events.CALENDAR_ID, calid);
                                            values.put(CalendarContract.Events.ORIGINAL_SYNC_ID, syncid);
                                            Uri u=CONTENT_URI.buildUpon() .appendQueryParameter
                                                    (android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
                                                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CalendarController.ACCOUNT_NAME)
                                                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarController.ACCOUNT_TYPE).build();
                                            getContentResolver().insert(u, values);
                                            finish();
                                            Toast.makeText(ViewEvent.this,"Deleting...", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 1:
                                            // If we are deleting the first event in the series and all
                                            // following events, then delete them all.
                                            if (abs(dtstart- start.getTimeInMillis())<2000) //setting a tolerance of 2 seconds
                                            {                                                  //coz exact times dont match for some reason
                                                Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id);
                                                getContentResolver().delete(uri,null,null);
                                                finish();
                                                Toast.makeText(ViewEvent.this,"Deleting...", Toast.LENGTH_SHORT).show();
                                                break;
                                            }
                                            // Modify the repeating event to end just before this event time
                                            EventRecurrence eventRecurrence = new EventRecurrence();
                                            eventRecurrence.parse(rule);
                                            Time date = new Time();
                                            date.set(start.getTimeInMillis());
                                            date.second--;
                                            date.normalize(false);
                                            // Android calendar seems to require the UNTIL string to be in UTC
                                            date.switchTimezone(Time.TIMEZONE_UTC);
                                            eventRecurrence.until = date.format2445();

                                            values = new ContentValues();
                                            values.put(CalendarContract.Events.DTSTART, dtstart);
                                            values.put(CalendarContract.Events.RRULE, eventRecurrence.toString());
                                            Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id);
                                            getContentResolver().update(uri,values,null,null);
                                            finish();
                                            Toast.makeText(ViewEvent.this,"Deleting...", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 2:
                                            //delete the event simply if all events are to be deleted
                                            Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id);
                                            getContentResolver().delete(deleteUri, null, null);
                                            finish();
                                            Toast.makeText(ViewEvent.this,"Deleting...", Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                }
                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                    //do nothing if user says Cancel
                                }
                            })
                            .show();
                }
                break;
            case R.id.eview_edit:
                Intent i=new Intent(getBaseContext(),EditEvent.class);
                long duration = end.getTimeInMillis() - start.getTimeInMillis();
                //put data in intent and call the edit activity
                i.putExtra("ID",id);
                i.putExtra("Ti",title);
                i.putExtra("De",desc);
                i.putExtra("Ve",venue);
                i.putExtra("Ru",rule);
                i.putExtra("Dts",dtstart);
                i.putExtra("Du", duration);
                startActivityForResult(i,101);
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
    //callback from edit activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(resultCode){
            case 0: //edits were not made, so do nothing
                break;
            case 1:
                finish(); // return to main activity if edits were made
                break;
        }
    }
}
