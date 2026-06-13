package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.proyectobdmotos.models.*;
import org.proyectobdmotos.services.*;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.services.exceptions.ValidationException;

import java.util.List;

public class ClienteFormController {

    // -------------------------------------------------------------
    // Controles FXML
    // -------------------------------------------------------------
    @FXML
    private TextField campoNombreUsuario;
    @FXML
    private PasswordField campoPassword;
    @FXML
    private CheckBox checkVerPassword;
    @FXML
    private TextField campoPasswordVisible;
    @FXML
    private TextField campoGmail;
    @FXML
    private CheckBox checkEsAdmin;

    @FXML
    private TextField campoCI;
    @FXML
    private TextField campoNombre;
    @FXML
    private TextField campoPrimerApellido;
    @FXML
    private TextField campoSegundoApellido;
    @FXML
    private TextField campoEdad;
    @FXML
    private ComboBox<String> comboSexo;
    @FXML
    private TextField campoTelefono;
    @FXML
    private ComboBox<Municipio> comboMunicipio;
    @FXML
    private GridPane gridCliente;

    // -------------------------------------------------------------
    // Dependencias
    // -------------------------------------------------------------
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;

    // -------------------------------------------------------------
    // Estado interno
    // -------------------------------------------------------------
    private boolean modoEdicion = false;
    private Cliente clienteEditando = null;
    private Usuario usuarioEditando = null;

    // -------------------------------------------------------------
    // Datos estáticos para pasar desde otro controlador
    // -------------------------------------------------------------
    private static Cliente clienteAEditarStatic;
    private static Usuario usuarioAEditarStatic;

    /**
     * Establece el cliente que se editará al abrir el formulario.
     * Método estático para comunicación entre controladores.
     */
    public static void setClienteAEditarStatic(Cliente c) {
        clienteAEditarStatic = c;
    }

    /**
     * Establece el usuario asociado al cliente en edición.
     */
    public static void setUsuarioAEditarStatic(Usuario u) {
        usuarioAEditarStatic = u;
    }

    // -------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------
    public ClienteFormController(ClienteService clienteService,
                                 UsuarioService usuarioService,
                                 ReferenceDataStore referenceDataStore) {
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
    }

    // -------------------------------------------------------------
    // Inicialización
    // -------------------------------------------------------------

    /**
     * Configura los componentes de la interfaz y carga los datos
     * iniciales. Si hay datos estáticos de edición, prepara el
     * formulario para ese modo.
     */
    @FXML
    private void initialize() {
        configurarComboSexo();
        configurarComboMunicipio();
        configurarListenerAdmin();
        configurarListenerMostrarPassword();

        if (clienteAEditarStatic != null) {
            setModoEdicion(clienteAEditarStatic, usuarioAEditarStatic);
            clienteAEditarStatic = null;
            usuarioAEditarStatic = null;
        }
    }

    /**
     * Llena el ComboBox de sexo con las opciones básicas.
     */
    private void configurarComboSexo() {
        comboSexo.getItems().addAll("Masculino", "Femenino");
    }

    /**
     * Carga los municipios desde el servicio y configura
     * la fábrica de celdas para mostrar el nombre del municipio.
     */
    private void configurarComboMunicipio() {
        List<Municipio> municipios = clienteService.listarMunicipios();
        comboMunicipio.getItems().setAll(municipios);
        comboMunicipio.setCellFactory(param -> new ListCell<Municipio>() {
            @Override
            protected void updateItem(Municipio item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreMunicipio());
            }
        });
        comboMunicipio.setConverter(new StringConverter<Municipio>() {
            @Override
            public String toString(Municipio m) {
                return m != null ? m.getNombreMunicipio() : "";
            }

            @Override
            public Municipio fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Vincula el checkbox de administrador con la visibilidad del
     * panel de datos del cliente. Si es admin, se oculta el cliente.
     */
    private void configurarListenerAdmin() {
        checkEsAdmin.selectedProperty().addListener((obs, oldVal, newVal) -> {
            boolean visible = !newVal;
            gridCliente.setVisible(visible);
            gridCliente.setManaged(visible);
        });
        if (checkEsAdmin.isSelected()) {
            gridCliente.setVisible(false);
            gridCliente.setManaged(false);
        }
    }

    /**
     * Sincroniza la visibilidad del campo de contraseña con el checkbox
     * "ver contraseña", copiando el texto entre los dos campos.
     */
    private void configurarListenerMostrarPassword() {
        checkVerPassword.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                campoPasswordVisible.setText(campoPassword.getText());
                campoPasswordVisible.setVisible(true);
                campoPassword.setVisible(false);
            } else {
                campoPasswordVisible.setVisible(false);
                campoPassword.setVisible(true);
            }
        });
    }

    // -------------------------------------------------------------
    // Modo edición
    // -------------------------------------------------------------

    /**
     * Coloca el formulario en modo edición con los datos del cliente
     * y usuario proporcionados.
     */
    public void setModoEdicion(Cliente cliente, Usuario usuario) {
        this.modoEdicion = true;
        this.clienteEditando = cliente;
        this.usuarioEditando = usuario;

        cargarDatosCliente(cliente);
        seleccionarMunicipio(cliente.getIdMunicipio());

        if (usuario != null) {
            cargarDatosUsuario(usuario);
        }
    }

    /**
     * Rellena los campos visuales con los datos del cliente.
     */
    private void cargarDatosCliente(Cliente cliente) {
        campoCI.setText(cliente.getCiCliente());
        campoNombre.setText(cliente.getNombreCliente());
        campoPrimerApellido.setText(cliente.getPrimerApellido());
        campoSegundoApellido.setText(cliente.getSegundoApellido());
        campoEdad.setText(String.valueOf(cliente.getEdad()));
        comboSexo.setValue(cliente.getSexo() == Sexo.MASCULINO ? "Masculino" : "Femenino");
        campoTelefono.setText(cliente.getNumeroContacto());
    }

    /**
     * Selecciona el municipio en el combo que coincida con el id dado.
     * Utiliza un bucle con variable booleana para evitar break/continue.
     */
    private void seleccionarMunicipio(int idMunicipio) {
        boolean encontrado = false;
        int indice = 0;
        int total = comboMunicipio.getItems().size();
        while (!encontrado && indice < total) {
            Municipio m = comboMunicipio.getItems().get(indice);
            if (m.getIdMunicipio() == idMunicipio) {
                comboMunicipio.getSelectionModel().select(m);
                encontrado = true;
            }
            indice++;
        }
    }

    /**
     * Rellena los campos visuales con los datos del usuario.
     * Si es admin, oculta el panel del cliente.
     */
    private void cargarDatosUsuario(Usuario usuario) {
        campoNombreUsuario.setText(usuario.getNombreUsuario() != null ? usuario.getNombreUsuario() : "");
        campoGmail.setText(usuario.getGmail() != null ? usuario.getGmail() : "");
        checkEsAdmin.setSelected(usuario.isEsAdmin());
        boolean visible = !usuario.isEsAdmin();
        gridCliente.setVisible(visible);
        gridCliente.setManaged(visible);

        campoPassword.clear();
        campoPasswordVisible.setText(usuario.getPassword() != null ? usuario.getPassword() : "");
    }

    // -------------------------------------------------------------
    // Acción Guardar
    // -------------------------------------------------------------

    /**
     * Orquesta el guardado del formulario. Valida campos, decide si
     * es creación o edición, y ejecuta la operación correspondiente.
     */
    @FXML
    private void onGuardar() {
        boolean esAdmin = checkEsAdmin.isSelected();
        boolean camposUsuarioValidos = validarCamposUsuario();
        boolean camposClienteValidos = true;

        if (!esAdmin && camposUsuarioValidos) {
            camposClienteValidos = validarCamposCliente();
        }

        boolean puedeGuardar = camposUsuarioValidos && (esAdmin || camposClienteValidos);

        if (puedeGuardar) {
            ejecutarGuardado(esAdmin);
        }
    }

    /**
     * Verifica que los campos del usuario (nombre, gmail, contraseña)
     * estén completos. Muestra errores si algo falta.
     * Retorna true si todo es válido.
     */
    private boolean validarCamposUsuario() {
        String nombreUsuario = obtenerTexto(campoNombreUsuario);
        String gmail = obtenerTexto(campoGmail);
        String password = obtenerTexto(campoPassword);

        if (nombreUsuario.isEmpty() || gmail.isEmpty()) {
            mostrarError("Los campos de la cuenta (usuario, gmail) son obligatorios.");
            return false;
        }
        if (!modoEdicion && password.isEmpty()) {
            mostrarError("La contraseña es obligatoria para un nuevo usuario.");
            return false;
        }
        return true;
    }

    /**
     * Verifica que los campos obligatorios del cliente estén completos.
     * Retorna true si todos están llenos.
     */
    private boolean validarCamposCliente() {
        String ci = obtenerTexto(campoCI);
        String nombre = obtenerTexto(campoNombre);
        String primerApellido = obtenerTexto(campoPrimerApellido);
        String edadTexto = obtenerTexto(campoEdad);
        String telefono = obtenerTexto(campoTelefono);

        if (ci.isEmpty() || nombre.isEmpty() || primerApellido.isEmpty() ||
                edadTexto.isEmpty() || telefono.isEmpty() ||
                comboSexo.getValue() == null || comboMunicipio.getValue() == null) {
            mostrarError("Todos los campos del cliente son obligatorios.");
            return false;
        }
        return true;
    }

    /**
     * Ejecuta la lógica de guardado: creación o actualización según
     * el modo. Envuelve las operaciones en try-catch para manejar errores.
     */
    private void ejecutarGuardado(boolean esAdmin) {
        try {
            if (modoEdicion) {
                actualizarEntidades(esAdmin);
            } else {
                crearNuevoUsuarioYCliente(esAdmin);
            }
            MainController.getInstance().onGoBack();
        } catch (ValidationException e) {
            e.printStackTrace();
            mostrarError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error en formulario de cliente: " + e.getMessage());
            mostrarError("Error inesperado al guardar: " + e.getMessage());
        }
    }

    /**
     * Actualiza el cliente (si no es admin) y el usuario en edición.
     */
    private void actualizarEntidades(boolean esAdmin) {
        if (!esAdmin) {
            actualizarClienteExistente();
        }
        actualizarUsuarioExistente();
    }

    /**
     * Crea un nuevo usuario y, si no es admin, un nuevo cliente asociado.
     */
    private void crearNuevoUsuarioYCliente(boolean esAdmin) {
        Usuario nuevoUsuario = usuarioService.registrarUsuarioConRol(
                obtenerTexto(campoNombreUsuario),
                obtenerTexto(campoPassword),
                obtenerTexto(campoGmail),
                esAdmin
        );
        if (!esAdmin) {
            Cliente nuevoCliente = construirClienteDesdeCampos();
            nuevoCliente.setIdUsuario(nuevoUsuario.getId());
            clienteService.crearCliente(nuevoCliente);
        }
    }

    /**
     * Aplica los valores de los campos al cliente en edición y lo persiste.
     */
    private void actualizarClienteExistente() {
        if (clienteEditando != null) {
            clienteEditando.setCiCliente(obtenerTexto(campoCI));
            clienteEditando.setNombreCliente(obtenerTexto(campoNombre));
            clienteEditando.setPrimerApellido(obtenerTexto(campoPrimerApellido));
            clienteEditando.setSegundoApellido(obtenerTexto(campoSegundoApellido));
            clienteEditando.setEdad(parsearEntero(obtenerTexto(campoEdad), 0));
            clienteEditando.setSexo(comboSexo.getValue().equals("Masculino") ? Sexo.MASCULINO : Sexo.FEMENINO);
            clienteEditando.setNumeroContacto(obtenerTexto(campoTelefono));
            clienteEditando.setIdMunicipio(comboMunicipio.getValue().getIdMunicipio());
            clienteService.actualizarCliente(clienteEditando);
        }
    }

    /**
     * Aplica los valores de los campos al usuario en edición y lo persiste.
     * Solo actualiza la contraseña si se ha escrito una nueva.
     */
    private void actualizarUsuarioExistente() {
        if (usuarioEditando != null) {
            Integer idUsuario = usuarioEditando.getId();
            if (idUsuario != null && idUsuario > 0) {
                usuarioEditando.setNombreUsuario(obtenerTexto(campoNombreUsuario));
                usuarioEditando.setGmail(obtenerTexto(campoGmail));
                usuarioEditando.setEsAdmin(checkEsAdmin.isSelected());

                String nuevaPassword = obtenerTexto(campoPassword);
                if (!nuevaPassword.isEmpty()) {
                    usuarioEditando.setPassword(nuevaPassword);
                }

                try {
                    usuarioService.actualizarUsuario(usuarioEditando);
                    Logger.log("Usuario actualizado: id=" + idUsuario +
                            ", nombre=" + usuarioEditando.getNombreUsuario() +
                            ", gmail=" + usuarioEditando.getGmail());
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.logError("Error al actualizar usuario: " + e.getMessage());
                    mostrarError("No se pudo actualizar la cuenta de usuario.");
                }
            } else {
                Logger.log("No se actualiza usuario porque idUsuario es nulo o 0.");
            }
        }
    }

    /**
     * Construye un objeto Cliente a partir de los campos del formulario.
     * Retorna un cliente nuevo con los datos actuales.
     */
    private Cliente construirClienteDesdeCampos() {
        Cliente nuevo = new Cliente();
        nuevo.setCiCliente(obtenerTexto(campoCI));
        nuevo.setNombreCliente(obtenerTexto(campoNombre));
        nuevo.setPrimerApellido(obtenerTexto(campoPrimerApellido));
        nuevo.setSegundoApellido(obtenerTexto(campoSegundoApellido));
        nuevo.setEdad(parsearEntero(obtenerTexto(campoEdad), 0));
        nuevo.setSexo(comboSexo.getValue().equals("Masculino") ? Sexo.MASCULINO : Sexo.FEMENINO);
        nuevo.setNumeroContacto(obtenerTexto(campoTelefono));
        nuevo.setIdMunicipio(comboMunicipio.getValue().getIdMunicipio());
        return nuevo;
    }

    // -------------------------------------------------------------
    // Navegación
    // -------------------------------------------------------------

    /**
     * Cancela el formulario y vuelve a la pantalla anterior.
     */
    @FXML
    private void onCancelar() {
        MainController.getInstance().onGoBack();
    }

    // -------------------------------------------------------------
    // Utilidades
    // -------------------------------------------------------------

    /**
     * Obtiene el texto de un campo de texto y lo devuelve recortado.
     * Si el campo es nulo, devuelve cadena vacía.
     */
    private String obtenerTexto(TextField campo) {
        return campo.getText() != null ? campo.getText().trim() : "";
    }

    /**
     * Convierte una cadena a entero, devolviendo el valor por defecto
     * si falla la conversión.
     */
    private int parsearEntero(String texto, int valorPorDefecto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return valorPorDefecto;
        }
    }

    /**
     * Muestra un mensaje de error en un cuadro de diálogo.
     */
    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje).showAndWait();
    }
}