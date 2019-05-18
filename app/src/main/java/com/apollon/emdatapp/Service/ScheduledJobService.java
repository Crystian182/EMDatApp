package com.apollon.emdatapp.Service;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.apollon.emdatapp.Model.GPSMeasure;
import com.apollon.emdatapp.Model.Measure;
import com.apollon.emdatapp.Model.Network;
import com.apollon.emdatapp.Model.NetworkMeasure;
import com.apollon.emdatapp.Model.PhoneInfo;
import com.apollon.emdatapp.Model.Report;
import com.apollon.emdatapp.Model.SIMInfo;
import com.apollon.emdatapp.Model.UnitMeasurement;
import com.apollon.emdatapp.Model.WiFiMeasure;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ScheduledJobService extends Service implements SensorEventListener {

    private final String BACKEND_ADDRESS = "51.68.124.145:8000";
    private final long DATA_UPDATING_INTERVAL = 2000; //2 sec
    private final long DATA_SENDING_INTERVAL = 30; //30 sec
    private final long GPS_VALID_TIME = 60000; //1 min

    private TelephonyManager telephonyManager;
    private SensorManager sensorManager;
    private WifiManager wifiManager;
    private Sensor sensor;
    private LocationManager locationManager;

    private PhoneInfo phoneInfo = null;
    private SIMInfo simInfo = null;
    private Measure emMeasure = null;
    private NetworkMeasure networkMeasure = null;
    private GPSMeasure gpsMeasure = null;
    private ArrayList<WiFiMeasure> wiFiMeasures = null;

    private int counter = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        initializeListeners();

        HandlerThread handlerThread = new HandlerThread("background-thread");
        handlerThread.start();
        final Handler handlerDataUpdating = new Handler(handlerThread.getLooper());
        handlerDataUpdating.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    updateAll();
                    if (counter == DATA_SENDING_INTERVAL) {
                        //if (gpsMeasure != null && (new Date().getTime() - gpsMeasure.getDate().getTime() <= GPS_VALID_TIME)) {
                         if(gpsMeasure != null) {
                            sendData();
                         } /*else {
                            getLastKnownLocation();
                            if(gpsMeasure != null) {
                                sendData();
                            }
                        }*/
                        counter = 0;
                    } else {
                        counter++;
                    }
                } else {
                    gpsMeasure = null;
                }
                sendInfoToActivity(phoneInfo);
                handlerDataUpdating.postDelayed(this, DATA_UPDATING_INTERVAL);
            }
        }, DATA_UPDATING_INTERVAL);
    }

    private void sendInfoToActivity(PhoneInfo phoneInfo) {
        Intent intent = new Intent("infoUpdates");

        Report report = new Report();
        if (phoneInfo != null) {
            report.setPhoneInfo(phoneInfo);
        }
        if (simInfo != null) {
            report.setSimInfo(simInfo);
        }
        if (networkMeasure != null) {
            report.setNetworkMeasure(networkMeasure);
        }
        if (gpsMeasure != null) {
            report.setGpsMeasure(gpsMeasure);
        }
        if (emMeasure != null) {
            report.setEmMeasure(emMeasure);
        }
        if (wiFiMeasures != null) {
            report.setWifiMeasure(wiFiMeasures);
        }
        report.setDate(new Date());

        Gson gson = new Gson();
        String reportJson = gson.toJson(report);
        intent.putExtra("report", reportJson);
        LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(intent);
    }

    public void sendData() {
        Report report = new Report();
        if (phoneInfo != null) {
            report.setPhoneInfo(phoneInfo);
        }
        if (simInfo != null) {
            report.setSimInfo(simInfo);
        }
        if (networkMeasure != null) {
            report.setNetworkMeasure(networkMeasure);
        }
        if (gpsMeasure != null) {
            report.setGpsMeasure(gpsMeasure);
        }
        if (emMeasure != null) {
            report.setEmMeasure(emMeasure);
        }
        if (wiFiMeasures != null) {
            report.setWifiMeasure(wiFiMeasures);
        }
        report.setDate(new Date());

        try {
            Gson gson = new Gson();
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = "http://" + BACKEND_ADDRESS + "/misurazioni";
            final String requestBody = gson.toJson(report);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //Toast.makeText(InfoAnalysisActivity.this, "Report inviato!",
                    //Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Toast.makeText(InfoAnalysisActivity.this, "Errore durante l'invio",
                    //Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void initializeListeners() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        initializeWifiManager();
        updateMagneticField();
        updatePosition();
    }

    public void updateAll() {
        updatePhoneInfo();
        updateSIMInfo();
        updateTelephonyManager();
    }

    public void updatePhoneInfo() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            phoneInfo = new PhoneInfo();
            phoneInfo.setImei(telephonyManager.getDeviceId());
            phoneInfo.setManufacturer(Build.MANUFACTURER);
            phoneInfo.setModel(Build.MODEL);
        }
    }

    public void updateSIMInfo() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            simInfo = new SIMInfo();
            simInfo.setSeriale(telephonyManager.getSimSerialNumber());
            simInfo.setNetworkCountry(telephonyManager.getNetworkCountryIso());
            simInfo.setSIMCountry(telephonyManager.getSimCountryIso());
            simInfo.setCarrier(telephonyManager.getNetworkOperatorName());
        }
    }

    public void updateTelephonyManager() {
        networkMeasure = new NetworkMeasure();
        updateDataConnection();
        updateDataNetworkType();
        updateVoiceNetworkType();
        updateSignalStrength();
    }

    public void updateDataConnection() {
        networkMeasure.setDataState(getStateDataConnection(telephonyManager.getDataState()));
    }

    public void updateDataNetworkType() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            String[] voiceNetType = getNetworkTypeName(telephonyManager.getDataNetworkType());
            if (voiceNetType != null) {
                Network dataNetwork = new Network();
                dataNetwork.setGeneration(voiceNetType[1]);
                dataNetwork.setName(voiceNetType[0]);

                networkMeasure.setDataNetwork(dataNetwork);
            }
        }
    }

    public void updateVoiceNetworkType() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            String[] voiceNetType = getNetworkTypeName(telephonyManager.getVoiceNetworkType());
            if (voiceNetType != null) {
                Network voiceNetwork = new Network();
                voiceNetwork.setGeneration(voiceNetType[1]);
                voiceNetwork.setName(voiceNetType[0]);

                networkMeasure.setVoiceNetwork(voiceNetwork);
            }
        }
    }

    public String[] getNetworkTypeName(int networkType) {
        String[] network = new String[2];
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                network[0] = "GPRS";
                network[1] = "2G";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                network[0] = "EDGE";
                network[1] = "2G";
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                network[0] = "CDMA";
                network[1] = "2G";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                network[0] = "UMTS";
                network[1] = "2G";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                network[0] = "HSDPA";
                network[1] = "3G";
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                network[0] = "HSUPA";
                network[1] = "3G";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                network[0] = "HSPA";
                network[1] = "3G";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                network[0] = "LTE";
                network[1] = "4G";
                break;
            default:
                network = null;
        }
        return network;
    }

    public String getStateDataConnection(int state) {
        switch (state) {
            case TelephonyManager.DATA_DISCONNECTED:
                return "Disconnesso";
            case TelephonyManager.DATA_CONNECTING:
                return "In connessione";
            case TelephonyManager.DATA_CONNECTED:
                return "Connesso";
            case TelephonyManager.DATA_SUSPENDED:
                return "Sospeso";
            default:
                return "Sconosciuta";
        }
    }

    public void updateSignalStrength() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            UnitMeasurement unitMeasurement = new UnitMeasurement();
            unitMeasurement.setName("dBm");
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                String[] dataNet = getNetworkTypeName(telephonyManager.getNetworkType());
                if (dataNet != null) {
                    switch (dataNet[1]) {
                        case "3G":
                        case "2G":
                            Measure signalMeasure = new Measure();
                            signalMeasure.setUnitMeasurement(unitMeasurement);
                            signalMeasure.setValue(Double.valueOf(-113 + 2 * telephonyManager.getSignalStrength().getGsmSignalStrength()));
                            networkMeasure.setMeasure(signalMeasure);
                            break;
                        case "4G":
                            try {
                                Method[] methods = android.telephony.SignalStrength.class
                                        .getMethods();
                                for (Method mthd : methods) {
                                    if (mthd.getName().equals("getLteRsrp")) {
                                        signalMeasure = new Measure();
                                        signalMeasure.setUnitMeasurement(unitMeasurement);
                                        signalMeasure.setValue(Double.valueOf(mthd.invoke(telephonyManager.getSignalStrength()).toString()));
                                        networkMeasure.setMeasure(signalMeasure);
                                    }
                                }
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            } else {
                List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();   //This will give info of all sims present inside your mobile
                if (cellInfos != null) {
                    for (int i = 0; i < cellInfos.size(); i++) {
                        if (cellInfos.get(i) instanceof CellInfoWcdma) {
                            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(0);
                            CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();

                            Measure signalMeasure = new Measure();
                            signalMeasure.setUnitMeasurement(unitMeasurement);
                            signalMeasure.setValue(Double.valueOf(cellSignalStrengthWcdma.getDbm() - 116));
                            networkMeasure.setMeasure(signalMeasure);

                        } else if (cellInfos.get(i) instanceof CellInfoGsm) {
                            CellInfoGsm cellInfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
                            CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();

                            Measure signalMeasure = new Measure();
                            signalMeasure.setUnitMeasurement(unitMeasurement);
                            signalMeasure.setValue(Double.valueOf(cellSignalStrengthGsm.getDbm() - 116));
                            networkMeasure.setMeasure(signalMeasure);

                        } else if (cellInfos.get(i) instanceof CellInfoLte) {
                            CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
                            CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                            Measure signalMeasure = new Measure();
                            signalMeasure.setUnitMeasurement(unitMeasurement);
                            signalMeasure.setValue(Double.valueOf(cellSignalStrengthLte.getDbm()));
                            networkMeasure.setMeasure(signalMeasure);

                        }
                        break;
                    }
                }
            }
        }
    }

    /************************************ MAGNETIC FIELD ************************************/
    public void updateMagneticField() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float azimuth = Math.round(event.values[0]);
        float pitch = Math.round(event.values[1]);
        float roll = Math.round(event.values[2]);

        double computedTesla = Math.sqrt((azimuth * azimuth) + (pitch * pitch) + (roll * roll));
        emMeasure = new Measure();
        emMeasure.setValue(computedTesla);
        UnitMeasurement unitMeasurement = new UnitMeasurement();
        unitMeasurement.setName("μT");
        emMeasure.setUnitMeasurement(unitMeasurement);

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /****************************************************************************************/

    public void updatePosition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            final LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    UnitMeasurement unitMeasurement = new UnitMeasurement();
                    unitMeasurement.setName("degree");

                    Measure latMeasure = new Measure();
                    latMeasure.setUnitMeasurement(unitMeasurement);
                    latMeasure.setValue(location.getLatitude());

                    Measure lngMeasure = new Measure();
                    lngMeasure.setUnitMeasurement(unitMeasurement);
                    lngMeasure.setValue(location.getLongitude());

                    gpsMeasure = new GPSMeasure();
                    gpsMeasure.setLat(latMeasure);
                    gpsMeasure.setLng(lngMeasure);
                    gpsMeasure.setDate(new Date());
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100,
                    0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100,
                    0, locationListener);
        }
    }

    public void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            List<String> providers = locationManager.getAllProviders();
            Location bestLoc = null;

            for (int i = 0; i < providers.size(); i++) {
                if(bestLoc != null) {
                    if(locationManager.getLastKnownLocation(providers.get(i)).getTime() >= bestLoc.getTime()) {
                        bestLoc = locationManager.getLastKnownLocation(providers.get(i));
                    }
                } else {
                    bestLoc = locationManager.getLastKnownLocation(providers.get(i));
                }
            }

            if(bestLoc != null) {
                UnitMeasurement unitMeasurement = new UnitMeasurement();
                unitMeasurement.setName("degree");

                Measure latMeasure = new Measure();
                latMeasure.setUnitMeasurement(unitMeasurement);
                latMeasure.setValue(bestLoc.getLatitude());

                Measure lngMeasure = new Measure();
                lngMeasure.setUnitMeasurement(unitMeasurement);
                lngMeasure.setValue(bestLoc.getLongitude());

                gpsMeasure = new GPSMeasure();
                gpsMeasure.setLat(latMeasure);
                gpsMeasure.setLng(lngMeasure);
                gpsMeasure.setDate(new Date());
            }

        }
    }


    public void initializeWifiManager() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);

        wifiManager.startScan(); // without starting scan, we may never receive any scan results

        final IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION); // you can keep this filter if you want to get fresh results when singnal stregth of the APs was changed
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        wifiManager.startScan();

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                wifiManager.startScan(); // start scan again to get fresh results ASAP
                updateWifi();
            }
        };

        getApplicationContext().registerReceiver(receiver, filter);

    }

    public int getWiFiChannel(Integer frequency) {
        switch(frequency) {
            case 2412:
                return 1;
            case 2417:
                return 2;
            case 2422:
                return 3;
            case 2427:
                return 4;
            case 2432:
                return 5;
            case 2437:
                return 6;
            case 2442:
                return 7;
            case 2447:
                return 8;
            case 2452:
                return 9;
            case 2457:
                return 10;
            case 2462:
                return 11;
            case 2467:
                return 12;
            case 2472:
                return 13;
            case 2484:
                return 14;
            case 5180:
                return 36;
            case 5200:
                return 40;
            case 5220:
                return 44;
            case 5240:
                return 48;
            case 5260:
                return 52;
            case 5280:
                return 56;
            case 5300:
                return 60;
            case 5320:
                return 64;
            case 5500:
                return 100;
            case 5520:
                return 104;
            case 5540:
                return 108;
            case 5560:
                return 112;
            case 5580:
                return 116;
            case 5600:
                return 120;
            case 5620:
                return 124;
            case 5640:
                return 128;
            case 5660:
                return 132;
            case 5680:
                return 136;
            case 5700:
                return 140;
            case 5720:
                return 144;
            case 5745:
                return 149;
            case 5765:
                return 153;
            case 5785:
                return 157;
            case 5805:
                return 161;
            case 5825:
                return 165;
            default:
                return 0;
        }
    }

    public void updateWifi() {
        List<ScanResult> mScanResults = wifiManager.getScanResults();
        if (wifiManager.isWifiEnabled()) {

            wiFiMeasures = new ArrayList<WiFiMeasure>();

            UnitMeasurement unitMeasurement = new UnitMeasurement();
            unitMeasurement.setName("dBm");

            UnitMeasurement unitMeasurementFrequency = new UnitMeasurement();
            unitMeasurementFrequency.setName("MHz");

            for(ScanResult results : mScanResults) {
                WiFiMeasure wiFiMeasure = new WiFiMeasure();
                if(results.SSID.equals("")) {
                    wiFiMeasure.setSSID("Rete nascosta");
                } else {
                    wiFiMeasure.setSSID(results.SSID);

                }

                Measure dBmMeasure = new Measure();
                dBmMeasure.setValue(Double.valueOf(results.level));
                dBmMeasure.setUnitMeasurement(unitMeasurement);
                wiFiMeasure.setdBmMeasure(dBmMeasure);

                Measure frequencyMeasure = new Measure();
                frequencyMeasure.setValue(Double.valueOf(results.frequency));
                frequencyMeasure.setUnitMeasurement(unitMeasurementFrequency);
                wiFiMeasure.setFrequencyMeasure(frequencyMeasure);

                wiFiMeasure.setChannel(getWiFiChannel(wiFiMeasure.getFrequencyMeasure().getValue().intValue()));

                wiFiMeasures.add(wiFiMeasure);
            }
        }
    }
}
