package com.patelheggere.tripplanner.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

public class PlaceDetails implements Parcelable {
    private int placeId;
    private String placeName;
    private double lat, lng;
    private long id;
    private String name;
    private double distance;
    private String eventName;

    public PlaceDetails() {
    }

    public PlaceDetails(int placeId, String placeName, double lat, double lng) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.lat = lat;
        this.lng = lng;
    }

    protected PlaceDetails(Parcel in) {
        placeId = in.readInt();
        placeName = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        id = in.readLong();
        name = in.readString();
        distance = in.readDouble();
        eventName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(placeId);
        dest.writeString(placeName);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeDouble(distance);
        dest.writeString(eventName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PlaceDetails> CREATOR = new Creator<PlaceDetails>() {
        @Override
        public PlaceDetails createFromParcel(Parcel in) {
            return new PlaceDetails(in);
        }

        @Override
        public PlaceDetails[] newArray(int size) {
            return new PlaceDetails[size];
        }
    };

    public int getPlaceId() {
        return placeId;
    }

    public void setPlaceId(int placeId) {
        this.placeId = placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public static Comparator<PlaceDetails> NameComparator = new Comparator<PlaceDetails>() {

        public int compare(PlaceDetails s1, PlaceDetails s2) {
            String nam1 = s1.getName().toUpperCase();
            String nam2 = s2.getName().toUpperCase();
            //ascending order
            return nam1.compareTo(nam2);

            //descending order
            // return nam2.compareTo(nam1);
        }};


    public static Comparator<PlaceDetails> distanceComparator = new Comparator<PlaceDetails>() {

        public int compare(PlaceDetails s1, PlaceDetails s2) {

            int d1 = (int)(s1.getDistance()*100000);
            int d2 = (int)(s2.getDistance()*100000);
            //  Log.d(TAG, "compare: d1:"+d1+" d2:"+d2);
            /*For ascending order*/
            return (d1-d2);

            /*For descending order*/
            // return (int)d2-(int)d1;
        }};


}
