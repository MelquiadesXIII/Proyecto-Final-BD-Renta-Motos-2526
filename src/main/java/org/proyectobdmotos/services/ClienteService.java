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

    public void crearCliente(Cliente cliente) 
    {
        clienteDAO.insertar(cliente);
    }


    public void actualizarCliente(Cliente cliente) {
        Logger.log("Actualizando cliente id=" + cliente.getIdCliente() + " ci=" + cliente.getCiCliente());
        clienteDAO.actualizar(cliente);
    }

    public void eliminarCliente(String ci) {
        Logger.log("Eliminando cliente por CI: " + ci);
        Optional<Cliente> encontrado = clienteDAO.buscarPorCi(ci);
        boolean clienteExiste = encontrado.isPresent();
        ValidationException validationException = null;

        if (!clienteExiste) {
            Logger.logError("Cliente no encontrado para eliminar: " + ci);
            validationException = new ValidationException(
                    BusinessErrorCode.CLIENTE_NO_ENCONTRADO,
                    "No se puede eliminar el cliente: no existe");
        }

        if (clienteExiste) {
            clienteDAO.eliminar(encontrado.get().getIdCliente());
        }

        if (!clienteExiste) {
            throw validationException;
        }
    }

    public void eliminarClienteConCascada(String ci) {
        Logger.log("Eliminando cliente con cascada por CI: " + ci);
        Optional<Cliente> encontrado = clienteDAO.buscarPorCi(ci);
        boolean clienteExiste = encontrado.isPresent();
        ValidationException validationException = null;

        if (!clienteExiste) {
            Logger.logError("Cliente no encontrado para eliminar en cascada: " + ci);
            validationException = new ValidationException(
                    BusinessErrorCode.CLIENTE_NO_ENCONTRADO,
                    "No se puede eliminar el cliente: no existe");
        }

        if (clienteExiste) {
            clienteDAO.eliminarConCascada(encontrado.get().getIdCliente());
        }

        if (!clienteExiste) {
            throw validationException;
        }
    }

    public Optional<Cliente> buscarPorId(int idCliente) {
        return clienteDAO.buscarPorId(idCliente);
    }

    public Optional<Cliente> buscarPorIdUsuario(int idUsuario) {
        return clienteDAO.buscarPorIdUsuario(idUsuario);
    }

    public Optional<Cliente> buscarPorCi(String ci) {
        return clienteDAO.buscarPorCi(ci);
    }

    public List<Cliente> listarTodos() {
        return clienteDAO.listarTodos();
    }

    public List<ClienteDTO> listarClientesPorMunicipio() {
        return clienteDAO.listarClientesPorMunicipio();
    }

    public List<Cliente> obtenerClientesIncumplidores() {
        return clienteDAO.obtenerClientesIncumplidores();
    }


    public List<CliRepDTO> listarClientesReporte() {
        return clienteDAO.listarClientesReporte();
    }

    public List<IncumpDTO> listarIncumplidores() {
        return clienteDAO.listarIncumplidores();
    }

    public List<ClienteUsuarioDTO> listarClientesConUsuario() {
        return clienteDAO.listarClientesConUsuario();
    }

    public String obtenerNombreMunicipio(int idMunicipio) {
        return clienteDAO.obtenerNombreMunicipio(idMunicipio);
    }

    public List<Cliente> buscarClientesPorTexto(String texto) {
        return clienteDAO.buscarClientesPorTexto(texto);
    }

    public List<Municipio> listarMunicipios() {
        return clienteDAO.listarMunicipios();
    }


}
