package org.proyectobdmotos.dto;

import java.time.LocalDate;

import org.proyectobdmotos.models.Situacion;

/**
 * DTO para el reporte de situación de motos.
 */
public class SituacionMotoDTO {

    private final String matricula;
    private final String marca;
    private final Situacion situacion;
    private final LocalDate fechaFinContrato;

    public SituacionMotoDTO(String matricula, String marca, Situacion situacion, LocalDate fechaFinContrato) {
        this.matricula = matricula;
        this.marca = marca;
        this.situacion = situacion;
        this.fechaFinContrato = fechaFinContrato;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getMarca() {
        return marca;
    }

    public Situacion getSituacion() {
        return situacion;
    }

    public LocalDate getFechaFinContrato() {
        return fechaFinContrato;
    }
}
