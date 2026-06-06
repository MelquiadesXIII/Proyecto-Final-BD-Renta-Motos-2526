package org.proyectobdmotos.dao;

import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dto.CliRepDTO;
import org.proyectobdmotos.dto.ClienteDTO;
import org.proyectobdmotos.dto.ClienteUsuarioDTO;
import org.proyectobdmotos.dto.IncumpDTO;
import org.proyectobdmotos.models.Cliente;

public interface IClienteDAO extends GenericDAO<Cliente, Integer> {

    List<ClienteDTO> listarClientesPorMunicipio();

    List<Cliente> obtenerClientesIncumplidores();

    void eliminarConCascada(Integer idCliente);

    Optional<Cliente> buscarPorCi(String ci);

    List<CliRepDTO> listarClientesReporte();

    List<IncumpDTO> listarIncumplidores();

    Optional<Cliente> buscarPorId(int idCliente) ;

    Optional<Cliente> buscarPorIdUsuario(int idUsuario);

    List<ClienteUsuarioDTO> listarClientesConUsuario();

    String obtenerNombreMunicipio(int idMunicipio);

    List<Cliente> buscarClientesPorTexto(String texto);
}