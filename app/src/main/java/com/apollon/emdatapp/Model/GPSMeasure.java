package com.apollon.emdatapp.Model;

import java.util.Date;

public class GPSMeasure {

    private Measure lat;
    private Measure lng;
    private Date date;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
