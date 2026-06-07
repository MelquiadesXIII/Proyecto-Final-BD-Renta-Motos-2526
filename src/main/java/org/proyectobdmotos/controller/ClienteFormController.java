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

    private final ClienteService clienteService;
    private final UsuarioService usuarioService;

    private boolean modoEdicion = false;
    private Cliente clienteEditando = null;
    private Usuario usuarioEditando = null;

    private static Cliente clienteAEditarStatic;
    private static Usuario usuarioAEditarStatic;

    public static void setClienteAEditarStatic(Cliente c) { clienteAEditarStatic = c; }
    public static void setUsuarioAEditarStatic(Usuario u) { usuarioAEditarStatic = u; }

    public ClienteFormController(ClienteService clienteService,
                                 UsuarioService usuarioService,
                                 ReferenceDataStore referenceDataStore) {
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
    }

    @FXML
    private void initialize() {
        comboSexo.getItems().addAll("Masculino", "Femenino");

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

        checkEsAdmin.selectedProperty().addListener((obs, oldVal, newVal) -> {
            boolean visible = !newVal;
            gridCliente.setVisible(visible);
            gridCliente.setManaged(visible);
        });
        if (checkEsAdmin.isSelected()) {
            gridCliente.setVisible(false);
            gridCliente.setManaged(false);
        }

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

        if (clienteAEditarStatic != null) {
            setModoEdicion(clienteAEditarStatic, usuarioAEditarStatic);
            clienteAEditarStatic = null;
            usuarioAEditarStatic = null;
        }
    }

    public void setModoEdicion(Cliente cliente, Usuario usuario) {
        this.modoEdicion = true;
        this.clienteEditando = cliente;
        this.usuarioEditando = usuario;

        campoCI.setText(cliente.getCiCliente());
        campoNombre.setText(cliente.getNombreCliente());
        campoPrimerApellido.setText(cliente.getPrimerApellido());
        campoSegundoApellido.setText(cliente.getSegundoApellido());
        campoEdad.setText(String.valueOf(cliente.getEdad()));
        comboSexo.setValue(cliente.getSexo() == Sexo.MASCULINO ? "Masculino" : "Femenino");
        campoTelefono.setText(cliente.getNumeroContacto());

        int idMunicipio = cliente.getIdMunicipio();
        boolean encontrado = false;
        int i = 0;
        while (!encontrado && i < comboMunicipio.getItems().size()) {
            Municipio m = comboMunicipio.getItems().get(i);
            if (m.getIdMunicipio() == idMunicipio) {
                comboMunicipio.getSelectionModel().select(m);
                encontrado = true;
            }
            i++;
        }

        if (usuario != null) {
            campoNombreUsuario.setText(usuario.getNombreUsuario() != null ? usuario.getNombreUsuario() : "");
            campoGmail.setText(usuario.getGmail() != null ? usuario.getGmail() : "");
            checkEsAdmin.setSelected(usuario.isEsAdmin());
            boolean visible = !usuario.isEsAdmin();
            gridCliente.setVisible(visible);
            gridCliente.setManaged(visible);

            campoPassword.clear();
            campoPasswordVisible.setText(usuario.getPassword() != null ? usuario.getPassword() : "");
        }
    }

    @FXML
    private void onGuardar() {
        boolean esAdmin = checkEsAdmin.isSelected();
        boolean camposUsuarioValidos = true;
        boolean camposClienteValidos = true;

        String nombreUsuario = campoNombreUsuario.getText() != null ? campoNombreUsuario.getText().trim() : "";
        String gmail = campoGmail.getText() != null ? campoGmail.getText().trim() : "";
        String password = campoPassword.getText() != null ? campoPassword.getText().trim() : "";

        if (nombreUsuario.isEmpty() || gmail.isEmpty()) {
            mostrarError("Los campos de la cuenta (usuario, gmail) son obligatorios.");
            camposUsuarioValidos = false;
        }
        if (!modoEdicion && password.isEmpty()) {
            mostrarError("La contraseña es obligatoria para un nuevo usuario.");
            camposUsuarioValidos = false;
        }

        if (!esAdmin && camposUsuarioValidos) {
            String ci = campoCI.getText() != null ? campoCI.getText().trim() : "";
            String nombre = campoNombre.getText() != null ? campoNombre.getText().trim() : "";
            String primerApellido = campoPrimerApellido.getText() != null ? campoPrimerApellido.getText().trim() : "";
            String edadTexto = campoEdad.getText() != null ? campoEdad.getText().trim() : "";
            String telefono = campoTelefono.getText() != null ? campoTelefono.getText().trim() : "";

            if (ci.isEmpty() || nombre.isEmpty() || primerApellido.isEmpty() ||
                    edadTexto.isEmpty() || telefono.isEmpty() ||
                    comboSexo.getValue() == null || comboMunicipio.getValue() == null) {
                mostrarError("Todos los campos del cliente son obligatorios.");
                camposClienteValidos = false;
            }
        }

        boolean puedeGuardar = camposUsuarioValidos && (esAdmin || camposClienteValidos);

        if (puedeGuardar) {
            try {
                if (modoEdicion) {
                    if (!esAdmin) actualizarClienteExistente();
                    actualizarUsuarioExistente();
                } else {
                    Usuario nuevoUsuario = usuarioService.registrarUsuarioConRol(
                            nombreUsuario,
                            password,
                            gmail,
                            esAdmin
                    );
                    if (!esAdmin) {
                        Cliente nuevoCliente = construirClienteDesdeCampos();
                        nuevoCliente.setIdUsuario(nuevoUsuario.getId());
                        clienteService.crearCliente(nuevoCliente);
                    }
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
    }

    private void actualizarClienteExistente() throws ValidationException {
        if (clienteEditando != null) {
            clienteEditando.setCiCliente(campoCI.getText() != null ? campoCI.getText().trim() : "");
            clienteEditando.setNombreCliente(campoNombre.getText() != null ? campoNombre.getText().trim() : "");
            clienteEditando.setPrimerApellido(campoPrimerApellido.getText() != null ? campoPrimerApellido.getText().trim() : "");
            clienteEditando.setSegundoApellido(campoSegundoApellido.getText() != null ? campoSegundoApellido.getText().trim() : "");
            clienteEditando.setEdad(Integer.parseInt(campoEdad.getText() != null ? campoEdad.getText().trim() : "0"));
            clienteEditando.setSexo(comboSexo.getValue().equals("Masculino") ? Sexo.MASCULINO : Sexo.FEMENINO);
            clienteEditando.setNumeroContacto(campoTelefono.getText() != null ? campoTelefono.getText().trim() : "");
            clienteEditando.setIdMunicipio(comboMunicipio.getValue().getIdMunicipio());
            clienteService.actualizarCliente(clienteEditando);
        }
    }

    private void actualizarUsuarioExistente() {
        if (usuarioEditando != null) {
            Integer idUsuario = usuarioEditando.getId();
            if (idUsuario != null && idUsuario > 0) {
                usuarioEditando.setNombreUsuario(campoNombreUsuario.getText() != null ? campoNombreUsuario.getText().trim() : "");
                usuarioEditando.setGmail(campoGmail.getText() != null ? campoGmail.getText().trim() : "");
                usuarioEditando.setEsAdmin(checkEsAdmin.isSelected());

                String nuevaPassword = campoPassword.getText() != null ? campoPassword.getText().trim() : "";
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

    private Cliente construirClienteDesdeCampos() {
        Cliente nuevo = new Cliente();
        nuevo.setCiCliente(campoCI.getText() != null ? campoCI.getText().trim() : "");
        nuevo.setNombreCliente(campoNombre.getText() != null ? campoNombre.getText().trim() : "");
        nuevo.setPrimerApellido(campoPrimerApellido.getText() != null ? campoPrimerApellido.getText().trim() : "");
        nuevo.setSegundoApellido(campoSegundoApellido.getText() != null ? campoSegundoApellido.getText().trim() : "");
        nuevo.setEdad(Integer.parseInt(campoEdad.getText() != null ? campoEdad.getText().trim() : "0"));
        nuevo.setSexo(comboSexo.getValue().equals("Masculino") ? Sexo.MASCULINO : Sexo.FEMENINO);
        nuevo.setNumeroContacto(campoTelefono.getText() != null ? campoTelefono.getText().trim() : "");
        nuevo.setIdMunicipio(comboMunicipio.getValue().getIdMunicipio());
        return nuevo;
    }

    @FXML
    private void onCancelar() {
        MainController.getInstance().onGoBack();
    }

    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje).showAndWait();
    }
}