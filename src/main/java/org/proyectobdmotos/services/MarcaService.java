package org.proyectobdmotos.services;

import org.proyectobdmotos.dao.MarcaDAO;
import org.proyectobdmotos.models.Marca;
import java.sql.SQLException;

public class MarcaService {
    private final MarcaDAO marcaDAO;

    public MarcaService(MarcaDAO marcaDAO) { this.marcaDAO = marcaDAO; }

    public Marca crearMarca(String nombre) {
        if (marcaDAO.existeMarca(nombre)) {
            throw new RuntimeException("La marca ya existe.");
        }
        return marcaDAO.crearMarca(nombre);
    }
}