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
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

        resetFields();

        updateInfo();

    }

    public void updateInfo() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);

        updateMagneticField();

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    updateAll();
                } else {
                    resetFields();
                    if(!alertOn) {
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
                if (!locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
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
        String accesspoints = "";
        for(ScanResult results : mScanResults) {
            Log.i(LOG_TAG, results.SSID);
            if(results.SSID.equals("")) {
                accesspoints = accesspoints + "Rete nascosta " + results.level + "dBm\n";
            } else {
                accesspoints = accesspoints + results.SSID + " " + results.level + "dBm\n";
            }

        }
        wifi.setText(accesspoints);
    }
}
