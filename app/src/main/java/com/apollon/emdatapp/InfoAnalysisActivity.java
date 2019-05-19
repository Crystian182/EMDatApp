package com.apollon.emdatapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ServiceCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.apollon.emdatapp.Model.Report;
import com.apollon.emdatapp.Model.WiFiMeasure;
import com.apollon.emdatapp.Service.ScheduledJobService;
import com.google.gson.Gson;
import java.util.ArrayList;

public class InfoAnalysisActivity extends AppCompatActivity {


    private String loading = "Rilevamento...";
    private boolean alertOn = false;
    private LocationManager locationManager;
    AlertDialog dialog;

    private TextView imei;
    private TextView produttore;
    private TextView modello;
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

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                String reportString = intent.getStringExtra("report");
                Gson g = new Gson();
                Report report = g.fromJson(reportString, Report.class);
                setFields(report);
                alertOn = false;
            } else {
                if(!alertOn) {
                    showAlert();
                    alertOn = true;
                }
            }
        }
    };

    private BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopService(new Intent(InfoAnalysisActivity.this,ScheduledJobService.class));
            finishAndRemoveTask();
            System.exit(0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_analysis);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        imei = findViewById(R.id.imei);
        produttore = findViewById(R.id.produttore);
        modello = findViewById(R.id.modello);
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

        //Intent intent = new Intent(InfoAnalysisActivity.this, ScheduledJobService.class);
        //startService(intent);

        LocalBroadcastManager.getInstance(InfoAnalysisActivity.this).registerReceiver(
                mMessageReceiver, new IntentFilter("infoUpdates"));

        LocalBroadcastManager.getInstance(InfoAnalysisActivity.this).registerReceiver(
                stopReceiver, new IntentFilter("stopApp"));

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
            showAlert();
            alertOn = true;
        } else {
            alertOn = false;
        }
    }

    /*@Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mMessageReceiver);
    }*/

    public void resetFields() {
        imei.setText(loading);
        produttore.setText(loading);
        modello.setText(loading);
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

    public void setFields(Report report) {
        if(report.getPhoneInfo().getImei() != null) {
            imei.setText(report.getPhoneInfo().getImei());
        } else {
            imei.setText(loading);
        }
        if(report.getPhoneInfo().getManufacturer() != null) {
            produttore.setText(report.getPhoneInfo().getManufacturer());
        } else {
            produttore.setText(loading);
        }
        if(report.getPhoneInfo().getModel() != null) {
            modello.setText(report.getPhoneInfo().getModel());
        } else {
            modello.setText(loading);
        }
        if (report.getNetworkMeasure().getVoiceNetwork().getName() != null) {
            tipoReteVoceCellulare.setText(report.getNetworkMeasure().getVoiceNetwork().getName());
        } else {
            tipoReteVoceCellulare.setText(loading);
        }
        if(report.getNetworkMeasure().getVoiceNetwork().getGeneration() != null) {
            generazioneReteVoceCellulare.setText(report.getNetworkMeasure().getVoiceNetwork().getGeneration());
        } else {
            generazioneReteVoceCellulare.setText(loading);
        }
        if(report.getNetworkMeasure().getDataNetwork().getName() != null) {
            tipoReteDatiCellulare.setText(report.getNetworkMeasure().getDataNetwork().getName());
        } else {
            tipoReteDatiCellulare.setText(loading);
        }
        if(report.getNetworkMeasure().getDataNetwork().getGeneration() != null) {
            generazioneReteDatiCellulare.setText(report.getNetworkMeasure().getDataNetwork().getGeneration());
        } else {
            generazioneReteDatiCellulare.setText(loading);
        }
        if(report.getNetworkMeasure().getDataState() != null) {
            connessioneDati.setText(report.getNetworkMeasure().getDataState());
        } else {
            connessioneDati.setText(loading);
        }
        if(report.getNetworkMeasure().getMeasure().getValue() != null) {
            potenzaSegnale.setText(String.valueOf(report.getNetworkMeasure().getMeasure().getValue()) + " " + report.getNetworkMeasure().getMeasure().getUnitMeasurement().getName());
        } else {
            potenzaSegnale.setText(loading);
        }
        if(report.getSimInfo().getSeriale() != null) {
            serialeSim.setText(report.getSimInfo().getSeriale());
        } else {
            serialeSim.setText(loading);
        }
        if(report.getSimInfo().getNetworkCountry() != null) {
            networkCountry.setText(report.getSimInfo().getNetworkCountry());
        } else {
            networkCountry.setText(loading);
        }
        if(report.getSimInfo().getSIMCountry() != null) {
            simCountry.setText(report.getSimInfo().getSIMCountry());
        } else {
            simCountry.setText(loading);
        }
        if(report.getSimInfo().getCarrier() != null) {
            operatore.setText(report.getSimInfo().getCarrier());
        } else {
            operatore.setText(loading);
        }
        if(report.getEmMeasure().getValue() != null) {
            valueEM.setText(String.format("%.2f", report.getEmMeasure().getValue()) + " " + report.getEmMeasure().getUnitMeasurement().getName());
        } else {
            valueEM.setText(loading);
        }
        if(report.getGpsMeasure() != null) {
            lat.setText(String.valueOf(report.getGpsMeasure().getLat().getValue()));
            lng.setText(String.valueOf(report.getGpsMeasure().getLng().getValue()));
        } else {
            lat.setText(loading);
            lng.setText(loading);
        }
        if(report.isWiFiEnabled()) {
            if(report.getWifiMeasure() != null) {
                if(report.getWifiMeasure().size() > 0) {
                    String[] wifiRes = getWiFiStrings(report.getWifiMeasure());
                    wifi.setText(wifiRes[0]);
                    wifiLevel.setText(wifiRes[1]);
                } else {
                    wifi.setText(loading);
                    wifiLevel.setText("");
                }
            } else {
                wifi.setText(loading);
                wifiLevel.setText("");
            }
        } else {
            wifi.setText("Disconnessa");
            wifiLevel.setText("");
        }

    }

    private String[] getWiFiStrings(ArrayList<WiFiMeasure> measures) {
        String[] risultati = new String[2];
        String accesspoints = "";
        String levels = "";

        for(WiFiMeasure results : measures) {

            accesspoints = accesspoints + results.getSSID() + "\n";

            levels = levels + String.valueOf(results.getdBmMeasure().getValue()) + " " +
                    results.getdBmMeasure().getUnitMeasurement().getName() + "\n";
        }
        risultati[0] = accesspoints;
        risultati[1] = levels;
        return risultati;
    }

    private void showAlert() {
        try {
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
        } catch(Exception e){

        }
    }

}
