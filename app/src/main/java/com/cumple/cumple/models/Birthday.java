package com.cumple.cumple.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Birthday {
    private String id;
    private String userId;
    private String name;
    private String phone;
    private String ubicacion;
    private String date; // Formato: dd/MM/yyyy
    private boolean notificationEnabled;
    private int daysLeft;

    // Campos requeridos por la empresa
    private int area; // Cambiado a int para aceptar solo números
    private int mesAdquisicion; // Cambiado a int para representar el mes (1-12)

    // Variables para Firestore
    private int day;
    private int month;

    // Nuevo campo para Firestore
    public String mesAdquisicionNombre;

    // Constructor vacío requerido para Firestore
    public Birthday() {
    }

    // Constructor de la clase Birthday (verifica si el tipo de 'ubicacion' es int o String)
    public Birthday(String id, String userId, String name, String phone, String ubicacion, String date, int area, int mesAdquisicion, boolean notificationEnabled) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.ubicacion = ubicacion;
        this.date = date;
        this.area = area;
        this.mesAdquisicion = mesAdquisicion;
        this.notificationEnabled = notificationEnabled;
        calculateDaysLeft();
        extractDayAndMonth();
        setMesAdquisicionNombre(); // Asegurar que se establezca el valor inicial
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
        calculateDaysLeft();
        extractDayAndMonth();
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getMesAdquisicion() {
        return mesAdquisicion;
    }

    public void setMesAdquisicion(int mesAdquisicion) {
        this.mesAdquisicion = mesAdquisicion;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public int getDaysLeft() {
        return daysLeft;
    }

    public void setDaysLeft(int daysLeft) {
        this.daysLeft = daysLeft;
    }

    // Getters y setters para Firestore
    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    // Getter para Firestore
    public String getMesAdquisicionNombre() {
        return mesAdquisicionNombre;
    }

    // Setter para Firestore
    public void setMesAdquisicionNombre() {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        if (mesAdquisicion >= 1 && mesAdquisicion <= 12) {
            this.mesAdquisicionNombre = meses[mesAdquisicion - 1];
        } else {
            this.mesAdquisicionNombre = "";
        }
    }

    // Extraer día y mes para Firestore
    private void extractDayAndMonth() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date birthdayDate = dateFormat.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(birthdayDate);
            this.day = calendar.get(Calendar.DAY_OF_MONTH);
            this.month = calendar.get(Calendar.MONTH) + 1; // Calendar usa 0-11 para meses
        } catch (ParseException e) {
            e.printStackTrace();
            this.day = 1;
            this.month = 1;
        }
    }

    // Calcular días restantes hasta el próximo cumpleaños
    public void calculateDaysLeft() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date birthdayDate = dateFormat.parse(date);

            Calendar birthdayCal = Calendar.getInstance();
            birthdayCal.setTime(birthdayDate);

            Calendar today = Calendar.getInstance();

            Calendar nextBirthday = Calendar.getInstance();
            nextBirthday.set(Calendar.DAY_OF_MONTH, birthdayCal.get(Calendar.DAY_OF_MONTH));
            nextBirthday.set(Calendar.MONTH, birthdayCal.get(Calendar.MONTH));

            if (nextBirthday.before(today)) {
                nextBirthday.set(Calendar.YEAR, today.get(Calendar.YEAR) + 1);
            } else {
                nextBirthday.set(Calendar.YEAR, today.get(Calendar.YEAR));
            }

            long diffMillis = nextBirthday.getTimeInMillis() - today.getTimeInMillis();
            this.daysLeft = (int) (diffMillis / (24 * 60 * 60 * 1000));
        } catch (ParseException e) {
            e.printStackTrace();
            this.daysLeft = 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Birthday birthday = (Birthday) obj;
        return id != null && id.equals(birthday.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}