package com.cumple.cumple.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cumple.cumple.models.Birthday;
import com.cumple.cumple.utils.FirebaseHelper;
import com.cumple.cumple.utils.NotificationHelper;

public class BirthdayAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "BirthdayAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String birthdayId = intent.getStringExtra("birthdayId");
        if (birthdayId == null) {
            Log.e(TAG, "No birthdayId provided in intent");
            return;
        }

        FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
        firebaseHelper.getBirthdayById(birthdayId, new FirebaseHelper.OnBirthdayLoadedListener() {
            @Override
            public void onSuccess(Birthday birthday) {
                NotificationHelper notificationHelper = new NotificationHelper(context);
                notificationHelper.showTestNotification(birthday);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error fetching birthday: " + e.getMessage());
            }
        });
    }
}
