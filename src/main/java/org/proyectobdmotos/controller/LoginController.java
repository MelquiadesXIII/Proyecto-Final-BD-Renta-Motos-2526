package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.proyectobdmotos.ui.navigation.ScreenLoader;
import org.proyectobdmotos.utils.Logger;

public class LoginController {

    @FXML private TextField campoUsuario;
    @FXML private PasswordField campoContrasena;

    private final ScreenLoader screenLoader;

    // Constructor para inyectar ScreenLoader (imprescindible para cambiar de pantalla)
    public LoginController(ScreenLoader screenLoader) {
        this.screenLoader = screenLoader;
    }

    @FXML
    public void initialize() {
        Logger.log("LoginController inicializado");
    }

    @FXML
    private void onIngresar() {
        String usuario = campoUsuario.getText();
        String pass = campoContrasena.getText();
        Logger.log("Intento de login: " + usuario);
        // Aquí validas si es admin o cliente y cargas main.fxml o perfil.fxml
        // Ejemplo: screenLoader.load(...) y cambio de escena
    }

    @FXML
    private void goToRegister() {
        Logger.log("Ir a registro");
    }

    @FXML
    private void goToTerms() {
        Logger.log("Ir a términos");
    }
}
