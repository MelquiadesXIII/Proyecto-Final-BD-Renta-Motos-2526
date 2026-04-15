package org.proyectobdmotos.dao;

import java.util.List;

import org.proyectobdmotos.dto.ClienteDTO;
import org.proyectobdmotos.models.Cliente;

public interface IClienteDAO extends GenericDAO<Cliente, String> {

    List<ClienteDTO> listarClientesPorMunicipio();

    List<Cliente> obtenerClientesIncumplidores();

    void eliminarConCascada(String ci);
}