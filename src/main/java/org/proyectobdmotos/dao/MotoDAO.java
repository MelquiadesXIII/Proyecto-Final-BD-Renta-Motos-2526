package org.proyectobdmotos.dao;

import java.sql.Connection;

public class MotoDAO {

  private final Connection connection;

  public MotoDAO(Connection connection) {
      this.connection = connection;
  }
  
  
}
