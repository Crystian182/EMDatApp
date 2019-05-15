package com.apollon.emdatapp.Model;

public class WiFiMeasure {

    private String SSID;
    private Integer channel;
    private Measure dBmMeasure;
    private Measure frequencyMeasure;

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public Measure getdBmMeasure() {
        return dBmMeasure;
    }

    public void setdBmMeasure(Measure dBmMeasure) {
        this.dBmMeasure = dBmMeasure;
    }

    public Measure getFrequencyMeasure() {
        return frequencyMeasure;
    }

    public void setFrequencyMeasure(Measure frequencyMeasure) {
        this.frequencyMeasure = frequencyMeasure;
    }
}
