package org.proyectobdmotos.controller;

import java.time.LocalDate;
import java.util.List;

import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.services.ContratoService;
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
 * ContratoController: maneja eventos de la UI de contratos.
 * Delega operaciones a ContratoService y actualiza/observa AgenciaStore.
 */
public class ContratoController {

    private final ContratoService contratoService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;

    @FXML
    private TableView<Contrato> contratosTable;

    @FXML
    private TableColumn<Contrato, Integer> idColumn;

    @FXML
    private TableColumn<Contrato, Integer> clienteIdColumn;

    @FXML
    private TableColumn<Contrato, Integer> motoIdColumn;

    @FXML
    private TableColumn<Contrato, LocalDate> inicioColumn;

    @FXML
    private TableColumn<Contrato, LocalDate> finColumn;

    @FXML
    private TableColumn<Contrato, String> estadoColumn;

    public ContratoController(
        ContratoService contratoService,
        AgenciaStore agenciaStore,
        ReferenceDataStore referenceDataStore
    ) {
        this.contratoService = contratoService;
        this.agenciaStore = agenciaStore;
        this.referenceDataStore = referenceDataStore;
    }

    @FXML
    private void initialize() {
        Logger.log("Inicializando ContratoController...");
        configureTableColumns();
        bindStore();
        loadContratos();
    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idContrato"));
        clienteIdColumn.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        motoIdColumn.setCellValueFactory(new PropertyValueFactory<>("idMoto"));
        inicioColumn.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        finColumn.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        estadoColumn.setCellValueFactory(cellData -> {
            String estadoValue = "Activo";
            boolean contratoFinalizado = cellData.getValue().getFechaEntrega() != null;

            if (contratoFinalizado) {
                estadoValue = "Finalizado";
            }

            return new SimpleStringProperty(estadoValue);
        });
    }

    private void bindStore() {
        contratosTable.setItems(agenciaStore.getContratos());
    }

    private void loadContratos() {
        Task<List<Contrato>> loadTask = createLoadContratosTask();
        configureLoadContratosTask(loadTask);

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private Task<List<Contrato>> createLoadContratosTask() {
        return new Task<>() {
            @Override
            protected List<Contrato> call() {
                return contratoService.listarTodos();
            }
        };
    }

    private void configureLoadContratosTask(Task<List<Contrato>> loadTask) {
        loadTask.setOnSucceeded(event -> {
            List<Contrato> contratos = loadTask.getValue();
            boolean loadedSuccessfully = contratos != null;

            if (loadedSuccessfully) {
                agenciaStore.setContratos(contratos);
                Logger.logInfo("Contratos cargados: " + contratos.size());
            }
        });

        loadTask.setOnFailed(event -> {
            Throwable throwable = loadTask.getException();
            String message = throwable != null ? throwable.getMessage() : "Sin detalle";
            boolean isBusinessError = throwable instanceof BusinessException;

            if (isBusinessError) {
                Logger.logError("Error de negocio cargando contratos: " + message);
                showError("No se pudieron cargar los contratos", message);
            } else {
                Logger.logError("Error inesperado cargando contratos: " + message);
                showError("Error cargando contratos", message);
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
