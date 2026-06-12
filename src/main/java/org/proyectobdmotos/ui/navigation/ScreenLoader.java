package org.proyectobdmotos.ui.navigation;

import java.io.IOException;

import org.proyectobdmotos.controller.*;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.ui.AppCompositionRoot;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;


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

        loader.setControllerFactory(controllerClass -> {
            Logger.log("→ Creando controller: " + controllerClass.getSimpleName());

            // ========== ADMIN / LISTADOS ==========
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

            // ========== FORMULARIOS ==========
            if (controllerClass == ClienteFormController.class) {
                return new ClienteFormController(
                        compositionRoot.getClienteService(),
                        compositionRoot.getUsuarioService(),
                        compositionRoot.getReferenceDataStore());
            }

            if (controllerClass == ContratoUsuarioFormController.class) {
                return new ContratoUsuarioFormController(
                        compositionRoot.getContratoService(),
                        compositionRoot.getMotoService(),
                        compositionRoot.getAgenciaStore());
            }

            if (controllerClass == MotoFormController.class) {
                return new MotoFormController(
                        compositionRoot.getMotoService(),
                        compositionRoot.getAgenciaStore(),
                        compositionRoot.getReferenceDataStore(),
                        compositionRoot.getMarcaService(),
                        compositionRoot.getModeloService());
            }

            if (controllerClass == ContratoFormController.class) {
                return new ContratoFormController(
                        compositionRoot.getContratoService(),
                        compositionRoot.getClienteService(),
                        compositionRoot.getMotoService());
            }

            if (controllerClass == MarcaFormController.class) {
                return new MarcaFormController(compositionRoot.getMarcaService());
            }

            if (controllerClass == ModeloFormController.class) {
                return new ModeloFormController(
                        compositionRoot.getMotoService(),
                        compositionRoot.getMarcaService(),
                        compositionRoot.getModeloService());
            }

            if (controllerClass == EditarMarcaModeloController.class) {
                return new EditarMarcaModeloController(
                        compositionRoot.getModeloService(),
                        compositionRoot.getMarcaService(),
                        compositionRoot.getMotoService());
            }

            if (controllerClass == EliminarMarcaModeloController.class) {
                return new EliminarMarcaModeloController(
                        compositionRoot.getModeloService(),
                        compositionRoot.getMarcaService());
            }

            // ========== NAVEGACIÓN PRINCIPAL ==========
            if (controllerClass == LoginController.class) {
                return new LoginController(this,
                        compositionRoot.getUsuarioService(),
                        compositionRoot.getAgenciaStore(),
                        compositionRoot.getClienteService());
            }

            if (controllerClass == MainController.class) {
                return new MainController(this);
            }

            if (controllerClass == UserMainController.class) {
                return new UserMainController(this);
            }

            if (controllerClass == RegistroController.class) {
                return new RegistroController(this,
                        compositionRoot.getUsuarioService(),
                        compositionRoot.getClienteService(),
                        compositionRoot.getReferenceDataStore(),
                        compositionRoot.getAgenciaStore());
            }

            // ========== PANTALLAS DEL USUARIO ==========
            if (controllerClass == BienvenidoUsuarioController.class) {
                return new BienvenidoUsuarioController(compositionRoot.getAgenciaStore());
            }

            if (controllerClass == MisContratosController.class) {
                return new MisContratosController(
                        compositionRoot.getMotoService(),
                        compositionRoot.getContratoService(),
                        compositionRoot.getAgenciaStore());
            }

            if (controllerClass == PerfilController.class) {
                return new PerfilController(compositionRoot.getAgenciaStore());
            }

            // ========== AYUDA ==========
            if (controllerClass == AyudaController.class) {
                return new AyudaController();
            }

            if (controllerClass == BienvenidoAdminController.class) {
                return new BienvenidoAdminController(compositionRoot.getAgenciaStore());
            }

            // ========== OTROS ==========
            if (controllerClass == TerminosController.class) {
                return new TerminosController(this);
            }

            if (controllerClass == ReportesController.class) {
                return new ReportesController(
                        compositionRoot.getClienteService(),
                        compositionRoot.getMotoService(),
                        compositionRoot.getContratoService());
            }

            if (controllerClass == MarcasModelosController.class) {
                return new MarcasModelosController(compositionRoot.getModeloService());
            }

            if (controllerClass == InventarioController.class) {
                return new InventarioController(compositionRoot.getMotoService());
            }

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