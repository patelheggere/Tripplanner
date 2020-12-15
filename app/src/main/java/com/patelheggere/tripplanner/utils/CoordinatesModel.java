package com.patelheggere.tripplanner.utils;

public class CoordinatesModel {
    String easting, northing;
    double lat, lon;

    public CoordinatesModel() {
    }

    public CoordinatesModel(String easting, String northing, double lat, double lon) {
        this.easting = easting;
        this.northing = northing;
        this.lat = lat;
        this.lon = lon;
    }

    public String getEasting() {
        return easting;
    }

    public void setEasting(String easting) {
        this.easting = easting;
    }

    public String getNorthing() {
        return northing;
    }

    public void setNorthing(String northing) {
        this.northing = northing;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
