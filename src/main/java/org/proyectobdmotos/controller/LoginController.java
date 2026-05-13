package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.proyectobdmotos.ui.navigation.ScreenLoader;
import org.proyectobdmotos.utils.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginController {

    @FXML private TextField campoUsuario;
    @FXML private PasswordField campoContrasena;

    private final ScreenLoader screenLoader;

    // --- USUARIOS DE PRUEBA ---
    private static final Map<String, String> USUARIOS_ADMIN = new HashMap<>();
    static {
        USUARIOS_ADMIN.put("Admin Lian", "admin123");
        USUARIOS_ADMIN.put("Admin DarelL", "admin123");
        USUARIOS_ADMIN.put("Admin Dario", "admin123");
    }

    private static final Map<String, String> USUARIOS_CLIENTE = new HashMap<>();
    static {
        USUARIOS_CLIENTE.put("Cliente1", "1234");
        USUARIOS_CLIENTE.put("Dario", "1234");
    }

    public LoginController(ScreenLoader screenLoader) {
        this.screenLoader = screenLoader;
    }

    @FXML
    public void initialize() {
        Logger.log("LoginController inicializado");
    }

    @FXML
    private void onIngresar() {
        String usuario = campoUsuario.getText().trim();
        String pass = campoContrasena.getText().trim();

        if (usuario.isEmpty() || pass.isEmpty()) {
            mostrarError("Campos vacíos", "Por favor, ingresa usuario y contraseña.");
            return;
        }

        Logger.log("Intento de login: " + usuario);

        // ¿Es administrador?
        if (USUARIOS_ADMIN.containsKey(usuario) && USUARIOS_ADMIN.get(usuario).equals(pass)) {
            irAPantallaPrincipal();
        }
        // ¿Es cliente?
        else if (USUARIOS_CLIENTE.containsKey(usuario) && USUARIOS_CLIENTE.get(usuario).equals(pass)) {
            irAPerfil(usuario);
        }
        // Credenciales incorrectas
        else {
            mostrarError("Credenciales inválidas", "Usuario o contraseña incorrectos.");
        }
    }

    private void irAPantallaPrincipal() {
        try {
            Parent mainRoot = screenLoader.load("/fxml/main.fxml");
            Scene scene = new Scene(mainRoot);
            scene.getStylesheets().addAll(
                getClass().getResource("/styles/app.css").toExternalForm(),
                getClass().getResource("/styles/default.css").toExternalForm()
            );
            Stage stage = (Stage) campoUsuario.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Renta Motos - Sistema de Gestión");
            stage.setMaximized(true);
            Logger.logInfo("Login administrador exitoso");
        } catch (IOException e) {
            Logger.logError("Error al cargar main.fxml: " + e.getMessage());
            mostrarError("Error", "No se pudo abrir la aplicación principal.");
        }
    }

    private void irAPerfil(String nombreUsuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/perfil.fxml"));
            Parent perfilRoot = loader.load();
            PerfilController perfilController = loader.getController();
            perfilController.setNombreUsuario(nombreUsuario);

            Scene scene = new Scene(perfilRoot, 600, 400);
            Stage stage = (Stage) campoUsuario.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Perfil de " + nombreUsuario);
            stage.setMaximized(false);
            Logger.logInfo("Login cliente exitoso: " + nombreUsuario);
        } catch (IOException e) {
            Logger.logError("Error al cargar perfil.fxml: " + e.getMessage());
            mostrarError("Error", "No se pudo abrir el perfil.");
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    private void goToRegister() {
        Logger.log("Ir a registro");
        // Aquí puedes cargar una pantalla de registro si la creas
    }

    @FXML
    private void goToTerms() {
        Logger.log("Ir a términos");
        // Aquí puedes mostrar términos y condiciones
    }
}