package org.proyectobdmotos.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dao.IMotoDAO;
import org.proyectobdmotos.dto.MotoDTO;
import org.proyectobdmotos.dto.MotoRepDTO;
import org.proyectobdmotos.dto.SitMotoRepDTO;
import org.proyectobdmotos.dto.SituacionMotoDTO;
import org.proyectobdmotos.models.*;
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
        if (encontrada.isEmpty()) {
            Logger.logError("Moto no encontrada para eliminar: " + matricula);
            throw new ValidationException(
                    BusinessErrorCode.MOTO_NO_ENCONTRADA,
                    "No se puede eliminar la moto: no existe"
            );
        }
        motoDAO.eliminar(encontrada.get().getIdMoto());
    }

    public Optional<Moto> buscarPorMatricula(String matricula) {
        return motoDAO.buscarPorMatricula(matricula);
    }

    public List<Moto> listarTodos() {
        return motoDAO.listarTodos();
    }

    public boolean estaDisponible(String matricula) {
        Optional<Moto> moto = motoDAO.buscarPorMatricula(matricula);
        if (moto.isPresent()) {
            return motoDAO.estaDisponible(moto.get().getIdMoto());
        }
        return false;
    }

    public void cambiarEstado(String matricula, Situacion nuevaSituacion) {
        Logger.log("Cambiando estado de moto " + matricula + " a " + nuevaSituacion.getValor());
        Optional<Moto> encontrada = motoDAO.buscarPorMatricula(matricula);
        if (encontrada.isEmpty()) {
            Logger.logError("Moto no encontrada para cambiar estado: " + matricula);
            throw new ValidationException(
                    BusinessErrorCode.MOTO_NO_ENCONTRADA,
                    "No se puede cambiar el estado: moto no existe"
            );
        }
        motoDAO.cambiarEstado(encontrada.get().getIdMoto(), nuevaSituacion);
    }

    public ArrayList<Color> listarColores() throws ValidationException {
        try {
            return motoDAO.obtenerColores();
        } catch (SQLException e) {
            throw new ValidationException(
                    BusinessErrorCode.SIN_CONEXION_BD,
                    "No se pudieron cargar los colores",
                    e
            );
        }
    }

    public ArrayList<Marca> listarMarcas() throws ValidationException {
        try {
            return motoDAO.obtenerMarcas();
        } catch (SQLException e) {
            throw new ValidationException(
                    BusinessErrorCode.SIN_CONEXION_BD,
                    "No se pudieron cargar las marcas",
                    e
            );
        }
    }

    public ArrayList<Modelo> listarModelosPorMarca(int idMarca) throws ValidationException {
        try {
            return motoDAO.obtenerModelosPorMarca(idMarca);
        } catch (SQLException e) {
            throw new ValidationException(
                    BusinessErrorCode.SIN_CONEXION_BD,
                    "No se pudieron cargar los modelos de la marca",
                    e
            );
        }
    }

    public Modelo obtenerModeloPorId(int idModelo) throws ValidationException {
        try {
            return motoDAO.obtenerModeloPorId(idModelo);
        } catch (SQLException e) {
            throw new ValidationException(
                    BusinessErrorCode.SIN_CONEXION_BD,
                    "No se pudo obtener el modelo",
                    e
            );
        }
    }

    public Marca obtenerMarcaPorId(int idMarca) throws ValidationException {
        try {
            return motoDAO.obtenerMarcaPorId(idMarca);
        } catch (SQLException e) {
            throw new ValidationException(
                    BusinessErrorCode.SIN_CONEXION_BD,
                    "No se pudo obtener la marca",
                    e
            );
        }
    }

    public int obtenerIdColorPorNombre(String nombreColor) throws ValidationException {
        try {
            return motoDAO.obtenerIdColorPorNombre(nombreColor);
        } catch (SQLException e) {
            throw new ValidationException(
                    BusinessErrorCode.SIN_CONEXION_BD,
                    "No se pudo obtener el id del color",
                    e
            );
        }
    }

    public String obtenerNombreColorPorId(int idColor) throws ValidationException {
        try {
            return motoDAO.obtenerNombreColorPorId(idColor);
        } catch (SQLException e) {
            throw new ValidationException(
                    BusinessErrorCode.SIN_CONEXION_BD,
                    "No se pudo obtener el nombre del color",
                    e
            );
        }
    }
    public List<MotoRepDTO> listarMotosReporte() {
        return motoDAO.listarMotosReporte();
    }

    public List<SitMotoRepDTO> listarSituacionMotosReporte() {
        return motoDAO.listarSituacionMotosReporte();
    }

}