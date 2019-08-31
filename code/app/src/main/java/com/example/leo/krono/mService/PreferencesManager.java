package com.example.leo.krono.mService;

/**
 * Created by guptaji on 10/10/17.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.leo.krono.EventHandler;

import java.util.LinkedHashMap;
//class to manage preferences used by the service
class PreferencesManager {

    private static final String PREFS_NAME = "mainPreferences";

    private static final String PREF_RESTORE_STATE = "restaurerEtat";

    private static final String PREF_SAVED_MODE = "lastMode";

    private static final String PREF_SHOW_NOTIF = "afficherNotif";

    private static final String PREF_LAST_SET_RINGER_MODE = "lastSetRingerMode";

    static final int PREF_ACTION_RINGER_NOTHING = 0;
    static final int PREF_ACTION_RINGER_SILENT = 1;

    static final int PREF_SAVED_MODE_NO_VALUE = -99;

    private static final boolean PREF_RESTORE_STATE_DEFAULT = true;

    private static final boolean PREF_SHOW_NOTIF_DEFAULT = true;

    static final int PREF_LAST_SET_RINGER_MODE_NO_MODE = -99;

    //Gets the list of calendars which the user has chosen to enable muting on in the settings.
    static LinkedHashMap<Long, Boolean> getCheckedCalendars(Context context) {
        LinkedHashMap<Long, Boolean> res = new LinkedHashMap<>();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        EventHandler.populateIDS(context);
        if(sharedPref.getBoolean("mute_class",false))
            res.put(EventHandler.CalIDS.get("Classes"),sharedPref.getBoolean("mute_class",true));
        if(sharedPref.getBoolean("mute_tutorial",false))
            res.put(EventHandler.CalIDS.get("Tutorials"),sharedPref.getBoolean("mut_tutorial",true));
        if(sharedPref.getBoolean("mute_exam",false))
            res.put(EventHandler.CalIDS.get("Exams"),sharedPref.getBoolean("mute_exam",true));
        if(sharedPref.getBoolean("mute_lab",false))
            res.put(EventHandler.CalIDS.get("Labs"),sharedPref.getBoolean("mute_lab",true));
        return res;
    }


    static void saveMode(Context context, int mode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putInt(PREF_SAVED_MODE, mode).apply();
    }

    static void setLastSetRingerMode(Context context, int ringerMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putInt(PREF_LAST_SET_RINGER_MODE, ringerMode).apply();
    }

    static int getSavedMode(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(PREF_SAVED_MODE, PREF_SAVED_MODE_NO_VALUE);
    }

    static int getRingerAction(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if(sharedPref.getBoolean("mute_toggle",true)){
            return PREF_ACTION_RINGER_SILENT;
        }
        else
            return PREF_ACTION_RINGER_NOTHING;
    }

    static boolean getRestoreState(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(PREF_RESTORE_STATE, PREF_RESTORE_STATE_DEFAULT);
    }

    static boolean getShowNotif(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(PREF_SHOW_NOTIF, PREF_SHOW_NOTIF_DEFAULT);
    }

    static int getLastSetRingerMode(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(PREF_LAST_SET_RINGER_MODE, PREF_LAST_SET_RINGER_MODE_NO_MODE);
    }
}
