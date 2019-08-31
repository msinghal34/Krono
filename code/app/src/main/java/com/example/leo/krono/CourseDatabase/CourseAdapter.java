package com.example.leo.krono.CourseDatabase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.leo.krono.R;
import com.example.leo.krono.eAdders.AddWeekly;
import com.pepperonas.materialdialog.MaterialDialog;

import java.util.ArrayList;

/**
 * Created by guptaji on 15/10/17.
 */
//view adapter for displaying courses that match user input on a custom listview
    //also implements click listener on the view
public class CourseAdapter extends BaseAdapter implements Filterable {
    private static final String[] cats={"Add to classes","Add to tutorials", "Add to Labs"};
    private ArrayList<Course> mOriginalValues; // Original Values
    private ArrayList<Course> mDisplayedValues;    // Values to be displayed
    private LayoutInflater inflater;
    //constructor
    public CourseAdapter(Context context, ArrayList<Course> mCourseArrayList) {
        this.mOriginalValues = mCourseArrayList;
        this.mDisplayedValues = mCourseArrayList;
        inflater = LayoutInflater.from(context);
    }
    //returns count of the items in displayed view
    @Override
    public int getCount() {
        return mDisplayedValues.size();
    }
    //returns the item at a position
    @Override
    public Object getItem(int position) {
        return position;
    }
    //returns the id of an item at a position
    @Override
    public long getItemId(int position) {
        return position;
    }
    //class to encapsulate various view elements together
    private class ViewHolder {
        LinearLayout llContainer;
        TextView courseName,courseCode,courseVenue,courseSlot;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder ;

        if (convertView == null) {
            //bind variables to the views from activiy
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.courselist, null);
            holder.llContainer = (LinearLayout)convertView.findViewById(R.id.llContainer);
            holder.courseCode=(TextView)convertView.findViewById(R.id.courseCode);
            holder.courseName = (TextView) convertView.findViewById(R.id.courseName);
            holder.courseVenue= (TextView) convertView.findViewById(R.id.courseVenue);
            holder.courseSlot= (TextView) convertView.findViewById(R.id.courseSlot);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //set texts
        holder.courseName.setText(mDisplayedValues.get(position).getName());
        holder.courseCode.setText(mDisplayedValues.get(position).getCode());
        holder.courseVenue.setText(mDisplayedValues.get(position).getVenue());
        holder.courseSlot.setText(mDisplayedValues.get(position).getSlots());

        //listen to click events on the view
        holder.llContainer.setOnClickListener(new View.OnClickListener() {
                                                  public void onClick(View v) {
                                                      //show dialog with choices to add to which calendar
                                                      new MaterialDialog.Builder(v.getContext())
                                                              .title(null)
                                                              .dim(50)
                                                              .listItems(true, cats)
                                                              .itemClickListener(new MaterialDialog.ItemClickListener() {
                                                                  @Override
                                                                  public void onClick(View v, int pos, long id) {
                                                                      Intent intent=new Intent(v.getContext(), AddWeekly.class);
                                                                      intent.putExtra("R",42);
                                                                      intent.putExtra("Na",mDisplayedValues.get(position).getName());
                                                                      intent.putExtra("Co",mDisplayedValues.get(position).getCode());
                                                                      intent.putExtra("Ve",mDisplayedValues.get(position).getVenue());
                                                                      intent.putExtra("Sl",mDisplayedValues.get(position).getSlots());
                                                                      switch (pos) {
                                                                          case 0:
                                                                              intent.putExtra("CAL","Classes");
                                                                              v.getContext().startActivity(intent);
                                                                              break;
                                                                          case 1:
                                                                              intent.putExtra("CAL","Tutorials");
                                                                              v.getContext().startActivity(intent);
                                                                              break;
                                                                          case 2:
                                                                              intent.putExtra("CAL","Labs");
                                                                              v.getContext().startActivity(intent);
                                                                              break;
                                                                      }
                                                                  }
                                                              })
                                                              .show();
                                                  }
                                              });
        return convertView;
    }
    //method that filters the view according to user input
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                mDisplayedValues = (ArrayList<Course>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the view that data has changed
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<Course> FilteredArrList = new ArrayList<Course>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<Course>(mDisplayedValues); // saves the original data in mOriginalValues
                }

                 //  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 //  else does the Filtering and returns FilteredArrList(Filtered)
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return  
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        String data = mOriginalValues.get(i).getCode();
                        if (data.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrList.add(new Course(mOriginalValues.get(i).getName(),
                                                    mOriginalValues.get(i).getCode(),
                                                        mOriginalValues.get(i).getVenue(),
                                                            mOriginalValues.get(i).getSlots()));
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }
}