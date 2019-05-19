package com.apollon.emdatapp.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.apollon.emdatapp.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent stopIntent = new Intent("stopApp");
        LocalBroadcastManager.getInstance(context).sendBroadcast(stopIntent);
    }
}
