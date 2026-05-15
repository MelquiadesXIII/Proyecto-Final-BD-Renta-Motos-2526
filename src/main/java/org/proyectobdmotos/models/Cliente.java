package org.proyectobdmotos.models;

import org.proyectobdmotos.utils.Validator;

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
    private Integer idUsuario;


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
        Validator.validateCI(ciCliente);
        this.ciCliente = ciCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        Validator.validateText(nombreCliente);
        this.nombreCliente = nombreCliente;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        Validator.validateText(primerApellido);
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        if (segundoApellido != null) {
            Validator.validateText(segundoApellido);
        }
        this.segundoApellido = segundoApellido;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        Validator.validateAge(edad);
        this.edad = edad;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        Validator.nonNull(sexo);
        this.sexo = sexo;
    }

    public String getNumeroContacto() {
        return numeroContacto;
    }

    public void setNumeroContacto(String numeroContacto) {
        Validator.validateTelephoneNumber(numeroContacto);
        this.numeroContacto = numeroContacto;
    }

    public Integer getIdMunicipio() {
        return idMunicipio;
    }

    public void setIdMunicipio(Integer idMunicipio) {
        Validator.nonNull(idMunicipio);
        this.idMunicipio = idMunicipio;
    }

    public Integer getIdUsuario() { 
        return idUsuario; 
    }
    public void setIdUsuario(Integer idUsuario) { 
        this.idUsuario = idUsuario; 
    }
}
