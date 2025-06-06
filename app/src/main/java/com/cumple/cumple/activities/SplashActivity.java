package com.cumple.cumple.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.cumple.cumple.R;
import com.cumple.cumple.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 1500; // 1.5 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);
        Intent intent = sessionManager.isLoggedIn() ?
                new Intent(this, MainActivity.class) :
                new Intent(this, LoginActivity.class);

        startActivity(intent);
        finish();
    }

}
