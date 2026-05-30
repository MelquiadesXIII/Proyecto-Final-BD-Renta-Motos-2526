package org.proyectobdmotos.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.models.Usuario;
import org.proyectobdmotos.services.ClienteService;
import org.proyectobdmotos.services.UsuarioService;
import org.proyectobdmotos.services.exceptions.BusinessException;
import org.proyectobdmotos.services.exceptions.ValidationException;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.ui.navigation.ScreenLoader;
import org.proyectobdmotos.utils.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.concurrent.Task;

public class ClienteController {

    private final ClienteService clienteService;
    private final UsuarioService usuarioService;       // ← NUEVO: necesario para el formulario
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
                             UsuarioService usuarioService,
                             AgenciaStore agenciaStore,
                             ReferenceDataStore referenceDataStore) {
        this.screenLoader = screenLoader;
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
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

    @FXML
    private void onEditarCliente() {
        Cliente cliente = clientesTable.getSelectionModel().getSelectedItem();
        if (cliente == null) {
            mostrarAlerta("No ha seleccionado ningún cliente");
        } else {
            abrirFormulario(cliente, null);
        }
    }

    @FXML
    private void onEliminarCliente() {
        Cliente cliente = clientesTable.getSelectionModel().getSelectedItem();
        if (cliente == null) {
            mostrarAlerta("Seleccione un cliente de la tabla para eliminar.");
        } else {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Eliminación");
            confirmacion.setHeaderText("¿Eliminar al cliente " + cliente.getNombreCliente() + "?");
            confirmacion.setContentText("CI: " + cliente.getCiCliente());
            Optional<ButtonType> resultado = confirmacion.showAndWait();

            boolean eliminarConfirmado = resultado.isPresent() && resultado.get() == ButtonType.OK;
            if (eliminarConfirmado) {
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
        Task<List<Cliente>> loadTask = new Task<>() {
            @Override
            protected List<Cliente> call() {
                return clienteService.listarTodos();
            }
        };

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

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void abrirFormulario(Cliente cliente, Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cliente-form.fxml"));
            ClienteFormController formController = new ClienteFormController(
                    clienteService, usuarioService, referenceDataStore);
            loader.setController(formController);

            Parent root = loader.load();

            if (cliente != null) {
                formController.setModoEdicion(cliente, usuario);
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(cliente == null ? "Nuevo Cliente" : "Editar Cliente");
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