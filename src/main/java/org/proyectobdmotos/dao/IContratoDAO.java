package org.proyectobdmotos.dao;

import java.util.List;

import org.proyectobdmotos.dto.*;
import org.proyectobdmotos.models.Contrato;

public interface IContratoDAO extends GenericDAO<Contrato, Integer> {

    List<Contrato> listarContratosCompletos();
    
    List<ContRepDTO> listarContratosReporte();

    List<ResMarModDTO> resumenMarcasModelos();

    List<ResMunDTO> resumenMunicipios();

    List<IngAnualDTO> ingresosAnuales();

    List<MisContratosDTO> listarMisContratos(int idCliente);

    boolean tieneContratoAnteriorActivo(int idMoto, int idContratoActual);
    List<Contrato> listarTodos();
}
