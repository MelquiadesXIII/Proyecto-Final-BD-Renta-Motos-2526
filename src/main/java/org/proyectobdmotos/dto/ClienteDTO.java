package org.proyectobdmotos.dto;

/**
 * DTO para el reporte de clientes por municipio.
 */
public class ClienteDTO {

    private final String ci;
    private final String nombreCompleto;
    private final String municipio;
    private final int cantidadAlquileres;

    public ClienteDTO(String ci, String nombreCompleto, String municipio, int cantidadAlquileres) {
        this.ci = ci;
        this.nombreCompleto = nombreCompleto;
        this.municipio = municipio;
        this.cantidadAlquileres = cantidadAlquileres;
    }

    public String getCi() {
        return ci;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getMunicipio() {
        return municipio;
    }

    public int getCantidadAlquileres() {
        return cantidadAlquileres;
    }
}
