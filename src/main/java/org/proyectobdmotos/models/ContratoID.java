package org.proyectobdmotos.models;

import java.time.LocalDate;

public class ContratoID {

    private LocalDate fechaInicio;
    private String matriculaMoto;
    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ContratoID(LocalDate fechaInicio, String matriculaMoto) {
        setFechaInicio(fechaInicio);
        setMatriculaMoto(matriculaMoto);
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public String getMatriculaMoto() {
        return matriculaMoto;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    public void setMatriculaMoto(String matriculaMoto) {
        this.matriculaMoto = matriculaMoto;
    }
}