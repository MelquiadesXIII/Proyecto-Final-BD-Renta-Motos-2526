package org.proyectobdmotos;

import org.proyectobdmotos.database.DatabaseConnection;
import org.proyectobdmotos.ui.FxApp;
import org.proyectobdmotos.utils.Logger;

import javafx.application.Application;

/**
 * App: punto de entrada principal.
 * 1. Ejecuta migraciones de base de datos
 * 2. Lanza la aplicación JavaFX
 */
public class App {

  public static void main(String[] args) {
    Logger.log("=== Sistema de Renta de Motos ===");

    // 1. Ejecutar migraciones de base de datos
    Logger.log("Ejecutando migraciones de base de datos...");
    try {
      DatabaseConnection.runMigrations();
      Logger.logInfo("✓ Base de datos lista\n");
    } catch (Exception e) {
      Logger.logError("❌ Error en migraciones: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }

    // 2. Lanzar aplicación JavaFX
    Logger.log("Lanzando interfaz gráfica...\n");
    Application.launch(FxApp.class, args);

    System.exit(0);
  }
}
