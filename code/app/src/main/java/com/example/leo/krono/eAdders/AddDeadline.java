package com.example.leo.krono.eAdders;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leo.krono.EventHandler;
import com.example.leo.krono.R;
import com.example.leo.krono.activities.DatePickerFragment;
import com.example.leo.krono.activities.TimePickerFragment;

/**
 * Created by guptaji on 4/10/17.
 */
//activity to add a deadline to the calendar
public class AddDeadline extends AppCompatActivity{
     //these are the variables that are passed to the EventHandler
    private String date;
    private String time;
    private String title;
    private String note;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_deadline);
        findViewById(R.id.deadline_date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       //clicklistener for date picker
                DialogFragment newFragment = new DatePickerFragment((TextView)v);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });
        findViewById(R.id.deadline_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       //clicklistener for timepicker
                DialogFragment newFragment = new TimePickerFragment( (TextView)v);
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });
        findViewById(R.id.deadline_cancel).setOnClickListener(new View.OnClickListener() { //goes back to previous screen when cancel is pressed
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.deadline_save).setOnClickListener(new View.OnClickListener() {       //tries to save deadline to calendar
            @Override
            public void onClick(View v) {
                title=((EditText)findViewById(R.id.deadline_desc)).getText().toString();
                note=((EditText)findViewById(R.id.deadline_notes)).getText().toString();
                date=((TextView)findViewById(R.id.deadline_date)).getText().toString();
                time=((TextView)findViewById(R.id.deadline_time)).getText().toString();
                //check if input is valid
                if(date.equals("Date")||date==null||time.equals("Time")||
                                    time==null||title==null||title.equals(""))
                {
                    Toast toast = Toast.makeText(getBaseContext(),"Please enter valid values for all fields!",Toast.LENGTH_LONG);
                    toast.show();
                }
                else
                {
                    try
                    {
                        boolean added= EventHandler.makeDeadline(AddDeadline.this,title,note,
                                date+time); //call to eventhandler
                        if(added) {
                            Toast toast = Toast.makeText(getBaseContext(), "Added deadline!", Toast.LENGTH_SHORT);
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
}
