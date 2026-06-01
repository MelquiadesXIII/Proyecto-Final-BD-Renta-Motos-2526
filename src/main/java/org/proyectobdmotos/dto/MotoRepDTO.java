package org.proyectobdmotos.dto;

import java.time.LocalDate;

public class MotoRepDTO {
    private final LocalDate fecha;
    private final String matricula;
    private final String marca;
    private final String modelo;
    private final String color;
    private final double kmRecorridos;

    public MotoRepDTO(LocalDate fecha, String matricula, String marca,
                      String modelo, String color, double kmRecorridos) {
        this.fecha = fecha;
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
        this.color = color;
        this.kmRecorridos = kmRecorridos;
    }

    public LocalDate getFecha() { return fecha; }
    public String getMatricula() { return matricula; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public String getColor() { return color; }
    public double getKmRecorridos() { return kmRecorridos; }
}