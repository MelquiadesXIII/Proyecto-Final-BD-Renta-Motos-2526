package org.proyectobdmotos.dao;

import java.util.List;
import org.proyectobdmotos.models.Marca;

public interface IMarcaDAO extends GenericDAO<Marca, Integer> {
    Marca crearMarca(String nombre);
    boolean existeMarca(String nombre);
    boolean existenModelosConMarca(int idMarca);
    boolean existenMotosConMarca(int idMarca);
    void eliminarMarca(int idMarca);
    void actualizarMarca(Marca marca);
}