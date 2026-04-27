package org.proyectobdmotos.controller;

import java.util.List;

import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.services.ClienteService;
import org.proyectobdmotos.services.exceptions.BusinessException;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.utils.Logger;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * ClienteController: maneja eventos de la UI de clientes.
 * Delega operaciones a ClienteService y actualiza/observa AgenciaStore.
 */
public class ClienteController {

    private final ClienteService clienteService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;

    @FXML
    private TableView<Cliente> clientesTable;

    @FXML
    private TableColumn<Cliente, String> ciColumn;

    @FXML
    private TableColumn<Cliente, String> nombreColumn;

    @FXML
    private TableColumn<Cliente, Integer> municipioColumn;

    @FXML
    private TableColumn<Cliente, String> telefonoColumn;

    public ClienteController(
        ClienteService clienteService,
        AgenciaStore agenciaStore,
        ReferenceDataStore referenceDataStore
    ) {
        this.clienteService = clienteService;
        this.agenciaStore = agenciaStore;
        this.referenceDataStore = referenceDataStore;
    }

    @FXML
    private void initialize() {
        Logger.log("Inicializando ClienteController...");
        configureTableColumns();
        bindStore();
        loadClientes();
    }

    private void configureTableColumns() {
        ciColumn.setCellValueFactory(new PropertyValueFactory<>("ciCliente"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        municipioColumn.setCellValueFactory(new PropertyValueFactory<>("idMunicipio"));
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("numeroContacto"));
    }

    private void bindStore() {
        clientesTable.setItems(agenciaStore.getClientes());
    }

    private void loadClientes() {
        Task<List<Cliente>> loadTask = createLoadClientesTask();
        configureLoadClientesTask(loadTask);

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private Task<List<Cliente>> createLoadClientesTask() {
        return new Task<>() {
            @Override
            protected List<Cliente> call() {
                return clienteService.listarTodos();
            }
        };
    }

    private void configureLoadClientesTask(Task<List<Cliente>> loadTask) {
        loadTask.setOnSucceeded(event -> {
            List<Cliente> clientes = loadTask.getValue();
            boolean loadedSuccessfully = clientes != null;

            if (loadedSuccessfully) {
                agenciaStore.setClientes(clientes);
                Logger.logInfo("Clientes cargados: " + clientes.size());
            }
        });

        loadTask.setOnFailed(event -> {
            Throwable throwable = loadTask.getException();
            String message = throwable != null ? throwable.getMessage() : "Sin detalle";
            boolean isBusinessError = throwable instanceof BusinessException;

            if (isBusinessError) {
                Logger.logError("Error de negocio cargando clientes: " + message);
                showError("No se pudieron cargar los clientes", message);
            } else {
                Logger.logError("Error inesperado cargando clientes: " + message);
                showError("Error cargando clientes", message);
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
