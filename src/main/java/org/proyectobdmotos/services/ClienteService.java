package org.proyectobdmotos.services;

import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dao.IClienteDAO;
import org.proyectobdmotos.dto.ClienteDTO;
import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.utils.Logger;

public class ClienteService {

    private final IClienteDAO clienteDAO;

    public ClienteService(IClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    public void crearCliente(Cliente cliente) {
        Logger.log("Creando cliente: " + cliente.getCiCliente());
        clienteDAO.insertar(cliente);
    }

    public void actualizarCliente(Cliente cliente) {
        Logger.log("Actualizando cliente: " + cliente.getCiCliente());
        clienteDAO.actualizar(cliente);
    }

    public void eliminarCliente(String ci) {
        Logger.log("Eliminando cliente: " + ci);
        clienteDAO.eliminar(ci);
    }

    public void eliminarClienteConCascada(String ci) {
        Logger.log("Eliminando cliente con cascada: " + ci);
        clienteDAO.eliminarConCascada(ci);
    }

    public Optional<Cliente> buscarPorCi(String ci) {
        return clienteDAO.buscarPorId(ci);
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
}
