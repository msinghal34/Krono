package com.example.leo.krono.activities;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.OnDialogDismissListener;
import com.codetroopers.betterpickers.recurrencepicker.EventRecurrence;
import com.codetroopers.betterpickers.recurrencepicker.EventRecurrenceFormatter;
import com.codetroopers.betterpickers.recurrencepicker.RecurrencePickerDialogFragment;
import com.example.leo.krono.R;
import com.pepperonas.materialdialog.MaterialDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.provider.CalendarContract.Events.CONTENT_URI;
import static com.example.leo.krono.EventHandler.getDuration;
import static java.lang.Integer.parseInt;
//activity to edit events from user input
public class EditEvent extends AppCompatActivity
        implements RecurrencePickerDialogFragment.OnRecurrenceSetListener, OnDialogDismissListener {
    public static final String FRAG_TAG_RECUR_PICKER = "recurrencePickerDialogFragment";
    //variables declaration for the various fields
    public static EventRecurrence mEventRecurrence = new EventRecurrence();
    public static long id;
    public static String title="";
    public static String notes="";
    public static long dtstart;
    public static String venue="";
    public static long duration;
    public static final SimpleDateFormat spf=new SimpleDateFormat("yyyy'-'MM'-'dd", Locale.ENGLISH);
    public static final SimpleDateFormat dpf=new SimpleDateFormat("HH':'mm':00'",Locale.ENGLISH);
    public static String rule="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Editing");
        }
        //get data from calling intent
        Intent i=getIntent();
        id=i.getLongExtra("ID",-1);
        title=i.getStringExtra("Ti");
        notes=i.getStringExtra("De");
        dtstart=i.getLongExtra("Dts",-1);
        venue=i.getStringExtra("Ve");
        duration=i.getLongExtra("Du",-1);
        rule=i.getStringExtra("Ru");

        //now set various views to previous values
        ((EditText)findViewById(R.id.edit_title)).setText(title);
        if(notes==null||notes.equals("")){
            ((EditText)findViewById(R.id.edit_notes)).setText("");
        }
        else{
            ((EditText)findViewById(R.id.edit_notes)).setText(notes);
        }
        if(venue==null||venue.equals("")){
            ((EditText)findViewById(R.id.edit_venue)).setText("");
        }
        else{
            ((EditText)findViewById(R.id.edit_venue)).setText(venue);
        }
        Calendar start=Calendar.getInstance();
        start.setTimeInMillis(dtstart);
        ((TextView)findViewById(R.id.edit_sdate)).setText(spf.format(start.getTime()));
        ((TextView)findViewById(R.id.edit_stime)).setText(dpf.format(start.getTime()));
        Calendar end=Calendar.getInstance();
        end.setTimeInMillis(dtstart+duration);
        ((TextView)findViewById(R.id.edit_etime)).setText(dpf.format(end.getTime()));
        ((TextView)findViewById(R.id.edit_edate)).setText(spf.format(end.getTime()));
        if(rule!=null&&!rule.equals("")){
            EventRecurrence r = new EventRecurrence();
            r.parse(rule);
            String repeatString = EventRecurrenceFormatter.getRepeatString(this, getResources(), r, true);
            ((TextView)findViewById(R.id.edit_repeat)).setText("Repeats "+ repeatString);
        }
        //listen for input on the fields
        findViewById(R.id.edit_sdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       //clicklistener for date picker
                DialogFragment newFragment = new DatePickerFragment((TextView)v);
                newFragment.show(getFragmentManager(), "sdatePicker");
            }
        });
        findViewById(R.id.edit_stime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       //clicklistener for timepicker
                DialogFragment newFragment = new TimePickerFragment((TextView)v);
                newFragment.show(getFragmentManager(), "stimePicker");
            }
        });
        findViewById(R.id.edit_edate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       //clicklistener for date picker
                DialogFragment newFragment = new DatePickerFragment((TextView)v);
                newFragment.show(getFragmentManager(), "edatePicker");
            }
        });
        findViewById(R.id.edit_etime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       //clicklistener for timepicker
                DialogFragment newFragment = new TimePickerFragment((TextView)v);
                newFragment.show(getFragmentManager(), "etimePicker");
            }
        });
        findViewById(R.id.edit_repeat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {     //clicklistener for recurrence picker
                FragmentManager fm = getSupportFragmentManager();
                Bundle bundle = new Bundle();
                Time time = new Time();
                time.setToNow();
                bundle.putLong(RecurrencePickerDialogFragment.BUNDLE_START_TIME_MILLIS, time.toMillis(false));
                bundle.putString(RecurrencePickerDialogFragment.BUNDLE_TIME_ZONE, time.timezone);

                bundle.putString(RecurrencePickerDialogFragment.BUNDLE_RRULE, rule);

                RecurrencePickerDialogFragment rpd = (RecurrencePickerDialogFragment) fm.findFragmentByTag(
                        FRAG_TAG_RECUR_PICKER);
                if (rpd != null) {
                    rpd.dismiss();
                }
                rpd = new RecurrencePickerDialogFragment();
                rpd.setArguments(bundle);
                rpd.setOnRecurrenceSetListener(EditEvent.this);
                rpd.setOnDismissListener(EditEvent.this);
                rpd.show(fm, FRAG_TAG_RECUR_PICKER);
            }
        });
    }
    //callbacks for recurrence picker
    @Override
    public void onRecurrenceSet(String rrule) {
        rule = rrule;
        if (rule != null) {
            mEventRecurrence.parse(rule);
        }
        populateRepeats();
    }
    @SuppressLint("SetTextI18n")
    public void populateRepeats() {

        Resources r = getResources();
        String repeatString = "";
        if (!TextUtils.isEmpty(rule)) {
            repeatString = EventRecurrenceFormatter.getRepeatString(this, r, mEventRecurrence, true);
        }
        if(repeatString==null||repeatString.equals(""))
            ((TextView)findViewById(R.id.edit_repeat)).setText("Does not Repeat");
        else
            ((TextView)findViewById(R.id.edit_repeat)).setText("Repeats\n"+repeatString);
    }
    @Override
    public void onDialogDismiss(DialogInterface dialoginterface) {
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.editer, menu);
        return true;
    }
    //listen to action bar item selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_discard:
                //show dialog with choices
                new MaterialDialog.Builder(this)
                        .title(null)
                        .message("Are you sure you want to discard your changes?")
                        .dim(50)
                        .positiveText("KEEP EDITING")
                        .negativeText("DISCARD")
                        .positiveColor(R.color.green_700)
                        .negativeColor(R.color.pink_700)
                        .buttonCallback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                //do nothing if user wants to keep editing
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                //if discarding changes, return to previous activity with result code 0
                                setResult(0);
                                finish();
                                super.onNegative(dialog);
                            }
                        })
                        .show();
                break;
            case R.id.edit_save: //user clicked on save button
                //get user input from the views
                title= ((EditText)findViewById(R.id.edit_title)).getText().toString();
                notes=((EditText)findViewById(R.id.edit_notes)).getText().toString();
                venue= ((EditText)findViewById(R.id.edit_venue)).getText().toString();
                final String stime=((TextView)findViewById(R.id.edit_stime)).getText().toString();
                final String sdate=((TextView)findViewById(R.id.edit_sdate)).getText().toString();
                final String etime=((TextView)findViewById(R.id.edit_etime)).getText().toString();
                final String edate=((TextView)findViewById(R.id.edit_edate)).getText().toString();

                //check if input data is valid
                if(title==null||title.equals("")||Long.parseLong(sdate.replace("-","")+stime.replace(":",""))
                        >Long.parseLong(edate.replace("-","")+etime.replace(":","")))
                {
                    Toast toast = Toast.makeText(getBaseContext(),"Please enter valid values for all fields!",Toast.LENGTH_LONG);
                    toast.show();
                }
                else{
                    //display dialog with choices
                    new MaterialDialog.Builder(this)
                            .title(null)
                            .message("Are you sure you want to save this?")
                            .dim(50)
                            .positiveText("SAVE")
                            .negativeText("GO BACK")
                            .positiveColor(R.color.green_700)
                            .negativeColor(R.color.pink_700)
                            .buttonCallback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    //yes, try to save
                                    super.onPositive(dialog);
                                    try {
                                        Uri u = ContentUris.withAppendedId(CONTENT_URI, id);
                                        ContentValues values = new ContentValues();
                                        values.put(CalendarContract.Events.TITLE, title);
                                        values.put(CalendarContract.Events.DESCRIPTION, notes);
                                        values.put(CalendarContract.Events.EVENT_LOCATION, venue);
                                        Calendar cal = Calendar.getInstance();
                                        cal.set(parseInt((sdate + stime).substring(0, 4)), parseInt((sdate + stime).substring(5, 7)) - 1, parseInt((sdate + stime).substring(8, 10)),
                                                parseInt((sdate + stime).substring(10, 12)), parseInt((sdate + stime).substring(13, 15)));
                                        values.put(CalendarContract.Events.DTSTART, cal.getTimeInMillis());
                                        Calendar cal2 = Calendar.getInstance();
                                        cal2.set(parseInt((edate + etime).substring(0, 4)), parseInt((edate + etime).substring(5, 7)) - 1, parseInt((edate + etime).substring(8, 10)),
                                                parseInt((edate + etime).substring(10, 12)), parseInt((edate + etime).substring(13, 15)));
                                        if (rule != null && !rule.equals("")) {
                                            String duration = getDuration(cal2.getTimeInMillis() - cal.getTimeInMillis());
                                            values.put(CalendarContract.Events.DURATION, duration);
                                        } else
                                            values.put(CalendarContract.Events.DTEND, cal2.getTimeInMillis());
                                        values.put(CalendarContract.Events.RRULE, rule);
                                        getContentResolver().update(u, values, null, null);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    //because edits were made, set result code to 1
                                    setResult(1);
                                    finish();
                                    Toast.makeText(EditEvent.this, "Updating...", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                    //do nothing if user doesn't want to save yet
                                }
                            })
                            .show();
                }
                    break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
