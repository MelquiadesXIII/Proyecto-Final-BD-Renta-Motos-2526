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
import org.proyectobdmotos.utils.*;
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

    // -------------------------------------------------------------
    // Inicialización
    // -------------------------------------------------------------

    @FXML
    private void initialize() {
        Logger.log("Inicializando ClienteController...");
        configureTableColumns();
        loadClientes();
        fijarColumnas(clientesTable);
    }

    // -------------------------------------------------------------
    // Configuración de columnas
    // -------------------------------------------------------------

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

    // -------------------------------------------------------------
    // Carga de el clientes
    // -------------------------------------------------------------

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

    // -------------------------------------------------------------
    // Navegación hacia el formulario
    // -------------------------------------------------------------

    @FXML
    private void onCrearCliente() {
        ClienteFormController.setClienteAEditarStatic(null);
        ClienteFormController.setUsuarioAEditarStatic(null);
        MainController.getInstance().cargarVista("/fxml/cliente-form-view.fxml", "Nuevo Cliente");
    }

    @FXML
    private void onEditarCliente() {
        ClienteUsuarioDTO dto = clientesTable.getSelectionModel().getSelectedItem();
        boolean seleccionValida = dto != null;
        if (seleccionValida) {
            Cliente cliente = buscarClientePorId(dto.getIdCliente());
            boolean clienteExiste = cliente != null;
            if (clienteExiste) {
                Usuario usuario = buscarUsuarioDeCliente(cliente.getIdUsuario());
                prepararFormularioEdicion(cliente, usuario);
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
        Usuario usuario = null;
        boolean idValido = idUsuario != null && idUsuario > 0;
        if (idValido) {
            usuario = usuarioService.buscarPorId(idUsuario);
        }
        return usuario;
    }

    private void prepararFormularioEdicion(Cliente cliente, Usuario usuario) {
        ClienteFormController.setClienteAEditarStatic(cliente);
        ClienteFormController.setUsuarioAEditarStatic(usuario);
        MainController.getInstance().cargarVista("/fxml/cliente-form-view.fxml", "Editar Cliente");
    }

    // -------------------------------------------------------------
    // Eliminación de cliente
    // -------------------------------------------------------------

    @FXML
    private void onEliminarCliente() {
        ClienteUsuarioDTO dto = clientesTable.getSelectionModel().getSelectedItem();
        boolean seleccionValida = dto != null;
        if (seleccionValida) {
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
        boolean esOk = resultado.isPresent() && resultado.get() == ButtonType.OK;
        return esOk;
    }

    private void ejecutarEliminacion(ClienteUsuarioDTO dto) {
        try {
            clienteService.eliminarCliente(dto.getCi());
            loadClientes();
        } catch (ValidationException e) {
            mostrarAlerta("Error al eliminar: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------
    // Recarga manual
    // -------------------------------------------------------------

    @FXML
    private void onActualizarLista() {
        loadClientes();
    }

    // -------------------------------------------------------------
    // Método para fijar las columnas
    // -------------------------------------------------------------

    private void fijarColumnas(TableView<?> tabla) {
        int i = 0;
        while (i < tabla.getColumns().size()) {
            TableColumn<?, ?> columna = tabla.getColumns().get(i);
            columna.setResizable(false);
            columna.setReorderable(false);
            i++;
        }
    }

    // -------------------------------------------------------------
    // Alertas reutilizables
    // -------------------------------------------------------------

    private void showError(String headerText, String contentText) {
        AlertUtils.mostrarErrorTitulo(headerText, contentText);
    }

    private void mostrarAlerta(String mensaje) {
        AlertUtils.mostrarError(mensaje);
    }
}