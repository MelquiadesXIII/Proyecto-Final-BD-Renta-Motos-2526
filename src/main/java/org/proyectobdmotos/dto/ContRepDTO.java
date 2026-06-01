package org.proyectobdmotos.dto;

import java.time.LocalDate;

public class ContRepDTO {
    private final String nombreCliente;
    private final String matricula;
    private final String marca;
    private final String modelo;
    private final String formaPago;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final int prorrogaDias;
    private final String seguroAdicional;
    private final double importeTotal;

    public ContRepDTO(String nombreCliente, String matricula, String marca,
                      String modelo, String formaPago, LocalDate fechaInicio,
                      LocalDate fechaFin, int prorrogaDias, String seguroAdicional,
                      double importeTotal) {
        this.nombreCliente = nombreCliente;
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
        this.formaPago = formaPago;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.prorrogaDias = prorrogaDias;
        this.seguroAdicional = seguroAdicional;
        this.importeTotal = importeTotal;
    }

    public String getNombreCliente() { return nombreCliente; }
    public String getMatricula() { return matricula; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public String getFormaPago() { return formaPago; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public int getProrrogaDias() { return prorrogaDias; }
    public String getSeguroAdicional() { return seguroAdicional; }
    public double getImporteTotal() { return importeTotal; }
}