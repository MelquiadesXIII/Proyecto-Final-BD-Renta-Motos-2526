package org.proyectobdmotos.services;

import org.proyectobdmotos.dao.IUsuarioDAO;
import org.proyectobdmotos.dao.UsuarioDAO;
import org.proyectobdmotos.models.Usuario;
import org.proyectobdmotos.services.exceptions.BusinessErrorCode;
import org.proyectobdmotos.services.exceptions.ValidationException;
import java.sql.SQLException;
import org.proyectobdmotos.utils.Logger;

public class UsuarioService
{

    private final IUsuarioDAO usuarioDAO;

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario registrarUsuario(String nombreUsuario, String password, String gmail) throws ValidationException {
        try {
            if (usuarioDAO.findByUsername(nombreUsuario) != null) {
                throw new ValidationException(BusinessErrorCode.USUARIO_YA_EXISTE, "El nombre de usuario ya existe");
            }
            if (usuarioDAO.findByEmail(gmail) != null) {
                throw new ValidationException(BusinessErrorCode.EMAIL_YA_EXISTE, "El correo electrónico ya está registrado");
            }
        } catch (SQLException e) {
            throw new ValidationException(BusinessErrorCode.SIN_CONEXION_BD, "Error al verificar unicidad", e);
        }

        Usuario nuevo = new Usuario(null, nombreUsuario, password, gmail, false);
        try {
            return usuarioDAO.insert(nuevo);
        } catch (SQLException e) {
            throw new ValidationException(BusinessErrorCode.SIN_CONEXION_BD, "Error al guardar usuario", e);
        }
    }

    public Usuario autenticar(String nombreUsuario, String password) throws ValidationException {
        try {
            Usuario u = usuarioDAO.findByUsername(nombreUsuario);
            if (u == null || !u.getPassword().equals(password)) {
                throw new ValidationException(BusinessErrorCode.CREDENCIALES_INVALIDAS, "Usuario o contraseña incorrectos");
            }
            return u;
        } catch (SQLException e) {
            throw new ValidationException(BusinessErrorCode.SIN_CONEXION_BD, "Error al autenticar", e);
        }
    }


    public Usuario registrarUsuarioConRol(String nombreUsuario, String password, String gmail, boolean esAdmin) throws ValidationException {
        Usuario nuevo = new Usuario(null, nombreUsuario, password, gmail, esAdmin);
        try {
            if (usuarioDAO.findByUsername(nombreUsuario) != null) {
                throw new ValidationException(BusinessErrorCode.USUARIO_YA_EXISTE, "El nombre de usuario ya existe");
            }
            if (usuarioDAO.findByEmail(gmail) != null) {
                throw new ValidationException(BusinessErrorCode.EMAIL_YA_EXISTE, "El correo electrónico ya está registrado");
            }
            return usuarioDAO.insert(nuevo);
        } catch (SQLException e) {
            throw new ValidationException(BusinessErrorCode.SIN_CONEXION_BD, "Error al guardar usuario", e);
        }
    }


    public Usuario buscarPorId(int idUsuario) {
        try {
            return usuarioDAO.findById(idUsuario);
        } catch (SQLException e) {
            Logger.logError("Error al buscar usuario por ID: " + e.getMessage());
            throw new RuntimeException("Error al buscar usuario por ID", e);
        }
    }

    public void actualizarUsuario(Usuario usuario) {
        try {
            usuarioDAO.update(usuario);
        } catch (SQLException e) {
            Logger.logError("Error al actualizar usuario: " + e.getMessage());
            throw new RuntimeException("Error al actualizar usuario", e);
        }
    }

    public void eliminarUsuario(int idUsuario) {
        usuarioDAO.eliminarUsuario(idUsuario);
    }
}