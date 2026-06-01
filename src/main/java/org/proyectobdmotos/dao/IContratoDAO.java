package org.proyectobdmotos.dao;

import java.util.List;

import org.proyectobdmotos.dto.ContRepDTO;
import org.proyectobdmotos.dto.IngAnualDTO;
import org.proyectobdmotos.dto.ResMarModDTO;
import org.proyectobdmotos.dto.ResMunDTO;
import org.proyectobdmotos.models.Contrato;

public interface IContratoDAO extends GenericDAO<Contrato, Integer> {

    List<Contrato> listarContratosCompletos();
    
    List<ContRepDTO> listarContratosReporte();

    List<ResMarModDTO> resumenMarcasModelos();

    List<ResMunDTO> resumenMunicipios();

    List<IngAnualDTO> ingresosAnuales();
}
