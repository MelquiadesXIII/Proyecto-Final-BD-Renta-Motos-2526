package org.proyectobdmotos;

import java.sql.SQLException;

import org.proyectobdmotos.controller.Agencia;
import org.proyectobdmotos.database.DatabaseConnection;

public class App {

    public static void main(String[] args) {
    
    try {
      DatabaseConnection.runMigrations();
      System.out.println("Base de datos lista.");

      Agencia agencia = new Agencia(DatabaseConnection.getInstance());
      agencia.cargarDatos();
      System.out.println("Datos cargados en memoria.");
      
    } catch (SQLException e) {
        e.printStackTrace();
        System.err.println(e.getMessage() + "Error al inicializar la base de datos ");
    }
  }
}
