package com.example.leo.krono.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.leo.krono.R;
//activity to show the primary help/faq screen
public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //setup the FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start email intent to give feedback
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                //set recipient to Krono's mail address
                intent.setData(Uri.parse("mailto:\"leo.krono@gmail.com\""));
                //set a random request ID to make it look official
                intent.putExtra(Intent.EXTRA_SUBJECT, "ID:#" + (int) (Math.random() * 10000 + 5) + ";Feedback on Krono");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hlp, menu);
        return true;
    }
    //listen to action bar item selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //go to about screen
            case R.id.help_about:
                Intent intent=new Intent(getBaseContext(),AboutActivity.class);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}