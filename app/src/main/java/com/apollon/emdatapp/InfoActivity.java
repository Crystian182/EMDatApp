package com.apollon.emdatapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfoActivity extends AppCompatActivity {

    TelephonyManager telephonyManager;
    TextView dbm;
    TextView network;
    TextView deviceid;
    TextView simserial;
    TextView networkcountry;
    TextView simcountry;
    TextView imei;
    TextView wifisignal;
    TextView carrier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        dbm = findViewById(R.id.signal);
        network = findViewById(R.id.network);
        deviceid = findViewById(R.id.deviceid);
        simserial = findViewById(R.id.simserial);
        networkcountry = findViewById(R.id.networkcountry);
        simcountry = findViewById(R.id.simcountry);
        imei = findViewById(R.id.imei);
        wifisignal = findViewById(R.id.wifi);
        carrier = findViewById(R.id.carrier);

        updateSignals();
    }

    public void updateSignals() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {

            myPhoneStateListener psListener = new myPhoneStateListener();

            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(psListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

            String IMEINumber = telephonyManager.getDeviceId();
            String subscriberID = telephonyManager.getDeviceId();
            String SIMSerialNumber = telephonyManager.getSimSerialNumber();
            String networkCountryISO = telephonyManager.getNetworkCountryIso();
            String SIMCountryISO = telephonyManager.getSimCountryIso();
            String carrierName = telephonyManager.getNetworkOperatorName();

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
            //int numberOfLevels = 5;
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            //int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
            int signal = wifiInfo.getRssi();
            String wifi = String.valueOf(signal);

            deviceid.setText(subscriberID);
            simserial.setText(SIMSerialNumber);
            networkcountry.setText(networkCountryISO);
            simcountry.setText(SIMCountryISO);
            imei.setText(IMEINumber);
            wifisignal.setText(wifi);
            carrier.setText(carrierName);
        }
    }


    public class myPhoneStateListener extends PhoneStateListener {
        public int signalStrengthValue;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            /*Log.i(LOG_TAG, "onSignalStrengthsChanged: " + signalStrength);
            if (signalStrength.isGsm()) {
                Log.i(LOG_TAG, "onSignalStrengthsChanged: getGsmBitErrorRate "
                        + signalStrength.getGsmBitErrorRate());
                Log.i(LOG_TAG, "onSignalStrengthsChanged: getGsmSignalStrength "
                        + signalStrength.getGsmSignalStrength());
            } else if (signalStrength.getCdmaDbm() > 0) {
                Log.i(LOG_TAG, "onSignalStrengthsChanged: getCdmaDbm "
                        + signalStrength.getCdmaDbm());
                Log.i(LOG_TAG, "onSignalStrengthsChanged: getCdmaEcio "
                        + signalStrength.getCdmaEcio());
            } else {
                Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoDbm "
                        + signalStrength.getEvdoDbm());
                Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoEcio "
                        + signalStrength.getEvdoEcio());
                Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoSnr "
                        + signalStrength.getEvdoSnr());
            }*/

            // Reflection code starts from here
            try {
                Method[] methods = android.telephony.SignalStrength.class
                        .getMethods();
                for (Method mthd : methods) {
                    if (mthd.getName().equals("getLteSignalStrength")
                            || mthd.getName().equals("getLteRsrp")
                            || mthd.getName().equals("getLteRsrq")
                            || mthd.getName().equals("getLteRssnr")
                            || mthd.getName().equals("getLteCqi")) {
                        /*Log.i(LOG_TAG,
                                "onSignalStrengthsChanged: " + mthd.getName() + " "
                                        + mthd.invoke(signalStrength));*/
                        if(mthd.getName().equals("getLteRsrp")) {
                            dbm.setText(mthd.invoke(signalStrength).toString());
                            network.setText("LTE");
                        }
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
            // Reflection code ends here

        }
    }
}
