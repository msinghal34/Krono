package com.example.leo.krono.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.leo.krono.CalendarController;
import com.example.leo.krono.EventHandler;
import com.example.leo.krono.R;
import com.example.leo.krono.eAdders.AddDeadline;
import com.example.leo.krono.eAdders.AddEvent;
import com.example.leo.krono.eAdders.AddExam;
import com.example.leo.krono.eAdders.AddWeekly;
import com.example.leo.krono.eAdders.ReminderActivity;
import com.example.leo.krono.mService.MuteService;
import com.example.leo.krono.misc.AddCalAct;
import com.github.tibolte.agendacalendarview.AgendaCalendarView;
import com.github.tibolte.agendacalendarview.CalendarManager;
import com.github.tibolte.agendacalendarview.CalendarPickerController;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.models.DayItem;
import com.github.tibolte.agendacalendarview.models.IDayItem;
import com.github.tibolte.agendacalendarview.models.IWeekItem;
import com.github.tibolte.agendacalendarview.models.WeekItem;
import com.github.tibolte.agendacalendarview.render.DefaultEventRenderer;
import com.pepperonas.materialdialog.MaterialDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
//TODO: agenda skipping adapter twice at start (maybe coz of async behaviour, though, so can ignore?)
//TODO: move hardcoded strings from all classes and layouts to resources
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, CalendarPickerController {
    private static final int REQUEST_PERMISSIONS_WRITE_CALENDAR = 1;
    public static final String ACTION_SHOW_ACTIONS = "showActions";
    //list of choices in adding events dialog
    private static String[] cats = {
            "Event",
            "Class",
            "Tutorial",
            "Lab",
            "Deadline",
            "Exam",
    };
    //variables related to the FAB
    TextView reminders_text_view, events_text_view;
    FloatingActionButton fab_plus, fab_reminders, fab_events;
    Animation fab_open, fab_close, fab_clock, fab_anticlock;
    //boolean to check whether floating action button with plus logo is open or not
    boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set all preferences to their defaults on first run
        PreferenceManager.setDefaultValues(this, R.xml.pref_colors, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_minute, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_mute, false);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        if (!sharedPreferences.getBoolean(
                IntroActivity.COMPLETED_ONBOARDING_PREF_NAME, false)) {
            // The user hasn't seen the OnboardingFragment yet, so show it
            startActivity(new Intent(this, IntroActivity.class));
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        //Floating Action Buttons and their animations
        fab_plus = (FloatingActionButton) findViewById(R.id.fab_plus);
        fab_reminders = (FloatingActionButton) findViewById(R.id.fab_reminders);
        fab_events = (FloatingActionButton) findViewById(R.id.fab_events);
        
        reminders_text_view = (TextView) findViewById(R.id.rem_textView);
        events_text_view = (TextView) findViewById(R.id.events_textView);
        
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fab_clock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_clock);
        fab_anticlock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_anticlock);
       
        //Clicking of floating action button with plus logo
        fab_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                if (isOpen) {
                    fab_events.startAnimation(fab_close);
                    events_text_view.startAnimation(fab_close);
                    fab_reminders.startAnimation(fab_close);
                    reminders_text_view.startAnimation(fab_close);
                    fab_plus.startAnimation(fab_anticlock);
                    fab_reminders.setClickable(false);
                    fab_events.setClickable(false);
                    isOpen = false;
                } else {
                    fab_plus.startAnimation(fab_clock);
                    fab_reminders.startAnimation(fab_open);
                    reminders_text_view.startAnimation(fab_open);
                    fab_events.startAnimation(fab_open);
                    events_text_view.startAnimation(fab_open);
                    fab_reminders.setClickable(true);
                    fab_events.setClickable(true);
                    isOpen = true;
                }
            }
        });
        
        //Clicking of floating action button -> Reminders
        fab_reminders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                Intent intent = new Intent(getBaseContext(), ReminderActivity.class);
                fab_events.startAnimation(fab_close);
                events_text_view.startAnimation(fab_close);
                fab_reminders.startAnimation(fab_close);
                reminders_text_view.startAnimation(fab_close);
                fab_plus.startAnimation(fab_anticlock);
                fab_reminders.setClickable(false);
                fab_events.setClickable(false);
                isOpen = false;
                startActivity(intent);
            }
        });
        
        //Clicking of floating action button -> Events
        fab_events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                //show dialog with choices
                new MaterialDialog.Builder(MainActivity.this)
                        .title(null)
                        .dim(50)
                        .listItems(true, cats)
                        .itemClickListener(new MaterialDialog.ItemClickListener() {
                            @Override
                            public void onClick(View v, int position, long id) {
                                fab_events.startAnimation(fab_close);
                                events_text_view.startAnimation(fab_close);
                                fab_reminders.startAnimation(fab_close);
                                reminders_text_view.startAnimation(fab_close);
                                fab_plus.startAnimation(fab_anticlock);
                                fab_reminders.setClickable(false);
                                fab_events.setClickable(false);
                                isOpen = false;
                                switch (position) {
                                    //start intent to corresponding activity
                                    case 0:
                                        Intent intent = new Intent(getBaseContext(), AddEvent.class);
                                        startActivity(intent);
                                        break;
                                    case 1:
                                        intent = new Intent(getBaseContext(), AddWeekly.class);
                                        intent.putExtra("CAL", "Classes");
                                        startActivity(intent);
                                        break;
                                    case 2:
                                        intent = new Intent(getBaseContext(), AddWeekly.class);
                                        intent.putExtra("CAL", "Tutorials");
                                        startActivity(intent);
                                        break;
                                    case 3:
                                        intent = new Intent(getBaseContext(), AddWeekly.class);
                                        intent.putExtra("CAL", "Labs");
                                        startActivity(intent);
                                        break;
                                    case 4:
                                        intent = new Intent(getBaseContext(), AddDeadline.class);
                                        startActivity(intent);
                                        break;
                                    case 5:
                                        intent = new Intent(getBaseContext(), AddExam.class);
                                        startActivity(intent);
                                        break;
                                }
                            }
                        })
                        .show();
            }
        });

        //setup nav drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //disables the FAB of the scheduler view
            //that FAB was causing crashes with async behaviour enabled, btw
        AgendaCalendarView mAgendaCalendarView = (AgendaCalendarView) findViewById(R.id.agenda_calendar_view);
        mAgendaCalendarView.enableFloatingIndicator(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        //invalidate the view on pause
        getWindow().getDecorView().findViewById(R.id.agenda_calendar_view).invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        //check permission and request if it's not there
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED))
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, REQUEST_PERMISSIONS_WRITE_CALENDAR);
        else {
            addCalendar();
        }
    }

    @Override
    public void onBackPressed() {
        //close drawer on back press
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    //listen to action bar item selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.events:
                Intent intent = new Intent(this, InstiEvents.class);
                this.startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation drawer item clicks here.
        //close drawer on item select
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_WRITE_CALENDAR: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    addCalendar();
                } else {
                    //request again, although the limit seems to be 2 times in newer android versions
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, REQUEST_PERMISSIONS_WRITE_CALENDAR);
                }
            }
        }
    }
    //calls the add calendar activity if the calendars don't yet exist and then calls setAgendaView()
    private void addCalendar() {
        if (CalendarController.checkAccount(getBaseContext())) {
            setAgendaView();
            startService();
        } else {
            Intent intent = new Intent(this, AddCalAct.class);
            intent.setData(CalendarContract.Calendars.CONTENT_URI);
            startActivityForResult(intent, 421);
        }
    }
    //action on clicking on add course
    public void showCourses(MenuItem menu){
        Intent intent=new Intent(this,SearchCourse.class);
        this.startActivity(intent);
    }
    //open settings
    public void showSettings(MenuItem menu) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        this.startActivity(intent);
    }
    //starts the muting service if necessary
    public void startService() {
        NotificationManager notificationManager =
                (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        //need to check for permission to change DND policy on android M and above
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !notificationManager.isNotificationPolicyAccessGranted()) {

                Intent intent = new Intent(
                        android.provider.Settings
                                .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

                startActivityForResult(intent, 101);
            } else {
                MuteService.startIfNecessary(getBaseContext());
            }
        }
    }

    //callback
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        switch (requestCode) {
            case 101: //callback from startService(), means permission not granted
               this.startService(); //so ask for it in a loop (not tested)
                break;
            case 421: //callback from addCalendar(), means permission was granted and calendars are added!
                setAgendaView();   //so safe to initialise the scheduler view now
                startService();
                break;
        }
    }
    //open week view
    public void showWeekView(MenuItem menu) {
        Intent intent = new Intent(MainActivity.this, WeekView.class);
        this.startActivity(intent);

    }
    //show help screen
    public void showHelp(MenuItem menu) {
        Intent intent = new Intent(MainActivity.this, HelpActivity.class);
        this.startActivity(intent);

    }

    //interface methods for agenda from this point

    public void onDaySelected(IDayItem dayItem) {
        //just another callback
    }

    public void onEventSelected(CalendarEvent event) {
        //call view event activity here
        if (!((BaseCalendarEvent) event).isPlaceHolder()) { //checks if its a placeholder or a real event
            //put data in intent and call activity
            Intent intent = new Intent(getBaseContext(), ViewEvent.class);
            intent.putExtra("ID", event.getId());
            intent.putExtra("Ti", event.getTitle());
            intent.putExtra("Ve", event.getLocation());
            intent.putExtra("St", event.getStartTime());
            intent.putExtra("En", event.getEndTime());
            startActivity(intent);
        }
    }

    @Override
    public void onScrollToDate(Calendar calendar) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()));
        }
    }

    //initialises the agenda
    private void setAgendaView() {
        AgendaCalendarView mAgendaCalendarView = (AgendaCalendarView) findViewById(R.id.agenda_calendar_view);
        mAgendaCalendarView.enableFloatingIndicator(false);
        CalendarManager calendarManager = CalendarManager.getInstance(getApplicationContext());
        //run asynctask to populate events in view
        new updateView(this,calendarManager,mAgendaCalendarView).execute();
    }
}
//Asynctask to populate events in background thread
//used asynctask because querying instances on UI thread will choke the UI
class updateView extends AsyncTask<String,String,String>{
    private CalendarManager cm;
    private Context c;
    private AgendaCalendarView v;
    updateView(Context context,CalendarManager manager,AgendaCalendarView view){
        this.c=context;
        this.cm=manager;
        this.v=view;
    }
    @Override
    protected void onPostExecute(String r){
        //set results on screen after execution
        List<CalendarEvent> readyEvents = cm.getEvents();
        List<IDayItem> readyDays = cm.getDays();
        List<IWeekItem> readyWeeks = cm.getWeeks();
        v.init(Locale.getDefault(), readyWeeks, readyDays, readyEvents, (MainActivity)c);
        v.addEventRenderer(new DefaultEventRenderer());
        v.enableCalenderView(true);
    }
    //gets instances list in background thread
    @Override
    protected String doInBackground(String... params) {
        ArrayList<CalendarEvent> eventList = new ArrayList<>();
        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        //load events from 2 months back to 6 months from now
        minDate.add(Calendar.MONTH, -4);
        minDate.set(Calendar.DAY_OF_MONTH, 1);
        maxDate.add(Calendar.MONTH, 4);
        EventHandler.getSInstances(c,minDate,maxDate,eventList);  //call to eventhandler
        cm.buildCal(minDate, maxDate, Locale.getDefault(), new DayItem(), new WeekItem());
        cm.loadEvents(eventList, new BaseCalendarEvent());
        return null;
    }
}
