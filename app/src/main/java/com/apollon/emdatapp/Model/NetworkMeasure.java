package com.apollon.emdatapp.Model;

public class NetworkMeasure {

    private Network voiceNetwork;
    private Network dataNetwork;
    private Measure measure;
    private boolean dataConnected;

    public Network getVoiceNetwork() {
        return voiceNetwork;
    }

    public void setVoiceNetwork(Network voiceNetwork) {
        this.voiceNetwork = voiceNetwork;
    }

    public Network getDataNetwork() {
        return dataNetwork;
    }

    public void setDataNetwork(Network dataNetwork) {
        this.dataNetwork = dataNetwork;
    }

    public Measure getMeasure() {
        return measure;
    }

    public void setMeasure(Measure measure) {
        this.measure = measure;
    }

    public boolean isDataConnected() {
        return dataConnected;
    }

    public void setDataConnected(boolean dataConnected) {
        this.dataConnected = dataConnected;
    }
}
