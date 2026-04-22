package org.proyectobdmotos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.proyectobdmotos.utils.Logger;

public class FormaPagoDAO implements IFormaPagoDAO {

    private final Connection connection;

    public FormaPagoDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int findIdByNombre(String nombre) {
        String sql = "SELECT id_forma_pago FROM forma_pago WHERE nombre = ?";
        int id = -1;
        boolean error = false;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("id_forma_pago");
                } else {
                    error = true;
                    Logger.logError("No se encontró forma_pago con nombre: " + nombre);
                }
            }
        } catch (SQLException e) {
            error = true;
            Logger.logError("Error al buscar id de forma_pago por nombre: " + e.getMessage());
        }

        if (error) {
            throw new RuntimeException("No se encontró forma_pago con nombre: " + nombre);
        }

        return id;
    }

    @Override
    public String findNombreById(int id) {
        String sql = "SELECT nombre FROM forma_pago WHERE id_forma_pago = ?";
        String nombre = null;
        boolean error = false;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nombre = rs.getString("nombre");
                } else {
                    error = true;
                    Logger.logError("No se encontró forma_pago con id: " + id);
                }
            }
        } catch (SQLException e) {
            error = true;
            Logger.logError("Error al buscar nombre de forma_pago por id: " + e.getMessage());
        }

        if (error) {
            throw new RuntimeException("No se encontró forma_pago con id: " + id);
        }

        return nombre;
    }
}
