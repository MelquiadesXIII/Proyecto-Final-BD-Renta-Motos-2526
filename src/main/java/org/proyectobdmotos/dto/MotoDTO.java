package org.proyectobdmotos.dto;

/**
 * DTO para el reporte de motos con kilometraje.
 */
public class MotoDTO {

    private final String matricula;
    private final String marca;
    private final String modelo;
    private final double kmRecorridos;

    public MotoDTO(String matricula, String marca, String modelo, double kmRecorridos) {
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
        this.kmRecorridos = kmRecorridos;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public double getKmRecorridos() {
        return kmRecorridos;
    }
}
