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

    // -----------------------------------------------------------------
    // Getters públicos
    // -----------------------------------------------------------------

    /**
     * @return el cargador de pantallas utilizado por este controlador.
     */
    public ScreenLoader getScreenLoader() {
        return screenLoader;
    }

    /**
     * @return la ruta FXML de la vista actualmente mostrada.
     */
    public String getFxmlActual() {
        return fxmlActual;
    }

    /**
     * @return la pila de historial de navegación.
     */
    public Stack<String> getHistorial() {
        return historial;
    }

    /**
     * @return el contenedor central donde se muestran las vistas.
     */
    public StackPane getContentContainer() {
        return contentContainer;
    }

    /**
     * @return la instancia única de MainController.
     */
    public static MainController getInstance() {
        return instance;
    }

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    /**
     * Prepara el contenedor central y muestra la vista inicial.
     * Configura el atajo de teclado para retroceder.
     */
    @FXML
    private void initialize() {
        Logger.log("Inicializando MainController...");
        contentContainer.setMaxWidth(Double.MAX_VALUE);
        contentContainer.setMaxHeight(Double.MAX_VALUE);
        showInitialView();
        setupKeyboardShortcut();
    }

    // -----------------------------------------------------------------
    // Acciones de navegación (botones de la barra lateral)
    // -----------------------------------------------------------------

    /** Abre la vista de lista de clientes. */
    @FXML
    private void onShowClientes() {
        loadView("/fxml/cliente-lista.fxml", "Clientes");
    }

    /** Abre la vista de lista de motos. */
    @FXML
    private void onShowMotos() {
        loadView("/fxml/moto-lista.fxml", "Motos");
    }

    /** Abre la vista de lista de contratos. */
    @FXML
    private void onShowContratos() {
        loadView("/fxml/contrato-lista.fxml", "Contratos");
    }

    /** Abre la vista de inventario. */
    @FXML
    private void onShowInventario() {
        loadView("/fxml/inventario.fxml", "Inventario");
    }

    /** Abre la vista de reportes. */
    @FXML
    private void onShowReportes() {
        loadView("/fxml/reportes.fxml", "Reportes");
    }

    /** Abre la vista de ayuda. */
    @FXML
    private void onShowAyuda() {
        loadView("/fxml/ayuda.fxml", "Ayuda");
    }

    /** Abre el formulario de nuevo contrato. */
    @FXML
    private void onShowNuevoContrato() {
        loadView("/fxml/contrato-formulario.fxml", "Nuevo Contrato");
    }

    /** Abre la vista de marcas y modelos. */
    @FXML
    private void onShowMarcasModelos() {
        loadView("/fxml/marcas-modelos.fxml", "Marcas y Modelos");
    }

    /**
     * Retrocede a la vista anterior. Si el historial está vacío,
     * simplemente registra un mensaje informativo.
     */
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
                e.printStackTrace();
                Logger.logError("Error al retroceder: " + e.getMessage());
                showLoadError("Retroceder", e);
            }
        }
    }

    /**
     * Carga una vista en el panel central desde otro controlador.
     * @param fxmlPath   ruta del archivo FXML.
     * @param nombreVista nombre descriptivo para el log.
     */
    public void cargarVista(String fxmlPath, String nombreVista) {
        loadView(fxmlPath, nombreVista);
    }

    // -----------------------------------------------------------------
    // Métodos privados de navegación
    // -----------------------------------------------------------------

    /**
     * Establece la vista inicial de la aplicación (bienvenida del admin).
     * Limpia el historial antes de cargar la nueva vista.
     */
    private void showInitialView() {
        historial.clear();
        loadView("/fxml/bienvenido-admin.fxml", "Bienvenida");
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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de navegación");
        alert.setHeaderText("No se pudo abrir la vista de " + viewName);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
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
}