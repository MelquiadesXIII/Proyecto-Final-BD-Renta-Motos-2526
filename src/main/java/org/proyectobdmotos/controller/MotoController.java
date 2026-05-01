package org.proyectobdmotos.controller;

import java.util.List;

import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.services.exceptions.BusinessException;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.utils.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * MotoController: maneja eventos de la UI de motos.
 * Delega operaciones a MotoService y actualiza/observa AgenciaStore.
 */
public class MotoController {

    private final MotoService motoService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;

    @FXML
    private TableView<Moto> motosTable;

    @FXML
    private TableColumn<Moto, String> matriculaColumn;

    @FXML
    private TableColumn<Moto, Integer> modeloColumn;

    @FXML
    private TableColumn<Moto, Integer> colorColumn;

    @FXML
    private TableColumn<Moto, String> situacionColumn;

    public MotoController(
        MotoService motoService,
        AgenciaStore agenciaStore,
        ReferenceDataStore referenceDataStore
    ) {
        this.motoService = motoService;
        this.agenciaStore = agenciaStore;
        this.referenceDataStore = referenceDataStore;
    }

    @FXML
    private void initialize() {
        Logger.log("Inicializando MotoController...");
        configureTableColumns();
        bindStore();
        loadMotos();
    }

    private void configureTableColumns() {
        matriculaColumn.setCellValueFactory(new PropertyValueFactory<>("matriculaMoto"));
        modeloColumn.setCellValueFactory(new PropertyValueFactory<>("idModelo"));
        colorColumn.setCellValueFactory(new PropertyValueFactory<>("idColor"));
        situacionColumn.setCellValueFactory(cellData -> {
            String situacionValue = "";
            boolean hasSituacion = cellData.getValue().getSituacion() != null;

            if (hasSituacion) {
                situacionValue = cellData.getValue().getSituacion().getValor();
            }

            return new SimpleStringProperty(situacionValue);
        });
    }

    private void bindStore() {
        motosTable.setItems(agenciaStore.getMotos());
    }

    private void loadMotos() {
        Task<List<Moto>> loadTask = createLoadMotosTask();
        configureLoadMotosTask(loadTask);

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private Task<List<Moto>> createLoadMotosTask() {
        return new Task<>() {
            @Override
            protected List<Moto> call() {
                return motoService.listarTodos();
            }
        };
    }

    private void configureLoadMotosTask(Task<List<Moto>> loadTask) {
        loadTask.setOnSucceeded(event -> {
            List<Moto> motos = loadTask.getValue();
            boolean loadedSuccessfully = motos != null;

            if (loadedSuccessfully) {
                agenciaStore.setMotos(motos);
                Logger.logInfo("Motos cargadas: " + motos.size());
            }
        });

        loadTask.setOnFailed(event -> {
            Throwable throwable = loadTask.getException();
            String message = throwable != null ? throwable.getMessage() : "Sin detalle";
            boolean isBusinessError = throwable instanceof BusinessException;

            if (isBusinessError) {
                Logger.logError("Error de negocio cargando motos: " + message);
                showError("No se pudieron cargar las motos", message);
            } else {
                Logger.logError("Error inesperado cargando motos: " + message);
                showError("Error cargando motos", message);
            }
        });
    }

    private void showError(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}
