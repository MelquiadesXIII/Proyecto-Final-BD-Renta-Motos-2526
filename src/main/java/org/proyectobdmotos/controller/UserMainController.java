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

    public static UserMainController getInstance() { return instance; }

    @FXML
    private void initialize() {
        Logger.log("Inicializando UserMainController...");
        contentContainer.setMaxWidth(Double.MAX_VALUE);
        contentContainer.setMaxHeight(Double.MAX_VALUE);
        showInitialView();
        setupKeyboardShortcut();
    }

    @FXML private void onShowPerfil() { loadView("/fxml/perfil.fxml", "Perfil"); }
    @FXML private void onShowMisContratos() { loadView("/fxml/mis-contratos.fxml", "Mis Contratos"); }
    @FXML private void onShowAyuda() { loadView("/fxml/ayuda.fxml", "Ayuda"); }

    public void onGoBack() {
        if (historial.isEmpty()) {
            Logger.logInfo("Historial vacío, no se puede retroceder");
        } else {
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
    }

    public void cargarVista(String fxmlPath, String nombreVista) { loadView(fxmlPath, nombreVista); }

    private void showInitialView() {
        historial.clear();
        loadView("/fxml/bienvenido-usuario.fxml", "Bienvenida");
    }

    private void loadView(String fxmlPath, String viewName) {
        if (fxmlActual != null) { historial.push(fxmlActual); }
        Parent viewRoot = null;
        try {
            viewRoot = screenLoader.load(fxmlPath);
        } catch (IOException e) {
            Logger.logError("Error cargando vista " + viewName + ": " + e.getMessage());
            showLoadError(viewName, e);
        }
        if (viewRoot != null) {
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