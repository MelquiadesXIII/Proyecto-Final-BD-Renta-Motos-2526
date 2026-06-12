package org.proyectobdmotos.dao;

import java.sql.SQLException;
import org.proyectobdmotos.models.Usuario;

public interface IUsuarioDAO {
    Usuario insert(Usuario usuario) throws SQLException;
    Usuario findByUsername(String nombreUsuario) throws SQLException;
    Usuario findByEmail(String gmail) throws SQLException;
    Usuario findById(int idUsuario) throws SQLException;
    void update(Usuario usuario) throws SQLException;
    void eliminarUsuario(int idUsuario);
}