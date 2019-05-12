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
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.TextView;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


public class InfoActivity extends AppCompatActivity implements SensorEventListener {

    private TelephonyManager telephonyManager;
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

    private final String LOG_TAG = "onsignalchange";
    private static SensorManager sensorManager;
    private Sensor sensor;
    private LocationManager locationManager;
    private LocationListener locationListener;


    AlertDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

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

        imei.setText("Rilevamento...");
        tipoReteVoceCellulare.setText("Rilevamento...");
        generazioneReteVoceCellulare.setText("Rilevamento...");
        tipoReteDatiCellulare.setText("Rilevamento...");
        generazioneReteDatiCellulare.setText("Rilevamento...");
        connessioneDati.setText("Rilevamento...");
        potenzaSegnale.setText("Rilevamento...");
        serialeSim.setText("Rilevamento...");
        networkCountry.setText("Rilevamento...");
        simCountry.setText("Rilevamento...");
        operatore.setText("Rilevamento...");
        wifi.setText("Rilevamento...");
        valueEM.setText("Rilevamento...");
        lat.setText("Rilevamento...");
        lng.setText("Rilevamento...");

        updateSignals();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat.setText(String.valueOf(location.getLatitude()));
                lng.setText(String.valueOf(location.getLongitude()));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                showAlert();
                Log.i("AIA ", "RILEVATO");
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);

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
                }
            }
        });
        builder.setCancelable(false);
        builder.show();
        dialog = builder.create();

    }

    public void updateSignals() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {

            signalStateListener signalStateListener = new signalStateListener();

            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(signalStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
            //int numberOfLevels = 5;
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            //int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
            int signal = wifiInfo.getRssi();

            String wifidbm = String.valueOf(signal);

            serialeSim.setText(telephonyManager.getSimSerialNumber());
            networkCountry.setText(telephonyManager.getNetworkCountryIso());
            simCountry.setText(telephonyManager.getSimCountryIso());
            imei.setText(telephonyManager.getDeviceId());
            wifi.setText(wifidbm);
            operatore.setText(telephonyManager.getNetworkOperatorName());


        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            showAlert();
        }

        signalStateListener signalStateListener = new signalStateListener();
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(signalStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        if(sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            //non supportato
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float azimuth = Math.round(event.values[0]);
        float pitch = Math.round(event.values[1]);
        float roll = Math.round(event.values[2]);

        double tesla = Math.sqrt((azimuth * azimuth) + (pitch * pitch) + (roll * roll));

        valueEM.setText(String.format("%.0f", tesla) + " μT");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class signalStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            //check voice network type
            String[] voiceNet = getNetworkTypeName(telephonyManager.getVoiceNetworkType());
            generazioneReteVoceCellulare.setText(voiceNet[1]);
            tipoReteVoceCellulare.setText(voiceNet[0]);

            String[] dataNet = getNetworkTypeName(telephonyManager.getNetworkType());
            if(dataNet != null) {
                switch (dataNet[1]) {
                    case "3G":
                    case "2G":
                        potenzaSegnale.setText(String.valueOf(-113 + 2 * signalStrength.getGsmSignalStrength()) + " dBm");
                        break;
                    case "4G":
                        try {
                            Method[] methods = android.telephony.SignalStrength.class
                                    .getMethods();
                            for (Method mthd : methods) {
                                if (mthd.getName().equals("getLteRsrp")) {
                                    potenzaSegnale.setText(mthd.invoke(signalStrength).toString() + " dBm");
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

            onCellLocationChanged(telephonyManager.getCellLocation());
        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            super.onDataConnectionStateChanged(state, networkType);

            switch (state) {
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

            String[] dataNet = getNetworkTypeName(telephonyManager.getNetworkType());
            if(dataNet != null) {
                tipoReteDatiCellulare.setText(dataNet[0]);
                generazioneReteDatiCellulare.setText(dataNet[1]);
            }

            //aggiorno il livello di segnale
            onSignalStrengthsChanged(telephonyManager.getSignalStrength());
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
                    return null;
            }

            return network;
        }

    }



}
