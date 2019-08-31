package com.example.leo.krono.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.leo.krono.EventHandler;
import com.example.leo.krono.R;
import com.pepperonas.materialdialog.MaterialDialog;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//activity to show institute events from scraper
public class InstiEvents extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insti_events);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        try {
            //execute asynctask
            new get_page(this,(ListView)findViewById(R.id.eventlist)).execute("");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
//asynctask to fetch list of events and display in listview
//this class is the heart of this activity
class get_page extends AsyncTask<String, String, String> { //AsyncTask runs on a separate thread from the UI thread, use these if
                                                            // you want to do network or database access, or any heavy stuff, otherwise
                                                            // the UI will choke
    private static int minute;
    private Context context;
    private String message;
    private ListView resultsListView;
    private ProgressDialog progress;      //to display circular progress bar while loading
    //constructor
    get_page(Context context, ListView v){
        this.context=context;
        this.progress=new ProgressDialog(this.context);
        this.resultsListView=v;
    }
    protected void onPreExecute(){
        //initialise variables
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        minute = Integer.parseInt(sharedPref.getString("minute_event","10"));
        progress.setMessage("Connecting...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();
    }
    @Override
    protected void onPostExecute(String result){
        progress.dismiss();

        if (!message.equals("Unable to connect!")) //means successful http request
        {
            //message contains the results of the html query
            final HashMap<String, List<String>> nameAddresses = new HashMap<String, List<String>>();
            //break it up and store into the hashmap nameAddresses
            String[] arrofstr = message.split("<br>");
            for (String s : arrofstr) {
                String[] arr = s.split("%");
                nameAddresses.put(arr[0], Arrays.asList(arr[1],arr[2],arr[3],arr[4]));
            }

            //setting up listview...
            List<HashMap<String, String>> listItems = new ArrayList<>();
            SimpleAdapter adapter = new SimpleAdapter(context, listItems, R.layout.eventlist,
                    new String[]{"First Line", "Second Line"},
                    new int[]{R.id.text1, R.id.text2});


            for (Object o : nameAddresses.entrySet()) {
                HashMap<String, String> resultsMap = new HashMap<>();
                Map.Entry pair = (Map.Entry) o;
                resultsMap.put("First Line", pair.getKey().toString());
                List<String> a = (List<String>) pair.getValue();
                resultsMap.put("Second Line", a.get(1) + " at " + a.get(2));
                listItems.add(resultsMap);
            }

            resultsListView.setAdapter(adapter);
            resultsListView.setOnItemClickListener(new ListView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    final HashMap<String, String> o = (HashMap<String, String>) resultsListView.getAdapter().getItem(i);
                    String[] cats = {"Add to calendar", "Open in browser"};
                    //show dialog with choices on clicking list item
                    new MaterialDialog.Builder(context)
                            .title(null)
                            .dim(20)
                            .listItems(true, cats)
                            .itemClickListener(new MaterialDialog.ItemClickListener() {
                                @Override
                                public void onClick(View v, int position, long id) {
                                    switch (position) {
                                        case 0:
                                            //try to add event
                                            boolean added = EventHandler.addReminder(context, o.get("First Line"), null,
                                                    nameAddresses.get(o.get("First Line")).get(0),
                                                    nameAddresses.get(o.get("First Line")).get(1) + nameAddresses.get(o.get("First Line")).get(2),
                                                    null, null, "Events", minute);
                                            if (added) {//success
                                                Toast toast = Toast.makeText(context, "Added to Calendar!", Toast.LENGTH_SHORT);
                                                toast.show();
                                            } else {//failure
                                                Toast toast = Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT);
                                                toast.show();
                                            }
                                            break;
                                        case 1:
                                            //start intent to browser app
                                            String url = nameAddresses.get(o.get("First Line")).get(3);
                                            Intent i = new Intent(Intent.ACTION_VIEW);
                                            i.setData(Uri.parse(url));
                                            context.startActivity(i);
                                            break;
                                    }
                                }
                            })
                            .show();
                }
            });
        }
        else //if http request had failed
        {
            Toast toast = Toast.makeText(context, "Unable to connect to server!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    //background task to fetch list of events from scraper
    @Override
    protected String doInBackground(String... params)
    {
        String s =null;
        URL url;
        try {
            url = new URL("http://dbchecker.azurewebsites.net/");
        } catch (Exception e) {
            e.printStackTrace();
            return "Unable to connect!";
        }
        HttpURLConnection urlConnection = null;

        //try to connect to the server
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (Exception e) {
            //return error message if cannot connect
            e.printStackTrace();
            return "Unable to connect!";
        }

        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            s=readStream(in);
        } catch (Exception e) {
            e.printStackTrace();
            //return error message if reading from data stream fails
            this.message= "Unable to connect!";
            return "Unable to connect!";
        } finally {
            //close connection
            urlConnection.disconnect();
        }
        this.message= s;
        return s;
    }
    //method to read raw stream of data and return a string
    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is),1000);
        for (String line = r.readLine(); line != null; line =r.readLine()){
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

}
