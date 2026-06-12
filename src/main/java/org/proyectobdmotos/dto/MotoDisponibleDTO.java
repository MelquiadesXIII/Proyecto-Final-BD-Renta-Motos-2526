package org.proyectobdmotos.dto;

public class MotoDisponibleDTO {
    private int idMoto;
    private String matricula;
    private String marca;
    private String modelo;
    private String color;

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