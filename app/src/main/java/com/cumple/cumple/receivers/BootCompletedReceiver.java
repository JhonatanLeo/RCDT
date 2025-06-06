package com.cumple.cumple.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cumple.cumple.models.Birthday;
import com.cumple.cumple.utils.FirebaseHelper;
import com.cumple.cumple.utils.NotificationHelper;

import java.util.List;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "Dispositivo reiniciado, reprogramando notificaciones");

            FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
            NotificationHelper notificationHelper = new NotificationHelper(context);

            // Verificar si hay un usuario logueado
            if (firebaseHelper.getCurrentUser() == null) {
                Log.d(TAG, "No hay usuario logueado, no se reprogramarán notificaciones");
                return;
            }

            // Obtener todos los cumpleaños y reprogramar notificaciones
            firebaseHelper.getBirthdays(new FirebaseHelper.OnBirthdaysLoadedListener() {
                @Override
                public void onSuccess(List<Birthday> birthdays) {
                    Log.d(TAG, "Se encontraron " + birthdays.size() + " recordatorios para reprogramar");

                    for (Birthday birthday : birthdays) {
                        if (birthday.isNotificationEnabled()) {
                            notificationHelper.scheduleNotification(birthday);
                            Log.d(TAG, "Notificación reprogramada para: " + birthday.getName());
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error al cargar recordatorios después del reinicio: " + e.getMessage());
                }
            });
        }
    }
}
