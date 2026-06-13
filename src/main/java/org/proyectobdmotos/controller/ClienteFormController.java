package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.proyectobdmotos.models.*;
import org.proyectobdmotos.services.*;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.utils.AlertUtils;
import org.proyectobdmotos.utils.Logger;
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

    private void configurarComboSexo() {
        comboSexo.getItems().addAll("Masculino", "Femenino");
    }

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

    private void cargarDatosCliente(Cliente cliente) {
        campoCI.setText(cliente.getCiCliente());
        campoNombre.setText(cliente.getNombreCliente());
        campoPrimerApellido.setText(cliente.getPrimerApellido());
        campoSegundoApellido.setText(cliente.getSegundoApellido());
        campoEdad.setText(String.valueOf(cliente.getEdad()));
        comboSexo.setValue(cliente.getSexo() == Sexo.MASCULINO ? "Masculino" : "Femenino");
        campoTelefono.setText(cliente.getNumeroContacto());
    }

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
     * Valida los campos de usuario (nombre, gmail, contraseña).
     * Acumula errores y devuelve true solo si todos son correctos.
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
        boolean correoValido = gmail.contains("@") && gmail.contains(".");
        if (!correoValido) {
            mostrarError("El correo electrónico no tiene un formato válido.");
        }

        return camposObligatorios && passwordValida && correoValido;
    }

    /**
     * Valida todos los campos del cliente, incluyendo formatos.
     * Acumula los resultados en una variable local y devuelve un único valor al final.
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

        boolean ciValido = ci.matches("\\d{11}");
        if (!ciValido) {
            mostrarError("El carnet de identidad debe tener exactamente 11 dígitos numéricos.");
        }

        int edad = parsearEntero(edadTexto, -1);
        boolean edadValida = edad >= 18 && edad <= 99;
        if (!edadValida) {
            mostrarError("La edad debe ser un número entre 18 y 99.");
        }

        boolean telefonoValido = telefono.matches("[56]\\d{7}");
        if (!telefonoValido) {
            mostrarError("El teléfono debe tener 8 dígitos y empezar con 5 o 6.");
        }


        return camposLlenos && ciValido && edadValida && telefonoValido;
    }

    // -------------------- Ejecución del guardado --------------------

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

    private void actualizarEntidades(boolean esAdmin) {
        if (!esAdmin) {
            actualizarClienteExistente();
        }
        actualizarUsuarioExistente();
    }

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

    @FXML
    private void onCancelar() {
        MainController.getInstance().onGoBack();
    }

    // -------------------------------------------------------------
    // Utilidades de campos
    // -------------------------------------------------------------

    private String obtenerTexto(TextField campo) {
        return campo.getText() != null ? campo.getText().trim() : "";
    }

    /**
     * Convierte una cadena a entero con un valor por defecto si falla.
     * Solo tiene un return al final.
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

    private void mostrarError(String mensaje) {
        AlertUtils.mostrarError(mensaje);
    }
}