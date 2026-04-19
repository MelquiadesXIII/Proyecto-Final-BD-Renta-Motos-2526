package org.proyectobdmotos.services;

import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dao.IClienteDAO;
import org.proyectobdmotos.dao.IContratoDAO;
import org.proyectobdmotos.dao.IMotoDAO;
import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.models.ContratoID;
import org.proyectobdmotos.models.Situacion;
import org.proyectobdmotos.services.exceptions.BusinessErrorCode;
import org.proyectobdmotos.services.exceptions.ValidationException;
import org.proyectobdmotos.utils.Logger;

/**
 * ContratoService: orquesta operaciones de contratos.
 * Necesita acceso a todos los DAOs porque un contrato involucra:
 * - Cliente (quien renta)
 * - Moto (qué se renta)
 * - Contrato (el registro de la renta)
 */
public class ContratoService {

    private final IContratoDAO contratoDAO;
    private final IClienteDAO clienteDAO;
    private final IMotoDAO motoDAO;

    public ContratoService(IContratoDAO contratoDAO, IClienteDAO clienteDAO, IMotoDAO motoDAO) {
        this.contratoDAO = contratoDAO;
        this.clienteDAO = clienteDAO;
        this.motoDAO = motoDAO;
    }

    /**
     * Crea un nuevo contrato validando que el cliente exista y la moto esté disponible.
     * El trigger de BD se encarga de poner la moto en estado 'alquilada'.
     */
    public void crearContrato(Contrato contrato) {
        String ciCliente = contrato.getCiCliente();
        String matricula = contrato.getContratoID().getMatriculaMoto();

        boolean clienteExiste = clienteDAO.buscarPorId(ciCliente).isPresent();
        boolean motoDisponible = false;
        boolean puedeCrear = false;
        ValidationException validationException = null;

        if (!clienteExiste) {
            Logger.logError("Cliente no encontrado: " + ciCliente);
            validationException = new ValidationException(
                BusinessErrorCode.CLIENTE_NO_ENCONTRADO,
                "No se puede crear el contrato: cliente no encontrado"
            );
        }

        if (clienteExiste) {
            motoDisponible = motoDAO.estaDisponible(matricula);
            if (!motoDisponible) {
                Logger.logError("Moto no disponible: " + matricula);
                validationException = new ValidationException(
                    BusinessErrorCode.MOTO_NO_DISPONIBLE,
                    "No se puede crear el contrato: moto no disponible"
                );
            }
        }

        if (clienteExiste && motoDisponible) {
            puedeCrear = true;
        }

        if (puedeCrear) {
            Logger.log("Creando contrato para cliente " + ciCliente + " con moto " + matricula);
            contratoDAO.insertar(contrato);
        }

        if (!puedeCrear) {
            if (validationException == null) {
                validationException = new ValidationException(
                    BusinessErrorCode.CONTRATO_VALIDACION_FALLIDA,
                    "No se puede crear el contrato: validaciones fallidas"
                );
            }
            throw validationException;
        }
    }

    /**
     * Finaliza un contrato: registra la fecha de entrega y devuelve la moto a 'disponible'.
     */
    public void finalizarContrato(Contrato contrato) {
        Logger.log("Finalizando contrato: " + contrato.getContratoID().getMatriculaMoto());
        contratoDAO.actualizar(contrato);
        motoDAO.cambiarEstado(
            contrato.getContratoID().getMatriculaMoto(),
            Situacion.DISPONIBLE
        );
    }

    public Optional<Contrato> buscarPorId(ContratoID id) {
        return contratoDAO.buscarPorId(id);
    }

    public List<Contrato> listarTodos() {
        return contratoDAO.listarTodos();
    }

    public List<Contrato> listarContratosCompletos() {
        return contratoDAO.listarContratosCompletos();
    }

    public void eliminarContrato(ContratoID id) {
        Logger.log("Eliminando contrato: " + id.getMatriculaMoto() + " / " + id.getFechaInicio());
        contratoDAO.eliminar(id);
    }
}
