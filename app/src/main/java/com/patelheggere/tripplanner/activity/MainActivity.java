package com.patelheggere.tripplanner.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.patelheggere.tripplanner.BaseApplication;
import com.patelheggere.tripplanner.R;
import com.patelheggere.tripplanner.adapter.EventAdapter;
import com.patelheggere.tripplanner.model.DistanceFromToValue;
import com.patelheggere.tripplanner.model.DistanceModel;
import com.patelheggere.tripplanner.model.EventDetailModel;
import com.patelheggere.tripplanner.utils.DataParser;
import com.patelheggere.tripplanner.utils.GPSTracker;
import com.patelheggere.tripplanner.utils.TSPNearestNeighbour;
import com.patelheggere.tripplanner.utils.UtilsClass;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EventAdapter.SelectEditDelete {
    private static final String TAG = "MainActivity";
    private List<EventDetailModel> eventDetailList =new ArrayList<>();
    private DatabaseReference databaseReference, databaseReferenceDeleteEdit;
    private EventAdapter mAdapter;
    private RecyclerView mRecyclerViewEvents;
    private List<DistanceFromToValue> distanceFromToValues;
    private FloatingActionButton mFloatingActionButton;
    private Button mButtonSeePath, mButtonDate;
    private List<EventDetailModel> filteredList, reOrderedList;
    private String ddmmyyyy;
    private int year, month, day, mHour, mMinute;;
    private Calendar calendar;
    private TextView textViewNoData;
    private ProgressBar mProgressBar;
    GPSTracker gps;
    double latitude,  longitude;
    private int NoOfPoints=0;
    private AlertDialog alertDialog;
    private PopupWindow popupWindow;
    private LinearLayout mConstraintLayoutRoot;
    private Button closePopupBtn;
    private int selectedStartPoint = -1;
    private String selectedStartPlace;
    private static int[][] mDistanceMatrix = new int[32][32];
    private int NoOfCalls=-1;
    private DatabaseReference databaseReferenceDistance;
    private RadioGroup radioGroupPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkLocationPermission();
        intiViews();
        initData();
        initListener();
        gps = new GPSTracker(this);
        if(gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            Log.d(TAG, "onCreate: "+latitude+" long:"+longitude);
            // \n is for new line
           // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            gps.showSettingsAlert();
        }
    }

    private void intiViews() {
        mRecyclerViewEvents = findViewById(R.id.recyclerView);
        mFloatingActionButton = findViewById(R.id.floatingButtonAddEvent);
        mButtonDate = findViewById(R.id.buttonDate);
        mButtonSeePath = findViewById(R.id.buttonPreview);
        textViewNoData = findViewById(R.id.textViewNoEvent);
        mProgressBar = findViewById(R.id.progresBar);
        mButtonDate.setEnabled(false);
        mButtonSeePath.setEnabled(false);
    }

    private void initData()
    {
        mConstraintLayoutRoot = findViewById(R.id.linearLayoutTop);
        mProgressBar.setVisibility(View.VISIBLE);
        ddmmyyyy = UtilsClass.getDateDDMMYYY(System.currentTimeMillis());
        mButtonDate.setText(ddmmyyyy);
        databaseReference = BaseApplication.getFireBaseRef();
      //  databaseReferenceDistance = BaseApplication.getFireBaseRef().child("");
        databaseReference.child("eventDetails").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventDetailList.clear();
                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    EventDetailModel eventDetailModel = new EventDetailModel();
                    eventDetailModel = dataSnapshot1.getValue(EventDetailModel.class);
                    eventDetailModel.setKey(dataSnapshot1.getKey());
                    Log.d(TAG, "onDataChange: Key"+dataSnapshot1.getKey());
                    eventDetailList.add(eventDetailModel);
                }
                Collections.sort(eventDetailList, new Comparator<EventDetailModel>() {
                    @Override
                    public int compare(EventDetailModel o1, EventDetailModel o2) {
                        return Long.valueOf(o1.getTimeStamp()).compareTo(o2.getTimeStamp());
                    }
                });
                if(eventDetailList.size()>0)
                {
                    mButtonDate.setEnabled(true);
                    mButtonSeePath.setEnabled(true);
                }
                filteredList = new ArrayList<>();
                updateList();
                mProgressBar.setVisibility(View.GONE);

                //DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(MainActivity.this, linearLayoutManager.getOrientation());
                //mRecyclerViewEvents.addItemDecoration(dividerItemDecoration);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mProgressBar.setVisibility(View.GONE);
            }
        });

    }
    private void initListener()
    {
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddEventActivity.class));
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
        mButtonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                mHour = calendar.get(Calendar.HOUR_OF_DAY);
                mMinute = calendar.get(Calendar.MINUTE);
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        String dd = "", mm = "";
                        if (i2 < 10) {
                            dd = "0" + i2;
                        } else {
                            dd = "" + i2;
                        }
                        if (i1 < 9) {
                            mm = "0" + (i1 + 1);
                        } else {
                            mm = "" + (i1 + 1);
                        }
                        String date = dd+"/"+mm+"/"+i;

                        mButtonDate.setText(UtilsClass.convertToTimestamp(date));
                        updateList();
                    }
                }, year, month, day);
                // datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        mButtonSeePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(filteredList.size()==0)
                {
                    Toast.makeText(MainActivity.this, "No Events", Toast.LENGTH_LONG).show();
                }
                else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    double lat, lon;
                    distanceFromToValues = new ArrayList<>();
                    NoOfPoints = 0;
                    for (int i = 0; i < filteredList.size(); i++) {
                        if(latitude==0.0)
                        {
                            lat = 16.5132675;
                        }
                        else {
                            lat = latitude;
                        }
                        if(longitude==0.0)
                        {
                            lon =74.8991483;
                        }
                        else {
                            lon = longitude;
                        }
                        CreatePath(new LatLng(lat, lon), new LatLng(filteredList.get(i).getLat(), filteredList.get(i).getLon()));
                    }
                }

            }
        });
    }

    private void updateList() {
        filteredList.clear();
        if(eventDetailList!=null) {
            for (int i = 0; i < eventDetailList.size(); i++) {
                if (mButtonDate.getText().toString().equalsIgnoreCase(UtilsClass.getDateDDMMYYY(eventDetailList.get(i).getTimeStamp()))) {
                    filteredList.add(eventDetailList.get(i));
                }
            }
            if (filteredList.size() == 0) {
                mRecyclerViewEvents.setVisibility(View.GONE);
                textViewNoData.setVisibility(View.VISIBLE);

            } else {
                mAdapter = new EventAdapter(MainActivity.this, filteredList);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
                mRecyclerViewEvents.setLayoutManager(linearLayoutManager);
                mRecyclerViewEvents.setAdapter(mAdapter);
                mRecyclerViewEvents.setVisibility(View.VISIBLE);
                textViewNoData.setVisibility(View.GONE);
            }
        }
    }


    public void CreatePath(LatLng source, LatLng destination)
    {
        NoOfPoints+=1;
        NoOfCalls+= 1;
        String url = getUrl(source, destination);
        FetchUrl FetchUrl = new FetchUrl();
        FetchUrl.execute(url);
    }

    @Override
    public void selectedEditPosition(EventDetailModel position) {
        Intent intent = new Intent(this, AddEventActivity.class);
        intent.putExtra("Data", position);
        intent.putExtra("EDIT", true);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
       // Toast.makeText(this, position.getEventName(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void selectedDeletePosition(final EventDetailModel position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_confirm));
        builder.setMessage(getString(R.string.delete_msg));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                databaseReferenceDeleteEdit = BaseApplication.getFireBaseRef();
                databaseReferenceDeleteEdit = databaseReferenceDeleteEdit.child("eventDetails").child(position.getKey());
                databaseReferenceDeleteEdit.removeValue();
                alertDialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();

    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.startPoint:
               addStartPoint(filteredList);
                //upload();
                // Toast.makeText(getApplicationContext(),"Item 1 Selected",Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addStartPoint(final List<EventDetailModel> filteredList) {
        LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = layoutInflater.inflate(R.layout.start_point_popup,null);
        closePopupBtn = customView.findViewById(R.id.closeButton);
        radioGroupPlaces = customView.findViewById(R.id.radioGroup);
        for (int k=0; k<filteredList.size(); k++)
        {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(filteredList.get(k).getPlaceName());
            /*if(k==0)
            {
                //radioButton.setChecked(true);
                //selectedStartPoint = 0;
            }*/
            radioGroupPlaces.addView(radioButton);
        }

        if(radioGroupPlaces!=null)
        {
            radioGroupPlaces.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    selectedStartPoint = i-1;
                    Log.d(TAG, "addStartPoint: "+selectedStartPoint);
                }
            });
        }


        //instantiate popup window
        popupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        //display the popup window
        popupWindow.showAtLocation(mConstraintLayoutRoot, Gravity.BOTTOM, 0, 0);
        closePopupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                try {
                    if(selectedStartPoint!=-1)
                    {
                        mProgressBar.setVisibility(View.VISIBLE);
                        NoOfCalls = 0;
                        selectedStartPlace = filteredList.get(selectedStartPoint).getPlaceName();
                        reOrderedList = new ArrayList<>(filteredList);
                        EventDetailModel eventDetailModel = reOrderedList.get(selectedStartPoint);
                        reOrderedList.remove(selectedStartPoint);
                        reOrderedList.add(0, eventDetailModel);
                        int mListSize = reOrderedList.size();
                        for (int i=0; i<mListSize; i++)
                        {
                            for (int j = 0 ; j<mListSize;j++ )
                            {
                                if(i!=j)
                                {
                                    Log.d(TAG, "onDismiss: "+reOrderedList.get(i).getPlaceName()+" to "+reOrderedList.get(j).getPlaceName());
                                    CreatePath(new LatLng(reOrderedList.get(i).getLat(), reOrderedList.get(i).getLon()), new LatLng(reOrderedList.get(j).getLat(), reOrderedList.get(j).getLon()));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    finish();
                }
            }
        });

    }

    private class ParserTask extends AsyncTask<String, Integer, DistanceModel> {

        // Parsing the data in non-ui thread
        @Override
        protected DistanceModel doInBackground(String... jsonData) {

            JSONObject jObject;
            Long distanceText = null;
            DistanceModel distanceModel = new DistanceModel();
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DataParser parser = new DataParser();
                // Starts parsing data
                //routes = parser.parse(jObject);
                distanceModel = parser.parseDistnace(jObject);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return distanceModel;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(DistanceModel distance) {
            mProgressBar.setVisibility(View.GONE);
            NoOfCalls-=1;
            Log.d(TAG, "onPostExecute: no of calls:"+NoOfCalls);
            DecimalFormat decimalFormat = new DecimalFormat("##.###");
            if(reOrderedList!=null) {
                for (int i = 1; i <= reOrderedList.size(); i++) {
                    for (int j = 1; j <= reOrderedList.size(); j++) {
                        if (i != j && mDistanceMatrix[i][j] == 0) {
                            mDistanceMatrix[i][j] = (int) distance.getDistance() / 1000;
                            if (NoOfCalls == 0)
                            {
                                selectedStartPoint = -1;
                                String data = "";
                                TSPNearestNeighbour tspNearestNeighbour = new TSPNearestNeighbour();
                                int arr[] = tspNearestNeighbour.tsp(mDistanceMatrix, reOrderedList);
                                for (int m = 0; m < arr.length; m++) {
                                    data += (m + 1) + "." + reOrderedList.get(arr[m]).getPlaceName() + "\n";
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                View popupView = inflater.inflate(R.layout.path_layout, null, false);
                                LinearLayout linearLayout = popupView.findViewById(R.id.linearLayoutPath);
                                TextView textView = popupView.findViewById(R.id.textViewDistance);
                                textView.setText(data);
                                builder.setView(popupView);
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                                radioGroupPlaces = null;
                                mProgressBar.setVisibility(View.GONE);
                            }
                            databaseReferenceDistance = BaseApplication.getFireBaseRef();
                            databaseReferenceDistance = databaseReferenceDistance.child("distanceMatrix").child(reOrderedList.get(i - 1).getPlaceName() + "_TO_" + reOrderedList.get(j - 1).getPlaceName());
                            databaseReferenceDistance.setValue(mDistanceMatrix[i][j]);
                            //Log.d(TAG, "onPostExecute: distance:"+filteredList.get(i).getPlaceName() +" to "+filteredList.get(j).getPlaceName()+" :" +mDistanceMatrix[i][j]);
                            return;
                        }
                    }
                }
            }
            /*if(NoOfCalls==0)
            {
                Log.d(TAG, "onPostExecute: cities visited:");
                TSPNearestNeighbour tspNearestNeighbour = new TSPNearestNeighbour();
                tspNearestNeighbour.tsp(mDistanceMatrix);
            }*/

            /*for (int i=0; i<filteredList.size(); i++)
            {
                if(decimalFormat.format(filteredList.get(i).getLat()).equalsIgnoreCase(decimalFormat.format(distance.getLat())))
                {
                    DistanceFromToValue distanceFromToValue = new DistanceFromToValue();
                    distanceFromToValue.setDistance(distance.getDistance());
                    distanceFromToValue.setEventName(filteredList.get(i).getEventName());
                    distanceFromToValue.setPlaceName(filteredList.get(i).getPlaceName());
                    distanceFromToValues.add(distanceFromToValue);
                }
            }*/

            /*DistanceFromToValue distanceFromToValue = new DistanceFromToValue();
            distanceFromToValue.setDistance(distance.getDistance());
            distanceFromToValues.add(distanceFromToValue);

            //Log.d(TAG, "onPostExecute: "+distanceFromToValues.size()+" Noof:"+NoOfPoints);
            if(distanceFromToValues.size()==NoOfPoints)
            {
                for (int i=0; i<filteredList.size(); i++)
                {
                    distanceFromToValues.get(i).setPlaceName(filteredList.get(i).getPlaceName());
                    //Log.d(TAG, "onPostExecute: "+distanceFromToValues.get(i).getDistance());
                }
                Collections.sort(distanceFromToValues, new Comparator<DistanceFromToValue>() {
                    @Override
                    public int compare(DistanceFromToValue o1, DistanceFromToValue o2) {
                        return Long.valueOf(o1.getDistance()).compareTo(o2.getDistance());
                    }
                });
                String data="";
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.path_layout, null, false);
                LinearLayout linearLayout = popupView.findViewById(R.id.linearLayoutPath);
                for (int i=0; i<distanceFromToValues.size()-1; i++)
                {

                    TextView textView = new TextView(MainActivity.this);
                    textView.setId(i);
                    data+=distanceFromToValues.get(i).getPlaceName()+"("+ distanceFromToValues.get(i).getDistance()/1000+" KM)"+"->\n";
                    textView.setText(distanceFromToValues.get(i).getPlaceName()+"("+ distanceFromToValues.get(i).getDistance()/1000+" KM)"+"->");
                    textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                   // linearLayout.addView(textView);
                }
                TextView textView = popupView.findViewById(R.id.textViewDistance);
               // textView.setId(distanceFromToValues.size()-1);
                textView.setText(data+""+distanceFromToValues.get(distanceFromToValues.size()-1).getPlaceName()+"("+ distanceFromToValues.get(distanceFromToValues.size()-1).getDistance()/1000+" KM)");
                //textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                //linearLayout.addView(textView);
                builder.setView(popupView);
                AlertDialog alertDialog =  builder.create();
                alertDialog.show();
            }*/
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    private String getUrl(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        String sensor = "sensor=false";

        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters+"&key=AIzaSyC8ziRl8pEORuiLsKYHuE-PQ_rh7cnjpcU";

        return url;
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        //locationManager.requestLocationUpdates(provider, 400, 1, this);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

    @Override
    protected void onStop() {
        gps.stopUsingGPS();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if(popupWindow!=null && popupWindow.isShowing())
        {
            popupWindow.dismiss();
            return;
        }
        super.onBackPressed();
    }
}
