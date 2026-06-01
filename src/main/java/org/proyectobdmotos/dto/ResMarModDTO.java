package org.proyectobdmotos.dto;

import java.time.LocalDate;

public class ResMarModDTO {
    private final LocalDate fecha;
    private final String marca;
    private final String modelo;
    private final long cantidadMotos;
    private final double diasTotales;
    private final double ingresosTarjeta;
    private final double ingresosCheque;
    private final double ingresosEfectivo;
    private final double totalIngresosMarca;
    private final double totalGeneral;

    public ResMarModDTO(LocalDate fecha, String marca, String modelo,
                        long cantidadMotos, double diasTotales,
                        double ingresosTarjeta, double ingresosCheque,
                        double ingresosEfectivo, double totalIngresosMarca,
                        double totalGeneral) {
        this.fecha = fecha;
        this.marca = marca;
        this.modelo = modelo;
        this.cantidadMotos = cantidadMotos;
        this.diasTotales = diasTotales;
        this.ingresosTarjeta = ingresosTarjeta;
        this.ingresosCheque = ingresosCheque;
        this.ingresosEfectivo = ingresosEfectivo;
        this.totalIngresosMarca = totalIngresosMarca;
        this.totalGeneral = totalGeneral;
    }

    public LocalDate getFecha() { return fecha; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public long getCantidadMotos() { return cantidadMotos; }
    public double getDiasTotales() { return diasTotales; }
    public double getIngresosTarjeta() { return ingresosTarjeta; }
    public double getIngresosCheque() { return ingresosCheque; }
    public double getIngresosEfectivo() { return ingresosEfectivo; }
    public double getTotalIngresosMarca() { return totalIngresosMarca; }
    public double getTotalGeneral() { return totalGeneral; }
}