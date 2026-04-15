package org.proyectobdmotos.dao;

import java.util.List;

import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.models.ContratoID;

public interface IContratoDAO extends GenericDAO<Contrato, ContratoID> {

    List<Contrato> listarContratosCompletos();
}
