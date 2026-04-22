package org.proyectobdmotos.models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")

public class Contrato {

    private final ContratoID contratoID;
    private LocalDate fechaFin;
    private String ciCliente;
    private FormaPago formaPago;
    private int diasProrroga;
    private boolean seguroAdicional;
    private double tarifaNormal;
    private double tarifaProrroga;
    private LocalDate fechaEntrega;
    private double cantKmSalida;
    private double cantKmLlegada;

    public Contrato(double cantKmLlegada, double cantKmSalida, String ciCliente, int diasProrroga, LocalDate fechaEntrega, LocalDate fechaFin, LocalDate fechaInicio, FormaPago formaPago, String matriculaMoto, boolean seguroAdicional, double tarifaNormal, double tarifaProrroga) {
        setCantKmLlegada(cantKmLlegada);
        setCantKmSalida(cantKmSalida);
        setCiCliente(ciCliente);
        setDiasProrroga(diasProrroga);
        setFechaEntrega(fechaEntrega);
        setFechaFin(fechaFin);
        setFormaPago(formaPago);
        setSeguroAdicional(seguroAdicional);
        setTarifaNormal(tarifaNormal);
        setTarifaProrroga(tarifaProrroga);

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
    public double getTarifaNormal() {
        return tarifaNormal;
    }
    public void setTarifaNormal(double tarifaNormal) {
        this.tarifaNormal = tarifaNormal;
    }
    public double getTarifaProrroga() {
        return tarifaProrroga;
    }
    public void setTarifaProrroga(double tarifaProrroga) {
        this.tarifaProrroga = tarifaProrroga;
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

    public int calcularDiasPactados() {
        int diasPactados = 0;
        boolean tieneFechasValidas = false;

        if (contratoID.getFechaInicio() != null && fechaFin != null && !fechaFin.isBefore(contratoID.getFechaInicio())) {
            tieneFechasValidas = true;
        }

        if (tieneFechasValidas) {
            long diferenciaDias = ChronoUnit.DAYS.between(contratoID.getFechaInicio(), fechaFin);
            if (diferenciaDias == 0) {
                diasPactados = 1;
            } else {
                diasPactados = (int) diferenciaDias;
            }
        }

        return diasPactados;
    }

    public int calcularDiasProrrogaReal() {
        int diasProrrogaReal = 0;
        boolean tieneFechaEntrega = false;

        if (fechaEntrega != null) {
            tieneFechaEntrega = true;
        }

        if (fechaFin != null && tieneFechaEntrega && fechaEntrega.isAfter(fechaFin)) {
            diasProrrogaReal = (int) ChronoUnit.DAYS.between(fechaFin, fechaEntrega);
        }

        return diasProrrogaReal;
    }

    public double calcularImporteBase() {
        int diasPactados = calcularDiasPactados();
        double importeBase = 0.0;

        if (diasPactados > 0) {
            importeBase = diasPactados * getTarifaNormal();
        }

        return importeBase;
    }

    public double calcularRecargoProrroga() {
        int diasProrrogaReal = calcularDiasProrrogaReal();
        double recargoProrroga = 0.0;

        if (diasProrrogaReal > 0) {
            recargoProrroga = diasProrrogaReal * getTarifaProrroga();
        }

        return recargoProrroga;
    }

    public double calcularImporteTotalTeorico() {
        return calcularImporteBase() + calcularRecargoProrroga();
    }
}
