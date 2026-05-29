package org.proyectobdmotos.controller;

import java.io.IOException;
import java.util.Stack;

import org.proyectobdmotos.ui.navigation.ScreenLoader;
import org.proyectobdmotos.utils.Logger;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

/**
 * MainController: controla la navegación principal del shell UI.
 * Solo maneja eventos de UI y delega la carga de vistas al ScreenLoader.
 */
public class MainController {

    private final ScreenLoader screenLoader;
    // Historial de paginas a las que entro el usuario, ademas de
    // en la que se encuentra ahora mismo.
    private String fxmlActual;
    private final Stack<String> historial;

    @FXML
    private StackPane contentContainer;

    public MainController(ScreenLoader screenLoader) {
        this.screenLoader = screenLoader;
        this.historial = new Stack<>();
    }

    @FXML
    private void initialize() {
        Logger.log("Inicializando MainController...");
        showInitialView();
    }

    @FXML
    private void onShowClientes() {
        loadView("/fxml/cliente-lista.fxml", "Clientes");
    }

    @FXML
    private void onShowNuevoContrato() {
        loadView("/fxml/nuevo-contrato.fxml", "Nuevo Contrato");
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
    private void onShowContratos() {
        loadView("/fxml/contrato-lista.fxml", "Contratos");
    }

    @FXML
    private void onGoBack() {
        if (historial.isEmpty()) {
            Logger.logInfo("Historial vacío, no se puede retroceder");
            return;
        }
        String fxmlAnterior = historial.pop();
        try {
            Parent vista = screenLoader.load(fxmlAnterior);
            fxmlActual = fxmlAnterior;
            contentContainer.getChildren().setAll(vista);
            Logger.logInfo("Retrocediendo a: " + fxmlAnterior);
        } catch (IOException e) {
            Logger.logError("Error al retroceder: " + e.getMessage());
            showLoadError("Retroceder", e);
        }
    }

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
            showLoadError(viewName, e);
        }

        if (loadedSuccessfully) {
            fxmlActual = fxmlPath;
            contentContainer.getChildren().setAll(viewRoot);
            Logger.logInfo("Vista activa: " + viewName);
        }
    }

    @FXML
    private void onShowMotos() {
        loadView("/fxml/moto-lista.fxml", "Motos");
    }

    private void showLoadError(String viewName, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de navegación");
        alert.setHeaderText("No se pudo abrir la vista de " + viewName);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
}