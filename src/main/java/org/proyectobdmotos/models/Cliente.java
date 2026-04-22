package org.proyectobdmotos.models;

@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")

public class Cliente {

    private Integer idCliente;
    private String ciCliente;
    private String nombreCLiente;
    private String primerApellido;
    private String segundoApellido;
    private int edad;
    private Sexo sexo;
    private String numeroContacto;
    private String idMunicipio;

    public Cliente(Integer idCliente, String ciCliente, String nombreCLiente, String primerApellido,
            String segundoApellido, int edad, Sexo sexo, String numeroContacto, String idMunicipio) {
        setIdCliente(idCliente);
        setCiCliente(ciCliente);
        setNombreCLiente(nombreCLiente);
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
    public String getNombreCLiente() {
        return nombreCLiente;
    }
    public void setNombreCLiente(String nombreCLiente) {
        this.nombreCLiente = nombreCLiente;
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
    public String getIdMunicipio() {
        return idMunicipio;
    }
    public void setIdMunicipio(String idMunicipio) {
        this.idMunicipio = idMunicipio;
    }
}
