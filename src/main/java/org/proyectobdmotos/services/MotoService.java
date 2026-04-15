package org.proyectobdmotos.services;

import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dao.IMotoDAO;
import org.proyectobdmotos.dto.MotoDTO;
import org.proyectobdmotos.dto.SituacionMotoDTO;
import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.models.Situacion;
import org.proyectobdmotos.utils.Logger;

public class MotoService {

    private final IMotoDAO motoDAO;

    public MotoService(IMotoDAO motoDAO) {
        this.motoDAO = motoDAO;
    }

    public void crearMoto(Moto moto) {
        Logger.log("Creando moto: " + moto.getMatriculaMoto());
        motoDAO.insertar(moto);
    }

    public void actualizarMoto(Moto moto) {
        Logger.log("Actualizando moto: " + moto.getMatriculaMoto());
        motoDAO.actualizar(moto);
    }

    public void eliminarMoto(String matricula) {
        Logger.log("Eliminando moto: " + matricula);
        motoDAO.eliminar(matricula);
    }

    public Optional<Moto> buscarPorMatricula(String matricula) {
        return motoDAO.buscarPorId(matricula);
    }

    public List<Moto> listarTodas() {
        return motoDAO.listarTodos();
    }

    public boolean estaDisponible(String matricula) {
        return motoDAO.estaDisponible(matricula);
    }

    public void cambiarEstado(String matricula, Situacion nuevaSituacion) {
        Logger.log("Cambiando estado de moto " + matricula + " a " + nuevaSituacion.getValor());
        motoDAO.cambiarEstado(matricula, nuevaSituacion);
    }

    public List<MotoDTO> listarMotosConKilometraje() {
        return motoDAO.listarMotosConKilometraje();
    }

    public List<SituacionMotoDTO> listarSituacionMotos() {
        return motoDAO.listarSituacionMotos();
    }
}
