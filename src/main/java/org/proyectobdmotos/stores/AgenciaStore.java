package org.proyectobdmotos.stores;

import java.util.Collection;

import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.models.Moto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class AgenciaStore {

    private final ObservableList<Cliente> clientes;
    private final ObservableList<Moto> motos;
    private final ObservableList<Contrato> contratos;

    public AgenciaStore() {
        clientes = FXCollections.observableArrayList();
        contratos = FXCollections.observableArrayList();
        motos = FXCollections.observableArrayList();
    }

    public ObservableList<Cliente> getClientes() {
        return clientes;
    }
    public ObservableList<Moto> getMotos() {
        return motos;
    }
    public ObservableList<Contrato> getContratos() {
        return contratos;
    }

    public void setClientes(Collection<Cliente> nuevos) {
        clientes.setAll(nuevos);
    }

    public void setMotos(Collection<Moto> nuevos) {
        motos.setAll(nuevos);
    }

    public void setContratos(Collection<Contrato> nuevos) {
        contratos.setAll(nuevos);
    }
}
