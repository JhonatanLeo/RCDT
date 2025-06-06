package com.cumple.cumple.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cumple.cumple.utils.NotificationHelper;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Notificación recibida");

        String birthdayId = intent.getStringExtra("birthdayId");
        String birthdayName = intent.getStringExtra("birthdayName");
        boolean isTest = intent.getBooleanExtra("isTest", false);

        if (birthdayId == null || birthdayName == null) {
            Log.e(TAG, "No se proporcionó ID o nombre del recordatorio en el intent");
            return;
        }

        Log.d(TAG, "Mostrando notificación para: " + birthdayName + (isTest ? " (prueba)" : ""));

        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.showNotification(birthdayId, birthdayName, isTest);
    }
}
