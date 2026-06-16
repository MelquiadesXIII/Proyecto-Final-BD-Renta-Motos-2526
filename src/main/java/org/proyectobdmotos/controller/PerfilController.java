package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.stores.AgenciaStore;

public class PerfilController {

    @FXML private StackPane rootPane;

    @FXML private Label labelIniciales;
    @FXML private Label labelNombreCompleto;
    @FXML private Label labelCI;
    @FXML private Label labelPrimerApellido;
    @FXML private Label labelSegundoApellido;
    @FXML private Label labelEdad;
    @FXML private Label labelSexo;
    @FXML private Label labelTelefono;

    private final AgenciaStore agenciaStore;

    public PerfilController(AgenciaStore agenciaStore) {
        this.agenciaStore = agenciaStore;
    }

    @FXML
    private void initialize() {
        if (rootPane != null) {
            rootPane.setStyle(
                    "-fx-background-image: url('"
                            + getClass().getResource("/Utiles/usuario-bg.jpg").toExternalForm()
                            + "');"
                            + "-fx-background-size: cover;"
                            + "-fx-background-position: center center;"
                            + "-fx-background-repeat: no-repeat;"
            );
        }

        Cliente cliente = agenciaStore.getClienteActual();
        boolean clienteExiste = cliente != null;

        if (clienteExiste) {
            labelNombreCompleto.setText(cliente.getNombreCompleto());
            labelIniciales.setText(iniciales(cliente));
            labelCI.setText(orDash(cliente.getCiCliente()));
            labelPrimerApellido.setText(orDash(cliente.getPrimerApellido()));
            labelSegundoApellido.setText(orDash(cliente.getSegundoApellido()));
            labelEdad.setText(cliente.getEdad() + " años");
            labelSexo.setText(cliente.getSexo() != null ? capitalize(cliente.getSexo().getValor()) : "—");
            labelTelefono.setText(orDash(cliente.getNumeroContacto()));
        } else {
            labelNombreCompleto.setText("Usuario");
            labelIniciales.setText("?");
        }
    }

    private String iniciales(Cliente c) {
        StringBuilder sb = new StringBuilder();
        if (c.getNombreCliente() != null && !c.getNombreCliente().isEmpty()) {
            sb.append(Character.toUpperCase(c.getNombreCliente().charAt(0)));
        }
        if (c.getPrimerApellido() != null && !c.getPrimerApellido().isEmpty()) {
            sb.append(Character.toUpperCase(c.getPrimerApellido().charAt(0)));
        }
        String resultado = sb.length() > 0 ? sb.toString() : "?";
        return resultado;
    }

    private String orDash(String value) {
        String resultado = (value != null && !value.isBlank()) ? value : "—";
        return resultado;
    }

    private String capitalize(String text) {
        String resultado;
        if (text == null || text.isEmpty()) {
            resultado = "—";
        } else {
            resultado = Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase();
        }
        return resultado;
    }
}