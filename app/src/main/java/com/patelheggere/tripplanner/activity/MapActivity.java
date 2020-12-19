package com.patelheggere.tripplanner.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.patelheggere.tripplanner.BaseApplication;
import com.patelheggere.tripplanner.R;
import com.patelheggere.tripplanner.model.EventDetailModel;
import com.patelheggere.tripplanner.model.PlaceDetails;
import com.patelheggere.tripplanner.model.TalukByAcModel;
import com.patelheggere.tripplanner.utils.CoordinatesModel;
import com.patelheggere.tripplanner.utils.TSPNearestNeighbour;
import com.patelheggere.tripplanner.utils.Tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener {
    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private static int[][] mDistanceMatrix = new int[4][4];
    private List<EventDetailModel> eventDetailList =new ArrayList<>();
    private DatabaseReference databaseReference, databaseReferenceDeleteEdit;
    private List<EventDetailModel> filteredList, reOrderedList;
    private List<PlaceDetails> placeDetailsList;

    private TalukByAcModel talukByAcModel;
    private boolean isEventMapper;

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
        if(!isEventMapper) {
            String polyMap = getIntent().getStringExtra("MAP");

            polyMap = polyMap.replace("POLYGON ((", "");
            polyMap = polyMap.replace("))", "");
            polyMap = polyMap.replace(")", "");
            polyMap = polyMap.replace("(", "");

            drawPolygon1(polyMap, "");

            String pol = talukByAcModel.getPolyGon().replace("POLYGON ((", "");
            pol = pol.replace("))", "");
            pol = pol.replace(")", "");
            pol = pol.replace("(", "");


            LatLng lt = drawPolygon(pol, talukByAcModel.getKGISTHobliName());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lt));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lt, 10));
            mMap.setOnPolygonClickListener(this);
        }
        else if(placeDetailsList!=null && placeDetailsList.size()>0)
        {
            for(int i=0;i<placeDetailsList.size(); i++)
            {
                drawMarker(new LatLng(placeDetailsList.get(i).getLat(), placeDetailsList.get(i).getLng()), placeDetailsList.get(i).getName());
            }

            double lt = placeDetailsList.get(placeDetailsList.size()/2).getLat();
            double lng = placeDetailsList.get(placeDetailsList.size()/2).getLng();
            LatLng ltln = new LatLng(lt, lng);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(ltln));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ltln, 15));
        }
    }

    private LatLng drawPolygon(String poly, String tag)
    {
        String points[] = poly.split(",");
        String lats[] = new String[points.length];
        String longs[] = new String[points.length];
        for(int i=0;i<points.length; i++){
            String pt = points[i].trim().replace(" ","-");
            String coord[] = pt.split("-");
            lats[i]=coord[1];
            longs[i]=coord[0];
        }
        List<LatLng> latLngs = new ArrayList<>();
        for(int i=0;i<points.length; i++){
            CoordinatesModel coordinatesModel = Tools.UTM2Deg(lats[i], longs[i]);
            LatLng latLng = new LatLng(coordinatesModel.getLat(), coordinatesModel.getLon());
            latLngs.add(latLng);
        }
        Polygon polygon1 = mMap.addPolygon(new PolygonOptions()
                .clickable(true)
                .addAll(latLngs));
        polygon1.setTag(tag);
        polygon1.setStrokeColor(Color.RED);

        return latLngs.get(latLngs.size()/2);

    }

    private void drawPolygon1(String poly, String tag)
    {
        String points[] = poly.split(",");
        String lats[] = new String[points.length];
        String longs[] = new String[points.length];
        for(int i=0;i<points.length; i++){
            String pt = points[i].trim().replace(" ","-");
            String coord[] = pt.split("-");
            lats[i]=coord[1];
            longs[i]=coord[0];
        }
        List<LatLng> latLngs = new ArrayList<>();
        for(int i=0;i<points.length; i++){
            CoordinatesModel coordinatesModel = Tools.UTM2Deg(lats[i], longs[i]);
            LatLng latLng = new LatLng(coordinatesModel.getLat(), coordinatesModel.getLon());
            latLngs.add(latLng);
        }
        Polygon polygon1 = mMap.addPolygon(new PolygonOptions()
                .clickable(true)
                .addAll(latLngs));
        polygon1.setTag(tag);
        polygon1.setStrokeColor(Color.YELLOW);

    }
    private void initData()
    {
        talukByAcModel = getIntent().getParcelableExtra("DATA");
        isEventMapper = getIntent().getBooleanExtra("IS_EVENT", false);
        placeDetailsList = getIntent().getParcelableArrayListExtra("PLACE_DATA");
        if(!isEventMapper) {
            databaseReference = BaseApplication.getFireBaseRef();
            //  databaseReferenceDistance = BaseApplication.getFireBaseRef().child("");
            databaseReference.child("eventDetails").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    eventDetailList.clear();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        EventDetailModel eventDetailModel = new EventDetailModel();
                        eventDetailModel = dataSnapshot1.getValue(EventDetailModel.class);
                        eventDetailModel.setKey(dataSnapshot1.getKey());
                        Log.d(TAG, "onDataChange: Key" + dataSnapshot1.getKey());
                        eventDetailList.add(eventDetailModel);
                    }
                    Collections.sort(eventDetailList, new Comparator<EventDetailModel>() {
                        @Override
                        public int compare(EventDetailModel o1, EventDetailModel o2) {
                            return Long.valueOf(o1.getTimeStamp()).compareTo(o2.getTimeStamp());
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // mProgressBar.setVisibility(View.GONE);
                }
            });
        }
        else if(placeDetailsList!=null && placeDetailsList.size()>0)
        {

        }

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

    @Override
    public void onPolygonClick(Polygon polygon) {
        Log.d(TAG, "onPolygonClick: "+polygon.getTag().toString());
    }
}