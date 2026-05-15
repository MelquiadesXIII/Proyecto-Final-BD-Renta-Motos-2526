package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.proyectobdmotos.models.Usuario;
import org.proyectobdmotos.services.UsuarioService;
import org.proyectobdmotos.ui.navigation.ScreenLoader;
import org.proyectobdmotos.utils.Logger;

import java.io.IOException;

public class LoginController {

    @FXML private TextField campoUsuario;
    @FXML private PasswordField campoContrasena;

    private final ScreenLoader screenLoader;
    private final UsuarioService usuarioService;

    public LoginController(ScreenLoader screenLoader, UsuarioService usuarioService) {
        this.screenLoader = screenLoader;
        this.usuarioService = usuarioService;
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

        try {
            Usuario user = usuarioService.autenticar(usuario, pass);
            Logger.logInfo("Login exitoso: " + user.getNombreUsuario());

            if (user.isEsAdmin()) {
                irAPantallaPrincipal();
            } else {
                irAPerfil(user);
            }
        } catch (Exception e) {
            mostrarError("Error de autenticación", e.getMessage());
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

    private void irAPerfil(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/perfil.fxml"));
            Parent perfilRoot = loader.load();
            PerfilController perfilController = loader.getController();
            perfilController.setUsuario(usuario);

            Scene scene = new Scene(perfilRoot, 600, 400);
            Stage stage = (Stage) campoUsuario.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Perfil de " + usuario.getNombreUsuario());
            stage.setMaximized(false);
            Logger.logInfo("Login cliente exitoso: " + usuario.getNombreUsuario());
        } catch (IOException e) {
            Logger.logError("Error al cargar perfil.fxml: " + e.getMessage());
            mostrarError("Error", "No se pudo abrir el perfil.");
        }
    }

    @FXML
    private void goToRegister() {
        try {
            Parent registerRoot = screenLoader.load("/fxml/registro.fxml");
            Scene scene = new Scene(registerRoot);
            scene.getStylesheets().add(getClass().getResource("/styles/register.css").toExternalForm());
            Stage stage = (Stage) campoUsuario.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Crear cuenta");
        } catch (IOException e) {
            Logger.logError("Error al cargar registro: " + e.getMessage());
            mostrarError("Error", "No se pudo abrir el registro.");
        }
    }

    @FXML
    private void goToTerms() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Términos y Condiciones");
        alert.setHeaderText("Condiciones de uso del sistema de renta de motos");
        alert.setContentText("Aquí el texto completo de los términos y condiciones...");
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}