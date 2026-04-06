package org.proyectobdmotos.models;

import java.time.LocalDate;

@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")

public class Contrato {

    private final ContratoID contratoID;
    private LocalDate fechaFin;
    private String ciCliente;
    private FormaPago formaPago;
    private int diasProrroga;
    private boolean seguroAdicional;
    private static double tarifaNormal;
    private static double tarifaProrroga;
    private LocalDate fechaEntrega;
    private double cantKmSalida;
    private double cantKmLlegada;

    public Contrato(double cantKmLlegada, double cantKmSalida, String ciCliente, int diasProrroga, LocalDate fechaEntrega, LocalDate fechaFin, LocalDate fechaInicio, FormaPago formaPago, String matriculaMoto, boolean seguroAdicional) {
        setCantKmLlegada(cantKmLlegada);
        setCantKmSalida(cantKmSalida);
        setCiCliente(ciCliente);
        setDiasProrroga(diasProrroga);
        setFechaEntrega(fechaEntrega);
        setFechaFin(fechaFin);
        setFormaPago(formaPago);
        setSeguroAdicional(seguroAdicional);

        contratoID = new ContratoID(fechaInicio, matriculaMoto);
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }
    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }
    public String getCiCliente() {
        return ciCliente;
    }
    public void setCiCliente(String ciCliente) {
        this.ciCliente = ciCliente;
    }
    public FormaPago getFormaPago() {
        return formaPago;
    }
    public void setFormaPago(FormaPago formaPago) {
        this.formaPago = formaPago;
    }
    public int getDiasProrroga() {
        return diasProrroga;
    }
    public void setDiasProrroga(int diasProrroga) {
        this.diasProrroga = diasProrroga;
    }
    public boolean isSeguroAdicional() {
        return seguroAdicional;
    }
    public void setSeguroAdicional(boolean seguroAdicional) {
        this.seguroAdicional = seguroAdicional;
    }
    public static double getTarifaNormal() {
        return tarifaNormal;
    }
    public static void setTarifaNormal(double tarifaNormal) {
        Contrato.tarifaNormal = tarifaNormal;
    }
    public static double getTarifaProrroga() {
        return tarifaProrroga;
    }
    public static void setTarifaProrroga(double tarifaProrroga) {
        Contrato.tarifaProrroga = tarifaProrroga;
    }
    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }
    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }
    public double getCantKmSalida() {
        return cantKmSalida;
    }
    public void setCantKmSalida(double cantKmSalida) {
        this.cantKmSalida = cantKmSalida;
    }
    public double getCantKmLlegada() {
        return cantKmLlegada;
    }
    public void setCantKmLlegada(double cantKmLlegada) {
        this.cantKmLlegada = cantKmLlegada;
    }

    public ContratoID getContratoID() {
        return contratoID;
    }
}
