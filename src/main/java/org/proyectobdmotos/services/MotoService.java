package org.proyectobdmotos.services;

import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dao.IMotoDAO;
import org.proyectobdmotos.dto.MotoDTO;
import org.proyectobdmotos.dto.SituacionMotoDTO;
import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.models.Situacion;
import org.proyectobdmotos.services.exceptions.BusinessErrorCode;
import org.proyectobdmotos.services.exceptions.ValidationException;
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
        Logger.log("Actualizando moto id=" + moto.getIdMoto() + " matricula=" + moto.getMatriculaMoto());
        motoDAO.actualizar(moto);
    }

    public void eliminarMoto(String matricula) {
        Logger.log("Eliminando moto por matrícula: " + matricula);
        Optional<Moto> encontrada = motoDAO.buscarPorMatricula(matricula);
        boolean motoExiste = encontrada.isPresent();
        ValidationException validationException = null;

        if (!motoExiste) {
            Logger.logError("Moto no encontrada para eliminar: " + matricula);
            validationException = new ValidationException(
                BusinessErrorCode.MOTO_NO_ENCONTRADA,
                "No se puede eliminar la moto: no existe"
            );
        }

        if (motoExiste) {
            motoDAO.eliminar(encontrada.get().getIdMoto());
        }

        if (!motoExiste) {
            throw validationException;
        }
    }

    public Optional<Moto> buscarPorMatricula(String matricula) {
        return motoDAO.buscarPorMatricula(matricula);
    }

    public List<Moto> listarTodos() {
        return motoDAO.listarTodos();
    }

    public boolean estaDisponible(String matricula) {
        Optional<Moto> moto = motoDAO.buscarPorMatricula(matricula);
        boolean disponible = false;
        if (moto.isPresent()) {
            disponible = motoDAO.estaDisponible(moto.get().getIdMoto());
        }
        return disponible;
    }

    public void cambiarEstado(String matricula, Situacion nuevaSituacion) {
        Logger.log("Cambiando estado de moto " + matricula + " a " + nuevaSituacion.getValor());
        Optional<Moto> encontrada = motoDAO.buscarPorMatricula(matricula);
        boolean motoExiste = encontrada.isPresent();
        ValidationException validationException = null;

        if (!motoExiste) {
            Logger.logError("Moto no encontrada para cambiar estado: " + matricula);
            validationException = new ValidationException(
                BusinessErrorCode.MOTO_NO_ENCONTRADA,
                "No se puede cambiar el estado: moto no existe"
            );
        }

        if (motoExiste) {
            motoDAO.cambiarEstado(encontrada.get().getIdMoto(), nuevaSituacion);
        }

        if (!motoExiste) {
            throw validationException;
        }
    }

    public List<MotoDTO> listarMotosConKilometraje() {
        return motoDAO.listarMotosConKilometraje();
    }

    public List<SituacionMotoDTO> listarSituacionMotos() {
        return motoDAO.listarSituacionMotos();
    }
}
