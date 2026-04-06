package org.proyectobdmotos.dao;

import java.sql.Connection;

import org.proyectobdmotos.models.Moto;

public class MotoDAO extends AbstractGenericDAO<Moto, String> implements IMotoDAO{


  public MotoDAO(Connection connection) {
      super(connection);
  }
}
