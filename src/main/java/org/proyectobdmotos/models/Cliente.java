package org.proyectobdmotos.models;

@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")

public class Cliente {

    private Integer idCliente;
    private String ciCliente;
    private String nombreCliente;
    private String primerApellido;
    private String segundoApellido;
    private int edad;
    private Sexo sexo;
    private String numeroContacto;
    private Integer idMunicipio;

    public Cliente(Integer idCliente, String ciCliente, String nombreCliente, String primerApellido,
            String segundoApellido, int edad, Sexo sexo, String numeroContacto, Integer idMunicipio) {
        setIdCliente(idCliente);
        setCiCliente(ciCliente);
        setNombreCliente(nombreCliente);
        setPrimerApellido(primerApellido);
        setSegundoApellido(segundoApellido);
        setEdad(edad);
        setSexo(sexo);
        setNumeroContacto(numeroContacto);
        setIdMunicipio(idMunicipio);
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getCiCliente() {
        return ciCliente;
    }
    public void setCiCliente(String ciCliente) {
        this.ciCliente = ciCliente;
    }
    public String getNombreCliente() {
        return nombreCliente;
    }
    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }
    public String getPrimerApellido() {
        return primerApellido;
    }
    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }
    public String getSegundoApellido() {
        return segundoApellido;
    }
    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }
    public int getEdad() {
        return edad;
    }
    public void setEdad(int edad) {
        this.edad = edad;
    }
    public Sexo getSexo() {
        return sexo;
    }
    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }
    public String getNumeroContacto() {
        return numeroContacto;
    }
    public void setNumeroContacto(String numeroContacto) {
        this.numeroContacto = numeroContacto;
    }
    public Integer getIdMunicipio() {
        return idMunicipio;
    }
    public void setIdMunicipio(Integer idMunicipio) {
        this.idMunicipio = idMunicipio;
    }
}
