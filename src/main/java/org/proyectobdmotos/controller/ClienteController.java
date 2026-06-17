package org.proyectobdmotos.controller;

import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
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
import org.proyectobdmotos.utils.*;
import org.proyectobdmotos.utils.Logger;

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
    @FXML private StackPane rootPane;

    @FXML private Label labelCargando;

    public ClienteController(ScreenLoader screenLoader, ClienteService clienteService, UsuarioService usuarioService,
                             AgenciaStore agenciaStore, ReferenceDataStore referenceDataStore) {
        this.screenLoader = screenLoader;
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
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
        Logger.log("Inicializando ClienteController...");
        configureTableColumns();
        loadClientes();
        fijarColumnas(clientesTable);
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
        Task<List<ClienteUsuarioDTO>> loadTask = crearTareaCargaClientes();
        new Thread(loadTask).start();
    }

    private Task<List<ClienteUsuarioDTO>> crearTareaCargaClientes() {
        Task<List<ClienteUsuarioDTO>> loadTask = new Task<>() {
            @Override
            protected List<ClienteUsuarioDTO> call() {
                return clienteService.listarClientesConUsuario();
            }
        };
        loadTask.setOnSucceeded(event -> manejarCargaExitosa(loadTask.getValue()));
        loadTask.setOnFailed(event -> manejarCargaFallida(loadTask.getException()));
        return loadTask;
    }

    private void manejarCargaExitosa(List<ClienteUsuarioDTO> lista) {
        labelCargando.setVisible(false);
        if (lista != null) {
            clientesTable.getItems().setAll(lista);
            Logger.logInfo("Clientes cargados: " + lista.size());
        }
    }

    private void manejarCargaFallida(Throwable throwable) {
        labelCargando.setVisible(false);
        String message = throwable != null ? throwable.getMessage() : "Sin detalle";
        boolean isBusinessError = throwable instanceof BusinessException;
        if (isBusinessError) {
            Logger.logError("Error de negocio cargando clientes: " + message);
            showError("No se pudieron cargar los clientes", message);
        } else {
            Logger.logError("Error inesperado cargando clientes: " + message);
            showError("Error cargando clientes", message);
        }
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
        if (dto != null) {
            Cliente cliente = buscarClientePorId(dto.getIdCliente());
            if (cliente != null) {
                Usuario usuario = buscarUsuarioDeCliente(cliente.getIdUsuario());
                ClienteFormController.setClienteAEditarStatic(cliente);
                ClienteFormController.setUsuarioAEditarStatic(usuario);
                MainController.getInstance().cargarVista("/fxml/cliente-form-view.fxml", "Editar Cliente");
            } else {
                mostrarAlerta("Cliente no encontrado.");
            }
        } else {
            mostrarAlerta("Seleccione un cliente de la tabla.");
        }
    }

    private Cliente buscarClientePorId(int idCliente) {
        Optional<Cliente> opt = clienteService.buscarPorId(idCliente);
        return opt.orElse(null);
    }

    private Usuario buscarUsuarioDeCliente(Integer idUsuario) {
        if (idUsuario != null && idUsuario > 0) {
            return usuarioService.buscarPorId(idUsuario);
        }
        return null;
    }

    @FXML
    private void onEliminarCliente() {
        ClienteUsuarioDTO dto = clientesTable.getSelectionModel().getSelectedItem();
        if (dto != null) {
            boolean confirmado = confirmarEliminacion(dto);
            if (confirmado) {
                ejecutarEliminacion(dto);
            }
        } else {
            mostrarAlerta("Seleccione un cliente de la tabla para eliminar.");
        }
    }

    private boolean confirmarEliminacion(ClienteUsuarioDTO dto) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar al cliente " + dto.getNombreCompleto() + "?");
        confirmacion.setContentText("CI: " + dto.getCi());
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

    private void ejecutarEliminacion(ClienteUsuarioDTO dto) {
        try {
            clienteService.eliminarCliente(dto.getCi());
            loadClientes();
        } catch (ValidationException e) {
            mostrarAlerta("Error al eliminar: " + e.getMessage());
        }
    }

    @FXML
    private void onActualizarLista() {
        loadClientes();
    }

    private void showError(String headerText, String contentText) {
        AlertUtils.mostrarErrorTitulo(headerText, contentText);
    }

    private void mostrarAlerta(String mensaje) {
        AlertUtils.mostrarError(mensaje);
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