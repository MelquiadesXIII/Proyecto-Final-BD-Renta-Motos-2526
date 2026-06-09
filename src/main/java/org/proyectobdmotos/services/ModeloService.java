package org.proyectobdmotos.services;

import org.proyectobdmotos.dao.ModeloDAO;
import org.proyectobdmotos.models.Modelo;

public class ModeloService {
    private final ModeloDAO modeloDAO;

    public ModeloService(ModeloDAO modeloDAO) { this.modeloDAO = modeloDAO; }

    public Modelo crearModelo(int idMarca, String nombre) {
        if (modeloDAO.existeModelo(idMarca, nombre)) {
            throw new RuntimeException("El modelo ya existe en esa marca.");
        }
        return modeloDAO.crearModelo(idMarca, nombre);
    }
}