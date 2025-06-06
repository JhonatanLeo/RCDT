package com.cumple.cumple.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cumple.cumple.R;
import com.cumple.cumple.models.User;
import com.cumple.cumple.utils.FirebaseHelper;
import com.cumple.cumple.utils.SessionManager;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextInputLayout currentPasswordLayout;
    private TextInputLayout newPasswordLayout;
    private TextInputLayout confirmPasswordLayout;
    private Button changePasswordButton;
    private Button logoutButton;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // El título se establece en el XML (app:title="@string/profile_title")
        }

        nameTextView = findViewById(R.id.tv_name);
        emailTextView = findViewById(R.id.tv_email);
        currentPasswordLayout = findViewById(R.id.til_current_password);
        newPasswordLayout = findViewById(R.id.til_new_password);
        confirmPasswordLayout = findViewById(R.id.til_confirm_password);
        changePasswordButton = findViewById(R.id.btn_change_password);
        logoutButton = findViewById(R.id.btn_logout);
        progressBar = findViewById(R.id.progress_bar);

        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);

        loadUserData();

        if (changePasswordButton != null) {
            changePasswordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changePassword();
                }
            });
        }

        if (logoutButton != null) {
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sessionManager.logoutUser();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    private void loadUserData() {
        User user = sessionManager.getUserDetails();
        if (user != null) {
            if (user.getName() != null && nameTextView != null) {
                nameTextView.setText(user.getName());
            } else if (nameTextView != null) {
                nameTextView.setText(R.string.profile_name_placeholder); // Placeholder si no hay nombre
            }

            if (user.getEmail() != null && emailTextView != null) {
                emailTextView.setText(user.getEmail());
            } else if (emailTextView != null) {
                emailTextView.setText(R.string.profile_email_placeholder); // Placeholder si no hay email
            }
        }
    }

    private void changePassword() {
        if (!validatePasswordInputs()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        changePasswordButton.setEnabled(false);
        if (logoutButton != null) logoutButton.setEnabled(false);


        String currentPassword = currentPasswordLayout.getEditText().getText().toString().trim();
        String newPassword = newPasswordLayout.getEditText().getText().toString().trim();

        firebaseHelper.changePassword(currentPassword, newPassword, new FirebaseHelper.OnPasswordChangeListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(ProfileActivity.this, R.string.password_changed, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                changePasswordButton.setEnabled(true);
                if (logoutButton != null) logoutButton.setEnabled(true);

                // Limpiar campos y errores
                if(currentPasswordLayout.getEditText() != null) currentPasswordLayout.getEditText().setText("");
                if(newPasswordLayout.getEditText() != null) newPasswordLayout.getEditText().setText("");
                if(confirmPasswordLayout.getEditText() != null) confirmPasswordLayout.getEditText().setText("");
                currentPasswordLayout.setError(null);
                newPasswordLayout.setError(null);
                confirmPasswordLayout.setError(null);
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                changePasswordButton.setEnabled(true);
                if (logoutButton != null) logoutButton.setEnabled(true);

                String errorMessage = e.getMessage();
                if (errorMessage != null && (errorMessage.contains("password") || errorMessage.contains("CREDENTIAL_TOO_OLD_LOGIN_AGAIN") || errorMessage.contains("INVALID_LOGIN_CREDENTIALS"))) {
                    currentPasswordLayout.setError(getString(R.string.wrong_current_password));
                } else {
                    Toast.makeText(ProfileActivity.this, getString(R.string.error_password_change_failed) + (errorMessage != null ? ": " + errorMessage : ""), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean validatePasswordInputs() {
        boolean isValid = true;

        String currentPassword = currentPasswordLayout.getEditText() != null ? currentPasswordLayout.getEditText().getText().toString().trim() : "";
        String newPassword = newPasswordLayout.getEditText() != null ? newPasswordLayout.getEditText().getText().toString().trim() : "";
        String confirmPassword = confirmPasswordLayout.getEditText() != null ? confirmPasswordLayout.getEditText().getText().toString().trim() : "";

        // Validar contraseña actual
        if (currentPassword.isEmpty()) {
            currentPasswordLayout.setError(getString(R.string.error_empty_fields));
            isValid = false;
        } else {
            currentPasswordLayout.setError(null);
        }

        // Validar nueva contraseña
        if (newPassword.isEmpty()) {
            newPasswordLayout.setError(getString(R.string.error_empty_fields));
            isValid = false;
        } else if (newPassword.length() < 6) {
            newPasswordLayout.setError(getString(R.string.password_too_short));
            isValid = false;
        } else {
            newPasswordLayout.setError(null);
        }

        // Validar confirmación de contraseña
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError(getString(R.string.error_empty_fields));
            isValid = false;
        } else if (!confirmPassword.equals(newPassword)) {
            confirmPasswordLayout.setError(getString(R.string.passwords_not_match));
            isValid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        return isValid;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // O finish(); para cerrar la actividad
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
