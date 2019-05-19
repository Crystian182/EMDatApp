package com.apollon.emdatapp.Model;

import java.sql.Timestamp;

public class GPSMeasure {

    private Measure lat;
    private Measure lng;
    private String timestamp;

    public Measure getLat() {
        return lat;
    }

    public void setLat(Measure lat) {
        this.lat = lat;
    }

    public Measure getLng() {
        return lng;
    }

    public void setLng(Measure lng) {
        this.lng = lng;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
