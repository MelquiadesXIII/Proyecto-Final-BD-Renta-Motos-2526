package org.proyectobdmotos.controller;

import java.io.IOException;
import java.util.Stack;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.proyectobdmotos.ui.navigation.ScreenLoader;
import org.proyectobdmotos.ui.vistas.CreditosFinales;
import org.proyectobdmotos.utils.*;
import org.proyectobdmotos.utils.Logger;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class UserMainController {
    private final ScreenLoader screenLoader;
    private String fxmlActual;
    private final Stack<String> historial;
    private static UserMainController instance;

    @FXML private StackPane contentContainer;

    public UserMainController(ScreenLoader screenLoader) {
        this.screenLoader = screenLoader;
        this.historial = new Stack<>();
        instance = this;
    }

    /**
     * @return la instancia única de UserMainController.
     */
    public static UserMainController getInstance() {
        return instance;
    }

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    /**
     * Prepara el contenedor central, muestra la vista inicial
     * y configura el atajo de teclado para retroceder.
     */
    @FXML
    private void initialize() {
        Logger.log("Inicializando UserMainController...");
        contentContainer.setMaxWidth(Double.MAX_VALUE);
        contentContainer.setMaxHeight(Double.MAX_VALUE);
        showInitialView();
        setupKeyboardShortcut();
    }

    // -----------------------------------------------------------------
    // Acciones de navegación (botones de la barra lateral)
    // -----------------------------------------------------------------

    /** Abre la vista de perfil del usuario. */
    @FXML
    private void onShowPerfil() {
        loadView("/fxml/perfil.fxml", "Perfil");
    }

    /** Abre la vista de mis contratos. */
    @FXML
    private void onShowMisContratos() {
        loadView("/fxml/mis-contratos.fxml", "Mis Contratos");
    }


    /**
     * Retrocede a la vista anterior. Si el historial está vacío,
     * simplemente registra un mensaje informativo.
     */
    /**
     * Vuelve a la vista anterior. Si el historial está vacío, cierra la sesión
     * y carga la pantalla de login.
     */
    public void onGoBack() {
        if (historial.isEmpty()) {
            cerrarSesion();
        } else {
            String fxmlAnterior = historial.pop();
            try {
                Parent vista = screenLoader.load(fxmlAnterior);
                configurarEscalado(vista);
                fxmlActual = fxmlAnterior;
                contentContainer.getChildren().setAll(vista);
                Logger.logInfo("Retrocediendo a: " + fxmlAnterior);
            } catch (IOException e) {
                e.printStackTrace();
                Logger.logError("Error al retroceder: " + e.getMessage());
                showLoadError("Retroceder", e);
            }
        }
    }

    /**
     * Cierra la sesión actual y vuelve a la pantalla de login.
     * Carga el FXML del login en una nueva escena sobre la misma ventana.
     */
    private void cerrarSesion() {
        try {
            Parent loginRoot = screenLoader.load("/fxml/login.fxml");
            Scene scene = new Scene(loginRoot, ScreenUtils.getWidth(), ScreenUtils.getHeight());
            scene.getStylesheets().addAll(
                    getClass().getResource("/styles/login.css").toExternalForm());
            Stage stage = (Stage) contentContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Iniciar Sesión");
            stage.setMaximized(true);
            Logger.logInfo("Sesión cerrada, volviendo al login");
        } catch (IOException e) {
            e.printStackTrace();
            Logger.logError("Error al volver al login: " + e.getMessage());
            showLoadError("Login", e);
        }
    }

    /**
     * Botón explícito de cerrar sesión (lo enlazarás desde el FXML).
     */
    @FXML
    private void onCerrarSesion() {
        cerrarSesion();
    }

    /**
     * Carga una vista en el panel central desde otro controlador.
     * @param fxmlPath   ruta del archivo FXML.
     * @param nombreVista nombre descriptivo para el log.
     */
    public void cargarVista(String fxmlPath, String nombreVista) {
        Reproductor r = Reproductor.getInstancia();
        boolean esCreditos = fxmlPath.equals("/fxml/creditos-finales.fxml");
        boolean estabaEnCreditos = fxmlActual != null && fxmlActual.equals("/fxml/creditos-finales.fxml");


        if (esCreditos && !estabaEnCreditos) {
            r.cambiarMusicaIndice(0);
        }

        else if (!esCreditos && estabaEnCreditos) {
            r.cambiarMusicaIndice(1);
        }

        loadView(fxmlPath, nombreVista);
    }

    @FXML
    private void onShowAyuda() {
        CreditosFinalesController.setOnFinCallback(() -> {
            cargarVista("/fxml/bienvenido-admin.fxml", "Bienvenida Admin");
        });
        cargarVista("/fxml/creditos-finales.fxml", "Créditos");
    }

    // -----------------------------------------------------------------
    // Métodos privados de navegación
    // -----------------------------------------------------------------

    /**
     * Establece la vista inicial de la aplicación (bienvenida del usuario).
     * Limpia el historial antes de cargar la nueva vista.
     */
    private void showInitialView() {
        loadView("/fxml/bienvenido-usuario.fxml", "Bienvenida");
    }

    /**
     * Carga una vista en el contenedor central, gestionando el historial
     * y los errores de carga.
     */
    private void loadView(String fxmlPath, String viewName) {
        if (fxmlActual != null) {
            historial.push(fxmlActual);
        }

        boolean cargaExitosa = false;
        Parent viewRoot = null;

        try {
            viewRoot = screenLoader.load(fxmlPath);
            cargaExitosa = true;
        } catch (IOException e) {
            Logger.logError("Error cargando vista " + viewName + ": " + e.getMessage());
            showLoadError(viewName, e);
        }

        if (cargaExitosa) {
            fxmlActual = fxmlPath;
            configurarEscalado(viewRoot);
            contentContainer.getChildren().setAll(viewRoot);
            Logger.logInfo("Vista activa: " + viewName);
        }
    }

    /**
     * Configura el nodo raíz de una vista para que se expanda
     * al máximo del contenedor.
     */
    private void configurarEscalado(Parent root) {
        if (root instanceof Region) {
            Region region = (Region) root;
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMaxHeight(Double.MAX_VALUE);
        }
    }

    /**
     * Muestra un diálogo de error cuando una vista no puede cargarse.
     */
    private void showLoadError(String viewName, Exception exception) {
        AlertUtils.mostrarErrorCabecera(viewName, exception);
    }

    // -----------------------------------------------------------------
    // Atajo de teclado
    // -----------------------------------------------------------------

    /**
     * Configura el atajo de teclado Ctrl+Backspace para retroceder
     * a la vista anterior.
     */
    private void setupKeyboardShortcut() {
        contentContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.isControlDown() && event.getCode() == KeyCode.BACK_SPACE) {
                        onGoBack();
                        event.consume();
                    }
                });
            }
        });
    }
    @FXML
    private void onSalir() {
        Platform.exit();
    }


    /**
     * Muestra la animación de créditos finales y, al terminar,
     * regresa a la pantalla principal del usuario.
     */



}