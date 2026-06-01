package org.proyectobdmotos.dto;

import java.time.LocalDate;

public class CliRepDTO {
    private final LocalDate fecha;
    private final String municipio;
    private final String nombre;
    private final String ci;
    private final int cantidadContratos;
    private final double totalGastado;

    public CliRepDTO(LocalDate fecha, String municipio, String nombre,
                     String ci, int cantidadContratos, double totalGastado) {
        this.fecha = fecha;
        this.municipio = municipio;
        this.nombre = nombre;
        this.ci = ci;
        this.cantidadContratos = cantidadContratos;
        this.totalGastado = totalGastado;
    }

    public LocalDate getFecha() { return fecha; }
    public String getMunicipio() { return municipio; }
    public String getNombre() { return nombre; }
    public String getCi() { return ci; }
    public int getCantidadContratos() { return cantidadContratos; }
    public double getTotalGastado() { return totalGastado; }
}