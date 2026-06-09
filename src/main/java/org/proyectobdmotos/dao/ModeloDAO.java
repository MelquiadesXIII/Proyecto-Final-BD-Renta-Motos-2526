package org.proyectobdmotos.dao;

import java.sql.*;

import org.proyectobdmotos.models.Modelo;
import org.proyectobdmotos.utils.Logger;

public class ModeloDAO extends AbstractGenericDAO<Modelo, Integer> {

    public ModeloDAO(Connection connection) {
        super(connection);
    }

    @Override
    protected String getInsertSQL() { return "INSERT INTO modelo (id_marca, nombre_modelo) VALUES (?, ?)"; }
    @Override
    protected String getUpdateSQL() { return "UPDATE modelo SET id_marca = ?, nombre_modelo = ? WHERE id_modelo = ?"; }
    @Override
    protected String getDeleteSQL() { return "DELETE FROM modelo WHERE id_modelo = ?"; }
    @Override
    protected String getFindByIdSQL() { return "SELECT * FROM modelo WHERE id_modelo = ?"; }
    @Override
    protected String getFindAllSQL() { return "SELECT * FROM modelo ORDER BY nombre_modelo"; }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Modelo modelo) throws SQLException {
        ps.setInt(1, modelo.getIdMarca());
        ps.setString(2, modelo.getNombreModelo());
    }
    @Override
    protected void setUpdateParameters(PreparedStatement ps, Modelo modelo) throws SQLException {
        ps.setInt(1, modelo.getIdMarca());
        ps.setString(2, modelo.getNombreModelo());
        ps.setInt(3, modelo.getIdModelo());
    }
    @Override
    protected void setIdParameter(PreparedStatement ps, Integer id) throws SQLException {
        ps.setInt(1, id);
    }
    @Override
    protected Modelo mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new Modelo(rs.getInt("id_modelo"), rs.getInt("id_marca"), rs.getString("nombre_modelo"));
    }

    // Crear usando función SQL
    public Modelo crearModelo(int idMarca, String nombre) {
        String sql = "SELECT insertar_modelo(?, ?) AS id_modelo";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMarca);
            ps.setString(2, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_modelo");
                    return new Modelo(id, idMarca, nombre);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.logError("Error al crear modelo: " + e.getMessage());
            throw new RuntimeException("Error al crear modelo", e);
        }
        return null;
    }

    // Verificar existencia insensible a mayúsculas
    public boolean existeModelo(int idMarca, String nombre) {
        String sql = "SELECT existe_modelo(?, ?) AS existe";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMarca);
            ps.setString(2, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("existe");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.logError("Error al verificar modelo: " + e.getMessage());
            throw new RuntimeException("Error al verificar modelo", e);
        }
        return false;
    }
}