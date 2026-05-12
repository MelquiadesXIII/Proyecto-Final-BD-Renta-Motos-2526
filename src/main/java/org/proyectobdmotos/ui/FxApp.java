package org.proyectobdmotos.ui;

import java.sql.SQLException;

import org.proyectobdmotos.utils.Logger;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * FxApp: punto de entrada de JavaFX.
 * Construye el composition root y carga la pantalla inicial.
 */
public class FxApp extends Application {

    private AppCompositionRoot compositionRoot;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Logger.log("Iniciando aplicación JavaFX...\n");

        // Construir el grafo de dependencias
        try {
            compositionRoot = new AppCompositionRoot();
        } catch (SQLException e) {
            Logger.logError("Error al conectar a la base de datos: " + e.getMessage());
            throw new RuntimeException("No se pudo inicializar la aplicación", e);
        }

        Logger.log("Configurando ventana principal...");

        Parent root;
        try {
            root = compositionRoot.getScreenLoader().load("/fxml/main.fxml");
        } catch (Exception e) {
            Logger.logError("Error cargando main.fxml: " + e.getMessage());
            throw new RuntimeException("No se pudo cargar la interfaz principal", e);
        }

        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        Scene scene = new Scene(root, screenWidth, screenHeight);

        scene.getStylesheets().addAll(
                getClass().getResource("/styles/app.css").toExternalForm(),
                getClass().getResource("/styles/default.css").toExternalForm());

        primaryStage.setTitle("Renta Motos - Sistema de Gestión");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    @Override
    public void stop() {
        Logger.log("\nCerrando aplicación...");
    }
}
