package org.proyectobdmotos.controller;

import java.io.IOException;
import java.util.Stack;

import org.proyectobdmotos.ui.navigation.ScreenLoader;
import org.proyectobdmotos.utils.Logger;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class MainController {
    private final ScreenLoader screenLoader;
    private String fxmlActual;
    private final Stack<String> historial;

    private static MainController instance;

    @FXML
    private StackPane contentContainer;

    public MainController(ScreenLoader screenLoader) {
        this.screenLoader = screenLoader;
        this.historial = new Stack<>();
        instance = this;
    }

    public ScreenLoader getScreenLoader() {
        return screenLoader;
    }

    public String getFxmlActual() {
        return fxmlActual;
    }

    public Stack<String> getHistorial() {
        return historial;
    }

    public StackPane getContentContainer() {
        return contentContainer;
    }

    public static MainController getInstance() {
        return instance;
    }

    @FXML
    private void initialize() {
        Logger.log("Inicializando MainController...");
        showInitialView();
        setupKeyboardShortcut();
    }

    // ==================== NAVEGACIÓN ====================

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
    private void onShowAyuda() {
        loadView("/fxml/ayuda.fxml", "Ayuda");
    }

    @FXML
    private void onShowNuevoContrato() {
        loadView("/fxml/contrato-formulario.fxml", "Nuevo Contrato");
    }

    public void onGoBack() {
        if (historial.isEmpty()) {
            Logger.logInfo("Historial vacío, no se puede retroceder");
            return;
        }
        String fxmlAnterior = historial.pop();
        try {
            Parent vista = screenLoader.load(fxmlAnterior);
            configurarEscalado(vista);
            fxmlActual = fxmlAnterior;
            contentContainer.getChildren().setAll(vista);
            Logger.logInfo("Retrocediendo a: " + fxmlAnterior);
        } catch (IOException e) {
            Logger.logError("Error al retroceder: " + e.getMessage());
            showLoadError("Retroceder", e);
        }
    }

    /**
     * Permite que otros controladores carguen una vista en el panel central.
     */
    public void cargarVista(String fxmlPath, String nombreVista) {
        loadView(fxmlPath, nombreVista);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private void showInitialView() {
        historial.clear();
        loadView("/fxml/bienvenido-admin.fxml", "Bienvenida");
    }

    private void loadView(String fxmlPath, String viewName) {
        if (fxmlActual != null) {
            historial.push(fxmlActual);
        }

        boolean loadedSuccessfully = false;
        Parent viewRoot = null;

        try {
            viewRoot = screenLoader.load(fxmlPath);
            loadedSuccessfully = true;
        } catch (IOException e) {
            Logger.logError("Error cargando vista " + viewName + ": " + e.getMessage());
            e.printStackTrace();
            showLoadError(viewName, e);
        }

        if (loadedSuccessfully) {
            fxmlActual = fxmlPath;
            configurarEscalado(viewRoot);
            contentContainer.getChildren().setAll(viewRoot);
            Logger.logInfo("Vista activa: " + viewName);
        }
    }

    private void configurarEscalado(Parent root) {
        if (root instanceof Region) {
            Region region = (Region) root;
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMaxHeight(Double.MAX_VALUE);
        }
    }

    private void showLoadError(String viewName, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de navegación");
        alert.setHeaderText("No se pudo abrir la vista de " + viewName);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }

    // ==================== ATAJO DE TECLADO ====================

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
}