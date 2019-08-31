package com.example.leo.krono.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by guptaji on 3/10/17.
 */

//fragment that lets user pick time, for example usage see ReminderActivity
@SuppressLint("ValidFragment")
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    private TextView v;
    @SuppressLint("ValidFragment")
    public TimePickerFragment(TextView view)
    {
        this.v=view;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }
//do something when user picks a time
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String Hour=Integer.toString(hourOfDay);
        String Minute=Integer.toString(minute);
        if(hourOfDay<10)
            Hour='0'+Hour;
        if(minute<10)
            Minute='0'+Minute;
        //set the calling textview to the seleted time
        v.setText(Hour+':'+Minute+':'+"00");
    }
}