package org.proyectobdmotos.services;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dao.IMotoDAO;
import org.proyectobdmotos.dto.*;
import org.proyectobdmotos.models.*;
import org.proyectobdmotos.services.exceptions.BusinessErrorCode;
import org.proyectobdmotos.services.exceptions.ValidationException;
import org.proyectobdmotos.utils.Logger;

public class MotoService {

    private final IMotoDAO motoDAO;

    public MotoService(IMotoDAO motoDAO) {
        this.motoDAO = motoDAO;
    }

    // -----------------------------------------------------------------
    // Operaciones CRUD básicas
    // -----------------------------------------------------------------

    /**
     * Registra una nueva moto en el sistema.
     * La matrícula se registra en el log antes de la inserción.
     */
    public void crearMoto(Moto moto) {
        Logger.log("Creando moto: " + moto.getMatriculaMoto());
        motoDAO.insertar(moto);
    }

    /**
     * Actualiza los datos de una moto existente.
     * Se registra el id y la matrícula para trazabilidad.
     */
    public void actualizarMoto(Moto moto) {
        Logger.log("Actualizando moto id=" + moto.getIdMoto() + " matricula=" + moto.getMatriculaMoto());
        motoDAO.actualizar(moto);
    }

    /**
     * Elimina una moto a partir de su matrícula.
     * Si la moto no existe, lanza una excepción de validación.
     */
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

    // -----------------------------------------------------------------
    // Consultas
    // -----------------------------------------------------------------

    /**
     * Busca una moto por su matrícula exacta.
     * @return un Optional con la moto si existe, vacío en caso contrario.
     */
    public Optional<Moto> buscarPorMatricula(String matricula) {
        return motoDAO.buscarPorMatricula(matricula);
    }

    /**
     * Obtiene la lista completa de motos registradas.
     */
    public List<Moto> listarTodos() {
        return motoDAO.listarTodos();
    }

    /**
     * Verifica si una moto está disponible (situación DISPONIBLE).
     * @return true si la moto existe y su situación es DISPONIBLE; false en caso contrario.
     */
    public boolean estaDisponible(String matricula) {
        Optional<Moto> moto = motoDAO.buscarPorMatricula(matricula);
        boolean disponible = false;
        if (moto.isPresent()) {
            disponible = motoDAO.estaDisponible(moto.get().getIdMoto());
        }
        return disponible;
    }

    // -----------------------------------------------------------------
    // Cambio de estado
    // -----------------------------------------------------------------

    /**
     * Cambia la situación de una moto (DISPONIBLE, ALQUILADA, TALLER).
     * Lanza una excepción si la moto no existe.
     */
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

    // -----------------------------------------------------------------
    // Catálogos (colores, marcas, modelos)
    // -----------------------------------------------------------------

    /**
     * Obtiene la lista de todos los colores disponibles.
     * @throws ValidationException si falla la conexión a la base de datos.
     */
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

    /**
     * Obtiene la lista de todas las marcas.
     * @throws ValidationException si falla la conexión a la base de datos.
     */
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

    /**
     * Obtiene los modelos que pertenecen a una marca determinada.
     * @param idMarca identificador de la marca.
     * @throws ValidationException si falla la conexión a la base de datos.
     */
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

    /**
     * Busca un modelo por su identificador único.
     * @return el modelo encontrado.
     * @throws ValidationException si falla la conexión a la base de datos.
     */
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

    /**
     * Busca una marca por su identificador único.
     * @return la marca encontrada.
     * @throws ValidationException si falla la conexión a la base de datos.
     */
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

    /**
     * Obtiene el id de un color a partir de su nombre.
     * @return el identificador del color.
     * @throws ValidationException si falla la conexión a la base de datos.
     */
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

    /**
     * Obtiene el nombre de un color a partir de su id.
     * @return el nombre del color.
     * @throws ValidationException si falla la conexión a la base de datos.
     */
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

    // -----------------------------------------------------------------
    // Reportes y consultas especiales
    // -----------------------------------------------------------------

    /**
     * Obtiene la situación actual de todas las motos (incluye fechas de fin de contrato).
     */
    public List<SituacionMotoDTO> listarSituacionMotos() {
        return motoDAO.listarSituacionMotos();
    }

    /**
     * Obtiene los datos del reporte general de motos.
     */
    public List<MotoRepDTO> listarMotosReporte() {
        return motoDAO.listarMotosReporte();
    }

    /**
     * Obtiene el reporte de situación de motos.
     */
    public List<SitMotoRepDTO> listarSituacionMotosReporte() {
        return motoDAO.listarSituacionMotosReporte();
    }

    /**
     * Lista las motos que están disponibles en un rango de fechas determinado.
     * @return lista de objetos Moto (solo datos básicos).
     */
    public List<Moto> listarMotosDisponiblesEntre(LocalDate inicio, LocalDate fin) {
        return motoDAO.listarMotosDisponiblesEntre(inicio, fin);
    }

    /**
     * Lista las motos disponibles en un rango de fechas con detalle de marca, modelo y color.
     * @return lista de DTOs con la información completa para mostrar en la interfaz.
     */
    public List<MotoDisponibleDTO> listarMotosDisponiblesDetalle(LocalDate inicio, LocalDate fin) {
        return motoDAO.listarMotosDisponiblesDetalle(inicio, fin);
    }

    /**
     * Verifica si una moto ya tiene un contrato que se solape con el período indicado.
     * @return true si existe solapamiento, false en caso contrario.
     */
    public boolean existeSolapamiento(int idMoto, LocalDate inicio, LocalDate fin) {
        return motoDAO.existeSolapamiento(idMoto, inicio, fin);
    }

    public Optional<Moto> buscarPorId(int idMoto) {
        return motoDAO.buscarPorId(idMoto);
    }
}