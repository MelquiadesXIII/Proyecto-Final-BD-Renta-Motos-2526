package org.proyectobdmotos.services;

import org.proyectobdmotos.dao.MarcaDAO;
import org.proyectobdmotos.models.Marca;
import java.util.List;

public class MarcaService {
    private final MarcaDAO marcaDAO;

    public MarcaService(MarcaDAO marcaDAO) { this.marcaDAO = marcaDAO; }

    public Marca crearMarca(String nombre) {
        if (marcaDAO.existeMarca(nombre)) {
            throw new RuntimeException("La marca ya existe.");
        }
        return marcaDAO.crearMarca(nombre);
    }

    public boolean existenModelosConMarca(int idMarca) {
        return marcaDAO.existenModelosConMarca(idMarca);
    }

    public boolean existenMotosConMarca(int idMarca) {
        return marcaDAO.existenMotosConMarca(idMarca);
    }

    public void eliminarMarca(int idMarca) {
        marcaDAO.eliminarMarca(idMarca);
    }

    public List<Marca> listarTodas() {
        return marcaDAO.listarTodos();
    }

    public boolean existeMarca(String nombre) {
        return marcaDAO.existeMarca(nombre);
    }

    public void actualizarMarca(Marca marca) {
        marcaDAO.actualizar(marca);
    }
}