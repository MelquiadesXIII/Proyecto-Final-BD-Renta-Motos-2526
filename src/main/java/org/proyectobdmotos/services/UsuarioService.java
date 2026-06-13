package org.proyectobdmotos.services;

import org.proyectobdmotos.dao.IUsuarioDAO;
import org.proyectobdmotos.models.Usuario;
import org.proyectobdmotos.services.exceptions.BusinessErrorCode;
import org.proyectobdmotos.services.exceptions.ValidationException;
import java.sql.SQLException;
import org.proyectobdmotos.utils.Logger;

public class UsuarioService {

    private final IUsuarioDAO usuarioDAO;

    public UsuarioService(IUsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Registra un nuevo usuario normal (no administrador).
     * Valida que el nombre de usuario y el correo electrónico no estén ya registrados.
     * @return el usuario creado con su ID asignado.
     * @throws ValidationException si los datos no son únicos o falla la conexión a la BD.
     */
    public Usuario registrarUsuario(String nombreUsuario, String password, String gmail) throws ValidationException {
        validarUnicidad(nombreUsuario, gmail);
        Usuario nuevo = new Usuario(null, nombreUsuario, password, gmail, false);
        return insertarUsuario(nuevo);
    }

    /**
     * Registra un nuevo usuario con el rol especificado (admin o cliente).
     * Realiza las mismas validaciones de unicidad que el registro normal.
     * @return el usuario creado con su ID asignado.
     * @throws ValidationException si los datos no son únicos o falla la conexión a la BD.
     */
    public Usuario registrarUsuarioConRol(String nombreUsuario, String password, String gmail, boolean esAdmin) throws ValidationException {
        validarUnicidad(nombreUsuario, gmail);
        Usuario nuevo = new Usuario(null, nombreUsuario, password, gmail, esAdmin);
        return insertarUsuario(nuevo);
    }

    /**
     * Verifica que el nombre de usuario y el correo electrónico no existan ya en la base de datos.
     * Lanza una excepción de validación si alguno de ellos ya está registrado.
     * @throws ValidationException si hay duplicidad o error de conexión.
     */
    private void validarUnicidad(String nombreUsuario, String gmail) throws ValidationException {
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
    }

    /**
     * Inserta un nuevo usuario en la base de datos.
     * @return el usuario con el ID generado.
     * @throws ValidationException si falla la inserción.
     */
    private Usuario insertarUsuario(Usuario usuario) throws ValidationException {
        try {
            return usuarioDAO.insert(usuario);
        } catch (SQLException e) {
            throw new ValidationException(BusinessErrorCode.SIN_CONEXION_BD, "Error al guardar usuario", e);
        }
    }

    /**
     * Autentica a un usuario por nombre y contraseña.
     * @return el usuario si las credenciales son válidas.
     * @throws ValidationException si el usuario no existe o la contraseña no coincide.
     */
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

    /**
     * Busca un usuario por su identificador único.
     * @return el usuario encontrado, o null si no existe.
     */
    public Usuario buscarPorId(int idUsuario) {
        try {
            return usuarioDAO.findById(idUsuario);
        } catch (SQLException e) {
            Logger.logError("Error al buscar usuario por ID: " + e.getMessage());
            throw new RuntimeException("Error al buscar usuario por ID", e);
        }
    }

    /**
     * Actualiza los datos de un usuario existente.
     */
    public void actualizarUsuario(Usuario usuario) {
        try {
            usuarioDAO.update(usuario);
        } catch (SQLException e) {
            Logger.logError("Error al actualizar usuario: " + e.getMessage());
            throw new RuntimeException("Error al actualizar usuario", e);
        }
    }

    /**
     * Elimina un usuario por su identificador.
     */
    public void eliminarUsuario(int idUsuario) {
        usuarioDAO.eliminarUsuario(idUsuario);
    }
}