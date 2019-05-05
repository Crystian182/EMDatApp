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

    private static final int REQUEST_ACCESS_COARSE_LOCATION = 0;
    private static final int REQUEST_READ_PHONE_STATE = 0;
    private static final int REQUEST_ACCESS_WIFI_STATE = 0;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    public static String LOG_TAG = "CustomPhoneStateListener";

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

        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsNeeded.add("GPS");
        if (!addPermission(permissionsList, Manifest.permission.READ_PHONE_STATE))
            permissionsNeeded.add("Phone State");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_WIFI_STATE))
            permissionsNeeded.add("WiFi State");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }
    }

    public void updateSignals() {
        if (ContextCompat.checkSelfPermission(InfoActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(InfoActivity.this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(InfoActivity.this, Manifest.permission.ACCESS_WIFI_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            //telephonyManager.listen(new CustomPhoneStateListener(this),
            //        PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            int strengthDbm = 0;

            myPhoneStateListener psListener = new myPhoneStateListener();
            telephonyManager.listen(psListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

            try {
                if (telephonyManager.getAllCellInfo() != null) {
                    /*for (final CellInfo info : telephonyManager.getAllCellInfo()) {
                        if (info instanceof CellInfoGsm) {
                            final CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                            // do what you need

                            strengthDbm = gsm.getDbm();
                            network.setText("GSM");
                        } else if (info instanceof CellInfoCdma) {
                            final CellSignalStrengthCdma cdma = ((CellInfoCdma) info).getCellSignalStrength();
                            // do what you need
                            strengthDbm = cdma.getDbm();
                            network.setText("CDMA");
                        } else if (info instanceof CellInfoLte) {
                            final CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                            // do what you need
                            strengthDbm = lte.getDbm();
                            network.setText("LTE");
                        } else {
                            throw new Exception("Unknown type of cell signal!");
                        }
                    }*/
                } else {
                    dbm.setText("ehi" + telephonyManager.getCellLocation().toString());
                }

            } catch (Exception e) {
                Log.e("InfoActivity", "Unable to obtain cell signal information", e);
            }

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

            //dbm.setText(Integer.toString(strengthDbm));
            deviceid.setText(subscriberID);
            simserial.setText(SIMSerialNumber);
            networkcountry.setText(networkCountryISO);
            simcountry.setText(SIMCountryISO);
            imei.setText(IMEINumber);
            wifisignal.setText(wifi);
            carrier.setText(carrierName);
        }
    }

    public boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(InfoActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        if (ContextCompat.checkSelfPermission(InfoActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        if (ContextCompat.checkSelfPermission(InfoActivity.this, Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(InfoActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_CONTACTS, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    updateSignals();
                } else {
                    // Permission Denied
                    Toast.makeText(InfoActivity.this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
