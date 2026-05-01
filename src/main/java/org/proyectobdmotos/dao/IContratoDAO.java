package org.proyectobdmotos.dao;

import java.util.List;

import org.proyectobdmotos.models.Contrato;

public interface IContratoDAO extends GenericDAO<Contrato, Integer> {

    List<Contrato> listarContratosCompletos();
}
