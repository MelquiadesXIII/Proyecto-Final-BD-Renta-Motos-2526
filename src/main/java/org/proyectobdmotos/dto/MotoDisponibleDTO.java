package org.proyectobdmotos.dto;

public class MotoDisponibleDTO {
    private final int idMoto;
    private final String matricula;
    private final String marca;
    private final String modelo;
    private final String color;

    public MotoDisponibleDTO(int idMoto, String matricula, String marca, String modelo, String color) {
        this.idMoto = idMoto;
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
        this.color = color;
    }

    public int getIdMoto() { return idMoto; }
    public String getMatricula() { return matricula; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public String getColor() { return color; }
}