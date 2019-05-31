package com.apollon.emdatapp.Model;

import java.sql.Timestamp;
import java.util.ArrayList;

public class Report {

    private PhoneInfo phoneInfo;
    private SIMInfo simInfo;
    private NetworkMeasure networkMeasure;
    private Measure emMeasure;
    private boolean isWiFiEnabled;
    private ArrayList<WiFiMeasure> wifiMeasure;
    private GPSMeasure gpsMeasure;
    private String userID;
    private String timestamp;

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

    public boolean isWiFiEnabled() {
        return isWiFiEnabled;
    }

    public void setWiFiEnabled(boolean wiFiEnabled) {
        isWiFiEnabled = wiFiEnabled;
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

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
