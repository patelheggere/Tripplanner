package com.patelheggere.tripplanner.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.patelheggere.tripplanner.BaseApplication;
import com.patelheggere.tripplanner.R;
import com.patelheggere.tripplanner.adapter.EventAdapter;
import com.patelheggere.tripplanner.adapter.MatantaraEventAdapter;
import com.patelheggere.tripplanner.model.APIResponseModel;
import com.patelheggere.tripplanner.model.EventDetailModel;
import com.patelheggere.tripplanner.model.PlaceDetails;
import com.patelheggere.tripplanner.network.ApiInterface;
import com.patelheggere.tripplanner.network.RetrofitInstance;
import com.patelheggere.tripplanner.utils.GPSTracker;
import com.patelheggere.tripplanner.utils.SharedPrefsHelper;
import com.patelheggere.tripplanner.utils.UtilsClass;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.patelheggere.tripplanner.utils.Constants.ID;

public class MatantaraActivity extends AppCompatActivity implements MatantaraEventAdapter.SelectEditDelete{
    private static final String TAG = "MatantaraActivity";
    private DatabaseReference databaseReference;
    private List<PlaceDetails> placeDetailsList;
    private GPSTracker gps;
    private double latitude,  longitude;
    private MatantaraEventAdapter mAdapter;
    private RecyclerView mRecyclerViewEvents;
    private Button mButtonSeePath, mButtonDate;
    private ProgressBar mProgressBar;
    private int year, month, day, mHour, mMinute;;
    private Calendar calendar;
    private String ddmmyyyy, ddmmyyyy2;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matantara);
        mActionBar = getSupportActionBar();
        if(mActionBar!=null)
        {
            mActionBar.setTitle("Trip Planner");
        }
        setUpNetwork();
        mRecyclerViewEvents = findViewById(R.id.recyclerView);
        mButtonDate = findViewById(R.id.buttonDate);
        mButtonSeePath = findViewById(R.id.buttonPreview);
        mProgressBar = findViewById(R.id.progresBar);
        //mButtonDate.setEnabled(false);
        mButtonSeePath.setEnabled(false);
        initListener();
        gps = new GPSTracker(this);
        get_location_data();
        if(gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            Log.d(TAG, "onCreate: "+latitude+" long:"+longitude);
            //getPlaces();
            } else {
            gps.showSettingsAlert();
        }
    }
    private void initListener()
    {
        ddmmyyyy = UtilsClass.getDateDDMMYYY(System.currentTimeMillis());
        ddmmyyyy2 = UtilsClass.getDateDDMMYYY2(System.currentTimeMillis());

        getPlaceByTeam();
        mButtonDate.setText(ddmmyyyy);
        mButtonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                mHour = calendar.get(Calendar.HOUR_OF_DAY);
                mMinute = calendar.get(Calendar.MINUTE);
                DatePickerDialog datePickerDialog = new DatePickerDialog(MatantaraActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                        ddmmyyyy2 = UtilsClass.getDateDDMMYYY2(UtilsClass.convertToTimestamp2(date));
                        getPlaceByTeam();
                    }
                }, year, month, day);
                // datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        mButtonSeePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MatantaraActivity.this, MapActivity.class);
               intent.putParcelableArrayListExtra("PLACE_DATA", (ArrayList<? extends Parcelable>) placeDetailsList);
                intent.putExtra("IS_EVENT", true);
                startActivity(intent);
            }
        });
    }

    private void getPlaceByTeam(){
        mProgressBar.setVisibility(View.VISIBLE);
        Call<List<PlaceDetails>> call = apiInterface.GetTandaByTeamID(SharedPrefsHelper.getInstance().get(ID).toString(), ddmmyyyy2);
        call.enqueue(new Callback<List<PlaceDetails>>() {
            @Override
            public void onResponse(Call<List<PlaceDetails>> call, Response<List<PlaceDetails>> response) {
                mProgressBar.setVisibility(View.GONE);

                if(response.body()!=null && response.body().size()>=0)
                {
                    placeDetailsList = new ArrayList<>();
                    for(int i=0; i<response.body().size(); i++){
                        PlaceDetails eventDetailModel = response.body().get(i);
                        placeDetailsList.add(eventDetailModel);
                        eventDetailModel.setDistance(distance(latitude, longitude, eventDetailModel.getLat(), eventDetailModel.getLng()));
                    }
                Collections.sort(placeDetailsList, PlaceDetails.distanceComparator);
                mAdapter = new MatantaraEventAdapter(MatantaraActivity.this, placeDetailsList);
                setEvenData();
                if(placeDetailsList.size()>0)
                {
                    mButtonDate.setEnabled(true);
                    mButtonSeePath.setEnabled(true);
                }
                }
            }

            @Override
            public void onFailure(Call<List<PlaceDetails>> call, Throwable t) {
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(MatantaraActivity.this, "some thing wrong", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void getPlaces()
    {
        databaseReference = BaseApplication.getFireBaseRef();
        databaseReference = databaseReference.child("places");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                placeDetailsList = new ArrayList<>();
                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    PlaceDetails eventDetailModel = new PlaceDetails();
                    eventDetailModel = dataSnapshot1.getValue(PlaceDetails.class);
                    placeDetailsList.add(eventDetailModel);
                    eventDetailModel.setDistance(distance(latitude, longitude, eventDetailModel.getLat(), eventDetailModel.getLng()));
                    Log.d(TAG, "distance: "+distance(latitude, longitude, eventDetailModel.getLat(), eventDetailModel.getLng()));
                }
                Collections.sort(placeDetailsList, PlaceDetails.distanceComparator);
                mAdapter = new MatantaraEventAdapter(MatantaraActivity.this, placeDetailsList);
                setEvenData();
                if(placeDetailsList.size()>0)
                {
                    mButtonDate.setEnabled(true);
                    mButtonSeePath.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void setEvenData()
    {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MatantaraActivity.this, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewEvents.setLayoutManager(linearLayoutManager);
        mRecyclerViewEvents.setAdapter(mAdapter);
        mRecyclerViewEvents.setVisibility(View.VISIBLE);
    }
    private ApiInterface apiInterface;

    private void setUpNetwork() {
        RetrofitInstance retrofitInstance = new RetrofitInstance();
        retrofitInstance.setClient();
        apiInterface = retrofitInstance.getClient().create(ApiInterface.class);
    }
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            /*
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }

             */
            //return in meter
            return (dist);
        }
    }
    LocationListener mlocListener;
    LocationManager mlocManager;
    static Location loc1;
    public void get_location_data() {
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
        if (!mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && !mlocManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            showGPSDisabledAlertToUser();
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
        } else {
            if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                loc1 = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else if (mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                loc1 = mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else if (mlocManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                loc1 = mlocManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }
            if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mlocListener);
            } else if (mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, mlocListener);
            } else if (mlocManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                mlocManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0, mlocListener);
            }

        }
    }
    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);

                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 96;

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }

        }
    }

    @Override
    public void selectedEditPosition(PlaceDetails position) {
        mProgressBar.setVisibility(View.VISIBLE);
        Call<APIResponseModel> call = apiInterface.UpdateCompletion(position.getId()+"");
        call.enqueue(new Callback<APIResponseModel>() {
            @Override
            public void onResponse(Call<APIResponseModel> call, Response<APIResponseModel> response) {
                mProgressBar.setVisibility(View.GONE);
                if(response.body().isStatus())
                {
                    getPlaceByTeam();
                }
            }

            @Override
            public void onFailure(Call<APIResponseModel> call, Throwable t) {
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(MatantaraActivity.this, "some thing wrong", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void selectedDeletePosition(PlaceDetails position) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr="+latitude+","+longitude+"&daddr="+position.getLat()+","+position.getLng()+""));
                 startActivity(intent);
    }

    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            loc1 = loc;
            long time = loc1.getTime();
            Date date = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if(loc1!=null){
                latitude = loc1.getLatitude();
                longitude = loc1.getLongitude();
            }
          //  currentDateandTime = sdf.format(date);
            //  Log.d("currentDateandTime",currentDateandTime);
           // tvpoint.setText(String.valueOf(loc1.getLatitude()) + " N," + String.valueOf(loc1.getLongitude()) + " E   " + sdf.format(date));
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS disabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS enabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }


}