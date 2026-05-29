package org.proyectobdmotos.dao;

import org.proyectobdmotos.database.DatabaseConnection;
import org.proyectobdmotos.models.Usuario;
import java.sql.*;

public class UsuarioDAO {

    public Usuario insert(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nombre_usuario, password, gmail, es_admin) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getInstance();
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
        try (Connection conn = DatabaseConnection.getInstance();
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
        try (Connection conn = DatabaseConnection.getInstance();
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
}