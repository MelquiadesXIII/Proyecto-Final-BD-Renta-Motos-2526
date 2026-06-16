package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.stores.AgenciaStore;

public class PerfilController {

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
        Cliente cliente = agenciaStore.getClienteActual();
        if (cliente == null) {
            labelNombreCompleto.setText("Usuario");
            labelIniciales.setText("?");
            return;
        }

        labelNombreCompleto.setText(cliente.getNombreCompleto());
        labelIniciales.setText(iniciales(cliente));
        labelCI.setText(orDash(cliente.getCiCliente()));
        labelPrimerApellido.setText(orDash(cliente.getPrimerApellido()));
        labelSegundoApellido.setText(orDash(cliente.getSegundoApellido()));
        labelEdad.setText(cliente.getEdad() + " años");
        labelSexo.setText(cliente.getSexo() != null ? capitalize(cliente.getSexo().getValor()) : "—");
        labelTelefono.setText(orDash(cliente.getNumeroContacto()));
    }

    private String iniciales(Cliente c) {
        StringBuilder sb = new StringBuilder();
        if (c.getNombreCliente() != null && !c.getNombreCliente().isEmpty())
            sb.append(Character.toUpperCase(c.getNombreCliente().charAt(0)));
        if (c.getPrimerApellido() != null && !c.getPrimerApellido().isEmpty())
            sb.append(Character.toUpperCase(c.getPrimerApellido().charAt(0)));
        return sb.length() > 0 ? sb.toString() : "?";
    }

    private String orDash(String value) {
        return (value != null && !value.isBlank()) ? value : "—";
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "—";
        return Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase();
    }
}
