package org.proyectobdmotos.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dto.MotoDTO;
import org.proyectobdmotos.dto.SituacionMotoDTO;
import org.proyectobdmotos.models.*;

public interface IMotoDAO extends GenericDAO<Moto, Integer> {

    List<MotoDTO> listarMotosConKilometraje();
    List<SituacionMotoDTO> listarSituacionMotos();
    void cambiarEstado(Integer idMoto, Situacion nuevaSituacion);
    boolean estaDisponible(Integer idMoto);
    Optional<Moto> buscarPorMatricula(String matricula);

    ArrayList<Color> obtenerColores() throws SQLException;
    ArrayList<Marca> obtenerMarcas() throws SQLException;
    ArrayList<Modelo> obtenerModelosPorMarca(int idMarca) throws SQLException;
    Modelo obtenerModeloPorId(int idModelo) throws SQLException;
    Marca obtenerMarcaPorId(int idMarca) throws SQLException;
    int obtenerIdColorPorNombre(String nombreColor) throws SQLException;
    String obtenerNombreColorPorId(int idColor) throws SQLException;
}