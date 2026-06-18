package org.proyectobdmotos.services;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.proyectobdmotos.dao.IClienteDAO;
import org.proyectobdmotos.dto.CliRepDTO;
import org.proyectobdmotos.dto.ClienteDTO;
import org.proyectobdmotos.dto.ClienteUsuarioDTO;
import org.proyectobdmotos.dto.IncumpDTO;
import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.models.Municipio;
import org.proyectobdmotos.services.exceptions.BusinessErrorCode;
import org.proyectobdmotos.services.exceptions.ValidationException;
import org.proyectobdmotos.utils.Logger;

public class ClienteService {

    private final IClienteDAO clienteDAO;

    public ClienteService(IClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    /**
     * Crea un nuevo cliente en la base de datos.
     * Delega directamente en el DAO la inserción.
     */
    public void crearCliente(Cliente cliente) {
        clienteDAO.insertar(cliente);
    }

    /**
     * Actualiza los datos de un cliente existente.
     * Registra en el log la operación con el id y el CI.
     */
    public void actualizarCliente(Cliente cliente) {
        Logger.log("Actualizando cliente id=" + cliente.getIdCliente() + " ci=" + cliente.getCiCliente());
        clienteDAO.actualizar(cliente);
    }

    /**
     * Elimina un cliente a partir de su CI junto con su usuario asociado.
     * La eliminación del usuario la ejecuta automáticamente el trigger
     * trg_eliminar_usuario_al_borrar_cliente (V30) en la base de datos.
     * Si el cliente no existe, se lanza una excepción de validación.
     */
    public void eliminarCliente(String ci) {
        Logger.log("Eliminando cliente por CI: " + ci);
        Optional<Cliente> encontrado = clienteDAO.buscarPorCi(ci);
        if (encontrado.isEmpty()) {
            Logger.logError("Cliente no encontrado para eliminar: " + ci);
            throw new ValidationException(
                    BusinessErrorCode.CLIENTE_NO_ENCONTRADO,
                    "No se puede eliminar el cliente: no existe");
        } else {
            clienteDAO.eliminar(encontrado.get().getIdCliente());
        }
    }

    /**
     * Elimina un cliente, sus contratos y su usuario asociado.
     * Los contratos se borran explícitamente antes que el cliente; el usuario
     * se borra por el trigger trg_eliminar_usuario_al_borrar_cliente (V30).
     * Si el cliente no existe, lanza una excepción de validación.
     */
    public void eliminarClienteConCascada(String ci) {
        Logger.log("Eliminando cliente con cascada por CI: " + ci);
        Optional<Cliente> encontrado = clienteDAO.buscarPorCi(ci);
        if (encontrado.isEmpty()) {
            Logger.logError("Cliente no encontrado para eliminar en cascada: " + ci);
            throw new ValidationException(
                    BusinessErrorCode.CLIENTE_NO_ENCONTRADO,
                    "No se puede eliminar el cliente: no existe");
        } else {
            clienteDAO.eliminarConCascada(encontrado.get().getIdCliente());
        }
    }

    /**
     * Busca un cliente por su identificador único.
     * @return un Optional con el cliente si existe, vacío en caso contrario.
     */
    public Optional<Cliente> buscarPorId(int idCliente) {
        return clienteDAO.buscarPorId(idCliente);
    }

    /**
     * Busca un cliente asociado a un usuario por el id del usuario.
     * @return un Optional con el cliente si existe.
     */
    public Optional<Cliente> buscarPorIdUsuario(int idUsuario) {
        return clienteDAO.buscarPorIdUsuario(idUsuario);
    }

    /**
     * Busca un cliente por su carné de identidad.
     * @return un Optional con el cliente si se encuentra.
     */
    public Optional<Cliente> buscarPorCi(String ci) {
        return clienteDAO.buscarPorCi(ci);
    }

    /**
     * Obtiene la lista de todos los clientes.
     */
    public List<Cliente> listarTodos() {
        return clienteDAO.listarTodos();
    }

    /**
     * Lista los clientes agrupados por municipio con estadísticas de alquileres.
     */
    public List<ClienteDTO> listarClientesPorMunicipio() {
        return clienteDAO.listarClientesPorMunicipio();
    }

    /**
     * Obtiene la lista de clientes que han incumplido plazos de devolución.
     */
    public List<Cliente> obtenerClientesIncumplidores() {
        return clienteDAO.obtenerClientesIncumplidores();
    }

    /**
     * Obtiene los datos del reporte de clientes por municipio.
     */
    public List<CliRepDTO> listarClientesReporte() {
        return clienteDAO.listarClientesReporte();
    }

    /**
     * Obtiene los datos del reporte de clientes incumplidores.
     */
    public List<IncumpDTO> listarIncumplidores() {
        return clienteDAO.listarIncumplidores();
    }

    /**
     * Obtiene una lista de clientes con su información de usuario asociada.
     */
    public List<ClienteUsuarioDTO> listarClientesConUsuario() {
        return clienteDAO.listarClientesConUsuario();
    }

    /**
     * Obtiene el nombre del municipio a partir de su identificador.
     */
    public String obtenerNombreMunicipio(int idMunicipio) {
        return clienteDAO.obtenerNombreMunicipio(idMunicipio);
    }

    /**
     * Busca clientes cuyo nombre, apellido o CI coincidan parcialmente con el texto dado.
     */
    public List<Cliente> buscarClientesPorTexto(String texto) {
        return clienteDAO.buscarClientesPorTexto(texto);
    }

    /**
     * Obtiene la lista de todos los municipios disponibles.
     */
    public List<Municipio> listarMunicipios() {
        return clienteDAO.listarMunicipios();
    }
}