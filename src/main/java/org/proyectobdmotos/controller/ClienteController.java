package org.proyectobdmotos.controller;

import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dto.ClienteUsuarioDTO;
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

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ClienteController {

    private final ClienteService clienteService;
    private final UsuarioService usuarioService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;
    private final ScreenLoader screenLoader;

    @FXML private TableView<ClienteUsuarioDTO> clientesTable;
    @FXML private TableColumn<ClienteUsuarioDTO, Integer> colIdCliente;
    @FXML private TableColumn<ClienteUsuarioDTO, Integer> colIdUsuario;
    @FXML private TableColumn<ClienteUsuarioDTO, String> colCi;
    @FXML private TableColumn<ClienteUsuarioDTO, String> colNombre;
    @FXML private TableColumn<ClienteUsuarioDTO, String> colTelefono;
    @FXML private TableColumn<ClienteUsuarioDTO, String> colMunicipio;
    @FXML private TableColumn<ClienteUsuarioDTO, String> colUsuario;
    @FXML private TableColumn<ClienteUsuarioDTO, String> colGmail;
    @FXML private TableColumn<ClienteUsuarioDTO, Integer> colContratos;

    @FXML private Label labelCargando;

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
        loadClientes();
    }

    @FXML
    private void onCrearCliente() {
        ClienteFormController.setClienteAEditarStatic(null);
        ClienteFormController.setUsuarioAEditarStatic(null);
        MainController.getInstance().cargarVista("/fxml/cliente-form-view.fxml", "Nuevo Cliente");
    }

    @FXML
    private void onEditarCliente() {
        ClienteUsuarioDTO dto = clientesTable.getSelectionModel().getSelectedItem();
        if (dto == null) {
            mostrarAlerta("Seleccione un cliente de la tabla.");
            return;
        }
        Optional<Cliente> optCliente = clienteService.buscarPorId(dto.getIdCliente());
        if (optCliente.isEmpty()) {
            mostrarAlerta("Cliente no encontrado.");
            return;
        }
        Cliente cliente = optCliente.get();

        Integer idUsuario = cliente.getIdUsuario();
        Usuario usuario = null;
        if (idUsuario != null && idUsuario > 0) {
            usuario = usuarioService.buscarPorId(idUsuario);
        }

        ClienteFormController.setClienteAEditarStatic(cliente);
        ClienteFormController.setUsuarioAEditarStatic(usuario);

        MainController.getInstance().cargarVista("/fxml/cliente-form-view.fxml", "Editar Cliente");
    }

    @FXML
    private void onEliminarCliente() {
        ClienteUsuarioDTO dto = clientesTable.getSelectionModel().getSelectedItem();
        if (dto == null) {
            mostrarAlerta("Seleccione un cliente de la tabla para eliminar.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar al cliente " + dto.getNombreCompleto() + "?");
        confirmacion.setContentText("CI: " + dto.getCi());
        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                clienteService.eliminarCliente(dto.getCi());
                loadClientes();
            } catch (ValidationException e) {
                mostrarAlerta("Error al eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onActualizarLista() {
        loadClientes();
    }

    private void configureTableColumns() {
        colIdCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colIdUsuario.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colCi.setCellValueFactory(new PropertyValueFactory<>("ci"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colMunicipio.setCellValueFactory(new PropertyValueFactory<>("nombreMunicipio"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colGmail.setCellValueFactory(new PropertyValueFactory<>("gmail"));
        colContratos.setCellValueFactory(new PropertyValueFactory<>("cantidadContratos"));
    }

    private void loadClientes() {
        labelCargando.setVisible(true);
        Task<List<ClienteUsuarioDTO>> loadTask = new Task<>() {
            @Override
            protected List<ClienteUsuarioDTO> call() {
                return clienteService.listarClientesConUsuario();
            }
        };

        loadTask.setOnSucceeded(event -> {
            labelCargando.setVisible(false);
            List<ClienteUsuarioDTO> lista = loadTask.getValue();
            if (lista != null) {
                clientesTable.getItems().setAll(lista);
                Logger.logInfo("Clientes cargados: " + lista.size());
            }
        });

        loadTask.setOnFailed(event -> {
            labelCargando.setVisible(false);
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