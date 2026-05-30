package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.proyectobdmotos.models.*;
import org.proyectobdmotos.services.*;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.services.exceptions.ValidationException;

import java.util.List;

public class ClienteFormController {

    // ========== SECCIÓN USUARIO ==========
    @FXML
    private TextField campoNombreUsuario;
    @FXML
    private PasswordField campoPassword;
    @FXML
    private TextField campoGmail;
    @FXML
    private CheckBox checkEsAdmin;

    // ========== SECCIÓN CLIENTE ==========
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
    private ComboBox<Municipio> comboMunicipio;     // ← Ahora maneja objetos Municipio

    // ========== DEPENDENCIAS ==========
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;
    private final ReferenceDataStore referenceDataStore;

    private boolean modoEdicion = false;
    private Cliente clienteEditando = null;
    private Usuario usuarioEditando = null;

    public ClienteFormController(ClienteService clienteService,
                                 UsuarioService usuarioService,
                                 ReferenceDataStore referenceDataStore) {
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
        this.referenceDataStore = referenceDataStore;
    }

    @FXML
    private void initialize() {
        comboSexo.getItems().addAll("Masculino", "Femenino");

        // Cargar municipios desde ReferenceDataStore
        List<Municipio> municipios = referenceDataStore.getMunicipios();
        comboMunicipio.getItems().setAll(municipios);

        // Configurar visualización del combo de municipio
        comboMunicipio.setCellFactory(param -> new ListCell<Municipio>() {
            @Override
            protected void updateItem(Municipio item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreMunicipio());
            }
        });
        comboMunicipio.setConverter(new StringConverter<Municipio>() {
            @Override
            public String toString(Municipio municipio) {
                return (municipio != null) ? municipio.getNombreMunicipio() : "";
            }

            @Override
            public Municipio fromString(String string) {
                return null;
            }
        });
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

        for (Municipio m : comboMunicipio.getItems()) {
            if (m.getIdMunicipio() == idMunicipio) {
                comboMunicipio.getSelectionModel().select(m);
            }
        }

        if (usuario != null) {
            campoNombreUsuario.setText(usuario.getNombreUsuario());
            campoGmail.setText(usuario.getGmail());
            checkEsAdmin.setSelected(usuario.isEsAdmin());
        }
    }

    @FXML
    private void onGuardar() {
        boolean camposValidos = validarCampos();
        if (camposValidos) {
            try {
                if (modoEdicion) {
                    actualizarClienteExistente();
                } else {
                    crearNuevoCliente();
                }
                cerrarVentana();
            } catch (ValidationException e) {
                mostrarError(e.getMessage());
            } catch (Exception e) {
                Logger.logError("Error en formulario de cliente: " + e.getMessage());
                mostrarError("Error inesperado al guardar.");
            }
        }
    }

    private void crearNuevoCliente() throws ValidationException {
        // 1. Crear usuario
        Usuario nuevoUsuario = usuarioService.registrarUsuarioConRol(
                campoNombreUsuario.getText().trim(),
                campoPassword.getText().trim(),
                campoGmail.getText().trim(),
                checkEsAdmin.isSelected()
        );

        // 2. Construir cliente desde los campos
        Cliente nuevoCliente = construirClienteDesdeCampos();
        nuevoCliente.setIdUsuario(nuevoUsuario.getId());

        // 3. Guardar cliente
        clienteService.crearCliente(nuevoCliente);
    }

    private void actualizarClienteExistente() throws ValidationException {
        if (clienteEditando == null) return;

        clienteEditando.setCiCliente(campoCI.getText().trim());
        clienteEditando.setNombreCliente(campoNombre.getText().trim());
        clienteEditando.setPrimerApellido(campoPrimerApellido.getText().trim());
        clienteEditando.setSegundoApellido(campoSegundoApellido.getText().trim());
        clienteEditando.setEdad(Integer.parseInt(campoEdad.getText().trim()));
        clienteEditando.setSexo(comboSexo.getValue().equals("Masculino") ? Sexo.MASCULINO : Sexo.FEMENINO);
        clienteEditando.setNumeroContacto(campoTelefono.getText().trim());
        clienteEditando.setIdMunicipio(comboMunicipio.getValue().getIdMunicipio());

        clienteService.actualizarCliente(clienteEditando);
    }

    private Cliente construirClienteDesdeCampos() {
        Cliente nuevo = new Cliente();
        nuevo.setCiCliente(campoCI.getText().trim());
        nuevo.setNombreCliente(campoNombre.getText().trim());
        nuevo.setPrimerApellido(campoPrimerApellido.getText().trim());
        nuevo.setSegundoApellido(campoSegundoApellido.getText().trim());
        nuevo.setEdad(Integer.parseInt(campoEdad.getText().trim()));
        nuevo.setSexo(comboSexo.getValue().equals("Masculino") ? Sexo.MASCULINO : Sexo.FEMENINO);
        nuevo.setNumeroContacto(campoTelefono.getText().trim());
        nuevo.setIdMunicipio(comboMunicipio.getValue().getIdMunicipio());
        return nuevo;
    }

    @FXML
    private void onCancelar() {
        cerrarVentana();
    }

    // ========== MÉTODOS AUXILIARES ==========

    private boolean validarCampos() {
        boolean hayError = false;
        String mensaje = "Todos los campos obligatorios deben estar completos.";

        if (campoNombreUsuario.getText().trim().isEmpty() ||
                campoPassword.getText().trim().isEmpty() ||
                campoGmail.getText().trim().isEmpty() ||
                campoCI.getText().trim().isEmpty() ||
                campoNombre.getText().trim().isEmpty() ||
                campoPrimerApellido.getText().trim().isEmpty() ||
                campoEdad.getText().trim().isEmpty() ||
                campoTelefono.getText().trim().isEmpty() ||
                comboSexo.getValue() == null ||
                comboMunicipio.getValue() == null) {
            hayError = true;
        }

        if (hayError) {
            mostrarError(mensaje);
        }
        return !hayError;
    }

    private void cerrarVentana() {
        Stage stage = (Stage) campoCI.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje);
        alert.showAndWait();
    }
}