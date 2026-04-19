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
        boolean motoExiste = false;
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
            motoExiste = motoDAO.buscarPorId(matricula).isPresent();
        }

        if (clienteExiste && !motoExiste) {
            Logger.logError("Moto no encontrada: " + matricula);
            validationException = new ValidationException(
                BusinessErrorCode.MOTO_NO_ENCONTRADA,
                "No se puede crear el contrato: moto no encontrada"
            );
        }

        if (clienteExiste && motoExiste) {
            motoDisponible = motoDAO.estaDisponible(matricula);
            if (!motoDisponible) {
                Logger.logError("Moto no disponible: " + matricula);
                validationException = new ValidationException(
                    BusinessErrorCode.MOTO_NO_DISPONIBLE,
                    "No se puede crear el contrato: moto no disponible"
                );
            }
        }

        if (clienteExiste && motoExiste && motoDisponible) {
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
        String matricula = contrato.getContratoID().getMatriculaMoto();
        Optional<Contrato> contratoPersistido = contratoDAO.buscarPorId(contrato.getContratoID());
        boolean contratoExiste = contratoPersistido.isPresent();
        boolean motoExiste = false;
        boolean contratoYaFinalizado = false;
        boolean puedeFinalizar = false;
        ValidationException validationException = null;

        if (!contratoExiste) {
            Logger.logError("Contrato no encontrado: " + contrato.getContratoID().getFechaInicio() + " / " + matricula);
            validationException = new ValidationException(
                BusinessErrorCode.CONTRATO_NO_ENCONTRADO,
                "No se puede finalizar el contrato: no existe"
            );
        }

        if (contratoExiste) {
            contratoYaFinalizado = contratoPersistido.get().getFechaEntrega() != null;
            if (contratoYaFinalizado) {
                Logger.logError("Contrato ya finalizado: " + contrato.getContratoID().getFechaInicio() + " / " + matricula);
                validationException = new ValidationException(
                    BusinessErrorCode.CONTRATO_YA_FINALIZADO,
                    "No se puede finalizar el contrato: ya está finalizado"
                );
            }
        }

        if (contratoExiste && !contratoYaFinalizado) {
            motoExiste = motoDAO.buscarPorId(matricula).isPresent();
        }

        if (contratoExiste && !contratoYaFinalizado && !motoExiste) {
            Logger.logError("Moto no encontrada: " + matricula);
            validationException = new ValidationException(
                BusinessErrorCode.MOTO_NO_ENCONTRADA,
                "No se puede finalizar el contrato: moto no encontrada"
            );
        }

        if (contratoExiste && !contratoYaFinalizado && motoExiste) {
            puedeFinalizar = true;
        }

        if (puedeFinalizar) {
            Logger.log("Finalizando contrato: " + matricula);
            contratoDAO.actualizar(contrato);
            motoDAO.cambiarEstado(matricula, Situacion.DISPONIBLE);
        }

        if (!puedeFinalizar) {
            if (validationException == null) {
                validationException = new ValidationException(
                    BusinessErrorCode.CONTRATO_VALIDACION_FALLIDA,
                    "No se puede finalizar el contrato: validaciones fallidas"
                );
            }
            throw validationException;
        }
    }

    public void actualizarContrato(Contrato contrato) {
        boolean contratoExiste = contratoDAO.buscarPorId(contrato.getContratoID()).isPresent();
        boolean puedeActualizar = false;
        ValidationException validationException = null;

        if (!contratoExiste) {
            Logger.logError("Contrato no encontrado para actualizar: "
                + contrato.getContratoID().getFechaInicio() + " / "
                + contrato.getContratoID().getMatriculaMoto());
            validationException = new ValidationException(
                BusinessErrorCode.CONTRATO_NO_ENCONTRADO,
                "No se puede actualizar el contrato: no existe"
            );
        }

        if (contratoExiste) {
            puedeActualizar = true;
        }

        if (puedeActualizar) {
            Logger.log("Actualizando contrato: " + contrato.getContratoID().getMatriculaMoto());
            contratoDAO.actualizar(contrato);
        }

        if (!puedeActualizar) {
            if (validationException == null) {
                validationException = new ValidationException(
                    BusinessErrorCode.CONTRATO_VALIDACION_FALLIDA,
                    "No se puede actualizar el contrato: validaciones fallidas"
                );
            }
            throw validationException;
        }
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
        boolean contratoExiste = contratoDAO.buscarPorId(id).isPresent();
        boolean puedeEliminar = false;
        ValidationException validationException = null;

        if (!contratoExiste) {
            Logger.logError("Contrato no encontrado para eliminar: " + id.getFechaInicio() + " / " + id.getMatriculaMoto());
            validationException = new ValidationException(
                BusinessErrorCode.CONTRATO_NO_ENCONTRADO,
                "No se puede eliminar el contrato: no existe"
            );
        }

        if (contratoExiste) {
            puedeEliminar = true;
        }

        if (puedeEliminar) {
            Logger.log("Eliminando contrato: " + id.getMatriculaMoto() + " / " + id.getFechaInicio());
            contratoDAO.eliminar(id);
        }

        if (!puedeEliminar) {
            if (validationException == null) {
                validationException = new ValidationException(
                    BusinessErrorCode.CONTRATO_VALIDACION_FALLIDA,
                    "No se puede eliminar el contrato: validaciones fallidas"
                );
            }
            throw validationException;
        }
    }
}
