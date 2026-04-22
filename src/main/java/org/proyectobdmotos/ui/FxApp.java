package org.proyectobdmotos.ui;

import java.sql.SQLException;

import org.proyectobdmotos.utils.Logger;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * FxApp: punto de entrada de JavaFX.
 * Construye el composition root y carga la pantalla inicial.
 */
public class FxApp extends Application {

    @SuppressWarnings("unused")
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

        // Cargar pantalla inicial (placeholder: aquí se cargará una pantalla real cuando exista FXML)
        // Por ahora, mostramos una ventana vacía para verificar que todo funciona
        Logger.log("Configurando ventana principal...");
        
        // TODO: Reemplazar con carga de pantalla real cuando exista un FXML
        // Parent root = compositionRoot.getScreenLoader().load("/fxml/main.fxml");
        
        // Placeholder: crear una escena vacía por ahora
        Scene scene = new Scene(createPlaceholderRoot(), 800, 600);
        
        primaryStage.setTitle("Renta Motos - Sistema de Gestión");
        primaryStage.setScene(scene);
        primaryStage.show();

        Logger.logInfo("Aplicación iniciada correctamente\n");
    }

    /**
     * Crea un root placeholder mientras no existan archivos FXML.
     * NOTA: Esto se reemplazará cuando se diseñen las pantallas.
     */
    private Parent createPlaceholderRoot() {
        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox();
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
        vbox.setSpacing(20);
        
        javafx.scene.control.Label label = new javafx.scene.control.Label(
            """
            Sistema de Renta de Motos
            
            Composition Root inicializado correctamente.
            Esperando dise\u00f1o de pantallas FXML...""");
        label.setStyle("-fx-font-size: 16px; -fx-text-alignment: center;");
        
        vbox.getChildren().add(label);
        return vbox;
    }

    @Override
    public void stop() {
        Logger.log("\nCerrando aplicación...");
        // Aquí se pueden cerrar recursos si es necesario (Connection, etc.)
    }
}
