package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.models.Usuario;
import org.proyectobdmotos.services.ClienteService;
import org.proyectobdmotos.services.UsuarioService;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.ui.navigation.NavigationHistory;
import org.proyectobdmotos.ui.navigation.ScreenLoader;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.utils.ScreenUtils;
import org.proyectobdmotos.utils.TermsWindow;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML private TextField campoUsuario;
    @FXML private PasswordField campoContrasena;
    @FXML private ImageView fondoLogin;

    private final ScreenLoader screenLoader;
    private final UsuarioService usuarioService;
    private final AgenciaStore agenciaStore;
    private final ClienteService clienteService;

    public LoginController(ScreenLoader screenLoader, UsuarioService usuarioService,
                           AgenciaStore agenciaStore, ClienteService clienteService) {
        this.screenLoader = screenLoader;
        this.usuarioService = usuarioService;
        this.agenciaStore = agenciaStore;
        this.clienteService = clienteService;
    }

    @FXML
    public void initialize() {
        Logger.log("LoginController inicializado");
        if (fondoLogin != null) {
            StackPane parent = (StackPane) fondoLogin.getParent();
            fondoLogin.fitWidthProperty().bind(parent.widthProperty());
            fondoLogin.fitHeightProperty().bind(parent.heightProperty());
        }
    }

    @FXML
    private void onIngresar() {
        String usuario = campoUsuario.getText().trim();
        String pass = campoContrasena.getText().trim();

        if (usuario.isEmpty() || pass.isEmpty()) {
            mostrarError("Campos vacíos", "Por favor, ingresa usuario y contraseña.");
        } else {
            try {
                Usuario user = usuarioService.autenticar(usuario, pass);
                Logger.logInfo("Login exitoso: " + user.getNombreUsuario());

                if (user.isEsAdmin()) {
                    irAPantallaPrincipal(user);
                } else {
                    Optional<Cliente> clienteOpt = clienteService.buscarPorIdUsuario(user.getId());
                    if (clienteOpt.isPresent()) {
                        agenciaStore.setClienteActual(clienteOpt.get());
                    }
                    irAPantallaUsuario(user);
                }
            } catch (Exception e) {
                e.printStackTrace();
                mostrarError("Error de autenticación", e.getMessage());
            }
        }
    }

    private void irAPantallaPrincipal(Usuario user) {
        try {
            agenciaStore.setUsuarioActual(user);
            Parent mainRoot = screenLoader.load("/fxml/main.fxml");
            Scene scene = new Scene(mainRoot, ScreenUtils.getWidth(), ScreenUtils.getHeight());
            scene.getStylesheets().addAll(
                    getClass().getResource("/styles/app.css").toExternalForm(),
                    getClass().getResource("/styles/default.css").toExternalForm());
            Stage stage = (Stage) campoUsuario.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Renta Motos - Sistema de Gestión");
            stage.setMaximized(true);
            Logger.logInfo("Login administrador exitoso");
        } catch (IOException e) {
            e.printStackTrace();
            Logger.logError("Error al cargar main.fxml: " + e.getMessage());
            mostrarError("Error", "No se pudo abrir la aplicación principal.");
        }
    }

    private void irAPantallaUsuario(Usuario usuario) {
        try {
            agenciaStore.setUsuarioActual(usuario);
            Parent userMainRoot = screenLoader.load("/fxml/user-main.fxml");
            Scene scene = new Scene(userMainRoot, ScreenUtils.getWidth(), ScreenUtils.getHeight());
            scene.getStylesheets().addAll(
                    getClass().getResource("/styles/app.css").toExternalForm(),
                    getClass().getResource("/styles/default.css").toExternalForm());
            Stage stage = (Stage) campoUsuario.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Renta Motos - " + usuario.getNombreUsuario());
            stage.setMaximized(true);
            Logger.logInfo("Login cliente exitoso: " + usuario.getNombreUsuario());
        } catch (IOException e) {
            e.printStackTrace();
            Logger.logError("Error al cargar user-main.fxml: " + e.getMessage());
            mostrarError("Error", "No se pudo abrir la interfaz de usuario.");
        }
    }

    @FXML
    private void goToRegister() {
        try {
            NavigationHistory.push("/fxml/login.fxml");
            Parent registerRoot = screenLoader.load("/fxml/registro.fxml");
            Scene scene = new Scene(registerRoot, ScreenUtils.getWidth(), ScreenUtils.getHeight());
            scene.getStylesheets().add(getClass().getResource("/styles/register.css").toExternalForm());
            Stage stage = (Stage) campoUsuario.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Crear cuenta");
            stage.setMaximized(true);
        } catch (IOException e) {
            Logger.logError("Error al cargar registro: " + e.getMessage());
            mostrarError("Error", "No se pudo abrir el registro.");
        }
    }

    @FXML
    private void goToTerms() {
        TermsWindow.show((Stage) campoUsuario.getScene().getWindow());
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}