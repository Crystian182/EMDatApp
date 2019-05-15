package com.apollon.emdatapp.Model;

import java.util.ArrayList;
import java.util.Date;

public class Report {

    private PhoneInfo phoneInfo;
    private SIMInfo simInfo;
    private NetworkMeasure networkMeasure;
    private Measure emMeasure;
    private ArrayList<WiFiMeasure> wifiMeasure;
    private GPSMeasure gpsMeasure;
    private Date date;

    public PhoneInfo getPhoneInfo() {
        return phoneInfo;
    }

    public void setPhoneInfo(PhoneInfo phoneInfo) {
        this.phoneInfo = phoneInfo;
    }

    public SIMInfo getSimInfo() {
        return simInfo;
    }

    public void setSimInfo(SIMInfo simInfo) {
        this.simInfo = simInfo;
    }

    public NetworkMeasure getNetworkMeasure() {
        return networkMeasure;
    }

    public void setNetworkMeasure(NetworkMeasure networkMeasure) {
        this.networkMeasure = networkMeasure;
    }

    public Measure getEmMeasure() {
        return emMeasure;
    }

    public void setEmMeasure(Measure emMeasure) {
        this.emMeasure = emMeasure;
    }

    public ArrayList<WiFiMeasure> getWifiMeasure() {
        return wifiMeasure;
    }

    public void setWifiMeasure(ArrayList<WiFiMeasure> wifiMeasure) {
        this.wifiMeasure = wifiMeasure;
    }

    public GPSMeasure getGpsMeasure() {
        return gpsMeasure;
    }

    public void setGpsMeasure(GPSMeasure gpsMeasure) {
        this.gpsMeasure = gpsMeasure;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
