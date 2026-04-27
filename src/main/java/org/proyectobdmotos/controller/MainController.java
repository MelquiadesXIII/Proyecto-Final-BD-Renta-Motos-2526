package org.proyectobdmotos.controller;

import java.io.IOException;

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

    @FXML
    private StackPane contentContainer;

    public MainController(ScreenLoader screenLoader) {
        this.screenLoader = screenLoader;
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

    private void showInitialView() {
        loadView("/fxml/nuevo-contrato.fxml", "Nuevo Contrato");
    }

    private void loadView(String fxmlPath, String viewName) {
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
            contentContainer.getChildren().setAll(viewRoot);
            Logger.logInfo("Vista activa: " + viewName);
        }
    }

    private void showLoadError(String viewName, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de navegación");
        alert.setHeaderText("No se pudo abrir la vista de " + viewName);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
}
