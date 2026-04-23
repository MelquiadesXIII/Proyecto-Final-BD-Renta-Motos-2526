package org.proyectobdmotos.models;

@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")

public class Modelo {

    private Integer idModelo;
    private Integer idMarca;
    private String nombreModelo;

    
    public Modelo(Integer idModelo, Integer idMarca, String nombreModelo) {
        setIdModelo(idModelo);
        setIdMarca(idMarca);
        setNombreModelo(nombreModelo);
    }

    public Integer getIdModelo() {
        return idModelo;
    }
    public void setIdModelo(Integer idModelo) {
        this.idModelo = idModelo;
    }
    public Integer getIdMarca() {
        return idMarca;
    }
    public void setIdMarca(Integer idMarca) {
        this.idMarca = idMarca;
    }
    public String getNombreModelo() {
        return nombreModelo;
    }
    public void setNombreModelo(String nombreModelo) {
        this.nombreModelo = nombreModelo;
    }
}
