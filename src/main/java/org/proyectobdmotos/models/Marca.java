package org.proyectobdmotos.models;

@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")

public class Marca {

    private String idMarca;
    private String nombreMarca;


    public Marca(String idMarca, String nombreMarca) {
        setIdMarca(idMarca);
        setNombreMarca(nombreMarca);
    }
    
    public String getIdMarca() {
        return idMarca;
    }
    public void setIdMarca(String idMarca) {
        this.idMarca = idMarca;
    }
    public String getNombreMarca() {
        return nombreMarca;
    }
    public void setNombreMarca(String nombreMarca) {
        this.nombreMarca = nombreMarca;
    }
}
