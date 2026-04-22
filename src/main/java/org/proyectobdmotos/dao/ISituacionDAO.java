package org.proyectobdmotos.dao;

public interface ISituacionDAO {
    int findIdByNombre(String nombre);
    String findNombreById(int id);
}
