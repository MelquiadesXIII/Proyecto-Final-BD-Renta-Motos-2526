package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.proyectobdmotos.models.*;
import org.proyectobdmotos.services.*;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.ui.navigation.ScreenLoader;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.exceptions.BusinessException;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class RegistroController {

    @FXML private TextField campoNombreUsuario, campoGmail, campoCI, campoNombreCliente,
                           campoPrimerApellido, campoSegundoApellido, campoEdad, campoTelefono;
    @FXML private PasswordField campoPassword, campoConfirmarPassword;
    @FXML private ComboBox<String> comboSexo;      
    @FXML private ComboBox<String> comboMunicipio; 
    @FXML private CheckBox checkTerminos;

    private final ScreenLoader screenLoader;
    private final UsuarioService usuarioService;
    private final ClienteService clienteService;
    private final ReferenceDataStore referenceDataStore;

    public RegistroController(ScreenLoader screenLoader,
                              UsuarioService usuarioService,
                              ClienteService clienteService,
                              ReferenceDataStore referenceDataStore) {
        this.screenLoader = screenLoader;
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
        this.referenceDataStore = referenceDataStore;
    }

    @FXML
    private void initialize() {
        comboSexo.getItems().addAll("Masculino", "Femenino");
    }

    @FXML
    private void registrar() {
        // Validaciones básicas
        if (campoNombreUsuario.getText().trim().isEmpty() ||
            campoGmail.getText().trim().isEmpty() ||
            campoPassword.getText().trim().isEmpty() ||
            campoCI.getText().trim().isEmpty() ||
            campoNombreCliente.getText().trim().isEmpty() ||
            campoPrimerApellido.getText().trim().isEmpty() ||
            campoEdad.getText().trim().isEmpty() ||
            campoTelefono.getText().trim().isEmpty() ||
            comboSexo.getValue() == null ||
            comboMunicipio.getValue() == null) {
            mostrarError("Todos los campos obligatorios (*) deben estar completos.");
            return;
        }

        if (!campoPassword.getText().equals(campoConfirmarPassword.getText())) {
            mostrarError("Las contraseñas no coinciden.");
            return;
        }

        if (!checkTerminos.isSelected()) {
            mostrarError("Debe aceptar los términos y condiciones.");
            return;
        }

        try {
            // 1. Crear usuario (contraseña en texto plano, por defecto no admin)
            Usuario nuevoUsuario = usuarioService.registrarUsuario(
                campoNombreUsuario.getText().trim(),
                campoPassword.getText().trim(),
                campoGmail.getText().trim()
            );

            // 2. Crear cliente asociado
            Cliente nuevoCliente = new Cliente();
            nuevoCliente.setCi(campoCI.getText().trim());
            nuevoCliente.setNombreCliente(campoNombreCliente.getText().trim());
            nuevoCliente.setPrimerApellido(campoPrimerApellido.getText().trim());
            nuevoCliente.setSegundoApellido(campoSegundoApellido.getText().trim());
            nuevoCliente.setEdad(Integer.parseInt(campoEdad.getText().trim()));
            nuevoCliente.setSexo(comboSexo.getValue().equals("Masculino") ? Sexo.MASCULINO : Sexo.FEMENINO);
            nuevoCliente.setNumeroContacto(campoTelefono.getText().trim());
            nuevoCliente.setMunicipio(comboMunicipio.getValue());
            nuevoCliente.setIdUsuario(nuevoUsuario.getId());  // enlace

            clienteService.crearCliente(nuevoCliente);

            mostrarInfo("Registro exitoso", "La cuenta se ha creado correctamente. Ya puede iniciar sesión.");
            volverAlLogin();

        } catch (BusinessException e) {
            mostrarError(e.getMessage());
        } catch (NumberFormatException e) {
            mostrarError("La edad debe ser un número válido.");
        } catch (Exception e) {
            Logger.logError("Error en registro: " + e.getMessage());
            mostrarError("Error inesperado al crear la cuenta.");
        }
    }

    @FXML
    private void mostrarTerminos() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Términos y Condiciones");
        alert.setHeaderText("Condiciones de uso del sistema de renta de motos");
        alert.setContentText("Aquí el texto completo de los términos y condiciones...");
        alert.showAndWait();
    }

    private void volverAlLogin() {
        try {
            Parent loginRoot = screenLoader.load("/fxml/login.fxml");
            Stage stage = (Stage) campoNombreUsuario.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
        } catch (IOException e) {
            Logger.logError("Error al volver al login: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}