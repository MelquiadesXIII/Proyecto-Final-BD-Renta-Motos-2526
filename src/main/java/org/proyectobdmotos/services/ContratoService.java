package org.proyectobdmotos.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dao.IClienteDAO;
import org.proyectobdmotos.dao.IContratoDAO;
import org.proyectobdmotos.dao.IMotoDAO;
import org.proyectobdmotos.dto.*;
import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.models.Situacion;
import org.proyectobdmotos.services.exceptions.BusinessErrorCode;
import org.proyectobdmotos.services.exceptions.ValidationException;
import org.proyectobdmotos.utils.Logger;

/**
 * Orquesta las operaciones de negocio relacionadas con los contratos.
 * Valida la existencia del cliente y la disponibilidad de la moto antes
 * de crear o finalizar un contrato, delegando la persistencia en los DAOs.
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

    // -----------------------------------------------------------------
    // Creación de contrato
    // -----------------------------------------------------------------

    /**
     * Crea un nuevo contrato tras validar que el cliente existe y que la moto
     * está disponible en el período solicitado. Si alguna validación falla,
     * lanza una excepción con el código de error correspondiente.
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
            boolean haySolapamiento = motoDAO.existeSolapamiento(idMoto,
                    contrato.getFechaInicio(), contrato.getFechaFin());
            if (haySolapamiento) {
                Logger.logError("Moto con solapamiento: id=" + idMoto +
                        " periodo [" + contrato.getFechaInicio() + " – " + contrato.getFechaFin() + "]");
                validationException = new ValidationException(
                        BusinessErrorCode.MOTO_NO_DISPONIBLE,
                        "No se puede crear el contrato: la moto ya está alquilada en ese período"
                );
            } else {
                motoDisponible = true;
            }
        }

        if (clienteExiste && motoExiste && motoDisponible) {
            puedeCrear = true;
        }

        if (puedeCrear) {
            Logger.log("Creando contrato para cliente id=" + idCliente + " con moto id=" + idMoto);
            contratoDAO.insertar(contrato);
            motoDAO.cambiarEstado(idMoto, Situacion.ALQUILADA);
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

    // -----------------------------------------------------------------
    // Finalización de contrato
    // -----------------------------------------------------------------

    /**
     * Finaliza un contrato existente. Valida que el contrato no esté ya finalizado,
     * que la moto exista, que la fecha de entrega sea válida y que el kilometraje
     * de llegada no sea inferior al de salida. Si todo es correcto, actualiza el
     * contrato y cambia el estado de la moto a DISPONIBLE.
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
            // Después de cambiar el estado de la moto...
            motoDAO.actualizarKilometros(idMoto, contratoParaFinalizar.getCantKmLlegada());
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

    // -----------------------------------------------------------------
    // Actualización y eliminación
    // -----------------------------------------------------------------

    /**
     * Actualiza los datos de un contrato existente. Si no se encuentra,
     * lanza una excepción de validación.
     */
    public void actualizarContrato(Contrato contrato) {
        boolean contratoExiste = contratoDAO.buscarPorId(contrato.getIdContrato()).isPresent();
        if (contratoExiste) {
            Logger.log("Actualizando contrato: idMoto=" + contrato.getIdMoto());
            contratoDAO.actualizar(contrato);
        } else {
            Logger.logError("Contrato no encontrado para actualizar: "
                    + contrato.getFechaInicio() + " / idMoto=" + contrato.getIdMoto());
            throw new ValidationException(
                    BusinessErrorCode.CONTRATO_NO_ENCONTRADO,
                    "No se puede actualizar el contrato: no existe"
            );
        }
    }

    /**
     * Elimina un contrato por su identificador. Lanza excepción si no existe.
     */
    public void eliminarContrato(Integer idContrato) {
        boolean contratoExiste = contratoDAO.buscarPorId(idContrato).isPresent();
        if (contratoExiste) {
            Logger.log("Eliminando contrato: id=" + idContrato);
            contratoDAO.eliminar(idContrato);
        } else {
            Logger.logError("Contrato no encontrado para eliminar: id=" + idContrato);
            throw new ValidationException(
                    BusinessErrorCode.CONTRATO_NO_ENCONTRADO,
                    "No se puede eliminar el contrato: no existe"
            );
        }
    }

    // -----------------------------------------------------------------
    // Finalización simplificada (usada por la interfaz de usuario)
    // -----------------------------------------------------------------

    /**
     * Finaliza un contrato usando su ID, estableciendo como fecha de entrega
     * el día actual. Útil para la acción rápida desde la lista de contratos.
     */
    public void finalizarContrato(int idContrato) {
        Optional<Contrato> opt = contratoDAO.buscarPorId(idContrato);
        if (opt.isEmpty()) {
            throw new ValidationException(BusinessErrorCode.CONTRATO_NO_ENCONTRADO, "Contrato no existe");
        }
        Contrato c = opt.get();
        c.setFechaEntrega(LocalDate.now());
        finalizarContrato(c);
    }

    // -----------------------------------------------------------------
    // Consultas
    // -----------------------------------------------------------------

    /** Busca un contrato por su identificador. */
    public Optional<Contrato> buscarPorId(Integer idContrato) {
        return contratoDAO.buscarPorId(idContrato);
    }

    /** Lista todos los contratos. */
    public List<Contrato> listarTodos() {
        return contratoDAO.listarTodos();
    }

    /** Lista los contratos completos (con joins a cliente y moto). */
    public List<Contrato> listarContratosCompletos() {
        return contratoDAO.listarContratosCompletos();
    }

    /** Lista los contratos asociados a un cliente específico. */
    public List<MisContratosDTO> listarMisContratos(int idCliente) {
        return contratoDAO.listarMisContratos(idCliente);
    }

    // ===================== REPORTES =====================

    /** Lista los contratos para el reporte general. */
    public List<ContRepDTO> listarContratosReporte() {
        return contratoDAO.listarContratosReporte();
    }

    /** Obtiene el resumen de contratos por marcas y modelos. */
    public List<ResMarModDTO> resumenMarcasModelos() {
        return contratoDAO.resumenMarcasModelos();
    }

    /** Obtiene el resumen de contratos por municipios. */
    public List<ResMunDTO> resumenMunicipios() {
        return contratoDAO.resumenMunicipios();
    }

    /** Obtiene el reporte de ingresos anuales. */
    public List<IngAnualDTO> ingresosAnuales() {
        return contratoDAO.ingresosAnuales();
    }

    public boolean tieneContratoAnteriorActivo(int idMoto, int idContratoActual) {
        return contratoDAO.tieneContratoAnteriorActivo(idMoto, idContratoActual);
    }
}