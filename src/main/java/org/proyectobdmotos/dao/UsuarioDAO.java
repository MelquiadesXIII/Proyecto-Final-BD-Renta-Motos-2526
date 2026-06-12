package org.proyectobdmotos.dao;

import org.proyectobdmotos.database.DatabaseConnection;
import org.proyectobdmotos.models.Usuario;
import org.proyectobdmotos.utils.Logger;

import java.sql.*;

public class UsuarioDAO implements IUsuarioDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance();
    }

    public Usuario insert(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nombre_usuario, password, gmail, es_admin) VALUES (?, ?, ?, ?) RETURNING id_usuario";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getPassword());
            ps.setString(3, usuario.getGmail());
            ps.setBoolean(4, usuario.isEsAdmin());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt("id_usuario"));
                }
            }
        }
        return usuario;
    }

    public Usuario findByUsername(String nombreUsuario) throws SQLException {
        String sql = "SELECT id_usuario, nombre_usuario, password, gmail, es_admin FROM usuario WHERE nombre_usuario = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Usuario findByEmail(String gmail) throws SQLException {
        String sql = "SELECT id_usuario, nombre_usuario, password, gmail, es_admin FROM usuario WHERE gmail = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    private Usuario mapRow(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id_usuario"));
        u.setNombreUsuario(rs.getString("nombre_usuario"));
        u.setPassword(rs.getString("password"));
        u.setGmail(rs.getString("gmail"));
        u.setEsAdmin(rs.getBoolean("es_admin"));
        return u;
    }

    /**
     * Busca un usuario por su ID (consulta directa).
     */
    public Usuario findById(int idUsuario) throws SQLException {
        String sql = "SELECT id_usuario, nombre_usuario, password, gmail, es_admin FROM usuario WHERE id_usuario = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Actualiza un usuario (consulta directa).
     */
    public void update(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuario SET nombre_usuario = ?, password = ?, gmail = ?, es_admin = ? WHERE id_usuario = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getPassword());
            ps.setString(3, usuario.getGmail());
            ps.setBoolean(4, usuario.isEsAdmin());
            ps.setInt(5, usuario.getId());
            ps.executeUpdate();
        }
    }

    public void eliminarUsuario(int idUsuario) {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.logError("Error al eliminar usuario: " + e.getMessage());
            throw new RuntimeException("Error al eliminar usuario", e);
        }
    }
}