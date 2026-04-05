package org.proyectobdmotos.ui.navigation;

import java.io.IOException;

import org.proyectobdmotos.controller.ClienteController;
import org.proyectobdmotos.controller.ContratoController;
import org.proyectobdmotos.controller.MotoController;
import org.proyectobdmotos.ui.AppCompositionRoot;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * ScreenLoader: carga pantallas FXML y crea controllers con inyección de dependencias.
 * Usa FXMLLoader.setControllerFactory para evitar singletons y `new` en lugares arbitrarios.
 */
public final class ScreenLoader {

    private final AppCompositionRoot compositionRoot;

    public ScreenLoader(AppCompositionRoot compositionRoot) {
        this.compositionRoot = compositionRoot;
    }

    /**
     * Carga una pantalla FXML y retorna el Parent para mostrarlo en un Stage o Scene.
     *
     * @param fxmlPath Ruta relativa al classpath (ej: "/fxml/clientes.fxml")
     * @return Parent cargado con su controller inyectado
     * @throws IOException si el archivo FXML no existe o tiene errores
     */
    public Parent load(String fxmlPath) throws IOException {
        System.out.println("[ScreenLoader] Cargando pantalla: " + fxmlPath);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

        // Configurar controllerFactory: permite inyectar dependencias en constructores
        loader.setControllerFactory(controllerClass -> {
            System.out.println("[ScreenLoader]   → Creando controller: " + controllerClass.getSimpleName());

            // Mapeo de controllers conocidos con sus dependencias
            if (controllerClass == ClienteController.class) {
                return new ClienteController(
                    compositionRoot.getClienteService(),
                    compositionRoot.getAgenciaStore(),
                    compositionRoot.getReferenceDataStore()
                );
            }

            if (controllerClass == MotoController.class) {
                return new MotoController(
                    compositionRoot.getMotoService(),
                    compositionRoot.getAgenciaStore(),
                    compositionRoot.getReferenceDataStore()
                );
            }

            if (controllerClass == ContratoController.class) {
                return new ContratoController(
                    compositionRoot.getContratoService(),
                    compositionRoot.getAgenciaStore(),
                    compositionRoot.getReferenceDataStore()
                );
            }

            // Si llega aquí, el controller no está registrado
            throw new IllegalStateException(
                "Controller desconocido: " + controllerClass.getName() +
                ". Agrégalo al ScreenLoader.setControllerFactory"
            );
        });

        Parent root = loader.load();
        System.out.println("[ScreenLoader] ✓ Pantalla cargada: " + fxmlPath + "\n");

        return root;
    }
}
