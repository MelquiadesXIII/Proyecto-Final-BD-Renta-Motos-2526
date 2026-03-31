package org.proyectobdmotos;

import org.proyectobdmotos.database.DatabaseConnection;

public class App {

    public static void main(String[] args) {
        DatabaseConnection.runMigrations();
        System.out.println("Base de datos lista.");
    }
}
