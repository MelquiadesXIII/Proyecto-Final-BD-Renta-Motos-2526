package org.proyectobdmotos.controller;

import java.io.IOException;
import java.util.Stack;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.proyectobdmotos.ui.navigation.ScreenLoader;
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

    public static UserMainController getInstance() {
        return instance;
    }

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

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

    @FXML
    private void onShowPerfil() {
        loadView("/fxml/perfil.fxml", "Perfil");
    }

    @FXML
    private void onShowMisContratos() {
        loadView("/fxml/mis-contratos.fxml", "Mis Contratos");
    }

    // -----------------------------------------------------------------
    // Navegación hacia atrás y cierre de sesión
    // -----------------------------------------------------------------

    public void onGoBack() {
        if (historial.isEmpty()) {
            cerrarSesion();
        } else {
            String fxmlAnterior = historial.pop();
            loadViewSinHistorial(fxmlAnterior, "Atrás");
        }
    }

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

    @FXML
    private void onCerrarSesion() {
        cerrarSesion();
    }

    // -----------------------------------------------------------------
    // Carga de vistas
    // -----------------------------------------------------------------

    /**
     * Método público que simplemente llama al método privado loadView.
     */
    public void cargarVista(String fxmlPath, String nombreVista) {
        loadView(fxmlPath, nombreVista);
    }

    /**
     * Carga una vista en el contenedor central.
     * Si la vista ya es la actual, no hace nada.
     * Cambia la música automáticamente al entrar o salir de créditos.
     */
    private void loadView(String fxmlPath, String viewName) {
        boolean mismaVista = fxmlPath.equals(fxmlActual);
        boolean debeCargar = true;

        if (!mismaVista) {
            // Cambiar música según la nueva vista
            actualizarMusica(fxmlActual, fxmlPath);

            // Guardar en historial la vista actual
            if (fxmlActual != null) {
                historial.push(fxmlActual);
            }
        } else {
            debeCargar = false;
        }

        if (debeCargar) {
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
    }

    /**
     * Igual que loadView, pero sin guardar la vista actual en el historial.
     * Solo se usa al navegar hacia atrás.
     */
    private void loadViewSinHistorial(String fxmlPath, String viewName) {
        boolean mismaVista = fxmlPath.equals(fxmlActual);
        if (!mismaVista) {
            actualizarMusica(fxmlActual, fxmlPath);
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
    }

    /**
     * Cambia la canción del reproductor si se entra o sale de los créditos.
     */
    private void actualizarMusica(String origen, String destino) {
        Reproductor r = Reproductor.getInstancia();
        boolean entrandoACreditos = destino.equals("/fxml/creditos-finales.fxml");
        boolean saliendoDeCreditos = origen != null && origen.equals("/fxml/creditos-finales.fxml");

        if (entrandoACreditos && !saliendoDeCreditos) {
            r.cambiarMusicaIndice(0);
        } else if (!entrandoACreditos && saliendoDeCreditos) {
            r.cambiarMusicaIndice(1);
        }
    }

    // -----------------------------------------------------------------
    // Ayuda (Créditos)
    // -----------------------------------------------------------------

    @FXML
    private void onShowAyuda() {
        CreditosFinalesController.setOnFinCallback(() -> {
            cargarVista("/fxml/bienvenido-usuario.fxml", "Bienvenida");
        });
        cargarVista("/fxml/creditos-finales.fxml", "Créditos");
    }

    // -----------------------------------------------------------------
    // Métodos privados de navegación
    // -----------------------------------------------------------------

    private void showInitialView() {
        loadView("/fxml/bienvenido-usuario.fxml", "Bienvenida");
    }

    private void configurarEscalado(Parent root) {
        if (root instanceof Region) {
            Region region = (Region) root;
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMaxHeight(Double.MAX_VALUE);
        }
    }

    private void showLoadError(String viewName, Exception exception) {
        AlertUtils.mostrarErrorCabecera(viewName, exception);
    }

    // -----------------------------------------------------------------
    // Atajo de teclado
    // -----------------------------------------------------------------

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
}