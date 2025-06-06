package com.cumple.cumple.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.cumple.cumple.R;
import com.cumple.cumple.activities.MainActivity;
import com.cumple.cumple.models.Birthday;
import com.cumple.cumple.receivers.NotificationReceiver;

import java.util.Calendar;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "BirthdayReminders";

    private Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    @SuppressLint("ScheduleExactAlarm")
    public void scheduleNotification(Birthday birthday) {
        Log.d(TAG, "Programando notificación para: " + birthday.getName());

        // Configurar la fecha de la notificación (un día antes del cumpleaños)
        Calendar calendar = Calendar.getInstance();

        try {
            // Extraer día y mes del cumpleaños
            int day = birthday.getDay();
            int month = birthday.getMonth() - 1; // Calendar usa 0-11 para meses

            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, 9); // Notificar a las 9 AM
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // Si la fecha ya pasó este año, programar para el próximo año
            Calendar today = Calendar.getInstance();
            if (calendar.before(today)) {
                calendar.add(Calendar.YEAR, 1);
            }

            // Restar un día para notificar un día antes
            calendar.add(Calendar.DAY_OF_MONTH, -1);

            Log.d(TAG, "Fecha de notificación programada: " + calendar.getTime().toString());

            // Crear intent para el BroadcastReceiver
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("birthdayId", birthday.getId());
            intent.putExtra("birthdayName", birthday.getName());

            // Asegurarse de que el PendingIntent sea único para cada recordatorio
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    birthday.getId().hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Programar la alarma
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }

                Log.d(TAG, "Notificación programada para " + birthday.getName() + " el " + calendar.getTime());

                // Para pruebas: programar una notificación para 1 minuto después
                Calendar testCalendar = Calendar.getInstance();
                testCalendar.add(Calendar.MINUTE, 1);

                Intent testIntent = new Intent(context, NotificationReceiver.class);
                testIntent.putExtra("birthdayId", birthday.getId());
                testIntent.putExtra("birthdayName", birthday.getName());
                testIntent.putExtra("isTest", true);

                PendingIntent testPendingIntent = PendingIntent.getBroadcast(
                        context,
                        (birthday.getId() + "test").hashCode(),
                        testIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            testCalendar.getTimeInMillis(),
                            testPendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            testCalendar.getTimeInMillis(),
                            testPendingIntent
                    );
                }

                Log.d(TAG, "Notificación de prueba programada para " + birthday.getName() + " en 1 minuto");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al programar notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cancelNotification(String birthdayId) {
        try {
            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    birthdayId.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // También cancelar la notificación de prueba
            Intent testIntent = new Intent(context, NotificationReceiver.class);
            PendingIntent testPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (birthdayId + "test").hashCode(),
                    testIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                alarmManager.cancel(testPendingIntent);
                Log.d(TAG, "Notificaciones canceladas para ID: " + birthdayId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al cancelar notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showNotification(String birthdayId, String birthdayName, boolean isTest) {
        try {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
            );

            String title = isTest ?
                    context.getString(R.string.test_notification_title) :
                    context.getString(R.string.notification_title);

            String message = context.getString(R.string.notification_message, birthdayName);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            // Verificar permisos para Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "No se tienen permisos para mostrar notificaciones");
                    return;
                }
            }

            int notificationId = isTest ?
                    (birthdayId + "test").hashCode() :
                    birthdayId.hashCode();

            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notificación mostrada para: " + birthdayName + (isTest ? " (prueba)" : ""));
        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getString(R.string.notification_channel_description));

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Canal de notificaciones creado");
        }
    }

    public void showTestNotification(Birthday birthday) {
    }
}
