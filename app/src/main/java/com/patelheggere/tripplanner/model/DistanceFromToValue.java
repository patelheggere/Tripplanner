package com.patelheggere.tripplanner.model;

public class DistanceFromToValue {
    private String placeName, eventName;
    private long distance;

    public DistanceFromToValue() {
    }

    public DistanceFromToValue(String placeName, String eventName, long distance) {
        this.placeName = placeName;
        this.eventName = eventName;
        this.distance = distance;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }
}
