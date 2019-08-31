package com.example.leo.krono.eAdders;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
//activity to add an exam to the calendar
public class AddExam extends AppCompatActivity{
    private String date;
    private String title;
    private String venue;
    private String note;
    private String stime;
    private String etime;
    private int minute;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        minute = Integer.parseInt(sharedPref.getString("minute_exam","10"));
        setContentView(R.layout.activity_add_exam);
        findViewById(R.id.exam_date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       //clicklistener for date picker
                DialogFragment newFragment = new DatePickerFragment((TextView)v);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });
        findViewById(R.id.exam_stime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       //clicklistener for timepicker
                DialogFragment newFragment = new TimePickerFragment((TextView)v);
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });
        findViewById(R.id.exam_etime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       //clicklistener for timepicker
                DialogFragment newFragment = new TimePickerFragment((TextView)v);
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });
        findViewById(R.id.exam_cancel).setOnClickListener(new View.OnClickListener() { //goes back to previous screen when cancel is pressed
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.exam_save).setOnClickListener(new View.OnClickListener() {       //tries to save exam to calendar
            @Override
            public void onClick(View v) {
                title=((EditText)findViewById(R.id.exam_desc)).getText().toString();
                note=((EditText)findViewById(R.id.exam_notes)).getText().toString();
                date=((TextView)findViewById(R.id.exam_date)).getText().toString();
                venue=((TextView)findViewById(R.id.exam_venue)).getText().toString();
                stime=((TextView)findViewById(R.id.exam_stime)).getText().toString();
                etime=((TextView)findViewById(R.id.exam_etime)).getText().toString();
                if(date.equals("Date")||date==null||stime.equals("Start")
                        ||title==null||title.equals("")||etime.equals("End")||
                        Integer.parseInt(stime.replace(":",""))>=Integer.parseInt(etime.replace(":","")))
                {
                    Toast toast = Toast.makeText(getBaseContext(),"Please enter valid values for all fields!",Toast.LENGTH_LONG);
                    toast.show();
                }
                else
                {
                    try
                    {
                        boolean added= EventHandler.addReminder(AddExam.this,title,note,venue,
                                date+stime,date+etime,null,"Exams",minute); //call to eventhandler
                        if(added) {
                            Toast toast = Toast.makeText(getBaseContext(), "Added Exam!", Toast.LENGTH_SHORT);
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
