package com.patelheggere.tripplanner.model;

public class PlaceDetails {
    private int placeId;
    private String placeName;
    private double lat, lng;

    public PlaceDetails() {
    }

    public PlaceDetails(int placeId, String placeName, double lat, double lng) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.lat = lat;
        this.lng = lng;
    }

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
}
