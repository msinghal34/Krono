package com.example.leo.krono.eAdders;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leo.krono.EventHandler;
import com.example.leo.krono.R;
import com.example.leo.krono.activities.DatePickerFragment;
import com.example.leo.krono.activities.TimePickerFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

/**
 * Created by guptaji on 7/10/17.
 */
//activity to add event like classes, tuts and labs which recur weekly on different times on different days. Allows upto 4 ENTRIES PER WEEK ONLY
public class AddWeekly extends AppCompatActivity
implements AdapterView.OnItemSelectedListener {
    //regex to extract times from slots string of a course
    private static final String regex="[a-zA-Z]{3}-[a-zA-Z0-9]{2,3}-[0-9]{2}:[0-9]{2}:[0-9]{2}-[0-9]{2}:[0-9]{2}:[0-9]{2}";
    //booleans represent the state of the checkboxes
    private static boolean b1,b2,b3,b4;
    private static String w1,w2,w3,w4,CAL;
    private static int minute;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_weekly);
    }
    protected void onResume(){
        super.onResume();
        b1=false;
        b2=false;
        b3=false;
        b4=false;
        Intent i=getIntent();
        if(i.getIntExtra("R",0)==42){ //check if the calling activity was the course search activity
            ((EditText) findViewById(R.id.weekly_desc)).setText(i.getStringExtra("Co"));
            ((EditText) findViewById(R.id.weekly_notes)).setText(i.getStringExtra("Na"));
            if(i.getStringExtra("Ve").matches(".*\\w.*")){
                ((EditText) findViewById(R.id.weekly_venue)).setText(i.getStringExtra("Ve"));
            }
            String slots=i.getStringExtra("Sl");  //extract timings from the slots string and set in the textviews
            if(slots.matches(".*\\w.*")) {
                Pattern p = Pattern.compile(regex);
                List<String> list = new ArrayList<String>();
                Matcher m=p.matcher(slots);
                while (m.find()) {
                    list.add(m.group());
                }
                int index=0;
                for(String s:list){
                    String[] l=s.split("-");
                    switch(index){
                        case 0:
                            setSpinner(l[0],1);
                            b1=true;
                            ((CheckBox)findViewById(R.id.check1)).setChecked(true);
                            ((TextView) findViewById(R.id.start1)).setText(l[2]);
                            ((TextView) findViewById(R.id.end1)).setText(l[3]);
                            break;
                        case 1:
                            setSpinner(l[0],2);
                            b2=true;
                            ((CheckBox)findViewById(R.id.check2)).setChecked(true);
                            ((TextView) findViewById(R.id.start2)).setText(l[2]);
                            ((TextView) findViewById(R.id.end2)).setText(l[3]);
                            break;
                        case 2:
                            setSpinner(l[0],3);
                            b3=true;
                            ((CheckBox)findViewById(R.id.check3)).setChecked(true);
                            ((TextView) findViewById(R.id.start3)).setText(l[2]);
                            ((TextView) findViewById(R.id.end3)).setText(l[3]);
                            break;
                        case 3:
                            setSpinner(l[0],4);
                            b4=true;
                            ((CheckBox)findViewById(R.id.check4)).setChecked(true);
                            ((TextView) findViewById(R.id.start4)).setText(l[2]);
                            ((TextView) findViewById(R.id.end4)).setText(l[3]);
                            break;
                    }
                    index++;
                }
            }
            ((TextView) findViewById(R.id.weekly_start)).setText("2017-07-17");
            ((TextView) findViewById(R.id.weekly_end)).setText("2017-11-09");
        }
        CAL = i.getStringExtra("CAL");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        minute = Integer.parseInt(sharedPref.getString("minute_" + CAL.toLowerCase().substring(0, CAL.length() - 1), "10"));
        findViewById(R.id.weekly_cancel).setOnClickListener(new View.OnClickListener() { //goes back to previous screen when cancel is pressed
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.weekly_save).setOnClickListener(new View.OnClickListener() { //attempts to save the event and return to previous activity
            @Override
            public void onClick(View v) {
                String title = ((EditText) findViewById(R.id.weekly_desc)).getText().toString();
                String note = ((EditText) findViewById(R.id.weekly_notes)).getText().toString();
                String venue = ((EditText) findViewById(R.id.weekly_venue)).getText().toString();
                String sdate = ((TextView) findViewById(R.id.weekly_start)).getText().toString();
                String edate = ((TextView) findViewById(R.id.weekly_end)).getText().toString();
                if (title.equals("") || sdate.equals("Start Date") || edate.equals("End Date") ||
                        Integer.parseInt(sdate.replace("-", "")) > Integer.parseInt(edate.replace("-", ""))) {
                    Toast toast = Toast.makeText(getBaseContext(), "Please enter valid values!", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    boolean a1 = false, a2 = false, a3 = false, a4 = false;
                    if (b1) {
                        String stime = ((TextView) findViewById(R.id.start1)).getText().toString();
                        String etime = ((TextView) findViewById(R.id.end1)).getText().toString();
                        String rule = "FREQ=WEEKLY;BYDAY=" + w1 + ";WKST=SU;UNTIL=" +
                                edate.replaceAll("-", "") + "T" + etime.replaceAll(":", "");
                        if (stime.equals("Start") || etime.equals("End") ||
                                Integer.parseInt(stime.replace(":", "")) > Integer.parseInt(etime.replace(":", ""))) {
                            Toast toast = Toast.makeText(getBaseContext(), "Please enter valid values!", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            String s = sdate.substring(0, 8) + nextday(sdate, w1);
                            a1 = EventHandler.addReminder(getBaseContext(),
                                    title, note, venue,
                                    s + stime, s + etime,
                                    rule, CAL, minute);
                        }
                    } else
                        a1 = true;
                    if (b2) {
                        String stime = ((TextView) findViewById(R.id.start2)).getText().toString();
                        String etime = ((TextView) findViewById(R.id.end2)).getText().toString();
                        String rule = "FREQ=WEEKLY;BYDAY=" + w2 + ";WKST=SU;UNTIL=" +
                                edate.replaceAll("-", "") + "T" + etime.replaceAll(":", "");
                        if (stime.equals("Start") || etime.equals("End")
                                || Integer.parseInt(stime.replace(":", "")) > Integer.parseInt(etime.replace(":", ""))) {
                            Toast toast = Toast.makeText(getBaseContext(), "Please enter valid values!", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            String s = sdate.substring(0, 8) + nextday(sdate, w2);
                            a2 = EventHandler.addReminder(getBaseContext(),
                                    title, note, venue,
                                    s + stime, s + etime,
                                    rule, CAL, minute);
                        }

                    } else
                        a2 = true;
                    if (b3) {
                        String stime = ((TextView) findViewById(R.id.start3)).getText().toString();
                        String etime = ((TextView) findViewById(R.id.end3)).getText().toString();
                        String rule = "FREQ=WEEKLY;BYDAY=" + w3 + ";WKST=SU;UNTIL=" + edate.replaceAll("-", "") + "T" + etime.replaceAll(":", "");
                        if (stime.equals("Start") || etime.equals("End") || Integer.parseInt(stime.replace(":", "")) > Integer.parseInt(etime.replace(":", ""))) {
                            Toast toast = Toast.makeText(getBaseContext(), "Please enter valid values!", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            String s = sdate.substring(0, 8) + nextday(sdate, w3);
                            a3 = EventHandler.addReminder(getBaseContext(),
                                    title, note, venue,
                                    s + stime, s + etime,
                                    rule,
                                    CAL, minute);
                        }
                    } else
                        a3 = true;
                    if (b4) {
                        String stime = ((TextView) findViewById(R.id.start4)).getText().toString();
                        String etime = ((TextView) findViewById(R.id.end4)).getText().toString();
                        String rule = "FREQ=WEEKLY;BYDAY=" + w4 + ";WKST=SU;UNTIL="
                                + edate.replaceAll("-", "") + "T" + etime.replaceAll(":", "");
                        if (stime.equals("Start") || etime.equals("End") ||
                                Integer.parseInt(stime.replace(":", "")) > Integer.parseInt(etime.replace(":", ""))) {
                            Toast toast = Toast.makeText(getBaseContext(), "Please enter valid values!", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            String s = sdate.substring(0, 8) + nextday(sdate, w4);
                            a4 = EventHandler.addReminder(getBaseContext(),
                                    title, note, venue,
                                    s + stime, s + etime,
                                    rule, CAL, minute);
                        }
                    } else
                        a4 = true;
                    if ((!b1 || a1) && (!b2 || a2) && (!b3 || a3) && (!b4 || a4)) {
                        Toast toast = Toast.makeText(getBaseContext(), "Added successfuly!", Toast.LENGTH_SHORT);
                        toast.show();
                        finish();
                    } else {
                        Toast toast = Toast.makeText(getBaseContext(), "Some items could not be added!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
        });
        Spinner s = (Spinner) findViewById(R.id.wday1);
        s.setOnItemSelectedListener(this);
        s = (Spinner) findViewById(R.id.wday2);
        s.setOnItemSelectedListener(this);
        s = (Spinner) findViewById(R.id.wday3);
        s.setOnItemSelectedListener(this);
        s = (Spinner) findViewById(R.id.wday4);
        s.setOnItemSelectedListener(this);

    }
    //method that sets the spinner according to a day string
    private void setSpinner(String day, int index){
        int d;
        switch (day) {
            case "Mon":
                d = 0;
                break;
            case "Tue":
                d = 1;
                break;
            case "Wed":
                d = 2;
                break;
            case "Thu":
                d = 3;
                break;
            case "Fri":
                d = 4;
                break;
            case "Sat":
                d = 5;
                break;
            default:
                d = 0;
                break;
        }
        switch(index){
            case 1:
                ((Spinner) findViewById(R.id.wday1)).setSelection(d);
                break;
            case 2:
                ((Spinner) findViewById(R.id.wday2)).setSelection(d);
                break;
            case 3:
                ((Spinner) findViewById(R.id.wday3)).setSelection(d);
                break;
            case 4:
                ((Spinner) findViewById(R.id.wday4)).setSelection(d);
                break;
        }
    }
    //listens to clicks on time picker views
    public void onClickText(View view){
        DialogFragment newFragment = new TimePickerFragment((TextView)view);
        newFragment.show(getFragmentManager(), "timePicker");
    }
    //listens to clicks on date picker views
    public void onClickTextDate(View view){
        DialogFragment newFragment= new DatePickerFragment((TextView)view);
        newFragment.show(getFragmentManager(),"datepicker");
    }
    //listens to clicks on the checkboxes
    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        switch(view.getId()) {
            case R.id.check1:
                b1=checked;
                break;
            case R.id.check2:
                b2=checked;
                break;
            case R.id.check3:
                b3=checked;
                break;
            case R.id.check4:
                b4=checked;
                break;
        }
    }
    //method that returns the date of the first occurence of a particular day of week from a given date
    private String nextday(String s,String d){
        Calendar c = Calendar.getInstance();
        c.set(parseInt(s.substring(0, 4)), parseInt(s.substring(5, 7)) - 1, parseInt(s.substring(8, 10)));
        int plus;
        int nextday = 0;
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK); //sunday is 1, saturday is 7
        switch (d) {
            case "MO":
                nextday = 2;
                break;
            case "TU":
                nextday = 3;
                break;
            case "WE":
                nextday = 4;
                break;
            case "TH":
                nextday = 5;
                break;
            case "FR":
                nextday = 6;
                break;
            case "SA":
                nextday = 7;
                break;
        }
        if(nextday>=dayOfWeek)
            plus=nextday-dayOfWeek;
        else
            plus=7+nextday-dayOfWeek;
        String result=Integer.toString(Integer.parseInt(s.substring(8,10))+plus);
        if(Integer.parseInt(result)<10)
            return "0"+result;
        else
            return result;
    }
    //listen to spinner selections
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        int idee=parent.getId();
        switch(idee)
        {
            case R.id.wday1:
                w1=parent.getItemAtPosition(pos).toString();
                break;
            case R.id.wday2:
                w2=parent.getItemAtPosition(pos).toString();
                break;
            case R.id.wday3:
                w3=parent.getItemAtPosition(pos).toString();
                break;
            case R.id.wday4:
                w4=parent.getItemAtPosition(pos).toString();
                break;
        }

    }
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
