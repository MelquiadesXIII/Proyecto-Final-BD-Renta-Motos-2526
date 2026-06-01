package org.proyectobdmotos.dto;

import java.time.LocalDate;

public class IngAnualDTO {
    private final LocalDate fecha;
    private final double ingresoTotalAnual;
    private final String mes;
    private final double ingresoMensual;

    public IngAnualDTO(LocalDate fecha, double ingresoTotalAnual,
                       String mes, double ingresoMensual) {
        this.fecha = fecha;
        this.ingresoTotalAnual = ingresoTotalAnual;
        this.mes = mes;
        this.ingresoMensual = ingresoMensual;
    }

    public LocalDate getFecha() { return fecha; }
    public double getIngresoTotalAnual() { return ingresoTotalAnual; }
    public String getMes() { return mes; }
    public double getIngresoMensual() { return ingresoMensual; }
}