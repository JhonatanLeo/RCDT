package com.cumple.cumple.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cumple.cumple.models.Birthday;
import com.cumple.cumple.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private static FirebaseHelper instance;

    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private FirebaseHelper() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Configurar Firestore para caché offline
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);
    }
    
    public void setContext(Context context) {
        this.context = context;
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }
    
    /**
     * Verifica si el dispositivo está conectado a Internet
     * @return true si hay conexión a Internet, false en caso contrario
     */
    public boolean isConnectedToInternet() {
        if (context == null) {
            Log.e(TAG, "Context is null in isConnectedToInternet");
            return true; // Asumir que hay conexión si no podemos verificarlo
        }
        
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
    
    /**
     * Determina la fuente de datos a utilizar según el estado de la conexión
     * @return Source.CACHE si no hay conexión, Source.DEFAULT si hay conexión
     */
    private Source getDataSource() {
        return isConnectedToInternet() ? Source.DEFAULT : Source.CACHE;
    }

    // Interfaces para callbacks
    public interface OnRegistrationListener {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }

    public interface OnLoginListener {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }

    public interface OnUserFetchListener {
        void onSuccess(User user);
        void onFailure(Exception e);
    }

    public interface OnBirthdayAddedListener {
        void onSuccess(String birthdayId);
        void onFailure(Exception e);
    }

    public interface OnBirthdayUpdatedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnBirthdayDeletedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnBirthdaysLoadedListener {
        void onSuccess(List<Birthday> birthdays);
        void onFailure(Exception e);
    }

    public interface OnBirthdayLoadedListener {
        void onSuccess(Birthday birthday);
        void onFailure(Exception e);
    }

    public interface OnPasswordChangeListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    // Métodos de autenticación
    public void registerUser(String email, String password, String name, OnRegistrationListener listener) {
        Log.d(TAG, "Registrando usuario: " + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Guardar información adicional del usuario en Firestore
                            User newUser = new User(user.getUid(), name, email);
                            db.collection("users").document(user.getUid())
                                    .set(newUser)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Usuario registrado correctamente: " + user.getEmail());
                                            listener.onSuccess(user);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "Error al guardar datos del usuario: " + e.getMessage());
                                            listener.onFailure(e);
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Error al registrar usuario: " + task.getException().getMessage());
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }

    public void loginUser(String email, String password, OnLoginListener listener) {
        Log.d(TAG, "Iniciando sesión: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d(TAG, "Inicio de sesión exitoso: " + user.getEmail());
                            listener.onSuccess(user);
                        } else {
                            Log.e(TAG, "Error al iniciar sesión: " + task.getException().getMessage());
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }

    public void logoutUser() {
        Log.d(TAG, "Cerrando sesión");
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public String getUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public void getUserData(OnUserFetchListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario logueado");
            listener.onFailure(new Exception("No user is signed in"));
            return;
        }

        Log.d(TAG, "Obteniendo datos del usuario: " + currentUser.getEmail());

        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            Log.d(TAG, "Datos del usuario obtenidos correctamente");
                            listener.onSuccess(user);
                        } else {
                            Log.e(TAG, "No se encontraron datos del usuario");
                            listener.onFailure(new Exception("User data not found"));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al obtener datos del usuario: " + e.getMessage());
                        listener.onFailure(e);
                    }
                });
    }

    public void changePassword(String currentPassword, String newPassword, OnPasswordChangeListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e(TAG, "No hay usuario logueado");
            listener.onFailure(new Exception("No user is signed in"));
            return;
        }

        Log.d(TAG, "Cambiando contraseña para: " + user.getEmail());

        // Reautenticar al usuario
        mAuth.signInWithEmailAndPassword(user.getEmail(), currentPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Cambiar la contraseña
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Contraseña cambiada correctamente");
                                                listener.onSuccess();
                                            } else {
                                                Log.e(TAG, "Error al cambiar contraseña: " + task.getException().getMessage());
                                                listener.onFailure(task.getException());
                                            }
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Error al reautenticar: " + task.getException().getMessage());
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }

    // Métodos para gestionar cumpleaños
    public void addBirthday(Birthday birthday, OnBirthdayAddedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario logueado");
            listener.onFailure(new Exception("No user is signed in"));
            return;
        }

        Log.d(TAG, "Agregando recordatorio: " + birthday.getName());

        // Asegurarse de que el cumpleaños tenga el ID del usuario actual
        birthday.setUserId(currentUser.getUid());

        // Verificar si ya existe un recordatorio con el mismo ID
        if (birthday.getId() != null) {
            db.collection("birthdays").document(birthday.getId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                // Ya existe, actualizar en lugar de crear uno nuevo
                                updateBirthday(birthday, new OnBirthdayUpdatedListener() {
                                    @Override
                                    public void onSuccess() {
                                        listener.onSuccess(birthday.getId());
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        listener.onFailure(e);
                                    }
                                });
                            } else {
                                // No existe, crear uno nuevo
                                createNewBirthday(birthday, listener);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            listener.onFailure(e);
                        }
                    });
        } else {
            // No tiene ID, crear uno nuevo
            createNewBirthday(birthday, listener);
        }
    }

    private void createNewBirthday(Birthday birthday, OnBirthdayAddedListener listener) {
        db.collection("birthdays")
                .add(birthday)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String birthdayId = documentReference.getId();
                        Log.d(TAG, "Recordatorio agregado con ID: " + birthdayId);

                        // Actualizar el ID del cumpleaños en Firestore
                        documentReference.update("id", birthdayId)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "ID del recordatorio actualizado en Firestore");
                                        listener.onSuccess(birthdayId);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Error al actualizar ID del recordatorio: " + e.getMessage());
                                        listener.onFailure(e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al agregar recordatorio: " + e.getMessage());
                        listener.onFailure(e);
                    }
                });
    }

    public void updateBirthday(Birthday birthday, OnBirthdayUpdatedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario logueado");
            listener.onFailure(new Exception("No user is signed in"));
            return;
        }

        Log.d(TAG, "Actualizando recordatorio: " + birthday.getName() + " (ID: " + birthday.getId() + ")");

        // Asegurarse de que el cumpleaños tenga el ID del usuario actual
        birthday.setUserId(currentUser.getUid());

        db.collection("birthdays").document(birthday.getId())
                .set(birthday)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Recordatorio actualizado correctamente");
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al actualizar recordatorio: " + e.getMessage());
                        listener.onFailure(e);
                    }
                });
    }

    public void deleteBirthday(String birthdayId, OnBirthdayDeletedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario logueado");
            listener.onFailure(new Exception("No user is signed in"));
            return;
        }

        Log.d(TAG, "Eliminando recordatorio con ID: " + birthdayId);

        db.collection("birthdays").document(birthdayId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Recordatorio eliminado correctamente");
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al eliminar recordatorio: " + e.getMessage());
                        listener.onFailure(e);
                    }
                });
    }

    public void getBirthdays(OnBirthdaysLoadedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario logueado");
            listener.onFailure(new Exception("No user is signed in"));
            return;
        }

        Log.d(TAG, "Obteniendo recordatorios para el usuario: " + currentUser.getEmail());
        
        // Usar la fuente de datos adecuada (caché o servidor)
        Source source = getDataSource();
        if (source == Source.CACHE) {
            Log.d(TAG, "Usando caché para obtener recordatorios");
        }

        db.collection("birthdays")
                .whereEqualTo("userId", currentUser.getUid())
                .get(source)
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Birthday> birthdays = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Birthday birthday = document.toObject(Birthday.class);
                            // Asegurarse de que el ID esté establecido correctamente
                            if (birthday.getId() == null || birthday.getId().isEmpty()) {
                                birthday.setId(document.getId());
                            }
                            // Asegurar que el nombre del mes de adquisición esté establecido
                            birthday.setMesAdquisicionNombre();
                            birthdays.add(birthday);
                        }
                        Log.d(TAG, "Se encontraron " + birthdays.size() + " recordatorios");
                        listener.onSuccess(birthdays);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String mensaje = "Error al obtener recordatorios: " + e.getMessage();
                        
                        // Agregar más detalles para depuración
                        if (e instanceof FirebaseFirestoreException) {
                            FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
                            mensaje += " - Código: " + firestoreException.getCode();
                        }
                        
                        Log.e(TAG, mensaje);
                        
                        // Si hay error y estamos en línea, intentar desde la caché
                        if (source == Source.DEFAULT && isConnectedToInternet()) {
                            Log.d(TAG, "Reintentando desde caché después de error");
                            db.collection("birthdays")
                                    .whereEqualTo("userId", currentUser.getUid())
                                    .get(Source.CACHE)
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            List<Birthday> birthdays = new ArrayList<>();
                                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                Birthday birthday = document.toObject(Birthday.class);
                                                if (birthday.getId() == null || birthday.getId().isEmpty()) {
                                                    birthday.setId(document.getId());
                                                }
                                                birthday.setMesAdquisicionNombre();
                                                birthdays.add(birthday);
                                            }
                                            Log.d(TAG, "Se recuperaron " + birthdays.size() + " recordatorios de la caché");
                                            listener.onSuccess(birthdays);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception cacheException) {
                                            Log.e(TAG, "Error al recuperar de caché: " + cacheException.getMessage());
                                            listener.onFailure(e); // Devolver el error original
                                        }
                                    });
                        } else {
                            listener.onFailure(e);
                        }
                    }
                });
    }

    public void getBirthdayById(String birthdayId, OnBirthdayLoadedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario logueado");
            listener.onFailure(new Exception("No user is signed in"));
            return;
        }

        Log.d(TAG, "Obteniendo recordatorio con ID: " + birthdayId);

        db.collection("birthdays").document(birthdayId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Birthday birthday = documentSnapshot.toObject(Birthday.class);
                            // Asegurarse de que el ID esté establecido correctamente
                            if (birthday.getId() == null || birthday.getId().isEmpty()) {
                                birthday.setId(documentSnapshot.getId());
                            }
                            Log.d(TAG, "Recordatorio obtenido correctamente: " + birthday.getName());
                            listener.onSuccess(birthday);
                        } else {
                            Log.e(TAG, "No se encontró el recordatorio");
                            listener.onFailure(new Exception("Birthday not found"));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al obtener recordatorio: " + e.getMessage());
                        listener.onFailure(e);
                    }
                });
    }
}
