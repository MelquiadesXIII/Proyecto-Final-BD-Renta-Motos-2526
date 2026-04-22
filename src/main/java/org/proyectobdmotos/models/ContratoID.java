package org.proyectobdmotos.models;

import java.time.LocalDate;
import java.util.Objects;

public class ContratoID {

    private final LocalDate fechaInicio;
    private final Integer idMoto;

    public ContratoID(LocalDate fechaInicio, Integer idMoto) {
        this.fechaInicio = fechaInicio;
        this.idMoto = idMoto;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public Integer getIdMoto() {
        return idMoto;
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
                    && Objects.equals(idMoto, otroContratoId.idMoto);
        }

        if (esMismaReferencia) {
            esIgual = true;
        }

        return esIgual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fechaInicio, idMoto);
    }
}
