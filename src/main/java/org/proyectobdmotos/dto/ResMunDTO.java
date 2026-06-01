package org.proyectobdmotos.dto;

import java.time.LocalDate;

public class ResMunDTO {
    private final LocalDate fecha;
    private final String municipio;
    private final String marca;
    private final String modelo;
    private final double diasAlquilados;
    private final double diasProrroga;
    private final double valorEfectivo;
    private final double valorTotal;

    public ResMunDTO(LocalDate fecha, String municipio, String marca,
                     String modelo, double diasAlquilados,
                     double diasProrroga, double valorEfectivo,
                     double valorTotal) {
        this.fecha = fecha;
        this.municipio = municipio;
        this.marca = marca;
        this.modelo = modelo;
        this.diasAlquilados = diasAlquilados;
        this.diasProrroga = diasProrroga;
        this.valorEfectivo = valorEfectivo;
        this.valorTotal = valorTotal;
    }

    public LocalDate getFecha() { return fecha; }
    public String getMunicipio() { return municipio; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public double getDiasAlquilados() { return diasAlquilados; }
    public double getDiasProrroga() { return diasProrroga; }
    public double getValorEfectivo() { return valorEfectivo; }
    public double getValorTotal() { return valorTotal; }
}