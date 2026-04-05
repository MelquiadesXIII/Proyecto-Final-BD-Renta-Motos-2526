package org.proyectobdmotos.controller;

import org.proyectobdmotos.services.ContratoService;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;

/**
 * ContratoController: maneja eventos de la UI de contratos.
 * Delega operaciones a ContratoService y actualiza/observa AgenciaStore.
 */
public class ContratoController {

    private final ContratoService contratoService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;

    public ContratoController(
        ContratoService contratoService,
        AgenciaStore agenciaStore,
        ReferenceDataStore referenceDataStore
    ) {
        this.contratoService = contratoService;
        this.agenciaStore = agenciaStore;
        this.referenceDataStore = referenceDataStore;
    }

    // TODO: Agregar métodos handlers (@FXML) cuando se diseñe la UI
}
