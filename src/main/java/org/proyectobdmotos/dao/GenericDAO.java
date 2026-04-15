package org.proyectobdmotos.dao;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz genérica para operaciones CRUD comunes.
 * Aplica el Principio de Segregación de Interfaces (ISP).
 *
 * @param <T>  Tipo de la entidad
 * @param <ID> Tipo de la clave primaria
 */
public interface GenericDAO<T, ID> {

    void insertar(T entity);

    void actualizar(T entity);

    void eliminar(ID id);

    Optional<T> buscarPorId(ID id);

    List<T> listarTodos();
}
