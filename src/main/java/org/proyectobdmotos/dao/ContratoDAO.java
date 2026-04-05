package org.proyectobdmotos.dao;

import java.sql.Connection;

public class ContratoDAO {

    private final Connection connection;

    public ContratoDAO(Connection connection) {
        this.connection = connection;
    }
}
