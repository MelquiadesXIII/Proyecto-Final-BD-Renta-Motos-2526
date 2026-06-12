package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.proyectobdmotos.models.*;
import org.proyectobdmotos.services.*;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.ui.navigation.NavigationHistory;
import org.proyectobdmotos.ui.navigation.ScreenLoader;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.services.exceptions.ValidationException;
import org.proyectobdmotos.utils.ScreenUtils;
import org.proyectobdmotos.utils.TermsWindow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegistroController {

    @FXML private TextField campoNombreUsuario, campoGmail, campoCI, campoNombreCliente,
            campoPrimerApellido, campoSegundoApellido, campoEdad, campoTelefono;
    @FXML private PasswordField campoPassword, campoConfirmarPassword;
    @FXML private ComboBox<String> comboSexo;
    @FXML private ComboBox<String> comboMunicipio;
    @FXML private CheckBox checkTerminos;
    @FXML private ImageView fondoRegistro;

    private final ScreenLoader screenLoader;
    private final UsuarioService usuarioService;
    private final ClienteService clienteService;
    private final ReferenceDataStore referenceDataStore;
    private final AgenciaStore agenciaStore;                     // ← añadido

    private static final Map<String, Integer> municipiosMap = new HashMap<>();
    static {
        municipiosMap.put("Playa", 1);
        municipiosMap.put("Plaza de la Revolución", 2);
        municipiosMap.put("Centro Habana", 3);
        municipiosMap.put("La Habana Vieja", 4);
        municipiosMap.put("Regla", 5);
        municipiosMap.put("La Habana del Este", 6);
        municipiosMap.put("Guanabacoa", 7);
        municipiosMap.put("San Miguel del Padrón", 8);
        municipiosMap.put("Diez de Octubre", 9);
        municipiosMap.put("Cerro", 10);
        municipiosMap.put("Marianao", 11);
        municipiosMap.put("La Lisa", 12);
        municipiosMap.put("Boyeros", 13);
        municipiosMap.put("Arroyo Naranjo", 14);
        municipiosMap.put("Cotorro", 15);
    }

    public RegistroController(ScreenLoader screenLoader,
                              UsuarioService usuarioService,
                              ClienteService clienteService,
                              ReferenceDataStore referenceDataStore,
                              AgenciaStore agenciaStore) {
        this.screenLoader = screenLoader;
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
        this.referenceDataStore = referenceDataStore;
        this.agenciaStore = agenciaStore;
    }

    @FXML
    private void initialize() {
        comboSexo.getItems().addAll("Masculino", "Femenino");
        comboMunicipio.getItems().addAll(
                "Playa", "Plaza de la Revolución", "Centro Habana", "La Habana Vieja",
                "Regla", "La Habana del Este", "Guanabacoa", "San Miguel del Padrón",
                "Diez de Octubre", "Cerro", "Marianao", "La Lisa", "Boyeros",
                "Arroyo Naranjo", "Cotorro");
        if (fondoRegistro != null) {
            StackPane parent = (StackPane) fondoRegistro.getParent();
            fondoRegistro.fitWidthProperty().bind(parent.widthProperty());
            fondoRegistro.fitHeightProperty().bind(parent.heightProperty());
        }
    }

    @FXML
    private void registrar() {
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
        } else if (!campoPassword.getText().equals(campoConfirmarPassword.getText())) {
            mostrarError("Las contraseñas no coinciden.");
        } else if (!checkTerminos.isSelected()) {
            mostrarError("Debe aceptar los términos y condiciones.");
        } else {
            try {
                Usuario nuevoUsuario = usuarioService.registrarUsuario(
                        campoNombreUsuario.getText().trim(),
                        campoPassword.getText().trim(),
                        campoGmail.getText().trim());

                try {
                    Cliente nuevoCliente = new Cliente(
                            null,
                            campoCI.getText().trim(),
                            campoNombreCliente.getText().trim(),
                            campoPrimerApellido.getText().trim(),
                            campoSegundoApellido.getText().trim(),
                            Integer.parseInt(campoEdad.getText().trim()),
                            comboSexo.getValue().equals("Masculino") ? Sexo.MASCULINO : Sexo.FEMENINO,
                            campoTelefono.getText().trim(),
                            municipiosMap.getOrDefault(comboMunicipio.getValue(), -1));
                    nuevoCliente.setIdUsuario(nuevoUsuario.getId());
                    clienteService.crearCliente(nuevoCliente);

                    agenciaStore.setClienteActual(nuevoCliente);
                    irAPantallaUsuario(nuevoUsuario);
                } catch (Exception ex) {
                    usuarioService.eliminarUsuario(nuevoUsuario.getId());
                    throw ex;
                }
            } catch (ValidationException e) {
                e.printStackTrace();
                mostrarError(e.getMessage());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                mostrarError("La edad debe ser un número válido.");
            } catch (Exception e) {
                e.printStackTrace();
                Logger.logError("Error en registro: " + e.getMessage());
                mostrarError("Error inesperado al crear la cuenta.");
            }
        }
    }

    @FXML
    private void mostrarTerminos() {
        TermsWindow.show((Stage) campoNombreUsuario.getScene().getWindow());
    }

    @FXML
    private void volverAlLogin() {
        Parent anterior = NavigationHistory.goBack(screenLoader);
        if (anterior != null) {
            Stage stage = (Stage) campoNombreUsuario.getScene().getWindow();
            stage.setScene(new Scene(anterior));
        } else {
            Logger.logInfo("No hay pantalla anterior para retroceder.");
        }
    }

    private void irAPantallaUsuario(Usuario usuario) {
        try {
            Parent userMainRoot = screenLoader.load("/fxml/user-main.fxml");
            Scene scene = new Scene(userMainRoot, ScreenUtils.getWidth(), ScreenUtils.getHeight());
            scene.getStylesheets().addAll(
                    getClass().getResource("/styles/app.css").toExternalForm(),
                    getClass().getResource("/styles/default.css").toExternalForm());
            Stage stage = (Stage) campoNombreUsuario.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Renta Motos - " + usuario.getNombreUsuario());
            stage.setMaximized(true);
            Logger.logInfo("Registro cliente exitoso: " + usuario.getNombreUsuario());
        } catch (IOException e) {
            Logger.logError("Error al cargar user-main.fxml: " + e.getMessage());
            mostrarError("No se pudo abrir la interfaz de usuario.");
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