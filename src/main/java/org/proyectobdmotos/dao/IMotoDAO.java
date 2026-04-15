package org.proyectobdmotos.dao;

import java.util.List;

import org.proyectobdmotos.dto.MotoDTO;
import org.proyectobdmotos.dto.SituacionMotoDTO;
import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.models.Situacion;

public interface IMotoDAO extends GenericDAO<Moto, String> {

    List<MotoDTO> listarMotosConKilometraje();

    List<SituacionMotoDTO> listarSituacionMotos();

    void cambiarEstado(String matricula, Situacion nuevaSituacion);

    boolean estaDisponible(String matricula);
}
