package org.proyectobdmotos.models;

import java.time.LocalDate;
import java.util.Objects;

public class ContratoID {

    private final LocalDate fechaInicio;
    private final String matriculaMoto;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ContratoID(LocalDate fechaInicio, String matriculaMoto) {
        this.fechaInicio = fechaInicio;
        this.matriculaMoto = matriculaMoto;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public String getMatriculaMoto() {
        return matriculaMoto;
    }

    @Override
    public boolean equals(Object o) {
        boolean esMismaReferencia = false;
        boolean esIgual = false;

        if (this == o) {
            esMismaReferencia = true;
        }

        if (!esMismaReferencia && o instanceof ContratoID otroContratoId) {
            esIgual = Objects.equals(fechaInicio, otroContratoId.fechaInicio)
                    && Objects.equals(matriculaMoto, otroContratoId.matriculaMoto);
        }

        if (esMismaReferencia) {
            esIgual = true;
        }

        return esIgual;
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(fechaInicio, matriculaMoto);
        return hash;
    }
}
