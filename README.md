# RCDT - Recordatorio de Cumpleaños

RCDT (Recordatorio de Cumpleaños De Tus [Amigos/Familiares/Contactos]) es una aplicación Android diseñada para ayudarte a no olvidar nunca más los cumpleaños importantes. Gestiona fácilmente los cumpleaños, recibe recordatorios y mantén tu información de perfil actualizada, todo con una interfaz moderna y soporte para modo oscuro.

## ✨ Características Principales

*   **Gestión de Cumpleaños:** Añade, edita y elimina cumpleaños fácilmente, incluyendo nombre, fecha y notas opcionales.
*   **Lista de Próximos Cumpleaños:** Visualiza los próximos cumpleaños de forma clara y organizada.
*   **Notificaciones:** Recibe recordatorios para los cumpleaños que se aproximan un dia antes.
*   **Perfil de Usuario:**
    *   Visualiza tu nombre de usuario y correo electrónico.
    *   Cambia tu contraseña de forma segura.
    *   Cierra sesión.
*   **Integración con Firebase:**
    *   Autenticación de usuarios (registro, inicio de sesión, cambio de contraseña).
    *   Almacenamiento de datos de cumpleaños y perfiles de usuario (usando Firebase Firestore).
*   **Interfaz de Usuario Moderna:**
    *   Diseño basado en Material Components.
    *   Soporte completo para **Modo Oscuro** con elegantes acentos en amarillo brillante.
    *   Navegación intuitiva.
*   **Pantallas Implementadas:**
    *   Inicio de Sesión (Login)
    *   Registro (Register)
    *   Pantalla Principal (para listar cumpleaños - diseño base)
    *   Pantalla de Perfil (ProfileActivity) - Totalmente funcional y estilizada.

## 🛠️ Tecnologías Utilizadas

*   **Lenguaje:** Java
*   **Plataforma:** Android SDK Nativo
*   **Base de Datos y Autenticación:** Firebase (Authentication, Firestore/Realtime Database)
*   **Componentes de UI:** Material Components for Android
*   **Gestión de Dependencias:** Gradle

## 🚀 Configuración y Puesta en Marcha (Desarrollo)

1.  **Clonar el Repositorio:**
    ```bash
    git clone https://github.com/JhonatanLeo/RCDT.git
    cd RCDT
    ```
2.  **Abrir en Android Studio:** Importa el proyecto en la última versión estable de Android Studio.
3.  **Configurar Firebase:**
    *   Asegúrate de tener un proyecto Firebase creado.
    *   Descarga tu archivo `google-services.json` desde la consola de Firebase.
    *   Coloca el archivo `google-services.json` en el directorio `app/` de tu proyecto Android.
4.  **Construir y Ejecutar:** Android Studio debería sincronizar el proyecto con Gradle automáticamente. Luego, puedes construir y ejecutar la aplicación en un emulador o dispositivo físico.

## 📖 Cómo Usar la Aplicación

1.  **Registro/Inicio de Sesión:** Al abrir la aplicación por primera vez, regístrate con un correo y contraseña, o inicia sesión si ya tienes una cuenta.
2.  **Pantalla Principal:** (Diseño base) Aquí se listarán los cumpleaños que hayas añadido.
3.  **Añadir Cumpleaños:** Utiliza la opción para añadir nuevos cumpleaños, especificando nombre, fecha y notas adicionales como info de sus hogares recibidos.
4.  **Pantalla de Perfil:**
    *   Accede desde el menú.
    *   Visualiza tu información.
    *   Utiliza los campos provistos para cambiar tu contraseña actual por una nueva.
    *   Cierra tu sesión actual.

## 🎨 Personalización (Temas y Estilos)

La aplicación utiliza un sistema de temas (`themes.xml`) para definir la apariencia general, con soporte para modo claro y oscuro.
*   **Modo Oscuro:** Fondo principal negro (`#000000` o `#121212`), con texto primario blanco y acentos en amarillo brillante (`#FFE500`).
*   **Campos de Texto:** Estilizados para ser visibles en modo oscuro, con bordes gris claro en estado no enfocado y bordes amarillos cuando están enfocados. El texto de ayuda (hint) es gris claro y el texto ingresado es blanco. El icono para mostrar/ocultar contraseña es blanco.
*   **Tarjetas (Cards):** En modo oscuro, tienen fondo negro con bordes amarillos.

Este proyecto está listo para continuar su desarrollo y añadir más funcionalidades.
