package com.example.leo.krono.mService;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.example.leo.krono.R;
import com.example.leo.krono.activities.MainActivity;
//TODO: change color preferences???
/**
 * Created by guptaji on 10/10/17.
 */
//service to mute the phone for the duration of specified categories of events
public class MuteService extends Service {


    public static final int NOTIF_ID = 1427;


    private class LocalBinder extends Binder {
        MuteService getService() {
            return MuteService.this;
        }
    }

    public static class StartServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            intent.getAction();
            MuteService.startIfNecessary(context);
        }
    }

    private LocalBinder localBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    /**
     * Update ringer status depending on settings and time
     * @param event Current event
     */
    private void updateRingerStatus(CalendarEvent event) {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // get Current ringer state
        int currentRingerMode = 0;
        if (audio != null) {
            currentRingerMode = audio.getRingerMode();
        }
        int savedMode = PreferencesManager.getSavedMode(this);
        int ringerAction = PreferencesManager.getRingerAction(this);
        if(event == null) { // No current event running

            if(PreferencesManager.getRestoreState(this) // Restore is on
                    && savedMode != PreferencesManager.PREF_SAVED_MODE_NO_VALUE // There is a mode to restore
                    // Check if current setting matches the action (do not restore if user changed the setting herself)
                    && ((ringerAction == PreferencesManager.PREF_ACTION_RINGER_SILENT && currentRingerMode == AudioManager.RINGER_MODE_SILENT))) {
                // Restore
                if (Build.VERSION.SDK_INT <Build.VERSION_CODES.M) {
                    if (audio != null) {
                        audio.setRingerMode(savedMode);
                    }
                }
                else{
                    NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                    }
                }

                // Close notification if necessary
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (manager != null) {
                    manager.cancel(NOTIF_ID);
                }
            }
            // Delete saved mode
            PreferencesManager.saveMode(this, PreferencesManager.PREF_SAVED_MODE_NO_VALUE);
            PreferencesManager.setLastSetRingerMode(this, PreferencesManager.PREF_LAST_SET_RINGER_MODE_NO_MODE);
        }
        else { // We are inside an event
            if(((ringerAction == PreferencesManager.PREF_ACTION_RINGER_SILENT && currentRingerMode != AudioManager.RINGER_MODE_SILENT)) // Current ringer setting is different from action
                    && PreferencesManager.getLastSetRingerMode(this) == PreferencesManager.PREF_SAVED_MODE_NO_VALUE) { // And no action done (so no mode saved) -> the user may have changed the volume
                // Save state and change it if there is not already a saved state
                if(savedMode == PreferencesManager.PREF_SAVED_MODE_NO_VALUE)
                    PreferencesManager.saveMode(this, currentRingerMode);

                if (Build.VERSION.SDK_INT <Build.VERSION_CODES.M) {
                    audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }
                else{
                    NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                    }
                }

                PreferencesManager.setLastSetRingerMode(this, AudioManager.RINGER_MODE_SILENT);


//               show  Notification
                if(PreferencesManager.getShowNotif(this)) {
                    showNotif(event.getNom());
                }

            }
            // No action if the current setting is already OK (and do not save the current setting either)
        }
    }
    //method to show a notification when krono changes the ringer mode to silent
    @SuppressLint("NewApi")
    private void showNotif( String nomEven) {


        Resources res = getResources();
        android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_notif_ico)
                .setContentTitle("Ringer mode changed")
                .setContentText("Switched to silent for " + nomEven);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_SHOW_ACTIONS);

        // Stack for the activity
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        // Show notification
        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifManager != null) {
            notifManager.notify(NOTIF_ID, builder.build());
        }
    }
    //Set system alarm to trigger service on the next event that needs muting.
    private void setNextAlarm(CalendarEvent currentEvent, long timeNow, CalendarProvider provider) {

        PendingIntent pIntent = PendingIntent.getService(this, 0, new Intent(this, MuteService.class), PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        long nextExecutionTime;
        if(currentEvent != null) { // There is an event right now: call again at the end of the event
            nextExecutionTime = currentEvent.getEndTime().getTimeInMillis();
        }
        else { // No event right now: call at the beginning of next event
            CalendarEvent nextEvent = provider.getNextEvent(timeNow);

            if(nextEvent != null)
                nextExecutionTime = nextEvent.getStartTime().getTimeInMillis();
            else
                nextExecutionTime = -1;
        }


        if(nextExecutionTime != -1) {
            // Remove previous alarms
            if (alarmManager != null) {
                alarmManager.cancel(pIntent);
            }

            // Add new alarm. We need exact accuracy to avoid having the device ring because of delayed execution.
            if(Build.VERSION.SDK_INT >= 23) {
                if (alarmManager != null) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextExecutionTime, pIntent);
                }
            }
            else if(Build.VERSION.SDK_INT >= 19) {
                if (alarmManager != null) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextExecutionTime, pIntent);
                }
            }
            else {
                if (alarmManager != null) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, nextExecutionTime, pIntent);
                }
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Timestamp used in all requests (so it remains consistent)
        long timeNow = System.currentTimeMillis();

        // Get the current event, if any
        CalendarProvider provider = new CalendarProvider(this);
        CalendarEvent currentEvent = provider.getCurrentEvent(timeNow);

        updateRingerStatus(currentEvent);

        // Setup next execution
        setNextAlarm(currentEvent, timeNow, provider);

        return START_NOT_STICKY; // The service can be destroyed now that it has finished its work
    }
    //starts the service if preference is set to enable
    public static void startIfNecessary(Context c) {
        if(PreferencesManager.getRingerAction(c) != PreferencesManager.PREF_ACTION_RINGER_NOTHING)
            c.startService(new Intent(c, MuteService.class));
        }
}
