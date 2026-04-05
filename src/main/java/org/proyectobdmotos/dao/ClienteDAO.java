package org.proyectobdmotos.dao;

import java.sql.Connection;

public class ClienteDAO {

    private final Connection connection;

    public ClienteDAO(Connection connection) {
        this.connection = connection;
    }
}
