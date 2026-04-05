package org.proyectobdmotos.controller;

import java.awt.Color;
import java.sql.Connection;
import java.util.ArrayList;

import org.proyectobdmotos.dao.ClienteDAO;
import org.proyectobdmotos.dao.ContratoDAO;
import org.proyectobdmotos.dao.MotoDAO;
import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.models.Marca;
import org.proyectobdmotos.models.Modelo;
import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.models.Municipio;

@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")

public class Agencia {

  //Datos del negocio
  private final ArrayList<Cliente> clientes;
  private final ArrayList<Moto> motos;
  private final ArrayList<Contrato> contratos;

  //Catalogos de referencia
  private final ArrayList<Municipio> municipios;
  private final ArrayList<Color> colores;
  private final ArrayList<Marca> marcas;
  private final ArrayList<Modelo> modelos;

  //DAOs
  private final ClienteDAO clienteDAO;
  private final MotoDAO motoDAO;
  private final ContratoDAO contratoDAO;

  public Agencia(Connection connection) {
      clientes = new ArrayList<>();
      motos = new ArrayList<>();
      contratos = new ArrayList<>();

      municipios = new ArrayList<>();
      colores = new ArrayList<>();
      marcas = new ArrayList<>();
      modelos = new ArrayList<>();
      
      clienteDAO = new ClienteDAO(connection);
      motoDAO = new MotoDAO(connection);
      contratoDAO = new ContratoDAO(connection);
  }

  //Getters
  public ArrayList<Cliente> getClientes() {
    return clientes;
  }
  public ArrayList<Moto> getMotos() {
    return motos;
  }
  public ArrayList<Contrato> getContratos() {
    return contratos;
  }
  public ArrayList<Municipio> getMunicipios() {
    return municipios;
  }
  public ArrayList<Color> getColores() {
    return colores;
  }
  public ArrayList<Marca> getMarcas() {
    return marcas;
  }
  public ArrayList<Modelo> getModelos() {
      return modelos;
  }
  public ClienteDAO getClienteDAO() {
    return clienteDAO;
  }
  public MotoDAO getMotoDAO() {
    return motoDAO;
  }
  public ContratoDAO getContratoDAO() {
    return contratoDAO;
  }

  //Metodos
  public void cargarDatos() {

  }
  
}
