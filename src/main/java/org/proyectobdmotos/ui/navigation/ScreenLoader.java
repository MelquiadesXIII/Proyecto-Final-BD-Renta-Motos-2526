package org.proyectobdmotos.ui.navigation;

import java.io.IOException;

import org.proyectobdmotos.controller.ClienteController;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.controller.ContratoController;
import org.proyectobdmotos.controller.LoginController;
import org.proyectobdmotos.controller.MainController;
import org.proyectobdmotos.controller.MotoController;
import org.proyectobdmotos.controller.RegistroController;
import org.proyectobdmotos.controller.TerminosController;
import org.proyectobdmotos.ui.AppCompositionRoot;
import org.proyectobdmotos.ui.navigation.NavigationHistory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * ScreenLoader: carga pantallas FXML y crea controllers con inyección de
 * dependencias.
 * Usa FXMLLoader.setControllerFactory para evitar singletons y `new` en lugares
 * arbitrarios.
 */
public final class ScreenLoader {

    private final AppCompositionRoot compositionRoot;

    public ScreenLoader(AppCompositionRoot compositionRoot) {
        this.compositionRoot = compositionRoot;
    }

    /**
     * Carga una pantalla FXML y retorna el Parent para mostrarlo en un Stage o
     * Scene.
     *
     * @param fxmlPath Ruta relativa al classpath (ej: "/fxml/clientes.fxml")
     * @return Parent cargado con su controller inyectado
     * @throws IOException si el archivo FXML no existe o tiene errores
     */
    public Parent load(String fxmlPath) throws IOException {
        Logger.log("Cargando pantalla: " + fxmlPath);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

        // Configurar controllerFactory: permite inyectar dependencias en constructores
        loader.setControllerFactory(controllerClass -> {
            Logger.log("→ Creando controller: " + controllerClass.getSimpleName());

            // Mapeo de controllers conocidos con sus dependencias
            if (controllerClass == ClienteController.class) {
                return new ClienteController(
                        compositionRoot.getClienteService(),
                        compositionRoot.getAgenciaStore(),
                        compositionRoot.getReferenceDataStore());
            }

            if (controllerClass == MotoController.class) {
                return new MotoController(
                        compositionRoot.getMotoService(),
                        compositionRoot.getAgenciaStore(),
                        compositionRoot.getReferenceDataStore());
            }

            if (controllerClass == ContratoController.class) {
                return new ContratoController(
                        compositionRoot.getContratoService(),
                        compositionRoot.getAgenciaStore(),
                        compositionRoot.getReferenceDataStore());
            }

            if (controllerClass == TerminosController.class) {
                return new TerminosController(this);
            }

            if (controllerClass == LoginController.class) {
                return new LoginController(this, compositionRoot.getUsuarioService());
            }

            if (controllerClass == MainController.class) {
                return new MainController(this);
            }

            if (controllerClass == LoginController.class) {
                return new LoginController(this, compositionRoot.getUsuarioService());
            }

            if (controllerClass == RegistroController.class) {
                return new RegistroController(this,
                        compositionRoot.getUsuarioService(),
                        compositionRoot.getClienteService(),
                        compositionRoot.getReferenceDataStore());
            }

            // Si llega aquí, el controller no está registrado
            throw new IllegalStateException(
                    "Controller desconocido: " + controllerClass.getName() +
                            ". Agrégalo al ScreenLoader.setControllerFactory");
        });

        Parent root = loader.load();
        Logger.logInfo("✓ Pantalla cargada: " + fxmlPath + "\n");

        return root;
    }

    private void goToRegister() {
    try {
        NavigationHistory.push("/fxml/login.fxml");   // guardamos login
        Parent registerRoot = screenLoader.load("/fxml/registro.fxml");
        Scene scene = new Scene(registerRoot);
        scene.getStylesheets().add(getClass().getResource("/styles/register.css").toExternalForm());
        Stage stage = (Stage) campoUsuario.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Crear cuenta");
    } catch (IOException e) {
        Logger.logError("Error al cargar registro: " + e.getMessage());
        mostrarError("Error", "No se pudo abrir el registro.");
    }
}

@FXML
private void goToTerms() {
    try {
        NavigationHistory.push("/fxml/login.fxml");   // guardamos login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/terminos.fxml"));
        Parent termsRoot = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(termsRoot));
        stage.setTitle("Términos y Condiciones");
        stage.show();
    } catch (IOException e) {
        Logger.logError("Error al cargar términos: " + e.getMessage());
    }
}
}
