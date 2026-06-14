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

    /**
     * Prepara la tabla y carga los clientes al iniciar la pantalla.
     * No realiza lógica de negocio, solo configura y dispara la carga.
     */
    @FXML
    private void initialize() {
        Logger.log("Inicializando ClienteController...");
        configureTableColumns();
        loadClientes();
    }

    // -------------------------------------------------------------
    // Configuración de columnas
    // -------------------------------------------------------------

    /**
     * Asigna a cada columna de la tabla la propiedad del DTO
     * que debe mostrar. Centraliza el mapeo para que sea fácil
     * de modificar si cambian los nombres de los campos.
     */
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

    /**
     * Inicia la carga de clientes en segundo plano, mostrando
     * un indicador de progreso mientras se consulta la base de datos.
     * Delega en el servicio correspondiente y actualiza la tabla
     * cuando los datos están listos.
     */
    private void loadClientes() {
        labelCargando.setVisible(true);
        Task<List<ClienteUsuarioDTO>> loadTask = crearTareaCargaClientes();
        new Thread(loadTask).start();
    }

    /**
     * Construye la tarea asíncrona que obtiene la lista de clientes
     * y define qué hacer al tener éxito o fallar.
     * Separa la creación de la tarea de su ejecución.
     */
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

    /**
     * Oculta el indicador de carga y muestra los clientes en la tabla
     * cuando la consulta termina bien.
     */
    private void manejarCargaExitosa(List<ClienteUsuarioDTO> lista) {
        labelCargando.setVisible(false);
        if (lista != null) {
            clientesTable.getItems().setAll(lista);
            Logger.logInfo("Clientes cargados: " + lista.size());
        }
    }

    /**
     * Oculta el indicador de carga y muestra un mensaje de error
     * si la consulta falla, diferenciando entre error de negocio
     * y error técnico.
     */
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

    /**
     * Abre el formulario de cliente sin datos previos (modo creación).
     * Indica al controlador del formulario que no hay cliente ni usuario
     * seleccionados.
     */
    @FXML
    private void onCrearCliente() {
        ClienteFormController.setClienteAEditarStatic(null);
        ClienteFormController.setUsuarioAEditarStatic(null);
        MainController.getInstance().cargarVista("/fxml/cliente-form-view.fxml", "Nuevo Cliente");
    }

    /**
     * Carga el cliente y su usuario asociado (si existe) y los envía
     * al formulario en modo edición. Si no se selecciona ningún cliente
     * en la tabla, muestra un mensaje de advertencia.
     */
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

    /**
     * Busca un cliente por su id. Devuelve null si no existe.
     * Método auxiliar para mantener limpia la lógica del evento.
     */
    private Cliente buscarClientePorId(int idCliente) {
        Optional<Cliente> opt = clienteService.buscarPorId(idCliente);
        return opt.orElse(null);
    }

    /**
     * Busca el usuario asociado al id_usuario del cliente.
     * Retorna null si el id es nulo/cero o si no se encuentra.
     */
    private Usuario buscarUsuarioDeCliente(Integer idUsuario) {
        Usuario usuario = null;
        boolean idValido = idUsuario != null && idUsuario > 0;
        if (idValido) {
            usuario = usuarioService.buscarPorId(idUsuario);
        }
        return usuario;
    }

    /**
     * Coloca los objetos cliente y usuario en los campos estáticos
     * del controlador del formulario y abre la vista de edición.
     */
    private void prepararFormularioEdicion(Cliente cliente, Usuario usuario) {
        ClienteFormController.setClienteAEditarStatic(cliente);
        ClienteFormController.setUsuarioAEditarStatic(usuario);
        MainController.getInstance().cargarVista("/fxml/cliente-form-view.fxml", "Editar Cliente");
    }

    // -------------------------------------------------------------
    // Eliminación de cliente
    // -------------------------------------------------------------

    /**
     * Maneja la acción de eliminar un cliente. Verifica que haya
     * uno seleccionado, pide confirmación y, si se acepta,
     * ejecuta el borrado.
     */
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

    /**
     * Muestra un diálogo de confirmación y devuelve true si el usuario
     * elige "Aceptar". Centraliza el mensaje de la confirmación.
     */
    private boolean confirmarEliminacion(ClienteUsuarioDTO dto) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar al cliente " + dto.getNombreCompleto() + "?");
        confirmacion.setContentText("CI: " + dto.getCi());
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        boolean esOk = resultado.isPresent() && resultado.get() == ButtonType.OK;
        return esOk;
    }

    /**
     * Intenta eliminar el cliente usando el servicio. Si la operación
     * falla, muestra un mensaje de error. Si tiene éxito, recarga la tabla.
     */
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

    /**
     * Fuerza la recarga de la lista de clientes desde el servicio.
     * Útil para refrescar los datos después de cambios externos.
     */
    @FXML
    private void onActualizarLista() {
        loadClientes();
    }

    // -------------------------------------------------------------
    // Alertas reutilizables
    // -------------------------------------------------------------

    /**
     * Muestra un mensaje de error con título y contenido personalizados.
     * Centraliza la creación de alertas de error para mantener consistencia.
     */
    private void showError(String headerText, String contentText) {
        AlertUtils.mostrarErrorTitulo(headerText, contentText);
    }

    /**
     * Muestra un mensaje informativo con un solo texto.
     * Simplifica la comunicación de eventos no críticos.
     */
    private void mostrarAlerta(String mensaje) {
        AlertUtils.mostrarError(mensaje);
    }
}