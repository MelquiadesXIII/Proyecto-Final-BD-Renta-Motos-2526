package org.proyectobdmotos.dto;

import java.time.LocalDate;

public class IncumpDTO {
    private final LocalDate fecha;
    private final String nombreCompleto;
    private final LocalDate fechaFin;
    private final LocalDate fechaEntrega;

    public IncumpDTO(LocalDate fecha, String nombreCompleto,
                     LocalDate fechaFin, LocalDate fechaEntrega) {
        this.fecha = fecha;
        this.nombreCompleto = nombreCompleto;
        this.fechaFin = fechaFin;
        this.fechaEntrega = fechaEntrega;
    }

    public LocalDate getFecha() { return fecha; }
    public String getNombreCompleto() { return nombreCompleto; }
    public LocalDate getFechaFin() { return fechaFin; }
    public LocalDate getFechaEntrega() { return fechaEntrega; }
}