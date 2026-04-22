package org.proyectobdmotos.services;

import org.proyectobdmotos.models.Contrato;

public class AgenciaService {

    private final ClienteService clienteService;
    private final MotoService motoService;
    private final ContratoService contratoService;


    public AgenciaService(ClienteService clienteService, MotoService motoService, ContratoService contratoService) {
        this.clienteService = clienteService;
        this.motoService = motoService;
        this.contratoService = contratoService;
    }

    public ClienteService getClienteService() {
        return clienteService;
    }
    public MotoService getMotoService() {
        return motoService;
    }
    public ContratoService getContratoService() {
        return contratoService;
    }

    public void finalizarContrato(Contrato contrato) {
        contratoService.finalizarContrato(contrato);
    }
}
