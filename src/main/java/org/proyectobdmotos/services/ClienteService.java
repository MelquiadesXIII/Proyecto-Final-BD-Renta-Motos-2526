package org.proyectobdmotos.services;

import org.proyectobdmotos.dao.ClienteDAO;

public class ClienteService {

    private final ClienteDAO clienteDAO;

    public ClienteService(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    public ClienteDAO getClienteDAO() {
        return clienteDAO;
    }
}
