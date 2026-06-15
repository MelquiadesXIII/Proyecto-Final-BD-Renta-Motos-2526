package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.stores.AgenciaStore;

public class BienvenidoUsuarioController {

    @FXML private Label labelBienvenida;
    @FXML private StackPane rootPane;

    private final AgenciaStore agenciaStore;

    public BienvenidoUsuarioController(AgenciaStore agenciaStore) {
        this.agenciaStore = agenciaStore;
    }

    @FXML
    private void initialize() {
        if (rootPane != null) {
            rootPane.setStyle(
                    "-fx-background-image: url('"
                            + getClass().getResource("/Utiles/intro_usuario.jpg").toExternalForm()
                            + "');"
                            + "-fx-background-size: cover;"
                            + "-fx-background-position: center center;"
                            + "-fx-background-repeat: no-repeat;"
            );
        }

        Cliente cliente = agenciaStore.getClienteActual();
        String nombre = (cliente != null) ? cliente.getNombreCliente() : "Usuario";
        labelBienvenida.setText("Bienvenido Usuario " + nombre);
    }
}