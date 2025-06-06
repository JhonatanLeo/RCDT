package com.cumple.cumple.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.cumple.cumple.activities.LoginActivity;
import com.cumple.cumple.models.User;
import com.google.firebase.auth.FirebaseUser;

public class SessionManager {
    private static final String PREF_NAME = "BirthdayReminderPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private FirebaseHelper firebaseHelper;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        firebaseHelper = FirebaseHelper.getInstance();
    }

    public void createLoginSession(FirebaseUser user, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, user.getUid());
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.commit();
    }

    public void updateUserInfo(User user) {
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.commit();
    }

    public User getUserDetails() {
        String userId = pref.getString(KEY_USER_ID, "");
        String name = pref.getString(KEY_USER_NAME, "");
        String email = pref.getString(KEY_USER_EMAIL, "");

        return new User(userId, name, email);
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    public void logoutUser() {
        // Cerrar sesión en Firebase
        firebaseHelper.logoutUser();

        // Limpiar SharedPreferences
        editor.clear();
        editor.commit();

        // Redirigir al login
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void checkLogin() {
        if (!isLoggedIn()) {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }
}
