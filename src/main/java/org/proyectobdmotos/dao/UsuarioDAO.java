package org.proyectobdmotos.dao;

import org.proyectobdmotos.database.DatabaseConnection;
import org.proyectobdmotos.models.Usuario;
import org.proyectobdmotos.utils.Logger;

import java.sql.*;

public class UsuarioDAO implements IUsuarioDAO {

    /**
     * Obtiene una conexión a la base de datos usando el singleton DatabaseConnection.
     * Método privado para centralizar la obtención de conexiones.
     */
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance();
    }

    /**
     * Inserta un nuevo usuario en la base de datos y le asigna el ID generado.
     * @return el mismo objeto Usuario con el id actualizado.
     * @throws SQLException si ocurre un error de base de datos.
     */
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

    /**
     * Busca un usuario por su nombre de usuario exacto.
     * @return el usuario encontrado, o null si no existe.
     * @throws SQLException si ocurre un error de base de datos.
     */
    public Usuario findByUsername(String nombreUsuario) throws SQLException {
        String sql = "SELECT id_usuario, nombre_usuario, password, gmail, es_admin FROM usuario WHERE nombre_usuario = ?";
        Usuario usuario = null;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = mapRow(rs);
                }
            }
        }
        return usuario;
    }

    /**
     * Busca un usuario por su correo electrónico exacto.
     * @return el usuario encontrado, o null si no existe.
     * @throws SQLException si ocurre un error de base de datos.
     */
    public Usuario findByEmail(String gmail) throws SQLException {
        String sql = "SELECT id_usuario, nombre_usuario, password, gmail, es_admin FROM usuario WHERE gmail = ?";
        Usuario usuario = null;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = mapRow(rs);
                }
            }
        }
        return usuario;
    }

    /**
     * Mapea una fila del ResultSet a un objeto Usuario.
     * Método privado reutilizable para todas las consultas.
     */
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
     * Busca un usuario por su identificador único.
     * @return el usuario encontrado, o null si no existe.
     * @throws SQLException si ocurre un error de base de datos.
     */
    public Usuario findById(int idUsuario) throws SQLException {
        String sql = "SELECT id_usuario, nombre_usuario, password, gmail, es_admin FROM usuario WHERE id_usuario = ?";
        Usuario usuario = null;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = mapRow(rs);
                }
            }
        }
        return usuario;
    }

    /**
     * Actualiza los datos de un usuario existente.
     * @throws SQLException si ocurre un error de base de datos.
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

    /**
     * Elimina un usuario por su identificador.
     */
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