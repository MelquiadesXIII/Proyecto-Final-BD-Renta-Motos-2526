package org.proyectobdmotos.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dao.IClienteDAO;
import org.proyectobdmotos.dao.IContratoDAO;
import org.proyectobdmotos.dao.IMotoDAO;
import org.proyectobdmotos.models.Contrato;
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
        Integer idCliente = contrato.getIdCliente();
        Integer idMoto = contrato.getIdMoto();

        boolean clienteExiste = clienteDAO.buscarPorId(idCliente).isPresent();
        boolean motoExiste = false;
        boolean motoDisponible = false;
        boolean puedeCrear = false;
        ValidationException validationException = null;

        if (!clienteExiste) {
            Logger.logError("Cliente no encontrado: id=" + idCliente);
            validationException = new ValidationException(
                BusinessErrorCode.CLIENTE_NO_ENCONTRADO,
                "No se puede crear el contrato: cliente no encontrado"
            );
        }

        if (clienteExiste) {
            motoExiste = motoDAO.buscarPorId(idMoto).isPresent();
        }

        if (clienteExiste && !motoExiste) {
            Logger.logError("Moto no encontrada: id=" + idMoto);
            validationException = new ValidationException(
                BusinessErrorCode.MOTO_NO_ENCONTRADA,
                "No se puede crear el contrato: moto no encontrada"
            );
        }

        if (clienteExiste && motoExiste) {
            motoDisponible = motoDAO.estaDisponible(idMoto);
            if (!motoDisponible) {
                Logger.logError("Moto no disponible: id=" + idMoto);
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
            Logger.log("Creando contrato para cliente id=" + idCliente + " con moto id=" + idMoto);
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
        Integer idMoto = contrato.getIdMoto();
        Optional<Contrato> contratoPersistido = contratoDAO.buscarPorId(contrato.getIdContrato());
        boolean contratoExiste = contratoPersistido.isPresent();
        Contrato contratoBase = contratoPersistido.orElse(null);
        Contrato contratoParaFinalizar = null;
        LocalDate fechaInicioBase = null;
        LocalDate fechaFinBase = null;
        double cantKmSalidaBase = 0.0;
        boolean motoExiste = false;
        boolean contratoYaFinalizado = false;
        boolean contratoBaseDisponible = contratoBase != null;
        boolean validacionBaseOk = false;
        boolean fechaEntregaValida = false;
        boolean rangoFechasContratoValido = false;
        boolean kilometrajeValido = false;
        boolean contratoPreparado = false;
        boolean puedeFinalizar = false;
        ValidationException validationException = null;
        int diasProrrogaReal = 0;
        double recargoProrroga = 0.0;
        double importeTotalTeorico = 0.0;

        if (!contratoExiste) {
            Logger.logError("Contrato no encontrado: " + contrato.getFechaInicio() + " / idMoto=" + idMoto);
            validationException = new ValidationException(
                BusinessErrorCode.CONTRATO_NO_ENCONTRADO,
                "No se puede finalizar el contrato: no existe"
            );
        }

        if (contratoExiste) {
            contratoBase = contratoPersistido.get();
            fechaInicioBase = contratoBase.getFechaInicio();
            fechaFinBase = contratoBase.getFechaFin();
            cantKmSalidaBase = contratoBase.getCantKmSalida();
            contratoBaseDisponible = true;
            contratoYaFinalizado = contratoBase.getFechaEntrega() != null;
            if (contratoYaFinalizado) {
                Logger.logError("Contrato ya finalizado: " + contrato.getFechaInicio() + " / idMoto=" + idMoto);
                validationException = new ValidationException(
                    BusinessErrorCode.CONTRATO_YA_FINALIZADO,
                    "No se puede finalizar el contrato: ya está finalizado"
                );
            }
        }

        if (contratoExiste && !contratoYaFinalizado) {
            motoExiste = motoDAO.buscarPorId(idMoto).isPresent();
        }

        if (contratoExiste && !contratoYaFinalizado && !motoExiste) {
            Logger.logError("Moto no encontrada: id=" + idMoto);
            validationException = new ValidationException(
                BusinessErrorCode.MOTO_NO_ENCONTRADA,
                "No se puede finalizar el contrato: moto no encontrada"
            );
        }

        if (contratoExiste && !contratoYaFinalizado && motoExiste) {
            validacionBaseOk = true;
        }

        if (validacionBaseOk && contratoBaseDisponible) {
            if (contrato.getFechaEntrega() != null && fechaInicioBase != null && !contrato.getFechaEntrega().isBefore(fechaInicioBase)) {
                fechaEntregaValida = true;
            }

            if (!fechaEntregaValida) {
                Logger.logError("Fecha de entrega inválida para contrato: "
                    + contrato.getFechaInicio() + " / idMoto=" + idMoto);
                validationException = new ValidationException(
                    BusinessErrorCode.CONTRATO_FECHA_ENTREGA_INVALIDA,
                    "No se puede finalizar el contrato: fecha de entrega inválida"
                );
            }
        }

        if (validacionBaseOk && fechaEntregaValida && contratoBaseDisponible) {
            if (fechaFinBase != null && fechaInicioBase != null && !fechaFinBase.isBefore(fechaInicioBase)) {
                rangoFechasContratoValido = true;
            } else {
                Logger.logError("Rango de fechas del contrato inválido para finalización: "
                    + contrato.getFechaInicio() + " / idMoto=" + idMoto);
                validationException = new ValidationException(
                    BusinessErrorCode.CONTRATO_FECHA_ENTREGA_INVALIDA,
                    "No se puede finalizar el contrato: fechas del contrato inválidas"
                );
            }
        }

        if (validacionBaseOk && fechaEntregaValida && rangoFechasContratoValido && contratoBaseDisponible) {
            if (contrato.getCantKmLlegada() >= cantKmSalidaBase) {
                kilometrajeValido = true;
            }

            if (!kilometrajeValido) {
                Logger.logError("Kilometraje inválido para contrato: "
                    + contrato.getFechaInicio() + " / idMoto=" + idMoto);
                validationException = new ValidationException(
                    BusinessErrorCode.CONTRATO_KM_INVALIDO,
                    "No se puede finalizar el contrato: kilometraje inválido"
                );
            }
        }

        if (kilometrajeValido && contratoBase != null) {
            contratoParaFinalizar = contratoBase;
            contratoParaFinalizar.setFechaEntrega(contrato.getFechaEntrega());
            contratoParaFinalizar.setCantKmLlegada(contrato.getCantKmLlegada());
            diasProrrogaReal = contratoParaFinalizar.calcularDiasProrrogaReal();
            contratoParaFinalizar.setDiasProrroga(diasProrrogaReal);
            recargoProrroga = contratoParaFinalizar.calcularRecargoProrroga();
            importeTotalTeorico = contratoParaFinalizar.calcularImporteTotalTeorico();
            contratoPreparado = true;
        }

        if (contratoPreparado) {
            puedeFinalizar = true;
        }

        if (puedeFinalizar) {
            Logger.log("Finalizando contrato: idMoto=" + idMoto
                + " | dias_prorroga=" + diasProrrogaReal
                + " | recargo_prorroga=" + recargoProrroga
                + " | total_teorico=" + importeTotalTeorico);
            contratoDAO.actualizar(contratoParaFinalizar);
            motoDAO.cambiarEstado(idMoto, Situacion.DISPONIBLE);
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
        boolean contratoExiste = contratoDAO.buscarPorId(contrato.getIdContrato()).isPresent();
        boolean puedeActualizar = false;
        ValidationException validationException = null;

        if (!contratoExiste) {
            Logger.logError("Contrato no encontrado para actualizar: "
                + contrato.getFechaInicio() + " / idMoto="
                + contrato.getIdMoto());
            validationException = new ValidationException(
                BusinessErrorCode.CONTRATO_NO_ENCONTRADO,
                "No se puede actualizar el contrato: no existe"
            );
        }

        if (contratoExiste) {
            puedeActualizar = true;
        }

        if (puedeActualizar) {
            Logger.log("Actualizando contrato: idMoto=" + contrato.getIdMoto());
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

    public Optional<Contrato> buscarPorId(Integer idContrato) {
        return contratoDAO.buscarPorId(idContrato);
    }

    public List<Contrato> listarTodos() {
        return contratoDAO.listarTodos();
    }

    public List<Contrato> listarContratosCompletos() {
        return contratoDAO.listarContratosCompletos();
    }

    public void eliminarContrato(Integer idContrato) {
        boolean contratoExiste = contratoDAO.buscarPorId(idContrato).isPresent();
        boolean puedeEliminar = false;
        ValidationException validationException = null;

        if (!contratoExiste) {
            Logger.logError("Contrato no encontrado para eliminar: id=" + idContrato);
            validationException = new ValidationException(
                BusinessErrorCode.CONTRATO_NO_ENCONTRADO,
                "No se puede eliminar el contrato: no existe"
            );
        }

        if (contratoExiste) {
            puedeEliminar = true;
        }

        if (puedeEliminar) {
            Logger.log("Eliminando contrato: id=" + idContrato);
            contratoDAO.eliminar(idContrato);
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
