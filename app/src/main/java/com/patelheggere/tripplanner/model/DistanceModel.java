package com.patelheggere.tripplanner.model;

public class DistanceModel {
    private double lon, lat;
    private String placeName;
    private long distance;
    private String startAddress, endAddress;

    public DistanceModel() {
    }

    public DistanceModel(double lon, double lat, String placeName, long distance, String startAddress, String endAddress) {
        this.lon = lon;
        this.lat = lat;
        this.placeName = placeName;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.distance = distance;
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

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }
}
