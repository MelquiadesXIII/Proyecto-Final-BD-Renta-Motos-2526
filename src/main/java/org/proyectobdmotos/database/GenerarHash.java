package org.proyectobdmotos.database;

import org.mindrot.jbcrypt.BCrypt;

public class GenerarHash {
    public static void main(String[] args) {
        // Genera 3 hashes para la misma contraseña
        for (int i = 1; i <= 3; i++) {
            String hash = BCrypt.hashpw("Admin123", BCrypt.gensalt(12));
            System.out.println("Hash " + i + ": " + hash);
        }
    }
}