package com.patelheggere.tripplanner.model;

import android.os.Parcel;
import android.os.Parcelable;

public class EventDetailModel implements Parcelable {
    private double distance;
    private String eventName;
    private String placeName;
    private long timeStamp;
    private double lon, lat;
    private String time;
    private String key;
    private String imageURL;
    private String contactPerson, contactNumber;

    public EventDetailModel() {
    }

    public EventDetailModel(double distance, String eventName, String placeName, long timeStamp, double lon, double lat, String time, String key, String imageURL, String contactPerson, String contactNumber) {
        this.distance = distance;
        this.eventName = eventName;
        this.placeName = placeName;
        this.timeStamp = timeStamp;
        this.lon = lon;
        this.lat = lat;
        this.time = time;
        this.key = key;
        this.imageURL = imageURL;
        this.contactPerson = contactPerson;
        this.contactNumber = contactNumber;
    }

    protected EventDetailModel(Parcel in) {
        distance = in.readDouble();
        eventName = in.readString();
        placeName = in.readString();
        timeStamp = in.readLong();
        lon = in.readDouble();
        lat = in.readDouble();
        time = in.readString();
        key = in.readString();
        imageURL = in.readString();
        contactNumber = in.readString();
        contactPerson = in.readString();
    }

    public static final Creator<EventDetailModel> CREATOR = new Creator<EventDetailModel>() {
        @Override
        public EventDetailModel createFromParcel(Parcel in) {
            return new EventDetailModel(in);
        }

        @Override
        public EventDetailModel[] newArray(int size) {
            return new EventDetailModel[size];
        }
    };

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
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

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(distance);
        parcel.writeString(eventName);
        parcel.writeString(placeName);
        parcel.writeLong(timeStamp);
        parcel.writeDouble(lon);
        parcel.writeDouble(lat);
        parcel.writeString(time);
        parcel.writeString(key);
        parcel.writeString(imageURL);
        parcel.writeString(contactNumber);
        parcel.writeString(contactPerson);
    }

}
