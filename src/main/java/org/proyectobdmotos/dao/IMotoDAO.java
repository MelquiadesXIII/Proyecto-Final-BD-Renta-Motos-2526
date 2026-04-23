package org.proyectobdmotos.dao;

import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dto.MotoDTO;
import org.proyectobdmotos.dto.SituacionMotoDTO;
import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.models.Situacion;

public interface IMotoDAO extends GenericDAO<Moto, Integer> {

    List<MotoDTO> listarMotosConKilometraje();

    List<SituacionMotoDTO> listarSituacionMotos();

    void cambiarEstado(Integer idMoto, Situacion nuevaSituacion);

    boolean estaDisponible(Integer idMoto);

    Optional<Moto> buscarPorMatricula(String matricula);
}
