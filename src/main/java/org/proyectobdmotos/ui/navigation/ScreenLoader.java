package org.proyectobdmotos.ui.navigation;

import java.io.IOException;

import org.proyectobdmotos.controller.*;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.ui.AppCompositionRoot;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.proyectobdmotos.controller.ReportesController;

/**
 * ScreenLoader: carga pantallas FXML y crea controllers con inyección de
 * dependencias.
 * Usa FXMLLoader.setControllerFactory para evitar singletons y `new` en lugares
 * arbitrarios.
 */
public final class ScreenLoader {

    private final AppCompositionRoot compositionRoot;
    private Object lastController;

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
                return new ClienteController(this,
                        compositionRoot.getClienteService(),
                        compositionRoot.getUsuarioService(),
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

            if (controllerClass == LoginController.class) {
                return new LoginController(this, compositionRoot.getUsuarioService());
            }

            if (controllerClass == MainController.class) {
                return new MainController(this);
            }

            if (controllerClass == RegistroController.class) {
                return new RegistroController(this,
                        compositionRoot.getUsuarioService(),
                        compositionRoot.getClienteService(),
                        compositionRoot.getReferenceDataStore());
            }

            if (controllerClass == ContratoFormController.class) {
                return new ContratoFormController(
                        compositionRoot.getContratoService(),
                        compositionRoot.getClienteService(),
                        compositionRoot.getMotoService());
            }

            if (controllerClass == TerminosController.class) {
                return new TerminosController(this);
            }

            if (controllerClass == ReportesController.class) {
                return new ReportesController(
                        compositionRoot.getClienteService(),
                        compositionRoot.getMotoService(),
                        compositionRoot.getContratoService());
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

    public Object getLastController() {
        return lastController;
    }
}