package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.stores.AgenciaStore;

public class BienvenidoUsuarioController {

    @FXML private Label labelBienvenida;
    private final AgenciaStore agenciaStore;

    public BienvenidoUsuarioController(AgenciaStore agenciaStore) {
        this.agenciaStore = agenciaStore;
    }

    @FXML
    private void initialize() {
        Cliente cliente = agenciaStore.getClienteActual();
        String nombre = (cliente != null) ? cliente.getNombreCliente() : "Usuario";
        labelBienvenida.setText("Bienvenido Usuario " + nombre);
    }
}