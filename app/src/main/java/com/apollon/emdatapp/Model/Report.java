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
}
