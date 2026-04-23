package org.proyectobdmotos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.proyectobdmotos.utils.Logger;

public class SituacionDAO implements ISituacionDAO {

    private final Connection connection;

    public SituacionDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int findIdByNombre(String nombre) {
        String sql = "SELECT id_situacion FROM situacion WHERE nombre_situacion = ?";
        int id = -1;
        boolean error = false;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("id_situacion");
                } else {
                    error = true;
                    Logger.logError("No se encontró situacion con nombre: " + nombre);
                }
            }
        } catch (SQLException e) {
            error = true;
            Logger.logError("Error al buscar id de situacion por nombre: " + e.getMessage());
        }

        if (error) {
            throw new RuntimeException("No se encontró situacion con nombre: " + nombre);
        }

        return id;
    }

    @Override
    public String findNombreById(int id) {
        String sql = "SELECT nombre_situacion FROM situacion WHERE id_situacion = ?";
        String nombre = null;
        boolean error = false;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nombre = rs.getString("nombre_situacion");
                } else {
                    error = true;
                    Logger.logError("No se encontró situacion con id: " + id);
                }
            }
        } catch (SQLException e) {
            error = true;
            Logger.logError("Error al buscar nombre de situacion por id: " + e.getMessage());
        }

        if (error) {
            throw new RuntimeException("No se encontró situacion con id: " + id);
        }

        return nombre;
    }
}
