package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.proyectobdmotos.models.*;
import org.proyectobdmotos.services.*;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.utils.*;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.utils.Validator;
import org.proyectobdmotos.services.exceptions.ValidationException;

import java.util.List;

public class ClienteFormController {

    // -------------------------------------------------------------
    // Controles FXML
    // -------------------------------------------------------------
    @FXML private TextField campoNombreUsuario;
    @FXML private PasswordField campoPassword;
    @FXML private CheckBox checkVerPassword;
    @FXML private TextField campoPasswordVisible;
    @FXML private TextField campoGmail;
    @FXML private CheckBox checkEsAdmin;

    @FXML private TextField campoCI;
    @FXML private TextField campoNombre;
    @FXML private TextField campoPrimerApellido;
    @FXML private TextField campoSegundoApellido;
    @FXML private TextField campoEdad;
    @FXML private ComboBox<String> comboSexo;
    @FXML private TextField campoTelefono;
    @FXML private ComboBox<Municipio> comboMunicipio;
    @FXML private GridPane gridCliente;

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

    public static void setClienteAEditarStatic(Cliente c) { clienteAEditarStatic = c; }
    public static void setUsuarioAEditarStatic(Usuario u) { usuarioAEditarStatic = u; }

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
     * Prepara los combos, listeners y, si hay datos de edición estáticos,
     * coloca el formulario en modo edición.
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
     * Agrega las opciones de sexo al combo correspondiente.
     */
    private void configurarComboSexo() {
        comboSexo.getItems().addAll("Masculino", "Femenino");
    }

    /**
     * Carga los municipios desde el servicio y configura la visualización del combo.
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
            @Override public String toString(Municipio m) { return m != null ? m.getNombreMunicipio() : ""; }
            @Override public Municipio fromString(String string) { return null; }
        });
    }

    /**
     * Oculta o muestra los campos del cliente según el checkbox de administrador.
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
     * Sincroniza la visibilidad del campo de contraseña con el checkbox de mostrar.
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
     * Activa el modo edición y rellena los campos con los datos del cliente y usuario.
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
     * Orquesta la validación y el guardado del formulario.
     * Solo procede si los campos de usuario (y de cliente si aplica) son válidos.
     */
    @FXML
    private void onGuardar() {
        boolean esAdmin = checkEsAdmin.isSelected();
        boolean usuarioValido = validarCamposUsuario(esAdmin);
        boolean clienteValido = true;
        if (!esAdmin) {
            clienteValido = validarCamposCliente();
        }
        if (usuarioValido && clienteValido) {
            ejecutarGuardado(esAdmin);
        }
    }

    // -------------------- Validaciones --------------------

    /**
     * Valida los campos de usuario utilizando la clase Validator siempre que es posible.
     * Acumula errores y devuelve true solo si todos son correctos.
     * @param esAdmin indica si se está creando un administrador.
     * @return true si todos los campos de usuario son válidos.
     */
    private boolean validarCamposUsuario(boolean esAdmin) {
        String nombreUsuario = obtenerTexto(campoNombreUsuario);
        String gmail = obtenerTexto(campoGmail);
        String password = obtenerTexto(campoPassword);

        boolean camposObligatorios = !nombreUsuario.isEmpty() && !gmail.isEmpty();
        if (!camposObligatorios) {
            mostrarError("Los campos de la cuenta (usuario, gmail) son obligatorios.");
        }

        boolean passwordValida = modoEdicion || !password.isEmpty();
        if (!passwordValida) {
            mostrarError("La contraseña es obligatoria para un nuevo usuario.");
        }

        boolean longitudPassword = modoEdicion || password.length() >= 4;
        if (!longitudPassword) {
            mostrarError("La contraseña debe tener al menos 4 caracteres.");
        }

        boolean usuarioFormato = false;
        try {
            Validator.validateTextWithNumbers(nombreUsuario);
            usuarioFormato = true;
        } catch (ValidationException e) {
            mostrarError("Nombre de usuario: " + e.getMessage());
        }

        boolean correoValido = gmail.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$");
        if (!correoValido) {
            mostrarError("El correo electrónico no tiene un formato válido.");
        }

        return camposObligatorios && passwordValida && longitudPassword && usuarioFormato && correoValido;
    }

    /**
     * Valida todos los campos del cliente utilizando la clase Validator.
     * Acumula los resultados en variables booleanas y devuelve un único valor al final.
     * @return true si todos los campos del cliente son válidos.
     */
    private boolean validarCamposCliente() {
        String ci = obtenerTexto(campoCI);
        String nombre = obtenerTexto(campoNombre);
        String primerApellido = obtenerTexto(campoPrimerApellido);
        String edadTexto = obtenerTexto(campoEdad);
        String telefono = obtenerTexto(campoTelefono);

        boolean camposLlenos = !ci.isEmpty() && !nombre.isEmpty() && !primerApellido.isEmpty()
                && !edadTexto.isEmpty() && !telefono.isEmpty()
                && comboSexo.getValue() != null && comboMunicipio.getValue() != null;
        if (!camposLlenos) {
            mostrarError("Todos los campos del cliente son obligatorios.");
        }

        boolean ciValido = false;
        try {
            Validator.validateCI(ci);
            ciValido = true;
        } catch (ValidationException e) {
            mostrarError(e.getMessage());
        }

        boolean nombreValido = false;
        try {
            Validator.validateText(nombre);
            nombreValido = true;
        } catch (ValidationException e) {
            mostrarError("Nombre: " + e.getMessage());
        }

        boolean apellidoValido = false;
        try {
            Validator.validateText(primerApellido);
            apellidoValido = true;
        } catch (ValidationException e) {
            mostrarError("Primer apellido: " + e.getMessage());
        }

        int edad = parsearEntero(edadTexto, -1);
        boolean edadValida = false;
        try {
            Validator.validateAge(edad);
            edadValida = true;
        } catch (ValidationException e) {
            mostrarError("Edad: " + e.getMessage());
        }

        boolean telefonoValido = false;
        try {
            Validator.validateTelephoneNumber(telefono);
            telefonoValido = true;
        } catch (ValidationException e) {
            mostrarError(e.getMessage());
        }

        return camposLlenos && ciValido && nombreValido && apellidoValido && edadValida && telefonoValido;
    }

    // -------------------- Ejecución del guardado --------------------

    /**
     * Ejecuta la operación de guardado (creación o actualización) y maneja los errores.
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
     * Actualiza las entidades existentes (cliente y/o usuario) según el rol.
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
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.logError("Error al actualizar usuario: " + e.getMessage());
                    mostrarError("No se pudo actualizar la cuenta de usuario.");
                }
            }
        }
    }

    /**
     * Construye un objeto Cliente a partir de los campos del formulario.
     * Las validaciones de formato ya se realizaron antes de llamar a este método.
     * @return el nuevo cliente listo para persistir.
     */
    private Cliente construirClienteDesdeCampos() {
        Cliente nuevo = new Cliente();
        nuevo.setCiCliente(obtenerTexto(campoCI));
        nuevo.setNombreCliente(obtenerTexto(campoNombre));
        nuevo.setPrimerApellido(obtenerTexto(campoPrimerApellido));
        nuevo.setSegundoApellido(obtenerTexto(campoSegundoApellido));
        nuevo.setEdad(parsearEntero(obtenerTexto(campoEdad), 18));
        nuevo.setSexo(comboSexo.getValue().equals("Masculino") ? Sexo.MASCULINO : Sexo.FEMENINO);
        nuevo.setNumeroContacto(obtenerTexto(campoTelefono));
        nuevo.setIdMunicipio(comboMunicipio.getValue().getIdMunicipio());
        return nuevo;
    }

    // -------------------------------------------------------------
    // Cancelar
    // -------------------------------------------------------------

    /**
     * Cancela el formulario y vuelve a la pantalla anterior.
     */
    @FXML
    private void onCancelar() {
        MainController.getInstance().onGoBack();
    }

    // -------------------------------------------------------------
    // Utilidades de campos
    // -------------------------------------------------------------

    /**
     * Obtiene el texto de un campo de texto y lo devuelve recortado.
     * Si el campo es nulo, devuelve cadena vacía.
     * @return el texto sin espacios en los extremos.
     */
    private String obtenerTexto(TextField campo) {
        return campo.getText() != null ? campo.getText().trim() : "";
    }

    /**
     * Convierte una cadena a entero, devolviendo el valor por defecto si falla la conversión.
     * @return el entero parseado o el valor por defecto.
     */
    private int parsearEntero(String texto, int valorPorDefecto) {
        int resultado = valorPorDefecto;
        try {
            resultado = Integer.parseInt(texto);
        } catch (NumberFormatException ignored) {
            // se queda con el valor por defecto
        }
        return resultado;
    }

    // -------------------------------------------------------------
    // Alertas
    // -------------------------------------------------------------

    /**
     * Muestra un mensaje de error usando la utilidad centralizada de alertas.
     */
    private void mostrarError(String mensaje) {
        AlertUtils.mostrarError(mensaje);
    }
}