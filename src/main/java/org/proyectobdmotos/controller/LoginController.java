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
import org.proyectobdmotos.utils.*;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.utils.ScreenUtils;
import org.proyectobdmotos.utils.TermsWindow;
import org.proyectobdmotos.ui.vistas.CreditosFinales;
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
    private static boolean sonando = false;

    public LoginController(ScreenLoader screenLoader, UsuarioService usuarioService,
                           AgenciaStore agenciaStore, ClienteService clienteService) {
        this.screenLoader = screenLoader;
        this.usuarioService = usuarioService;
        this.agenciaStore = agenciaStore;
        this.clienteService = clienteService;
    }

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    /**
     * Configura la imagen de fondo para que se escale junto con el
     * contenedor padre al iniciar la pantalla de login.
     */
    @FXML
    public void initialize() {
        if (!sonando)
        {
            Reproductor r = Reproductor.getInstancia();
            r.setIndiceActual(1);
            r.activarMusica();
            sonando = true;
        }

        Logger.log("LoginController inicializado");
        if (fondoLogin != null) {
            StackPane parent = (StackPane) fondoLogin.getParent();
            fondoLogin.fitWidthProperty().bind(parent.widthProperty());
            fondoLogin.fitHeightProperty().bind(parent.heightProperty());
        }
    }

    // -----------------------------------------------------------------
    // Autenticación
    // -----------------------------------------------------------------

    /**
     * Lee las credenciales ingresadas, las valida contra el servicio
     * y redirige al panel correspondiente según el rol del usuario.
     * Si las credenciales son incorrectas, muestra un mensaje de error.
     */
    @FXML
    private void onIngresar() {
        String usuario = campoUsuario.getText().trim();
        String pass = campoContrasena.getText().trim();

        if (usuario.isEmpty() || pass.isEmpty()) {
            mostrarError("Campos vacíos", "Por favor, ingresa usuario y contraseña.");
        } else {
            autenticarUsuario(usuario, pass);
        }
    }

    /**
     * Consulta el servicio de usuarios con las credenciales proporcionadas.
     * Si la autenticación es exitosa, deriva al método adecuado según el rol.
     * Si falla, muestra un mensaje de error.
     */
    private void autenticarUsuario(String usuario, String pass) {
        try {
            Usuario user = usuarioService.autenticar(usuario, pass);
            Logger.logInfo("Login exitoso: " + user.getNombreUsuario());
            redirigirSegunRol(user);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error de autenticación", e.getMessage());
        }
    }

    /**
     * Decide la pantalla a la que debe ir el usuario según su rol.
     * Si es administrador, va al panel principal de administración.
     * Si es cliente, recupera sus datos y va al panel de usuario.
     */
    private void redirigirSegunRol(Usuario user) {
        if (user.isEsAdmin()) {
            irAPantallaPrincipal(user);
        } else {
            redirigirCliente(user);
        }
    }

    /**
     * Obtiene los datos del cliente asociado al usuario y los guarda
     * en el store, para luego abrir la interfaz de usuario.
     */
    private void redirigirCliente(Usuario user) {
        Optional<Cliente> clienteOpt = clienteService.buscarPorIdUsuario(user.getId());
        if (clienteOpt.isPresent()) {
            agenciaStore.setClienteActual(clienteOpt.get());
        }
        irAPantallaUsuario(user);
    }

    // -----------------------------------------------------------------
    // Navegación a pantallas principales
    // -----------------------------------------------------------------

    /**
     * Abre la pantalla principal del administrador (Main).
     * Guarda el usuario autenticado en el store y configura la escena.
     */
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

    /**
     * Abre la pantalla principal del usuario (UserMain).
     * Guarda el usuario autenticado en el store y configura la escena.
     */
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

    // -----------------------------------------------------------------
    // Navegación a otras pantallas
    // -----------------------------------------------------------------

    /**
     * Navega a la pantalla de registro de nuevos usuarios.
     */
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
            e.printStackTrace();
            Logger.logError("Error al cargar registro: " + e.getMessage());
            mostrarError("Error", "No se pudo abrir el registro.");
        }
    }

    /**
     * Abre la ventana con los términos y condiciones.
     */
    @FXML
    private void goToTerms() {
        TermsWindow.show((Stage) campoUsuario.getScene().getWindow());
    }

    // -----------------------------------------------------------------
    // Utilidades de alertas
    // -----------------------------------------------------------------

    /**
     * Muestra un diálogo de error con un título y un mensaje descriptivo.
     */
    @FXML
    private void goToCredits() {
        Stage stage = (Stage) campoUsuario.getScene().getWindow();
        CreditosFinales creditos = new CreditosFinales(() -> {
            try {
                Parent loginRoot = screenLoader.load("/fxml/login.fxml");
                Scene scene = new Scene(loginRoot, ScreenUtils.getWidth(), ScreenUtils.getHeight());
                scene.getStylesheets().add(getClass().getResource("/styles/login.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Iniciar Sesión");
                stage.setMaximized(true);
            } catch (IOException e) {
                Logger.logError("Error al volver a la pantalla de login: " + e.getMessage());
            }
        });
        Scene scene = new Scene(creditos);
        stage.setScene(scene);
        stage.setFullScreen(true);
    }
    
    private void mostrarError(String titulo, String mensaje) {
        AlertUtils.mostrarErrorTitulo(titulo, mensaje);
    }
}