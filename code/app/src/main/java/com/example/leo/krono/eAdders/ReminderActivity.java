package com.example.leo.krono.eAdders;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.OnDialogDismissListener;
import com.codetroopers.betterpickers.recurrencepicker.EventRecurrence;
import com.codetroopers.betterpickers.recurrencepicker.EventRecurrenceFormatter;
import com.codetroopers.betterpickers.recurrencepicker.RecurrencePickerDialogFragment;
import com.example.leo.krono.EventHandler;
import com.example.leo.krono.R;
import com.example.leo.krono.activities.DatePickerFragment;
import com.example.leo.krono.activities.TimePickerFragment;

/**
 * Created by guptaji on 3/10/17.
 */
//activity to add a reminder to the calendar
public class ReminderActivity extends AppCompatActivity
        implements RecurrencePickerDialogFragment.OnRecurrenceSetListener, OnDialogDismissListener {
    private static final String FRAG_TAG_RECUR_PICKER = "recurrencePickerDialogFragment";
    private EventRecurrence mEventRecurrence = new EventRecurrence();
    private String mRrule; //these are the variables that are passed to the EventHandler
    private String date;
    private String time;
    private String title;
    private String note;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        findViewById(R.id.reminder_date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       //clicklistener for date picker
                DialogFragment newFragment = new DatePickerFragment((TextView)v);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });
        findViewById(R.id.reminder_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       //clicklistener for timepicker
                DialogFragment newFragment = new TimePickerFragment((TextView)v);
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });
        findViewById(R.id.reminder_repeat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {     //clicklistener for recurrence picker
                FragmentManager fm = getSupportFragmentManager();
                Bundle bundle = new Bundle();
                Time time = new Time();
                time.setToNow();
                bundle.putLong(RecurrencePickerDialogFragment.BUNDLE_START_TIME_MILLIS, time.toMillis(false));
                bundle.putString(RecurrencePickerDialogFragment.BUNDLE_TIME_ZONE, time.timezone);

                bundle.putString(RecurrencePickerDialogFragment.BUNDLE_RRULE, mRrule);

                RecurrencePickerDialogFragment rpd = (RecurrencePickerDialogFragment) fm.findFragmentByTag(
                        FRAG_TAG_RECUR_PICKER);
                if (rpd != null) {
                    rpd.dismiss();
                }
                rpd = new RecurrencePickerDialogFragment();
                rpd.setArguments(bundle);
                rpd.setOnRecurrenceSetListener(ReminderActivity.this);
                rpd.setOnDismissListener(ReminderActivity.this);
                rpd.show(fm, FRAG_TAG_RECUR_PICKER);
            }
        });
        findViewById(R.id.reminder_cancel).setOnClickListener(new View.OnClickListener() { //goes back to previous screen when cancel is pressed
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.reminder_save).setOnClickListener(new View.OnClickListener() {       //tries to save reminder to calendar
            @Override
            public void onClick(View v) {
                title=((EditText)findViewById(R.id.reminder_desc)).getText().toString();
                date=((TextView)findViewById(R.id.reminder_date)).getText().toString();
                time=((TextView)findViewById(R.id.reminder_time)).getText().toString();
                note=((EditText)findViewById(R.id.reminder_notes)).getText().toString();
                if(date.equals("Date")||date==null||time.equals("Time")||time==null
                                            ||title==null||title.equals(""))
                {
                    Toast toast = Toast.makeText(getBaseContext(),"Please enter valid values for all fields!",Toast.LENGTH_LONG);
                    toast.show();
                }
                else
                {
                    try
                    {
                        boolean added= EventHandler.addReminder(ReminderActivity.this,
                                                    title,note,null,date+time,null,mRrule,"Reminders",0); //call to eventhandler
                        if(added){
                            Toast toast = Toast.makeText(getBaseContext(),"Added Reminder!",Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        else
                        {
                            Toast toast = Toast.makeText(getBaseContext(),"Something went wrong!",Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        finish();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    @Override
    public void onRecurrenceSet(String rrule) {
        mRrule = rrule;
        if (mRrule != null) {
            mEventRecurrence.parse(mRrule);
        }
        populateRepeats();
    }

    @Override
    public void onResume() {
        super.onResume();
        RecurrencePickerDialogFragment rpd = (RecurrencePickerDialogFragment) getSupportFragmentManager().findFragmentByTag(
                FRAG_TAG_RECUR_PICKER);
        if (rpd != null) {
            rpd.setOnRecurrenceSetListener(this);
        }
    }

    private void populateRepeats() {

        Resources r = getResources();
        String repeatString = "";
        if (!TextUtils.isEmpty(mRrule)) {
            repeatString = EventRecurrenceFormatter.getRepeatString(this, r, mEventRecurrence, true);
        }
        if(repeatString==null||repeatString.equals(""))
            ((TextView)findViewById(R.id.reminder_repeat)).setText("Does not Repeat");
        else
            ((TextView)findViewById(R.id.reminder_repeat)).setText("Repeats\n"+repeatString);

    }


    @Override
    public void onDialogDismiss(DialogInterface dialoginterface) {
    }
}
