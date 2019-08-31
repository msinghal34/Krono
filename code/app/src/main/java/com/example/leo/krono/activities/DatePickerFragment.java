package com.example.leo.krono.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by guptaji on 3/10/17.
 */
//fragment to show date picker
@SuppressLint("ValidFragment")
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    private TextView v;
    @SuppressLint("ValidFragment")
    public DatePickerFragment(TextView view)
    {
        this.v=view;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }
    //do something after the user picks a date
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        String Year=Integer.toString(year);
        String Month=Integer.toString(month+1);
        String Day=Integer.toString(day);
        if(month<9)            //because month is 1 less than actual value
            Month='0'+Month;
        if(day<10)
            Day='0'+Day;
        //sets calling text view to chosen date
        v.setText(Year+'-'+Month+'-'+Day);
    }
}
