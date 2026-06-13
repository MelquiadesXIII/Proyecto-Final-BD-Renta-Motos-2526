package org.proyectobdmotos.services;

import org.proyectobdmotos.dao.IMarcaDAO;
import org.proyectobdmotos.models.Marca;
import java.util.List;

public class MarcaService {
    private final IMarcaDAO marcaDAO;

    public MarcaService(IMarcaDAO marcaDAO) {
        this.marcaDAO = marcaDAO;
    }

    /**
     * Crea una nueva marca. Lanza una excepción si ya existe una con el mismo nombre.
     * @return la marca recién creada.
     */
    public Marca crearMarca(String nombre) {
        if (marcaDAO.existeMarca(nombre)) {
            throw new RuntimeException("La marca ya existe.");
        }
        return marcaDAO.crearMarca(nombre);
    }

    /**
     * Verifica si hay modelos asociados a una marca determinada.
     * @return true si la marca tiene al menos un modelo.
     */
    public boolean existenModelosConMarca(int idMarca) {
        return marcaDAO.existenModelosConMarca(idMarca);
    }

    /**
     * Verifica si hay motos que usen directamente una marca.
     * @return true si la marca está referenciada por alguna moto.
     */
    public boolean existenMotosConMarca(int idMarca) {
        return marcaDAO.existenMotosConMarca(idMarca);
    }

    /**
     * Elimina una marca por su identificador.
     */
    public void eliminarMarca(int idMarca) {
        marcaDAO.eliminarMarca(idMarca);
    }

    /**
     * Obtiene todas las marcas registradas.
     */
    public List<Marca> listarTodas() {
        return marcaDAO.listarTodos();
    }

    /**
     * Consulta si existe una marca con el nombre dado (insensible a mayúsculas/minúsculas).
     * @return true si la marca ya está registrada.
     */
    public boolean existeMarca(String nombre) {
        return marcaDAO.existeMarca(nombre);
    }

    /**
     * Actualiza los datos de una marca existente.
     */
    public void actualizarMarca(Marca marca) {
        marcaDAO.actualizar(marca);
    }
}