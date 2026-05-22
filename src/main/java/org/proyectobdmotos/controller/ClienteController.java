package org.proyectobdmotos.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.models.Usuario;
import org.proyectobdmotos.services.ClienteService;
import org.proyectobdmotos.services.exceptions.BusinessException;
import org.proyectobdmotos.services.exceptions.ValidationException;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.ui.navigation.ScreenLoader;
import org.proyectobdmotos.utils.Logger;

import javafx.stage.Stage; 
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;

/**
 * ClienteController: maneja eventos de la UI de clientes.
 * Delega operaciones a ClienteService y actualiza/observa AgenciaStore.
 */
public class ClienteController {

    private final ClienteService clienteService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;
    private final ScreenLoader screenLoader;

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

    public ClienteController(ScreenLoader screenLoader,
            ClienteService clienteService,
            AgenciaStore agenciaStore,
            ReferenceDataStore referenceDataStore) {
        this.screenLoader = screenLoader;
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

    @FXML
    private void onCrearCliente() {
        abrirFormulario(null, null);
    }

    // Funciona de la siguiente forma:
    // el admin puede seleccionarlo
    // en la tabla y de ahi empezar a editarlo
    @FXML
    private void onEditarCliente()
    {
        Cliente cliente = clientesTable.getSelectionModel().getSelectedItem();
        if(cliente == null)
        {
            mostrarAlerta("No ha seleccionado ningun cliente");
        }
        else
        {
            abrirFormulario(cliente, null);
        }
    }

    @FXML
    private void onEliminarCliente() {
        Cliente cliente = clientesTable.getSelectionModel().getSelectedItem();
        if (cliente == null) {
            mostrarAlerta("Seleccione un cliente de la tabla para eliminar.");
        }

        else {
            Alert entro = new Alert(AlertType.CONFIRMATION);
            entro.setTitle("Confirmar Eliminación");
            entro.setHeaderText("¿Eliminar al cliente " + cliente.getNombreCliente() + "?");
            entro.setContentText("CI: " + cliente.getCiCliente());
            Optional<ButtonType> resultado = entro.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                try {
                    clienteService.eliminarCliente(cliente.getCiCliente());
                    loadClientes();
                } catch (ValidationException e) {
                    mostrarAlerta("Error al eliminar: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void onActualizarLista() {
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

    private void abrirFormulario(Cliente c, Usuario u) {
        try {
            Parent root = screenLoader.load("/fxml/cliente-form.fxml");
            ClienteFormController formController = (ClienteFormController) screenLoader.getLastController();

            if (c != null) {
                formController.setModoEdicion(c, u);
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(c == null ? "Nuevo Cliente" : "Editar Cliente");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(clientesTable.getScene().getWindow());
            stage.showAndWait();
            loadClientes();
        } catch (IOException e) {
            Logger.logError("Error al cargar formulario de cliente: " + e.getMessage());
            mostrarAlerta("No se pudo abrir el formulario.");
        }

    }

    private void showError(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
