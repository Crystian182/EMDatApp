package com.apollon.emdatapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.apollon.emdatapp.Model.GPSMeasure;
import com.apollon.emdatapp.Model.Measure;
import com.apollon.emdatapp.Model.Network;
import com.apollon.emdatapp.Model.NetworkMeasure;
import com.apollon.emdatapp.Model.PhoneInfo;
import com.apollon.emdatapp.Model.SIMInfo;
import com.apollon.emdatapp.Model.UnitMeasurement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

public class InfoAnalysisActivity extends AppCompatActivity implements SensorEventListener {

    private int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    private int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    private String loading = "Rilevamento...";
    private String LOG_TAG = "prelev";
    private boolean alertOn = false;
    private TelephonyManager telephonyManager;
    private SensorManager sensorManager;
    private WifiManager wifiManager;
    private Sensor sensor;
    private LocationManager locationManager;
    private Handler handler;
    AlertDialog dialog;

    private TextView imei;
    private TextView tipoReteVoceCellulare;
    private TextView generazioneReteVoceCellulare;
    private TextView tipoReteDatiCellulare;
    private TextView generazioneReteDatiCellulare;
    private TextView connessioneDati;
    private TextView potenzaSegnale;
    private TextView serialeSim;
    private TextView networkCountry;
    private TextView simCountry;
    private TextView operatore;
    private TextView wifi;
    private TextView wifiLevel;
    private TextView valueEM;
    private TextView lat;
    private TextView lng;

    private PhoneInfo phoneInfo = null;
    private SIMInfo simInfo = null;
    private Measure emMeasure = null;
    private NetworkMeasure networkMeasure = null;
    private GPSMeasure gpsMeasure = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_analysis);

        imei = findViewById(R.id.imei);
        tipoReteVoceCellulare = findViewById(R.id.tipoReteVoceCellulare);
        generazioneReteVoceCellulare = findViewById(R.id.generazioneReteVoceCellulare);
        tipoReteDatiCellulare = findViewById(R.id.tipoReteDatiCellulare);
        generazioneReteDatiCellulare = findViewById(R.id.generazioneReteDatiCellulare);
        connessioneDati = findViewById(R.id.connessioneDati);
        potenzaSegnale = findViewById(R.id.potenzaSegnale);
        serialeSim = findViewById(R.id.serialeSim);
        networkCountry = findViewById(R.id.networkCountry);
        simCountry = findViewById(R.id.simCountry);
        operatore = findViewById(R.id.operatore);
        wifi = findViewById(R.id.wifi);
        valueEM = findViewById(R.id.valueEM);
        lat = findViewById(R.id.lat);
        lng = findViewById(R.id.lng);
        wifiLevel = findViewById(R.id.wifiLevel);

        resetFields();

        updateInfo();
        sendInfo();

        sendData();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alertOn = false;
        }
    }

    public void updateInfo() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        initializeWifiManager();
        updateMagneticField();
        updatePosition();

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    updateAll();
                } else {
                    resetFields();
                    if (!alertOn) {
                        showAlert();
                        alertOn = true;
                    }
                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);  //the time is in miliseconds

    }

    public void sendInfo() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(gpsMeasure != null && (new Date().getTime()-gpsMeasure.getDate().getTime() <= 60000)) {
                    sendData();
                }
                handler.postDelayed(this, 10000);
            }
        }, 10000);  //the time is in miliseconds
    }

    public void resetFields() {
        imei.setText(loading);
        tipoReteVoceCellulare.setText(loading);
        generazioneReteVoceCellulare.setText(loading);
        tipoReteDatiCellulare.setText(loading);
        generazioneReteDatiCellulare.setText(loading);
        connessioneDati.setText(loading);
        potenzaSegnale.setText(loading);
        serialeSim.setText(loading);
        networkCountry.setText(loading);
        simCountry.setText(loading);
        operatore.setText(loading);
        wifi.setText(loading);
        valueEM.setText(loading);
        lat.setText(loading);
        lng.setText(loading);
        wifiLevel.setText(loading);

        gpsMeasure = null;
    }

    private void showAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Abilita Posizione");
        builder.setMessage("Le impostazioni per il rilevamento della Posizione sono disattivate.\nPerfavore abilita la Posizione per continuare ad usare l'App");
        builder.setPositiveButton("Impostazioni", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });
        builder.setNegativeButton("Chiudi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showAlert();
                } else {
                    dialog.dismiss();
                    alertOn = false;
                }
            }
        });
        builder.setCancelable(false);
        builder.show();
        dialog = builder.create();

    }

    public void updateAll() {
        updatePhoneInfo();
        updateSIMInfo();
        updateTelephonyManager();
    }

    public void updatePosition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(InfoAnalysisActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
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

                    lat.setText(String.valueOf(gpsMeasure.getLat().getValue()));
                    lng.setText(String.valueOf(gpsMeasure.getLng().getValue()));
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
        }
    }

    public void updatePhoneInfo() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(InfoAnalysisActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            phoneInfo = new PhoneInfo();
            phoneInfo.setImei(telephonyManager.getDeviceId());
            imei.setText(phoneInfo.getImei());
        }
    }

    public void updateSIMInfo() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(InfoAnalysisActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            simInfo = new SIMInfo();
            simInfo.setSeriale(telephonyManager.getSimSerialNumber());
            simInfo.setNetworkCountry(telephonyManager.getNetworkCountryIso());
            simInfo.setSIMCountry(telephonyManager.getSimCountryIso());
            simInfo.setCarrier(telephonyManager.getNetworkOperatorName());

            serialeSim.setText(simInfo.getSeriale());
            networkCountry.setText(simInfo.getNetworkCountry());
            simCountry.setText(simInfo.getSIMCountry());
            operatore.setText(simInfo.getCarrier());
        }
    }

    /************************************ MAGNETIC FIELD ************************************/
    public void updateMagneticField() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            valueEM.setText("Non disponibile");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float azimuth = Math.round(event.values[0]);
        float pitch = Math.round(event.values[1]);
        float roll = Math.round(event.values[2]);

        double computedTesla = Math.sqrt((azimuth * azimuth) + (pitch * pitch) + (roll * roll));
        if(!alertOn) {
            emMeasure = new Measure();
            emMeasure.setValue(computedTesla);
            UnitMeasurement unitMeasurement = new UnitMeasurement();
            unitMeasurement.setName("μT");
            emMeasure.setUnitMeasurement(unitMeasurement);

            valueEM.setText(String.format("%.2f", emMeasure.getValue()) + " " + emMeasure.getUnitMeasurement().getName());
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    /****************************************************************************************/


    /********************************** TELEPHONY MANAGER ***********************************/
    public void updateTelephonyManager() {
        networkMeasure = new NetworkMeasure();
        updateDataConnection();
        updateDataNetworkType();
        updateVoiceNetworkType();
        updateSignalStrength();
    }

    public void updateSignalStrength() {
        if (ActivityCompat.checkSelfPermission(InfoAnalysisActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(InfoAnalysisActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
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
                            potenzaSegnale.setText(String.valueOf(networkMeasure.getMeasure().getValue().intValue()) + " " + networkMeasure.getMeasure().getUnitMeasurement().getName());
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
                                        potenzaSegnale.setText(String.valueOf(networkMeasure.getMeasure().getValue().intValue()) + " " + networkMeasure.getMeasure().getUnitMeasurement().getName());
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
                if(cellInfos!=null){
                    for (int i = 0 ; i<cellInfos.size(); i++){
                        if(cellInfos.get(i) instanceof CellInfoWcdma){
                            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(0);
                            CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();

                            Measure signalMeasure = new Measure();
                            signalMeasure.setUnitMeasurement(unitMeasurement);
                            signalMeasure.setValue(Double.valueOf(cellSignalStrengthWcdma.getDbm()-116));
                            networkMeasure.setMeasure(signalMeasure);

                            potenzaSegnale.setText(String.valueOf(networkMeasure.getMeasure().getValue().intValue()) + " " + networkMeasure.getMeasure().getUnitMeasurement().getName());
                        }else if(cellInfos.get(i) instanceof CellInfoGsm){
                            CellInfoGsm cellInfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
                            CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();

                            Measure signalMeasure = new Measure();
                            signalMeasure.setUnitMeasurement(unitMeasurement);
                            signalMeasure.setValue(Double.valueOf(cellSignalStrengthGsm.getDbm()-116));
                            networkMeasure.setMeasure(signalMeasure);

                            potenzaSegnale.setText(String.valueOf(networkMeasure.getMeasure().getValue().intValue()) + " " + networkMeasure.getMeasure().getUnitMeasurement().getName());
                        }else if(cellInfos.get(i) instanceof CellInfoLte){
                            CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
                            CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                            Measure signalMeasure = new Measure();
                            signalMeasure.setUnitMeasurement(unitMeasurement);
                            signalMeasure.setValue(Double.valueOf(cellSignalStrengthLte.getDbm()));
                            networkMeasure.setMeasure(signalMeasure);

                            potenzaSegnale.setText(String.valueOf(networkMeasure.getMeasure().getValue().intValue()) + " " + networkMeasure.getMeasure().getUnitMeasurement().getName());
                        }
                        break;
                    }
                }
            }
        }
    }

    public void updateDataConnection() {
        String connessioneDatiResult = getStateDataConnection(telephonyManager.getDataState());
        if(connessioneDatiResult.equals("Connesso")) {
            networkMeasure.setDataConnected(true);
        } else {
            networkMeasure.setDataConnected(false);
        }
        connessioneDati.setText(connessioneDatiResult);
    }

    public void updateDataNetworkType() {
        if (ActivityCompat.checkSelfPermission(InfoAnalysisActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(InfoAnalysisActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            String[] voiceNetType = getNetworkTypeName(telephonyManager.getDataNetworkType());
            if (voiceNetType != null) {
                Network dataNetwork = new Network();
                dataNetwork.setGeneration(voiceNetType[1]);
                dataNetwork.setName(voiceNetType[0]);

                networkMeasure.setDataNetwork(dataNetwork);
                generazioneReteDatiCellulare.setText(networkMeasure.getDataNetwork().getGeneration());
                tipoReteDatiCellulare.setText(networkMeasure.getDataNetwork().getName());
            }
        }
    }

    public void updateVoiceNetworkType() {
        if (ActivityCompat.checkSelfPermission(InfoAnalysisActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(InfoAnalysisActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            String[] voiceNetType = getNetworkTypeName(telephonyManager.getVoiceNetworkType());
            if(voiceNetType != null) {
                Network voiceNetwork = new Network();
                voiceNetwork.setGeneration(voiceNetType[1]);
                voiceNetwork.setName(voiceNetType[0]);

                networkMeasure.setVoiceNetwork(voiceNetwork);
                generazioneReteVoceCellulare.setText(networkMeasure.getVoiceNetwork().getGeneration());
                tipoReteVoceCellulare.setText(networkMeasure.getVoiceNetwork().getName());
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
    /****************************************************************************************/

    public void updateWifi() {
        List<ScanResult> mScanResults = wifiManager.getScanResults();
        if (!wifiManager.isWifiEnabled()) {
            wifi.setText("Non connesso");
            wifiLevel.setText("-");
        }
        else {
            String accesspoints = "";
            String levels = "";
            for(ScanResult results : mScanResults) {
                if(results.SSID.equals("")) {
                    accesspoints = accesspoints + "Rete nascosta \n";
                } else {
                    accesspoints = accesspoints + results.SSID +"\n";
                }
                levels = levels + results.level + "dBm\n";
            }
            wifi.setText(accesspoints);
            wifiLevel.setText(levels);
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

    public void sendData() {
        Log.i("scan: ", "INVIO");
        // Instantiate the RequestQueue.
        /*RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://jsonplaceholder.typicode.com/posts";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.i("response: ", response.substring(0, 100));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("response: ", error.toString());
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);*/
    }
}
