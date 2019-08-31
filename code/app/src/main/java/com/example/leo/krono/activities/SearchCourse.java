package com.example.leo.krono.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;

import com.example.leo.krono.CourseDatabase.Course;
import com.example.leo.krono.CourseDatabase.CourseAdapter;
import com.example.leo.krono.CourseDatabase.DatabaseAccessHelper;
import com.example.leo.krono.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guptaji on 15/10/17.
 */
//activity to allow a user to search courses by code and add to the calendar
public class SearchCourse extends AppCompatActivity {
    private EditText etSearch;
    private ListView lvCourses;
    private ArrayList<Course> mCourseArrayList = new ArrayList<Course>();
    private CourseAdapter adapter1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchcourses);
    }
    //initialise view variables
    private void initialize() {
        etSearch = (EditText) findViewById(R.id.etSearch);
        lvCourses = (ListView) findViewById(R.id.lvCourses);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialize();
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Search Courses");
        }
        // Text Change Listener to EditText field so that resultview udpates as the user types
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call back the Adapter with current characters to Filter to update the view
                adapter1.getFilter().filter(s.toString());
            }
            //callbacks
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        //Helper to access the stored database
        DatabaseAccessHelper databaseAccess = DatabaseAccessHelper.getInstance(this);
        databaseAccess.open();
        List<String[]> courses= databaseAccess.getCourses();
        databaseAccess.close();
        //load courses from database into the view
        if(mCourseArrayList.isEmpty()) {
            for (String[] element : courses) {
                mCourseArrayList.add(new Course(element[1], element[0], element[2], element[3]));
            }
        }
        adapter1 = new CourseAdapter(SearchCourse.this, mCourseArrayList);
        lvCourses.setAdapter(adapter1);
    }
}