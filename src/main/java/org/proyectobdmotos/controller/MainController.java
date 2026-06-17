package org.proyectobdmotos.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class MainController {
    private final ScreenLoader screenLoader;
    private String fxmlActual;
    private final Stack<String> historial;
    private static MainController instance;

    @FXML private StackPane contentContainer;
    @FXML private Label labelFecha;

    public MainController(ScreenLoader screenLoader) {
        this.screenLoader = screenLoader;
        this.historial = new Stack<>();
        instance = this;
    }

    public static MainController getInstance() {
        return instance;
    }

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    @FXML
    private void initialize() {
        Logger.log("Inicializando MainController...");
        contentContainer.setMaxWidth(Double.MAX_VALUE);
        contentContainer.setMaxHeight(Double.MAX_VALUE);
        labelFecha.setText("Hoy " + LocalDate.now().format(
                DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es"))));
        showInitialView();
        setupKeyboardShortcut();
    }

    // -----------------------------------------------------------------
    // Acciones de navegación (botones de la barra lateral)
    // -----------------------------------------------------------------

    @FXML
    private void onShowClientes() {
        loadView("/fxml/cliente-lista.fxml", "Clientes");
    }

    @FXML
    private void onShowMotos() {
        loadView("/fxml/moto-lista.fxml", "Motos");
    }

    @FXML
    private void onShowContratos() {
        loadView("/fxml/contrato-lista.fxml", "Contratos");
    }

    @FXML
    private void onShowInventario() {
        loadView("/fxml/inventario.fxml", "Inventario");
    }

    @FXML
    private void onShowReportes() {
        loadView("/fxml/reportes.fxml", "Reportes");
    }

    @FXML
    private void onShowNuevoContrato() {
        loadView("/fxml/contrato-formulario.fxml", "Nuevo Contrato");
    }

    @FXML
    private void onShowMarcasModelos() {
        loadView("/fxml/marcas-modelos.fxml", "Marcas y Modelos");
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

    public void cargarVista(String fxmlPath, String nombreVista) {
        loadView(fxmlPath, nombreVista);
    }

    private void loadView(String fxmlPath, String viewName) {
        boolean mismaVista = fxmlPath.equals(fxmlActual);
        boolean debeCargar = true;

        if (!mismaVista) {
            actualizarMusica(fxmlActual, fxmlPath);
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
                Throwable causa = e.getCause();
                Logger.logError("Error cargando vista " + viewName + ": " +
                        (causa != null ? causa.toString() : e.getMessage()));
                e.printStackTrace();
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
        // No se guarda historial
        boolean mismaVista = fxmlPath.equals(fxmlActual);
        if (mismaVista) {
            return;
        }

        actualizarMusica(fxmlActual, fxmlPath);

        // Cargar la vista sin modificar la pila de historial
        boolean cargaExitosa = false;
        Parent viewRoot = null;
        try {
            viewRoot = screenLoader.load(fxmlPath);
            cargaExitosa = true;
        } catch (IOException e) {
            Throwable causa = e.getCause();
            Logger.logError("Error cargando vista " + viewName + ": " +
                    (causa != null ? causa.toString() : e.getMessage()));
            e.printStackTrace();
            showLoadError(viewName, e);
        }

        if (cargaExitosa) {
            fxmlActual = fxmlPath;
            configurarEscalado(viewRoot);
            contentContainer.getChildren().setAll(viewRoot);
            Logger.logInfo("Vista activa: " + viewName);
        }
    }

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
            cargarVista("/fxml/bienvenido-admin.fxml", "Bienvenida Admin");
        });
        cargarVista("/fxml/creditos-finales.fxml", "Créditos");
    }

    // -----------------------------------------------------------------
    // Métodos privados de navegación
    // -----------------------------------------------------------------

    private void showInitialView() {
        loadView("/fxml/bienvenido-admin.fxml", "Bienvenida");
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