package com.cumple.cumple.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cumple.cumple.R;
import com.cumple.cumple.adapters.BirthdayAdapter;
import com.cumple.cumple.models.Birthday;
import com.cumple.cumple.utils.FirebaseHelper;
import com.cumple.cumple.utils.NotificationHelper;
import com.cumple.cumple.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BirthdayAdapter.OnBirthdayListener {
    private static final String TAG = "MainActivity";
    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fab;
    private TextView emptyStateTextView;
    private SearchView searchView;

    private BirthdayAdapter adapter;
    private List<Birthday> birthdayList;
    private List<Birthday> filteredList;

    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private NotificationHelper notificationHelper;
    private FirebaseAuth firebaseAuth;

    private int currentTab = 0; // 0: All, 1: Upcoming, 2: Today

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Google Play Services Security Provider
        initializeGooglePlayServices();
        
        // Inicializar Firebase y SessionManager
        firebaseHelper = FirebaseHelper.getInstance();
        firebaseHelper.setContext(this); // Establecer el contexto para verificar la conectividad
        sessionManager = new SessionManager(this);
        notificationHelper = new NotificationHelper(this);
        firebaseAuth = FirebaseAuth.getInstance();

        // Verificar si el usuario está logueado
        sessionManager.checkLogin();

        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        fab = findViewById(R.id.fab_add);
        emptyStateTextView = findViewById(R.id.tv_empty_state);

        // Configurar Toolbar
        setSupportActionBar(toolbar);

        // Configurar TabLayout
        setupTabLayout();

        // Configurar RecyclerView
        birthdayList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new BirthdayAdapter(filteredList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Configurar SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadBirthdays();
            }
        });

        // Configurar FAB
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddEditBirthdayActivity.class));
            }
        });

        // Solicitar permisos de notificación en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }

        // Cargar cumpleaños
        loadBirthdays();
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_all));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_upcoming));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_today));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterBirthdays(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void loadBirthdays() {
        swipeRefreshLayout.setRefreshing(true);

        firebaseHelper.getBirthdays(new FirebaseHelper.OnBirthdaysLoadedListener() {
            @Override
            public void onSuccess(List<Birthday> birthdays) {
                // Ejecutar cálculos pesados en un hilo secundario
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Procesar datos en hilo secundario
                        final List<Birthday> processedBirthdays = new ArrayList<>();
                        for (Birthday birthday : birthdays) {
                            birthday.calculateDaysLeft();
                            processedBirthdays.add(birthday);
                        }
                        
                        // Actualizar UI en el hilo principal
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                birthdayList.clear();
                                birthdayList.addAll(processedBirthdays);
                                
                                // Filtrar según la pestaña seleccionada
                                filterBirthdays(tabLayout.getSelectedTabPosition());
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void filterBirthdays(int tabPosition) {
        // Guardar la posición actual de la pestaña
        currentTab = tabPosition;
        
        // Ejecutar el filtrado en un hilo secundario
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Birthday> newFilteredList = new ArrayList<>();
                
                if (birthdayList.isEmpty()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            filteredList.clear();
                            adapter.notifyDataSetChanged();
                            showEmptyState();
                        }
                    });
                    return;
                }
                
                switch (currentTab) {
                    case 0: // Todos
                        newFilteredList.addAll(birthdayList);
                        break;
                    case 1: // Próximos (7 días)
                        for (Birthday birthday : birthdayList) {
                            if (birthday.getDaysLeft() <= 7 && birthday.getDaysLeft() > 0) {
                                newFilteredList.add(birthday);
                            }
                        }
                        break;
                    case 2: // Hoy
                        for (Birthday birthday : birthdayList) {
                            if (birthday.getDaysLeft() == 0) {
                                newFilteredList.add(birthday);
                            }
                        }
                        break;
                }
                
                // Actualizar la UI en el hilo principal
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        filteredList.clear();
                        filteredList.addAll(newFilteredList);
                        adapter.notifyDataSetChanged();
                        
                        if (filteredList.isEmpty()) {
                            showEmptyState();
                        } else {
                            hideEmptyState();
                        }
                    }
                });
            }
        }).start();
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateTextView.setVisibility(View.GONE);
    }

    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Las notificaciones están desactivadas", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // No recargar datos si ya los tenemos para evitar lag
        if (birthdayList == null || birthdayList.isEmpty()) {
            loadBirthdays();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchBirthdays(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            sessionManager.logoutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void searchBirthdays(String query) {
        if (query.isEmpty()) {
            filterBirthdays(tabLayout.getSelectedTabPosition());
            return;
        }
        
        // Buscar en un hilo secundario para evitar bloquear la UI
        final String lowerQuery = query.toLowerCase();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Birthday> searchResults = new ArrayList<>();
                
                for (Birthday birthday : birthdayList) {
                    if (birthday.getName().toLowerCase().contains(lowerQuery) ||
                            (birthday.getPhone() != null && birthday.getPhone().toLowerCase().contains(lowerQuery))) {
                        searchResults.add(birthday);
                    }
                }
                
                // Actualizar UI en el hilo principal
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        filteredList.clear();
                        filteredList.addAll(searchResults);
                        adapter.notifyDataSetChanged();
                        
                        if (filteredList.isEmpty()) {
                            showEmptyState();
                        } else {
                            hideEmptyState();
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onBirthdayClick(int position) {
        Birthday birthday = filteredList.get(position);
        Toast.makeText(this, "Ubicación: " + birthday.getUbicacion(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, AddEditBirthdayActivity.class);
        intent.putExtra("birthday_id", birthday.getId());
        startActivity(intent);
    }







    private void deleteBirthday(Birthday birthday) {
        firebaseHelper.deleteBirthday(birthday.getId(), new FirebaseHelper.OnBirthdayDeletedListener() {
            @Override
            public void onSuccess() {
                // Cancelar notificación si estaba programada
                notificationHelper.cancelNotification(birthday.getId());

                Toast.makeText(MainActivity.this, R.string.birthday_deleted, Toast.LENGTH_SHORT).show();
                loadBirthdays();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Inicializa los servicios de seguridad de Google Play
     * Resuelve el error: "Failed to get service from broker"
     */
    private void initializeGooglePlayServices() {
        try {
            // Actualizar los servicios de seguridad de Google Play
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            // El problema puede ser reparado por el usuario
            GoogleApiAvailability.getInstance()
                    .showErrorNotification(this, e.getConnectionStatusCode());
        } catch (GooglePlayServicesNotAvailableException e) {
            // El problema no puede ser reparado, la app debe manejarlo de forma adecuada
            Log.e(TAG, "Google Play Services no disponible: " + e.getMessage());
        } catch (Exception e) {
            // Capturar cualquier otra excepción por seguridad
            Log.e(TAG, "Error al inicializar Google Play Services: " + e.getMessage());
        }
    }
}
