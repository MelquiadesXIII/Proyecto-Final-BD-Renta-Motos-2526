package org.proyectobdmotos.dao;

import java.sql.Connection;

import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.models.ContratoID;

public class ContratoDAO extends AbstractGenericDAO<Contrato, ContratoID> implements IContratoDAO{


    public ContratoDAO(Connection connection) {
        super(connection);
    }
}
