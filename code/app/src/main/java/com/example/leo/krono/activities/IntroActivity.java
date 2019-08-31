package com.example.leo.krono.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.example.leo.krono.R;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;

/**
 * Created by guptaji on 20/10/17.
 */
//activity to show introduction to first time users of app
public class IntroActivity extends AppIntro2 {
    public static String COMPLETED_ONBOARDING_PREF_NAME="showOrnot";  //name of preference
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    //add intro slides
        addSlide(AppIntro2Fragment.newInstance("Welome to Krono", "Managing your life made simpler", R.drawable.main_screen,getResources().getColor(R.color.bpDark_gray)));
        addSlide(AppIntro2Fragment.newInstance("Material design", "Intuitive layout makes navigating easier", R.drawable.material_screen,getResources().getColor(R.color.bpLight_gray)));
        addSlide(AppIntro2Fragment.newInstance("Made for IIT-Bombay","Automatically add your course with one click",R.drawable.course_screen,getResources().getColor(R.color.bpDark_gray)));
        addSlide(AppIntro2Fragment.newInstance("Stay up-to-date","Interesting events in the Insti made available to you",R.drawable.insti_screen,getResources().getColor(R.color.bpLight_gray)));
        //hide actionbar
        if(getSupportActionBar()!=null) {
            getSupportActionBar().hide();
        }
        //set animation
        setFlowAnimation();
        //hide status bar
        showStatusBar(false);
        // Hide Skip/Done button.
        showSkipButton(true);
        setProgressButtonEnabled(true);
        setVibrate(false);
        //set preference to not show this after first run
        SharedPreferences.Editor sharedPreferencesEditor =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
        sharedPreferencesEditor.putBoolean(
                COMPLETED_ONBOARDING_PREF_NAME, true);
        sharedPreferencesEditor.apply();
}
    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        finish();
        Toast.makeText(getBaseContext(),"Setting up Krono, this might take a few seconds...",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        finish();
        Toast.makeText(getBaseContext(),"Setting up Krono, this might take a few seconds...",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
