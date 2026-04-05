package org.proyectobdmotos.services;

import org.proyectobdmotos.dao.ClienteDAO;
import org.proyectobdmotos.dao.ContratoDAO;
import org.proyectobdmotos.dao.MotoDAO;

/**
 * ContratoService: orquesta operaciones de contratos.
 * Necesita acceso a todos los DAOs porque un contrato involucra:
 * - Cliente (quien renta)
 * - Moto (qué se renta)
 * - Contrato (el registro de la renta)
 */
public class ContratoService {

    private final ContratoDAO contratoDAO;
    private final ClienteDAO clienteDAO;
    private final MotoDAO motoDAO;

    public ContratoService(ContratoDAO contratoDAO, ClienteDAO clienteDAO, MotoDAO motoDAO) {
        this.contratoDAO = contratoDAO;
        this.clienteDAO = clienteDAO;
        this.motoDAO = motoDAO;
    }

    public ContratoDAO getContratoDAO() {
        return contratoDAO;
    }

    public ClienteDAO getClienteDAO() {
        return clienteDAO;
    }

    public MotoDAO getMotoDAO() {
        return motoDAO;
    }

    // TODO: Agregar métodos de negocio como:
    // - crearContrato(clienteId, motoId, fechas...) → valida cliente, verifica disponibilidad moto, crea contrato
    // - finalizarContrato(contratoId) → actualiza estado contrato y moto
}

