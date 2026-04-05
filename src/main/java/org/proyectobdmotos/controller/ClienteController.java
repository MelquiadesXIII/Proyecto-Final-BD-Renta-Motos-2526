package org.proyectobdmotos.controller;

import org.proyectobdmotos.services.ClienteService;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;

/**
 * ClienteController: maneja eventos de la UI de clientes.
 * Delega operaciones a ClienteService y actualiza/observa AgenciaStore.
 */
public class ClienteController {

    private final ClienteService clienteService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;

    public ClienteController(
        ClienteService clienteService,
        AgenciaStore agenciaStore,
        ReferenceDataStore referenceDataStore
    ) {
        this.clienteService = clienteService;
        this.agenciaStore = agenciaStore;
        this.referenceDataStore = referenceDataStore;
    }

    // TODO: Agregar métodos handlers (@FXML) cuando se diseñe la UI
}
