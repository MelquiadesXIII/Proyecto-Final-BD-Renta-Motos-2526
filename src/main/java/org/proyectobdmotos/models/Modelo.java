package org.proyectobdmotos.models;

@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")

public class Modelo {

    private String idModelo;
    private String idMarca;
    private String nombreModelo;

    
    public Modelo(String idModelo, String idMarca, String nombreModelo) {
        setIdModelo(idModelo);
        setIdMarca(idMarca);
        setNombreModelo(nombreModelo);
    }

    public String getIdModelo() {
        return idModelo;
    }
    public void setIdModelo(String idModelo) {
        this.idModelo = idModelo;
    }
    public String getIdMarca() {
        return idMarca;
    }
    public void setIdMarca(String idMarca) {
        this.idMarca = idMarca;
    }
    public String getNombreModelo() {
        return nombreModelo;
    }
    public void setNombreModelo(String nombreModelo) {
        this.nombreModelo = nombreModelo;
    }
}
