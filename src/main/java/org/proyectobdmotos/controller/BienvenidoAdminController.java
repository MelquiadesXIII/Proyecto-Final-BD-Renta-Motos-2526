package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.proyectobdmotos.models.Usuario;
import org.proyectobdmotos.stores.AgenciaStore;

public class BienvenidoAdminController {

    @FXML private Label labelBienvenida;

    private final AgenciaStore agenciaStore;

    public BienvenidoAdminController(AgenciaStore agenciaStore) {
        this.agenciaStore = agenciaStore;
    }

    @FXML
    public void initialize() {
        Usuario u = agenciaStore.getUsuarioActual();
        if (u != null) {
            labelBienvenida.setText("Bienvenido Gran Administrador: " + u.getNombreUsuario() + " – Esperando sus órdenes");
        } else {
            labelBienvenida.setText("Bienvenido a la Administración de las Motos");
        }
    }
}