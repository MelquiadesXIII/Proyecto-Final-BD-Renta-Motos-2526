package org.proyectobdmotos.dao;

public interface IFormaPagoDAO {
    int findIdByNombre(String nombre);
    String findNombreById(int id);
}
