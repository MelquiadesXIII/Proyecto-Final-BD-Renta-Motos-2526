package org.proyectobdmotos;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.proyectobdmotos.database.DatabaseConnection;
import org.proyectobdmotos.ui.FxApp;

import javafx.application.Application;

/**
 * App: punto de entrada principal.
 * 1. Ejecuta migraciones de base de datos
 * 2. Lanza la aplicación JavaFX
 */
public class App {

  private static final Logger LOGGER = Logger.getLogger(App.class.getName());

  public static void main(String[] args) {
    System.out.println("=== Sistema de Renta de Motos ===\n");

    // 1. Ejecutar migraciones de base de datos
    System.out.println("[App] Ejecutando migraciones de base de datos...");
    try {
      DatabaseConnection.runMigrations();
      System.out.println("[App] ✓ Base de datos lista\n");
    } catch (Exception e) {
      System.err.println("[App] ❌ Error en migraciones: " + e.getMessage());
      LOGGER.log(
        Level.SEVERE,
        "Error ejecutando migraciones de base de datos",
        e
      );
      System.exit(1);
    }

    // 2. Lanzar aplicación JavaFX
    System.out.println("[App] Lanzando interfaz gráfica...\n");
    Application.launch(FxApp.class, args);

    System.exit(0);
  }
}
