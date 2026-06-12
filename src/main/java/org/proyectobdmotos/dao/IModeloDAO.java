package org.proyectobdmotos.dao;

import java.util.List;
import org.proyectobdmotos.dto.ModeloConMarcaDTO;
import org.proyectobdmotos.models.Modelo;

public interface IModeloDAO extends GenericDAO<Modelo, Integer> {
    Modelo crearModelo(int idMarca, String nombre);
    boolean existeModelo(int idMarca, String nombre);
    boolean existeMotoConModelo(int idModelo);
    void eliminarModelo(int idModelo);
    List<ModeloConMarcaDTO> listarModelosConMarca();
    void actualizarModelo(Modelo modelo);
}