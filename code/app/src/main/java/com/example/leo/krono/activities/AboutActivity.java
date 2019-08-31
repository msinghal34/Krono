package com.example.leo.krono.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import com.example.leo.krono.R;

/**
 * Created by guptaji on 16/10/17.
 */
//activity that shows the about us screen
public class AboutActivity extends AppCompatActivity {
    //html formatted string for the info
    private static final String about_string="Krono has been designed and developed by Team Leo, a team of these three IIT-Bombay students:<br><br> " +
            "&#8226; Yash Gupta<br>&#8226; Mayank Singhal<br>&#8226; Neelesh Verma<br> <h2>Indirect Contributors</h2>&#8226; Jeff Gilfelt <a href=\"https://github.com/jgilfelt/android-sqlite-asset-helper\">(SQL Asset Helper)</a><br>" +
            "&#8226; Thibault Guégan <a href=\"https://github.com/Tibolte/AgendaCalendarView\">(Agenda Calendar View)</a><br>&#8226; Raquib-ul-Alam <a href=\"https://github.com/alamkanak/Android-Week-View\">(Android Week View)</a><br>" +
            "&#8226; Derek Brameyer <a href=\"https://github.com/code-troopers/android-betterpickers\">(BetterPickers)</a><br>" +
            "&#8226; Kizito Nwose <a href=\"https://github.com/kizitonwose/colorpreference\">(ColorPreference)</a><br>&#8226; Martin Pfeffer <a href=\"https://github.com/pepperonas/MaterialDialog\">(Material Dialogs)</a><br>" +
            "&#8226; Paolo Rotolo <a href=\"https://github.com/apl-devs/AppIntro\">(AppIntro)</a><br>" +
            "<br><b>Thanks to ASC, IIT-Bombay for running course data!</b>";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        //get version name of current build
        PackageManager manager = getBaseContext().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getBaseContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = null;
        if (info != null) {
            version = info.versionName;
        }
        //set version name in textview
        ((TextView)findViewById(R.id.about_version)).setText("Version "+ version);
        //set info in textview
        setTextViewHTML((TextView)findViewById(R.id.about_text),about_string);
    }
    //method that finds URLs in spannable string and assigns a click listener to each
    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span)
    {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(span.getURL()));
                view.getContext().startActivity(i);
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }
    //method that takes a textview and a html string and makes all the links clickable
    protected void setTextViewHTML(TextView text, String html)
    {
        CharSequence sequence;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            sequence = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            sequence = Html.fromHtml(html);
        }
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
