package org.proyectobdmotos.dao;

import java.sql.Connection;

import org.proyectobdmotos.models.Cliente;

public class ClienteDAO extends AbstractGenericDAO<Cliente, String> implements IClienteDAO{

    public ClienteDAO(Connection connection) {
        super(connection);
    }
}
