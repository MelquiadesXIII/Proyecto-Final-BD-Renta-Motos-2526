package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.proyectobdmotos.services.ClienteService;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.services.UsuarioService;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;

public record MotoControllerForm()
{
    @FXML
    private TextField campoMatricula;
    @FXML
    private ComboBox<String> comboModelo;
    @FXML
    private ComboBox<String> comboColor;
    @FXML
    private TextField campoKilometros;

    private final MotoService clienteService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;

    public MotoFormController(MotoService motoService,
                              AgenciaStore agenciaStore,
                              ReferenceDataStore referenceDataStore)
    {
        this.motoService = motoService;
        this.agenciaStore = agenciaStore;
        this.referenceDataStore= referenceDataStore;
    }


    public void initialize()
    {
        comboColor.getItems()
    }

}
