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
import org.proyectobdmotos.utils.*;
import org.proyectobdmotos.services.exceptions.ValidationException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegistroController {

    @FXML private TextField campoNombreUsuario, campoGmail, campoCI, campoNombreCliente,
            campoPrimerApellido, campoSegundoApellido, campoTelefono;
    @FXML private PasswordField campoPassword, campoConfirmarPassword;
    @FXML private ComboBox<String> comboSexo;
    @FXML private ComboBox<String> comboMunicipio;
    @FXML private CheckBox checkTerminos;
    @FXML private ImageView fondoRegistro;

    private final ScreenLoader screenLoader;
    private final UsuarioService usuarioService;
    private final ClienteService clienteService;
    private final ReferenceDataStore referenceDataStore;
    private final AgenciaStore agenciaStore;

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

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

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

    // -----------------------------------------------------------------
    // Registro
    // -----------------------------------------------------------------

    @FXML
    private void registrar() {
        if (!validarCamposObligatorios()) {
            mostrarError("Todos los campos obligatorios (*) deben estar completos.");
        } else if (!campoPassword.getText().equals(campoConfirmarPassword.getText())) {
            mostrarError("Las contraseñas no coinciden.");
        } else if (!checkTerminos.isSelected()) {
            mostrarError("Debe aceptar los términos y condiciones.");
        } else {
            ejecutarRegistro();
        }
    }

    private boolean validarCamposObligatorios() {
        boolean nombreUsuarioVacio = campoNombreUsuario.getText().trim().isEmpty();
        boolean gmailVacio = campoGmail.getText().trim().isEmpty();
        boolean passwordVacio = campoPassword.getText().trim().isEmpty();
        boolean ciVacio = campoCI.getText().trim().isEmpty();
        boolean nombreClienteVacio = campoNombreCliente.getText().trim().isEmpty();
        boolean primerApellidoVacio = campoPrimerApellido.getText().trim().isEmpty();
        boolean telefonoVacio = campoTelefono.getText().trim().isEmpty();
        boolean sexoNulo = comboSexo.getValue() == null;
        boolean municipioNulo = comboMunicipio.getValue() == null;

        return !nombreUsuarioVacio && !gmailVacio && !passwordVacio && !ciVacio
                && !nombreClienteVacio && !primerApellidoVacio
                && !telefonoVacio && !sexoNulo && !municipioNulo;
    }

    // -----------------------------------------------------------------
    // Validaciones detalladas (usan exactamente los métodos de Validator)
    // -----------------------------------------------------------------

    private boolean validarClienteCompleto() {
        String ci = campoCI.getText().trim();
        String nombre = campoNombreCliente.getText().trim();
        String primerApellido = campoPrimerApellido.getText().trim();
        String segundoApellido = campoSegundoApellido.getText().trim();
        String telefono = campoTelefono.getText().trim();

        boolean ciValido = false;
        try {
            Validator.validateCI(ci);
            if (comboSexo.getValue() != null) {
                Validator.validateCISexo(ci, comboSexo.getValue());
            }
            ciValido = true;
        } catch (ValidationException e) {
            mostrarError(e.getMessage());
        }

        boolean edadValida = false;
        if (ciValido) {
            try {
                int edadCalculada = Validator.calcularEdadDesdeCI(ci);
                Validator.validateAge(edadCalculada);
                edadValida = true;
            } catch (ValidationException e) {
                mostrarError("Edad calculada desde el CI: " + e.getMessage());
            }
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

        boolean segundoApellidoValido = true;
        if (!segundoApellido.isEmpty()) {
            try {
                Validator.validateText(segundoApellido);
            } catch (ValidationException e) {
                mostrarError("Segundo apellido: " + e.getMessage());
                segundoApellidoValido = false;
            }
        }

        boolean telefonoValido = false;
        try {
            Validator.validateTelephoneNumber(telefono);
            telefonoValido = true;
        } catch (ValidationException e) {
            mostrarError(e.getMessage());
        }

        boolean ciUnico = true;
        if (ciValido) {
            try {
                Validator.validateUniqueField("ci", ci);
            } catch (ValidationException e) {
                mostrarError(e.getMessage());
                ciUnico = false;
            }
        }

        return ciValido && edadValida && nombreValido && apellidoValido
                && segundoApellidoValido && telefonoValido && ciUnico;
    }

    private boolean validarUsuario() {
        String nombreUsuario = campoNombreUsuario.getText().trim();
        String gmail = campoGmail.getText().trim();
        String password = campoPassword.getText().trim();

        boolean camposObligatorios = !nombreUsuario.isEmpty() && !gmail.isEmpty();
        if (!camposObligatorios) {
            mostrarError("Los campos de la cuenta (usuario y gmail) son obligatorios.");
        }

        boolean passwordValida = !password.isEmpty();
        if (!passwordValida) {
            mostrarError("La contraseña es obligatoria.");
        }

        boolean longitudPassword = password.length() >= 4;
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

        boolean unicidadValida = true;
        if (camposObligatorios && usuarioFormato && correoValido) {
            try {
                usuarioService.verificarUnicidadRegistro(nombreUsuario, gmail);
            } catch (ValidationException e) {
                mostrarError(e.getMessage());
                unicidadValida = false;
            }
        }

        return camposObligatorios && passwordValida && longitudPassword
                && usuarioFormato && correoValido && unicidadValida;
    }

    // -----------------------------------------------------------------
    // Ejecución del registro
    // -----------------------------------------------------------------

    private void ejecutarRegistro() {
        if (!validarUsuario() || !validarClienteCompleto()) {
            return;
        }

        try {
            Usuario nuevoUsuario = usuarioService.registrarUsuario(
                    campoNombreUsuario.getText().trim(),
                    campoPassword.getText().trim(),
                    campoGmail.getText().trim());

            try {
                Cliente nuevoCliente = construirClienteDesdeCampos();
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
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error en registro: " + e.getMessage());
            mostrarError("Error inesperado al crear la cuenta.");
        }
    }

    private Cliente construirClienteDesdeCampos() {
        String ci = campoCI.getText().trim();
        String segundoApellido = campoSegundoApellido.getText().trim();
        Cliente nuevo = new Cliente();
        nuevo.setCiCliente(ci);
        nuevo.setNombreCliente(campoNombreCliente.getText().trim());
        nuevo.setPrimerApellido(campoPrimerApellido.getText().trim());
        nuevo.setSegundoApellido(segundoApellido.isEmpty() ? null : segundoApellido);
        nuevo.setEdad(Validator.calcularEdadDesdeCI(ci));
        nuevo.setSexo(comboSexo.getValue().equals("Masculino") ? Sexo.MASCULINO : Sexo.FEMENINO);
        nuevo.setNumeroContacto(campoTelefono.getText().trim());
        nuevo.setIdMunicipio(municipiosMap.getOrDefault(comboMunicipio.getValue(), -1));
        return nuevo;
    }

    // -----------------------------------------------------------------
    // Navegación
    // -----------------------------------------------------------------

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
            e.printStackTrace();
            Logger.logError("Error al cargar user-main.fxml: " + e.getMessage());
            mostrarError("No se pudo abrir la interfaz de usuario.");
        }
    }

    // -----------------------------------------------------------------
    // Alertas
    // -----------------------------------------------------------------

    private void mostrarError(String mensaje) {
        AlertUtils.mostrarError(mensaje);
    }
}