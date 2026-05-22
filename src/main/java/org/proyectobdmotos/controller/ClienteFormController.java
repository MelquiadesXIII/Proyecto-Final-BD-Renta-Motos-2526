package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.proyectobdmotos.models.*;
import org.proyectobdmotos.services.*;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.services.exceptions.ValidationException;

public class ClienteFormController {

    // ========== SECCIÓN USUARIO ==========
    @FXML private TextField campoNombreUsuario;
    @FXML private PasswordField campoPassword;
    @FXML private TextField campoGmail;
    @FXML private CheckBox checkEsAdmin;

    // ========== SECCIÓN CLIENTE ==========
    @FXML private TextField campoCI;
    @FXML private TextField campoNombre;
    @FXML private TextField campoPrimerApellido;
    @FXML private TextField campoSegundoApellido;
    @FXML private TextField campoEdad;
    @FXML private ComboBox<String> comboSexo;
    @FXML private TextField campoTelefono;
    @FXML private ComboBox<String> comboMunicipio;

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

        comboMunicipio.getItems().addAll(
            "Playa", "Plaza de la Revolución", "Centro Habana",
            "La Habana Vieja", "Regla", "La Habana del Este",
            "Guanabacoa", "San Miguel del Padrón", "Diez de Octubre",
            "Cerro", "Marianao", "La Lisa", "Boyeros",
            "Arroyo Naranjo", "Cotorro"
        );
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

        
        if (usuario != null) {
            campoNombreUsuario.setText(usuario.getNombreUsuario());
            campoGmail.setText(usuario.getGmail());
            checkEsAdmin.setSelected(usuario.isEsAdmin());
        }
    }

    @FXML
    private void onGuardar() {
        if (!validarCampos()) return;

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

        // Actualizar datos del cliente
        clienteEditando.setCiCliente(campoCI.getText().trim());
        clienteEditando.setNombreCliente(campoNombre.getText().trim());
        clienteEditando.setPrimerApellido(campoPrimerApellido.getText().trim());
        clienteEditando.setSegundoApellido(campoSegundoApellido.getText().trim());
        clienteEditando.setEdad(Integer.parseInt(campoEdad.getText().trim()));
        clienteEditando.setSexo(comboSexo.getValue().equals("Masculino") ? Sexo.MASCULINO : Sexo.FEMENINO);
        clienteEditando.setNumeroContacto(campoTelefono.getText().trim());

        clienteService.actualizarCliente(clienteEditando);
    }

    private Cliente construirClienteDesdeCampos() {
        return new Cliente(
            null,
            campoCI.getText().trim(),
            campoNombre.getText().trim(),
            campoPrimerApellido.getText().trim(),
            campoSegundoApellido.getText().trim(),
            Integer.parseInt(campoEdad.getText().trim()),
            comboSexo.getValue().equals("Masculino") ? Sexo.MASCULINO : Sexo.FEMENINO,
            campoTelefono.getText().trim(),
            null 
        );
    }

    // ========== ACCIÓN CANCELAR ==========
    @FXML
    private void onCancelar() {
        cerrarVentana();
    }

    // ========== MÉTODOS AUXILIARES ==========
    private boolean validarCampos() {
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
            mostrarError("Todos los campos obligatorios deben estar completos.");
            return false;
        }
        return true;
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