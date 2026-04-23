package org.proyectobdmotos.models;

@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")

public class Marca {

    private Integer idMarca;
    private String nombreMarca;


    public Marca(Integer idMarca, String nombreMarca) {
        setIdMarca(idMarca);
        setNombreMarca(nombreMarca);
    }
    
    public Integer getIdMarca() {
        return idMarca;
    }
    public void setIdMarca(Integer idMarca) {
        this.idMarca = idMarca;
    }
    public String getNombreMarca() {
        return nombreMarca;
    }
    public void setNombreMarca(String nombreMarca) {
        this.nombreMarca = nombreMarca;
    }
}
