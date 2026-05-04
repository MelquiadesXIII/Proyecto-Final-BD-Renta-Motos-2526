package org.proyectobdmotos.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Generador de IDs secuenciales respaldado por la tabla {@code id_secuencia}.
 *
 * <p>Cada llamada a {@link #generateId(String)} incrementa en 1 el contador
 * almacenado para el nombre de clase indicado y devuelve el nuevo valor.
 * El proceso es seguro para uso no concurrente (aplicación de un solo hilo).</p>
 *
 * <p>La tabla {@code id_secuencia} se crea y siembra en la migración
 * {@code V9__id_secuencia.sql}.</p>
 */
public class IdGenerator {

    private static final String SQL_INCREMENT =
        "UPDATE id_secuencia SET ultimo_id = ultimo_id + 1 WHERE nombre_clase = ?";

    private static final String SQL_SELECT =
        "SELECT ultimo_id FROM id_secuencia WHERE nombre_clase = ?";

    private final Connection connection;

    public IdGenerator(Connection connection) {
        this.connection = connection;
    }

    /**
     * Genera el siguiente ID secuencial para la clase indicada.
     *
     * @param className nombre simple de la clase Java (ej. {@code "Cliente"})
     * @return el nuevo ID generado, o {@code null} si el nombre de clase no existe en la tabla
     */
    public Integer generateId(String className) {
        Integer newId = null;

        try {
            try (PreparedStatement psIncrement = connection.prepareStatement(SQL_INCREMENT)) {
                psIncrement.setString(1, className);
                psIncrement.executeUpdate();
            }

            try (PreparedStatement psSelect = connection.prepareStatement(SQL_SELECT)) {
                psSelect.setString(1, className);
                try (ResultSet rs = psSelect.executeQuery()) {
                    boolean found = rs.next();
                    if (found) {
                        newId = rs.getInt("ultimo_id");
                    }
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al generar ID para \"" + className + "\": " + e.getMessage());
            throw new RuntimeException(
                "Error al generar ID para \"" + className + "\": " + e.getMessage(), e);
        }

        return newId;
    }
}
