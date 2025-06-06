package com.cumple.cumple.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cumple.cumple.R;
import com.cumple.cumple.models.Birthday;
import com.cumple.cumple.utils.FirebaseHelper;
import com.cumple.cumple.utils.NotificationHelper;
import com.cumple.cumple.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AddEditBirthdayActivity extends AppCompatActivity {
    private static final String TAG = "AddEditBirthdayActivity";

    private TextInputLayout nameLayout;
    private TextInputLayout phoneLayout;
    private TextInputLayout dateLayout;
    private TextInputLayout areaLayout;

    private TextInputEditText nameEditText;
    private TextInputEditText phoneEditText;
    private TextInputEditText dateEditText;
    private TextInputEditText areaEditText;
    private Spinner mesAdquisicionSpinner;
    private TextInputLayout ubicacionLayout;
    private TextInputEditText ubicacionEditText;

    private Switch notificationSwitch;
    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;
    private Toolbar toolbar;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private NotificationHelper notificationHelper;

    private Birthday birthday;
    private boolean isEditMode = false;
    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private int selectedMesAdquisicion = 1; // Por defecto enero

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_birthday);

        // Inicializar Firebase y SessionManager
        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);
        notificationHelper = new NotificationHelper(this);

        // Inicializar vistas
        initViews();

        // Configurar Toolbar
        setupToolbar();

        // Configurar DatePicker
        setupDatePicker();

        // Configurar Spinner de meses
        setupMonthSpinner();

        // Verificar si estamos en modo edición
        if (getIntent().hasExtra("birthday_id")) {
            isEditMode = true;
            String birthdayId = getIntent().getStringExtra("birthday_id");
            loadBirthdayData(birthdayId);

            // Mostrar botón de eliminar en modo edición
            deleteButton.setVisibility(View.VISIBLE);
        }

        // Configurar listeners
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBirthday();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBirthday();
            }
        });
    }

    private void initViews() {
        nameLayout = findViewById(R.id.nameLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        dateLayout = findViewById(R.id.dateLayout);
        areaLayout = findViewById(R.id.areaLayout);

        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        dateEditText = findViewById(R.id.dateEditText);
        areaEditText = findViewById(R.id.areaEditText);
        ubicacionLayout = findViewById(R.id.ubicacionLayout);
        ubicacionEditText = findViewById(R.id.ubicacionEditText);

        mesAdquisicionSpinner = findViewById(R.id.mesAdquisicionSpinner);

        notificationSwitch = findViewById(R.id.notificationSwitch);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        deleteButton = findViewById(R.id.deleteButton);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(isEditMode ? R.string.edit_birthday : R.string.add_birthday);
    }

    private void setupDatePicker() {
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void setupMonthSpinner() {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, meses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mesAdquisicionSpinner.setAdapter(adapter);

        mesAdquisicionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMesAdquisicion = position + 1; // Los meses van de 1 a 12
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMesAdquisicion = 1; // Por defecto enero
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                android.R.style.Theme_DeviceDefault_Dialog_Alert,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        dateEditText.setText(dateFormat.format(selectedDate.getTime()));
                    }
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadBirthdayData(String birthdayId) {
        progressBar.setVisibility(View.VISIBLE);

        firebaseHelper.getBirthdayById(birthdayId, new FirebaseHelper.OnBirthdayLoadedListener() {
            @Override
            public void onSuccess(Birthday loadedBirthday) {
                birthday = loadedBirthday;

                // Llenar campos con los datos del cumpleaños
                nameEditText.setText(birthday.getName());
                phoneEditText.setText(birthday.getPhone());
                dateEditText.setText(birthday.getDate());
                areaEditText.setText(String.valueOf(birthday.getArea()));
                ubicacionEditText.setText(birthday.getUbicacion());


                // Seleccionar el mes en el spinner (los meses van de 1 a 12)
                int mesIndex = birthday.getMesAdquisicion() - 1;
                if (mesIndex >= 0 && mesIndex < 12) {
                    mesAdquisicionSpinner.setSelection(mesIndex);
                }

                notificationSwitch.setChecked(birthday.isNotificationEnabled());

                try {
                    Date date = dateFormat.parse(birthday.getDate());
                    selectedDate.setTime(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al cargar el recordatorio: " + e.getMessage());
                Toast.makeText(AddEditBirthdayActivity.this, "Error al cargar el recordatorio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                finish();
            }
        });
    }

    private void saveBirthday() {
        if (!validateInputs()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String ubicacion = ubicacionEditText.getText().toString().trim();


        // Convertir área a entero
        int area;
        try {
            area = Integer.parseInt(areaEditText.getText().toString().trim());
        } catch (NumberFormatException e) {
            areaLayout.setError(getString(R.string.error_invalid_number));
            progressBar.setVisibility(View.GONE);

            return;
        }

        boolean notificationEnabled = notificationSwitch.isChecked();

        if (isEditMode && birthday != null) {
            // Actualizar cumpleaños existente
            birthday.setName(name);
            birthday.setPhone(phone);
            birthday.setDate(date);
            birthday.setArea(area);
            birthday.setMesAdquisicion(selectedMesAdquisicion);
            birthday.setNotificationEnabled(notificationEnabled);
            birthday.calculateDaysLeft();
            birthday.setUbicacion(ubicacion);


            firebaseHelper.updateBirthday(birthday, new FirebaseHelper.OnBirthdayUpdatedListener() {
                @Override
                public void onSuccess() {
                    // Actualizar notificación si está habilitada
                    if (notificationEnabled) {
                        notificationHelper.scheduleNotification(birthday);
                    } else {
                        notificationHelper.cancelNotification(birthday.getId());
                    }

                    Toast.makeText(AddEditBirthdayActivity.this, R.string.birthday_updated, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error al actualizar el recordatorio: " + e.getMessage());
                    Toast.makeText(AddEditBirthdayActivity.this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            // Crear nuevo cumpleaños
            String id = UUID.randomUUID().toString();
            SessionManager sessionManager = new SessionManager(this);
            String userId = sessionManager.getUserId();

            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "User ID from SessionManager is null or empty. Cannot save birthday. User might need to log in again.");
                Toast.makeText(this, "Error: No se pudo obtener el ID de usuario. Por favor, inicie sesión de nuevo.", Toast.LENGTH_LONG).show();
                return; // Prevent further execution
            }

            Log.d(TAG, "Attempting to save new birthday. User ID from SessionManager: " + userId);

            Birthday newBirthday = new Birthday(id, userId, name, phone, ubicacion, date, area, selectedMesAdquisicion, notificationEnabled);


            firebaseHelper.addBirthday(newBirthday, new FirebaseHelper.OnBirthdayAddedListener() {
                @Override
                public void onSuccess(String birthdayId) {
                    // Programar notificación si está habilitada
                    if (notificationEnabled) {
                        newBirthday.setId(birthdayId);
                        notificationHelper.scheduleNotification(newBirthday);
                    }

                    Toast.makeText(AddEditBirthdayActivity.this, R.string.birthday_added, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error al agregar el recordatorio: " + e.getMessage());
                    Toast.makeText(AddEditBirthdayActivity.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void deleteBirthday() {
        if (birthday == null) {
            Toast.makeText(this, "No se puede eliminar, recordatorio no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        firebaseHelper.deleteBirthday(birthday.getId(), new FirebaseHelper.OnBirthdayDeletedListener() {
            @Override
            public void onSuccess() {
                // Cancelar notificación si estaba programada
                notificationHelper.cancelNotification(birthday.getId());

                Toast.makeText(AddEditBirthdayActivity.this, R.string.birthday_deleted, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al eliminar el recordatorio: " + e.getMessage());
                Toast.makeText(AddEditBirthdayActivity.this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validar nombre
        if (nameEditText.getText().toString().trim().isEmpty()) {
            nameLayout.setError(getString(R.string.error_empty_fields));
            isValid = false;
        } else {
            nameLayout.setError(null);
        }

        // Validar fecha
        if (dateEditText.getText().toString().trim().isEmpty()) {
            dateLayout.setError(getString(R.string.error_empty_fields));
            isValid = false;
        } else {
            dateLayout.setError(null);
        }

        // Validar área (debe ser un número)
        String areaText = areaEditText.getText().toString().trim();
        if (areaText.isEmpty()) {
            areaLayout.setError(getString(R.string.error_empty_fields));
            isValid = false;
        } else {
            try {
                Integer.parseInt(areaText);
                areaLayout.setError(null);
            } catch (NumberFormatException e) {
                areaLayout.setError(getString(R.string.error_invalid_number));
                isValid = false;
            }
        }

        return isValid;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
