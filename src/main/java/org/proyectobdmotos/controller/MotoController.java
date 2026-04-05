package org.proyectobdmotos.controller;

import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;

/**
 * MotoController: maneja eventos de la UI de motos.
 * Delega operaciones a MotoService y actualiza/observa AgenciaStore.
 */
public class MotoController {

    private final MotoService motoService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;

    public MotoController(
        MotoService motoService,
        AgenciaStore agenciaStore,
        ReferenceDataStore referenceDataStore
    ) {
        this.motoService = motoService;
        this.agenciaStore = agenciaStore;
        this.referenceDataStore = referenceDataStore;
    }

    // TODO: Agregar métodos handlers (@FXML) cuando se diseñe la UI
}
