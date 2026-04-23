package org.proyectobdmotos.dao;

import java.util.List;

import org.proyectobdmotos.models.Contrato;
// import org.proyectobdmotos.models.ContratoID; // removed after refactor

public interface IContratoDAO extends GenericDAO<Contrato, Integer> {

    List<Contrato> listarContratosCompletos();
}
