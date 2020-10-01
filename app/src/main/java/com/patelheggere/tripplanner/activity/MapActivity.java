package com.patelheggere.tripplanner.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.patelheggere.tripplanner.BaseApplication;
import com.patelheggere.tripplanner.R;
import com.patelheggere.tripplanner.model.EventDetailModel;
import com.patelheggere.tripplanner.utils.TSPNearestNeighbour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private static int[][] mDistanceMatrix = new int[4][4];
    private List<EventDetailModel> eventDetailList =new ArrayList<>();
    private DatabaseReference databaseReference, databaseReferenceDeleteEdit;
    private List<EventDetailModel> filteredList, reOrderedList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initData();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        LatLng TutorialsPoint = new LatLng(14.1655606, 76.6480134);
         mMap.addMarker(new MarkerOptions().position(TutorialsPoint).title("patel"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(TutorialsPoint));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(TutorialsPoint, 15));
    }

    private void initData()
    {
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

                filteredList = new ArrayList<>();
                for(int i=0;i<5; i++) {
                    for (int j = 0; j < 5; j++) {
                        if (i != j && mDistanceMatrix[i][j] == 0) {
                            mDistanceMatrix[i][j] = (i+j)+10;
                        }
                    }
                }
                int arr[] = new TSPNearestNeighbour().tsp(mDistanceMatrix, eventDetailList);
                for (int i=0;i<arr.length; i++)
                {
                    Log.d(TAG, "onDataChange: Distance:"+arr[i]);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
               // mProgressBar.setVisibility(View.GONE);
            }
        });

    }


    /* private ApiInterface apiInterface;

     private void setUpNetwork() {
         RetrofitInstance retrofitInstance = new RetrofitInstance();
         retrofitInstance.setClient();
         apiInterface = retrofitInstance.getClient().create(ApiInterface.class);
     }
 */
    private void drawMarker(LatLng point, String village) {
// Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(village);

// Setting latitude and longitude for the marker
        markerOptions.position(point);

// Adding marker on the Google Map
        mMap.addMarker(markerOptions);
    }
}