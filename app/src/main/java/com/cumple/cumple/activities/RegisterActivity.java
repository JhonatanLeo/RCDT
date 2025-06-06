package com.cumple.cumple.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cumple.cumple.R;
import com.cumple.cumple.utils.FirebaseHelper;
import com.cumple.cumple.utils.SessionManager;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private Button registerButton;
    private TextView loginLink;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase y SessionManager
        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);

        // Inicializar vistas
        nameLayout = findViewById(R.id.til_name);
        emailLayout = findViewById(R.id.til_email);
        passwordLayout = findViewById(R.id.til_password);
        confirmPasswordLayout = findViewById(R.id.til_confirm_password);
        registerButton = findViewById(R.id.btn_register);
        loginLink = findViewById(R.id.tv_login_link);
        progressBar = findViewById(R.id.progress_bar);

        // Configurar listeners
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Volver a LoginActivity
            }
        });
    }

    private void registerUser() {
        // Validar campos
        if (!validateInputs()) {
            return;
        }

        // Mostrar progreso
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        String name = nameLayout.getEditText().getText().toString().trim();
        String email = emailLayout.getEditText().getText().toString().trim();
        String password = passwordLayout.getEditText().getText().toString().trim();

        firebaseHelper.registerUser(email, password, name, new FirebaseHelper.OnRegistrationListener() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Crear sesión
                sessionManager.createLoginSession(user, name);

                // Redirigir al MainActivity
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);

                String errorMessage = e.getMessage();
                if (errorMessage.contains("email")) {
                    emailLayout.setError(getString(R.string.email_already_in_use));
                } else if (errorMessage.contains("password")) {
                    passwordLayout.setError(getString(R.string.weak_password));
                } else {
                    Toast.makeText(RegisterActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String name = nameLayout.getEditText().getText().toString().trim();
        String email = emailLayout.getEditText().getText().toString().trim();
        String password = passwordLayout.getEditText().getText().toString().trim();
        String confirmPassword = confirmPasswordLayout.getEditText().getText().toString().trim();

        // Validar nombre
        if (name.isEmpty()) {
            nameLayout.setError(getString(R.string.error_empty_fields));
            isValid = false;
        } else {
            nameLayout.setError(null);
        }

        // Validar email
        if (email.isEmpty()) {
            emailLayout.setError(getString(R.string.error_empty_fields));
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.invalid_email));
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        // Validar contraseña
        if (password.isEmpty()) {
            passwordLayout.setError(getString(R.string.error_empty_fields));
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError(getString(R.string.weak_password));
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        // Validar confirmación de contraseña
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError(getString(R.string.error_empty_fields));
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordLayout.setError(getString(R.string.passwords_dont_match));
            isValid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        return isValid;
    }
}
