package org.proyectobdmotos.services;

import org.proyectobdmotos.dao.IModeloDAO;
import org.proyectobdmotos.dao.ModeloDAO;
import org.proyectobdmotos.dto.ModeloConMarcaDTO;
import org.proyectobdmotos.models.Modelo;

import java.util.List;

public class ModeloService {
    private final IModeloDAO modeloDAO;

    public ModeloService(ModeloDAO modeloDAO) { this.modeloDAO = modeloDAO; }

    public Modelo crearModelo(int idMarca, String nombre) {
        if (modeloDAO.existeModelo(idMarca, nombre)) {
            throw new RuntimeException("El modelo ya existe en esa marca.");
        }
        return modeloDAO.crearModelo(idMarca, nombre);
    }
    public boolean existeMotoConModelo(int idModelo) { return modeloDAO.existeMotoConModelo(idModelo); }
    public void eliminarModelo(int idModelo) { modeloDAO.eliminarModelo(idModelo); }
    public List<ModeloConMarcaDTO> listarModelosConMarca() {
        return modeloDAO.listarModelosConMarca();
    }
    public List<Modelo> listarTodos() {
        return modeloDAO.listarTodos();
    }

    public void actualizarModelo(Modelo modelo) {
        modeloDAO.actualizarModelo(modelo);
    }

    public boolean existeModelo(int idMarca, String nombre) {
        return modeloDAO.existeModelo(idMarca, nombre);
    }

}