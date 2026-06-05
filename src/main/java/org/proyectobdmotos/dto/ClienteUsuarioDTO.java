package org.proyectobdmotos.dto;

public class ClienteUsuarioDTO {
    private final int idCliente;
    private final int idUsuario;
    private final String ci;
    private final String nombreCompleto;
    private final String telefono;
    private final String nombreMunicipio;
    private final String nombreUsuario;
    private final String gmail;
    private final int cantidadContratos;

    public ClienteUsuarioDTO(int idCliente, int idUsuario, String ci,
                             String nombreCompleto, String telefono,
                             String nombreMunicipio, String nombreUsuario,
                             String gmail, int cantidadContratos) {
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.ci = ci;
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.nombreMunicipio = nombreMunicipio;
        this.nombreUsuario = nombreUsuario;
        this.gmail = gmail;
        this.cantidadContratos = cantidadContratos;
    }

    public int getIdCliente() { return idCliente; }
    public int getIdUsuario() { return idUsuario; }
    public String getCi() { return ci; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getTelefono() { return telefono; }
    public String getNombreMunicipio() { return nombreMunicipio; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getGmail() { return gmail; }
    public int getCantidadContratos() { return cantidadContratos; }
}