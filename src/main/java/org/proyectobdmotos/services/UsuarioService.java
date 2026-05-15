package org.proyectobdmotos.services;

import org.proyectobdmotos.dao.UsuarioDAO;
import org.proyectobdmotos.models.Usuario;
import org.proyectobdmotos.exceptions.BusinessException;
import org.proyectobdmotos.exceptions.BusinessErrorCode;
import java.sql.SQLException;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario registrarUsuario(String nombreUsuario, String password, String gmail) throws BusinessException {
        try {
            if (usuarioDAO.findByUsername(nombreUsuario) != null) {
                throw new BusinessException("El nombre de usuario ya existe", BusinessErrorCode.DUPLICATE_USERNAME);
            }
            if (usuarioDAO.findByEmail(gmail) != null) {
                throw new BusinessException("El correo electrónico ya está registrado", BusinessErrorCode.DUPLICATE_EMAIL);
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al verificar unicidad", e, BusinessErrorCode.DATABASE_ERROR);
        }

        // Por defecto NO es administrador
        Usuario nuevo = new Usuario(null, nombreUsuario, password, gmail, false);
        try {
            return usuarioDAO.insert(nuevo);
        } catch (SQLException e) {
            throw new BusinessException("Error al guardar usuario", e, BusinessErrorCode.DATABASE_ERROR);
        }
    }

    public Usuario autenticar(String nombreUsuario, String password) throws BusinessException {
        try {
            Usuario u = usuarioDAO.findByUsername(nombreUsuario);
            if (u == null || !u.getPassword().equals(password)) {
                throw new BusinessException("Usuario o contraseña incorrectos", BusinessErrorCode.INVALID_CREDENTIALS);
            }
            return u;
        } catch (SQLException e) {
            throw new BusinessException("Error al autenticar", e, BusinessErrorCode.DATABASE_ERROR);
        }
    }
}