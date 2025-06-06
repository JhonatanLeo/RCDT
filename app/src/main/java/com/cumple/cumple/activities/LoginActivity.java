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

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private Button loginButton;
    private TextView registerLink;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase y SessionManager
        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);

        // Inicializar vistas
        emailLayout = findViewById(R.id.til_email);
        passwordLayout = findViewById(R.id.til_password);
        loginButton = findViewById(R.id.btn_login);
        registerLink = findViewById(R.id.tv_register_link);
        progressBar = findViewById(R.id.progress_bar);

        // Configurar listeners
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginUser() {
        // Validar campos
        if (!validateInputs()) {
            return;
        }

        // Mostrar progreso
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        String email = emailLayout.getEditText().getText().toString().trim();
        String password = passwordLayout.getEditText().getText().toString().trim();

        firebaseHelper.loginUser(email, password, new FirebaseHelper.OnLoginListener() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Obtener datos del usuario
                firebaseHelper.getUserData(new FirebaseHelper.OnUserFetchListener() {
                    @Override
                    public void onSuccess(com.cumple.cumple.models.User userData) {
                        // Crear sesión
                        sessionManager.createLoginSession(user, userData.getName());

                        // Redirigir al MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);

                String errorMessage = e.getMessage();
                if (errorMessage.contains("password")) {
                    passwordLayout.setError(getString(R.string.wrong_password));
                } else if (errorMessage.contains("user")) {
                    emailLayout.setError(getString(R.string.user_not_found));
                } else {
                    Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String email = emailLayout.getEditText().getText().toString().trim();
        String password = passwordLayout.getEditText().getText().toString().trim();

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
        } else {
            passwordLayout.setError(null);
        }

        return isValid;
    }
}
