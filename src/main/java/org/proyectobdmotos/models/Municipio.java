package org.proyectobdmotos.models;

@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")

public class Municipio {

    private int idMunicipio;
    private String nombreMunicipio;


    public Municipio(int idMunicipio, String nombreMunicipio) {
        setIdMunicipio(idMunicipio);
        setNombreMunicipio(nombreMunicipio);
    }

    public int getIdMunicipio() {
        return idMunicipio;
    }

    public void setIdMunicipio(int idMunicipio) {
        this.idMunicipio = idMunicipio;
    }

    public String getNombreMunicipio() {
        return nombreMunicipio;
    }

    public void setNombreMunicipio(String nombreMunicipio) {
        this.nombreMunicipio = nombreMunicipio;
    }

    
}
