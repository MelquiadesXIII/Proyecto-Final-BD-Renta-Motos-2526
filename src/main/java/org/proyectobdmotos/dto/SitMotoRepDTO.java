package org.proyectobdmotos.dto;

import java.time.LocalDate;

public class SitMotoRepDTO {
    private final LocalDate fecha;
    private final String matriculaMarca;
    private final String situacion;
    private final LocalDate fechaFinContrato;

    public SitMotoRepDTO(LocalDate fecha, String matriculaMarca,
                         String situacion, LocalDate fechaFinContrato) {
        this.fecha = fecha;
        this.matriculaMarca = matriculaMarca;
        this.situacion = situacion;
        this.fechaFinContrato = fechaFinContrato;
    }

    public LocalDate getFecha() { return fecha; }
    public String getMatriculaMarca() { return matriculaMarca; }
    public String getSituacion() { return situacion; }
    public LocalDate getFechaFinContrato() { return fechaFinContrato; }
}