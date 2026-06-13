package org.proyectobdmotos.services;

import org.proyectobdmotos.dao.IModeloDAO;
import org.proyectobdmotos.dto.ModeloConMarcaDTO;
import org.proyectobdmotos.models.Modelo;

import java.util.List;

public class ModeloService {
    private final IModeloDAO modeloDAO;

    public ModeloService(IModeloDAO modeloDAO) {
        this.modeloDAO = modeloDAO;
    }

    /**
     * Crea un nuevo modelo asociado a una marca. Lanza una excepción si el modelo
     * ya existe para esa marca (mismo nombre e id de marca).
     * @return el modelo recién creado.
     */
    public Modelo crearModelo(int idMarca, String nombre) {
        if (modeloDAO.existeModelo(idMarca, nombre)) {
            throw new RuntimeException("El modelo ya existe en esa marca.");
        }
        return modeloDAO.crearModelo(idMarca, nombre);
    }

    /**
     * Verifica si hay alguna moto que use el modelo indicado.
     * @return true si al menos una moto referencia este modelo.
     */
    public boolean existeMotoConModelo(int idModelo) {
        return modeloDAO.existeMotoConModelo(idModelo);
    }

    /**
     * Elimina un modelo por su identificador.
     */
    public void eliminarModelo(int idModelo) {
        modeloDAO.eliminarModelo(idModelo);
    }

    /**
     * Obtiene la lista de modelos junto con el nombre de su marca asociada.
     */
    public List<ModeloConMarcaDTO> listarModelosConMarca() {
        return modeloDAO.listarModelosConMarca();
    }

    /**
     * Obtiene todos los modelos registrados.
     */
    public List<Modelo> listarTodos() {
        return modeloDAO.listarTodos();
    }

    /**
     * Actualiza los datos de un modelo existente.
     */
    public void actualizarModelo(Modelo modelo) {
        modeloDAO.actualizarModelo(modelo);
    }

    /**
     * Verifica si ya existe un modelo con el mismo nombre dentro de una marca.
     * @return true si el modelo ya está registrado para esa marca.
     */
    public boolean existeModelo(int idMarca, String nombre) {
        return modeloDAO.existeModelo(idMarca, nombre);
    }
}