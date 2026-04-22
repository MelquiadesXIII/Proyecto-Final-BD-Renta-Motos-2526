package org.proyectobdmotos.dao;

public interface ISexoDAO {
    int findIdByNombre(String nombre);
    String findNombreById(int id);
}
