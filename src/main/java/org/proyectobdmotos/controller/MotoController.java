package org.proyectobdmotos.controller;

import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.models.Situacion;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.services.exceptions.BusinessException;
import org.proyectobdmotos.services.exceptions.ValidationException;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.utils.*;
import org.proyectobdmotos.utils.Logger;

public class MotoController {

    private final MotoService motoService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;

    @FXML private TableView<Moto> tablaMotos;
    @FXML private TableColumn<Moto, String> colMatricula;
    @FXML private TableColumn<Moto, String> colMarca;
    @FXML private TableColumn<Moto, String> colModelo;
    @FXML private TableColumn<Moto, String> colColor;
    @FXML private TableColumn<Moto, Double> colKilometros;
    @FXML private TableColumn<Moto, String> colSituacion;
    @FXML private StackPane rootPane;

    public MotoController(MotoService motoService, AgenciaStore agenciaStore, ReferenceDataStore referenceDataStore) {
        this.motoService = motoService;
        this.agenciaStore = agenciaStore;
        this.referenceDataStore = referenceDataStore;
    }

    @FXML
    private void initialize() {
        if (rootPane != null) {
            rootPane.setStyle(
                    "-fx-background-image: url('"
                            + getClass().getResource("/Utiles/fondoTablas.png").toExternalForm()
                            + "');"
                            + "-fx-background-size: cover;"
                            + "-fx-background-position: center center;"
                            + "-fx-background-repeat: no-repeat;"
            );
        }
        Logger.log("Inicializando MotoController...");
        configureTableColumns();
        bindStore();
        loadMotos();
        fijarColumnas(tablaMotos);
    }

    private void configureTableColumns() {
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matriculaMoto"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("nombreMarca"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("nombreModelo"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("nombreColor"));
        colKilometros.setCellValueFactory(new PropertyValueFactory<>("cantKmRecorridos"));
        colSituacion.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSituacion().getValor()));
    }

    private void bindStore() {
        tablaMotos.setItems(agenciaStore.getMotos());
    }

    private void loadMotos() {
        Task<List<Moto>> loadTask = crearTareaCargaMotos();
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private Task<List<Moto>> crearTareaCargaMotos() {
        Task<List<Moto>> loadTask = new Task<>() {
            @Override
            protected List<Moto> call() {
                return motoService.listarTodos();
            }
        };
        loadTask.setOnSucceeded(event -> manejarCargaExitosa(loadTask.getValue()));
        loadTask.setOnFailed(event -> manejarCargaFallida(loadTask.getException()));
        return loadTask;
    }

    private void manejarCargaExitosa(List<Moto> motos) {
        if (motos != null) {
            agenciaStore.setMotos(motos);
            Logger.logInfo("Motos cargadas: " + motos.size());
        }
    }

    private void manejarCargaFallida(Throwable throwable) {
        String message = throwable != null ? throwable.getMessage() : "Sin detalle";
        boolean isBusinessError = throwable instanceof BusinessException;
        if (isBusinessError) {
            Logger.logError("Error de negocio cargando motos: " + message);
            showError("No se pudieron cargar las motos", message);
        } else {
            Logger.logError("Error inesperado cargando motos: " + message);
            showError("Error cargando motos", message);
        }
    }

    @FXML
    private void onCrearMoto() {
        MotoFormController.setMotoAEditarStatic(null);
        MainController.getInstance().cargarVista("/fxml/moto-form-view.fxml", "Nueva Moto");
    }

    @FXML
    private void onEditarMoto() {
        Moto motoSeleccionada = tablaMotos.getSelectionModel().getSelectedItem();
        if (motoSeleccionada == null) {
            mostrarAlerta("Seleccione una moto de la tabla para editar.");
        } else {
            MotoFormController.setMotoAEditarStatic(motoSeleccionada);
            MainController.getInstance().cargarVista("/fxml/moto-form-view.fxml", "Editar Moto");
        }
    }

    @FXML
    private void onEliminarMoto() {
        Moto motoSeleccionada = tablaMotos.getSelectionModel().getSelectedItem();
        if (motoSeleccionada == null) {
            mostrarAlerta("Seleccione una moto de la tabla para eliminar.");
        } else {
            boolean confirmado = confirmarEliminacion(motoSeleccionada);
            if (confirmado) {
                ejecutarEliminacion(motoSeleccionada);
            }
        }
    }

    private boolean confirmarEliminacion(Moto moto) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar la moto con matrícula " + moto.getMatriculaMoto() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

    private void ejecutarEliminacion(Moto moto) {
        try {
            motoService.eliminarMoto(moto.getMatriculaMoto());
            loadMotos();
            mostrarAlerta("Moto eliminada correctamente.");
        } catch (ValidationException e) {
            e.printStackTrace();
            mostrarAlerta("Error al eliminar: " + e.getMessage());
        }
    }

    @FXML
    private void onCambiarEstado() {
        Moto motoSeleccionada = tablaMotos.getSelectionModel().getSelectedItem();
        if (motoSeleccionada == null) {
            mostrarAlerta("Seleccione una moto de la tabla.");
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Disponible", "Disponible", "Taller");
        dialog.setTitle("Cambiar estado");
        dialog.setHeaderText("Moto: " + motoSeleccionada.getMatriculaMoto());
        dialog.setContentText("Seleccione el nuevo estado:");
        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            String estadoElegido = resultado.get();
            Situacion situacion = Situacion.fromValor(estadoElegido);
            try {
                motoService.cambiarEstado(motoSeleccionada.getMatriculaMoto(), situacion);
                loadMotos();
                mostrarAlerta("Estado cambiado a " + estadoElegido + ".");
            } catch (ValidationException e) {
                e.printStackTrace();
                mostrarAlerta("Error al cambiar estado: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onActualizarLista() {
        loadMotos();
    }

    private void showError(String headerText, String contentText) {
        AlertUtils.mostrarErrorConTitulo(headerText, contentText);
    }

    private void mostrarAlerta(String mensaje) {
        AlertUtils.mostrarInfo(mensaje);
    }

    private void fijarColumnas(TableView<?> tabla) {
        for (TableColumn<?, ?> columna : tabla.getColumns()) {
            columna.setReorderable(false);
        }
        tabla.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                Platform.runLater(() -> tabla.getColumns().forEach(c -> c.setResizable(false)));
            }
        });
    }
}