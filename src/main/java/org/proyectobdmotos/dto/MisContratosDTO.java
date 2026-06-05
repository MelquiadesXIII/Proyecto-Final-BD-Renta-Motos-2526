package org.proyectobdmotos.dto;

import java.time.LocalDate;

public class MisContratosDTO {
    private final int idContrato;
    private final String matriculaMoto;
    private final String marca;
    private final String modelo;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final String estado;     
    private final double importe;

    public MisContratosDTO(int idContrato, String matriculaMoto, String marca,
                           String modelo, LocalDate fechaInicio, LocalDate fechaFin,
                           String estado, double importe) {
        this.idContrato = idContrato;
        this.matriculaMoto = matriculaMoto;
        this.marca = marca;
        this.modelo = modelo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.importe = importe;
    }

    public int getIdContrato() { return idContrato; }
    public String getMatriculaMoto() { return matriculaMoto; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public String getEstado() { return estado; }
    public double getImporte() { return importe; }
}