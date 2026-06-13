package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.stores.AgenciaStore;

public class PerfilController {

    @FXML private Label labelNombreUsuario;
    private final AgenciaStore agenciaStore;

    public PerfilController(AgenciaStore agenciaStore) {
        this.agenciaStore = agenciaStore;
    }

    /**
     * Al cargar la pantalla de perfil, obtiene el cliente actual del store
     * y muestra un saludo personalizado. Si no hay cliente, muestra un mensaje genérico.
     */
    @FXML
    private void initialize() {
        Cliente cliente = agenciaStore.getClienteActual();
        if (cliente != null) {
            labelNombreUsuario.setText("Bienvenido, " + cliente.getNombreCliente());
        } else {
            labelNombreUsuario.setText("Bienvenido");
        }
    }
}