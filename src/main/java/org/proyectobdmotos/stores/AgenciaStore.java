package org.proyectobdmotos.stores;

import java.util.Collection;

import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.models.Moto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.proyectobdmotos.models.Usuario;

public final class AgenciaStore {

    private final ObservableList<Cliente> clientes;
    private final ObservableList<Moto> motos;
    private final ObservableList<Contrato> contratos;
    private Cliente clienteActual;
    private Usuario UsuarioActual;



    public AgenciaStore() {
        clientes = FXCollections.observableArrayList();
        contratos = FXCollections.observableArrayList();
        motos = FXCollections.observableArrayList();
    }

    // ============ Listas observables ============
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

    // ============ Cliente actual ============
    public Cliente getClienteActual() {
        return clienteActual;
    }
    public void setClienteActual(Cliente cliente) {
        this.clienteActual = cliente;
    }

    // ============ Usuario actual ============
    public Usuario getUsuarioActual() {
        return UsuarioActual;
    }

    public void setUsuarioActual(Usuario usuarioActual) {
        UsuarioActual = usuarioActual;
    }
}