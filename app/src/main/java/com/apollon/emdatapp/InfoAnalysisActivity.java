package com.apollon.emdatapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
    private Date gpsTime;
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

    private String imeiValue;
    private String serialeSIMValue;
    private String networkCountryValue;
    private String simCountryValue;
    private String carrierNameValue;
    private String teslaValue;
    private String connessioneDatiValue;
    private String generazioneReteVoceValue;
    private String tipoReteVoceValue;
    private String generazioneReteDatiValue;
    private String tipoReteDatiValue;
    private String potenzaSegnaleValue;
    private String latValue;
    private String lngValue;

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
        wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);

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
        updateWifi();
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
                    latValue = String.valueOf(String.valueOf(location.getLatitude()));
                    lngValue = String.valueOf(String.valueOf(location.getLongitude()));
                    lat.setText(latValue);
                    lng.setText(lngValue);
                    gpsTime = new Date();
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
            imeiValue = telephonyManager.getDeviceId();
            imei.setText(imeiValue);
        }
    }

    public void updateSIMInfo() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(InfoAnalysisActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            serialeSIMValue = telephonyManager.getSimSerialNumber();
            networkCountryValue = telephonyManager.getNetworkCountryIso();
            simCountryValue = telephonyManager.getSimCountryIso();
            carrierNameValue = telephonyManager.getNetworkOperatorName();

            serialeSim.setText(serialeSIMValue);
            networkCountry.setText(networkCountryValue);
            simCountry.setText(simCountryValue);
            operatore.setText(carrierNameValue);
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
        teslaValue = String.format("%.2f", computedTesla) + " μT";
        valueEM.setText(teslaValue);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    /****************************************************************************************/


    /********************************** TELEPHONY MANAGER ***********************************/
    public void updateTelephonyManager() {
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

            if (android.os.Build.VERSION.SDK_INT >= 28) {
                String[] dataNet = getNetworkTypeName(telephonyManager.getNetworkType());
                if (dataNet != null) {
                    switch (dataNet[1]) {
                        case "3G":
                        case "2G":
                            potenzaSegnaleValue = String.valueOf(-113 + 2 * telephonyManager.getSignalStrength().getGsmSignalStrength()) + " dBm";
                            potenzaSegnale.setText(potenzaSegnaleValue);
                            break;
                        case "4G":
                            try {
                                Method[] methods = android.telephony.SignalStrength.class
                                        .getMethods();
                                for (Method mthd : methods) {
                                    if (mthd.getName().equals("getLteRsrp")) {
                                        potenzaSegnaleValue = mthd.invoke(telephonyManager.getSignalStrength()).toString() + " dBm";
                                        potenzaSegnale.setText(potenzaSegnaleValue);
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
                            potenzaSegnaleValue = String.valueOf(cellSignalStrengthWcdma.getDbm()-116);
                            potenzaSegnale.setText(potenzaSegnaleValue);
                        }else if(cellInfos.get(i) instanceof CellInfoGsm){
                            CellInfoGsm cellInfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
                            CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                            potenzaSegnaleValue = String.valueOf(cellSignalStrengthGsm.getAsuLevel()-116);
                            potenzaSegnale.setText(potenzaSegnaleValue);
                        }else if(cellInfos.get(i) instanceof CellInfoLte){
                            CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
                            CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                            potenzaSegnaleValue = String.valueOf(cellSignalStrengthLte.getDbm());
                            potenzaSegnale.setText(potenzaSegnaleValue);
                        }
                        break;
                    }
                }
            }
        }
    }

    public void updateDataConnection() {
        connessioneDatiValue = getStateDataConnection(telephonyManager.getDataState());
        connessioneDati.setText(connessioneDatiValue);
    }

    public void updateDataNetworkType() {
        if (ActivityCompat.checkSelfPermission(InfoAnalysisActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(InfoAnalysisActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            String[] voiceNetType = getNetworkTypeName(telephonyManager.getDataNetworkType());
            if (voiceNetType != null) {
                generazioneReteDatiValue = voiceNetType[1];
                tipoReteDatiValue = voiceNetType[0];
                generazioneReteDatiCellulare.setText(generazioneReteDatiValue);
                tipoReteDatiCellulare.setText(tipoReteDatiValue);
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
                generazioneReteVoceValue = voiceNetType[1];
                tipoReteVoceValue = voiceNetType[0];
                generazioneReteVoceCellulare.setText(generazioneReteVoceValue);
                tipoReteVoceCellulare.setText(tipoReteVoceValue);
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

    public void sendData() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
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
        queue.add(stringRequest);
    }
}
