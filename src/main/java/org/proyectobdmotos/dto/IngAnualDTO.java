package org.proyectobdmotos.dto;

public class IngAnualDTO {
    private final String mes;
    private final double ingresoMensual;

    public IngAnualDTO(String mes, double ingresoMensual) {
        this.mes = mes;
        this.ingresoMensual = ingresoMensual;
    }

    public String getMes() { return mes; }
    public double getIngresoMensual() { return ingresoMensual; }
}