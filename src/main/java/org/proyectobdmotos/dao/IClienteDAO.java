package org.proyectobdmotos.dao;

import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dto.ClienteDTO;
import org.proyectobdmotos.models.Cliente;

public interface IClienteDAO extends GenericDAO<Cliente, Integer> {

    List<ClienteDTO> listarClientesPorMunicipio();

    List<Cliente> obtenerClientesIncumplidores();

    void eliminarConCascada(Integer idCliente);

    Optional<Cliente> buscarPorCi(String ci);
}