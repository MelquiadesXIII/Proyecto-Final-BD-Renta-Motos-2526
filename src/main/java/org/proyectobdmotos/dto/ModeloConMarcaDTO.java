package org.proyectobdmotos.dto;

public class ModeloConMarcaDTO
{
    private final int idModelo;
    private final String nombreModelo;
    private final int idMarca;
    private final String nombreMarca;

    public ModeloConMarcaDTO(int idModelo, String nombreModelo, int idMarca, String nombreMarca) {
        this.idModelo = idModelo;
        this.nombreModelo = nombreModelo;
        this.idMarca = idMarca;
        this.nombreMarca = nombreMarca;
    }

    public int getIdModelo() {
        return idModelo;
    }

    public String getNombreModelo() {
        return nombreModelo;
    }

    public int getIdMarca() {
        return idMarca;
    }

    public String getNombreMarca() {
        return nombreMarca;
    }
}