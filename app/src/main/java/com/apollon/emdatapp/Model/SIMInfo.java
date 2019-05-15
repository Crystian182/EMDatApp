package com.apollon.emdatapp.Model;

public class SIMInfo {

    private String seriale;
    private String networkCountry;
    private String SIMCountry;
    private String carrier;

    public String getSeriale() {
        return seriale;
    }

    public void setSeriale(String seriale) {
        this.seriale = seriale;
    }

    public String getNetworkCountry() {
        return networkCountry;
    }

    public void setNetworkCountry(String networkCountry) {
        this.networkCountry = networkCountry;
    }

    public String getSIMCountry() {
        return SIMCountry;
    }

    public void setSIMCountry(String SIMCountry) {
        this.SIMCountry = SIMCountry;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }
}
