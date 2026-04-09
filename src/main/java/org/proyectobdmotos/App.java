package org.proyectobdmotos;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import org.proyectobdmotos.database.DatabaseConnection;
import org.proyectobdmotos.ui.FxApp;

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

    // Al cerrar la aplicación
    /*try {
      String os = System.getProperty("os.name").toLowerCase();
      if (os.contains("win")) {
        Runtime.getRuntime().exec(new String[]{"shutdown", "/r", "/t", "0"});
      } else if (
        os.contains("nix") || os.contains("nux") || os.contains("mac")
      ) {
        Runtime.getRuntime().exec(new String[]{"reboot"});
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.exit(0);*/
  }
}
