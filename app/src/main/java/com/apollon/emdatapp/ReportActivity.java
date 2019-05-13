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
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ReportActivity extends AppCompatActivity implements SensorEventListener {

    private int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    private int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    private String loading = "Rilevamento...";
    private String LOG_TAG = "prelev";
    private TelephonyManager telephonyManager;
    private SensorManager sensorManager;
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
    private TextView report;

    private Double tesla;
    private Double latValue;
    private Double lngValue;

    private boolean alertPresent = false;
    private boolean load = true;

    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

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
        report = findViewById(R.id.reportDate);

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
        report.setText(loading);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReportActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListenerGps);
            } else {
                showAlert();
                alertPresent = true;
            }
        }

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        updateInfo();

        handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG, "PRELEV");

                updateInfo();

                handler.postDelayed(this, 10000);



            }
        }, 10000);  //the time is in miliseconds


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float azimuth = Math.round(event.values[0]);
        float pitch = Math.round(event.values[1]);
        float roll = Math.round(event.values[2]);

        tesla = Math.sqrt((azimuth * azimuth) + (pitch * pitch) + (roll * roll));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void updateInfo() {
        if(latValue == null || lngValue == null || tesla == null) {
            report.setText("Raccolta informazioni...");
        } else {
            updateDate();
        }

        updatePhoneInfo();
        updateSIMInfo();
        updateMagneticField();
        updateVoiceSignal();
        updateDataSignal();
        updatePosition();
    }

    public void updateDate() {
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm:ss");
        report.setText(df.format(Calendar.getInstance().getTime()));
    }

    public void updatePhoneInfo() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReportActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            imei.setText(telephonyManager.getDeviceId());
        }
    }

    public void updateSIMInfo() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReportActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            serialeSim.setText(telephonyManager.getSimSerialNumber());
            networkCountry.setText(telephonyManager.getNetworkCountryIso());
            simCountry.setText(telephonyManager.getSimCountryIso());
            operatore.setText(telephonyManager.getNetworkOperatorName());
        }
    }

    public void updateMagneticField() {
        if(tesla != null) {
            valueEM.setText(String.format("%.2f", tesla) + " μT");
        }
    }

    public void updateVoiceSignal() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReportActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            String[] voiceNet = getNetworkTypeName(telephonyManager.getVoiceNetworkType());
            generazioneReteVoceCellulare.setText(voiceNet[1]);
            tipoReteVoceCellulare.setText(voiceNet[0]);
        }
    }

    public void updateDataSignal() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReportActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            //prendo il valore di dbm in base alla rete
            List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
            //Checking if list values are not null
            if (cellInfoList != null) {
                for (final CellInfo info : cellInfoList) {
                    if (info instanceof CellInfoGsm) {
                        //GSM Network
                        CellSignalStrengthGsm cellSignalStrength = ((CellInfoGsm)info).getCellSignalStrength();
                        double dBmlevel = cellSignalStrength.getDbm();
                        double asulevel = cellSignalStrength.getAsuLevel();
                        potenzaSegnale.setText(String.valueOf(String.format("%.0f",dBmlevel)) + " dBm");
                        break;
                    }
                    else if (info instanceof CellInfoCdma) {
                        //CDMA Network
                        CellSignalStrengthCdma cellSignalStrength = ((CellInfoCdma)info).getCellSignalStrength();
                        double dBmlevel = cellSignalStrength.getDbm();
                        double asulevel = cellSignalStrength.getAsuLevel();
                        potenzaSegnale.setText(String.valueOf(String.format("%.0f",dBmlevel)) + " dBm");
                        break;
                    }
                    else if (info instanceof CellInfoLte) {
                        //LTE Network
                        CellSignalStrengthLte cellSignalStrength = ((CellInfoLte)info).getCellSignalStrength();
                        double dBmlevel = cellSignalStrength.getDbm();
                        double asulevel = cellSignalStrength.getAsuLevel();
                        potenzaSegnale.setText(String.valueOf(String.format("%.0f",dBmlevel)) + " dBm");
                        break;
                    }
                    else if  (info instanceof CellInfoWcdma) {
                        //WCDMA Network
                        CellSignalStrengthWcdma cellSignalStrength = ((CellInfoWcdma)info).getCellSignalStrength();
                        double dBmlevel = cellSignalStrength.getDbm();
                        double asulevel = cellSignalStrength.getAsuLevel();
                        potenzaSegnale.setText(String.valueOf(String.format("%.0f",dBmlevel)) + " dBm");
                        break;
                    }
                }
            }

            //prendo nome e generazione della rete
            String[] dataNet = getNetworkTypeName(telephonyManager.getNetworkType());
            if(dataNet != null) {
                tipoReteDatiCellulare.setText(dataNet[0]);
                generazioneReteDatiCellulare.setText(dataNet[1]);
            }

            //controllo lo stato di connessione dei dati

            switch (telephonyManager.getDataState()) {
                case TelephonyManager.DATA_DISCONNECTED:
                    connessioneDati.setText("Disconnesso");
                    break;
                case TelephonyManager.DATA_CONNECTING:
                    connessioneDati.setText("In connessione");
                    break;
                case TelephonyManager.DATA_CONNECTED:
                    connessioneDati.setText("Connesso");
                    break;
                case TelephonyManager.DATA_SUSPENDED:
                    connessioneDati.setText("Sospeso");
                    break;
                default:
                    connessioneDati.setText("Rilevamento...");
                    break;
            }
        }
    }

    public void updatePosition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReportActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            if(latValue != null) {
                    lat.setText(String.valueOf(latValue));
                    latValue = null;
            }
            if(lngValue != null) {
                lng.setText(String.valueOf(lngValue));
                lngValue = null;
            }
        }
    }

    LocationListener locationListenerGps = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            latValue = location.getLatitude();
            lngValue = location.getLongitude();
            Log.i(LOG_TAG, "prelev " + latValue + " " + lngValue);
        }

        @Override
        public void onProviderDisabled(String provider) {
            showAlert();
            alertPresent = true;
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };


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
                return null;
        }

        return network;
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
                    alertPresent = false;
                }
            }
        });
        builder.setCancelable(false);
        builder.show();
        dialog = builder.create();

    }
}
