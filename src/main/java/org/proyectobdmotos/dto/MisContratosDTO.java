package org.proyectobdmotos.dto;

public class MisContratosDTO {
    private int idContrato;
    private String motoInfo;
    private String fechaInicio;
    private String fechaFin;
    private double costoTotal;
    private String fechaEntrega;
    public MisContratosDTO(int idContrato, String motoInfo, String fechaInicio, String fechaFin,
                           double costoTotal, String fechaEntrega) {
        this.idContrato = idContrato;
        this.motoInfo = motoInfo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.costoTotal = costoTotal;
        this.fechaEntrega = fechaEntrega;
    }

    public int getIdContrato() { return idContrato; }
    public String getMotoInfo() { return motoInfo; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public double getCostoTotal() { return costoTotal; }
    public String getFechaEntrega() { return fechaEntrega; }
}