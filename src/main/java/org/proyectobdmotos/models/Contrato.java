package org.proyectobdmotos.models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.proyectobdmotos.utils.Validator;

@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
public class Contrato {

    private Integer idContrato;
    private LocalDate fechaInicio;
    private Integer idMoto;
    private LocalDate fechaFin;
    private Integer idCliente;
    private FormaPago formaPago;
    private int diasProrroga;
    private boolean seguroAdicional;
    private double tarifaNormal;
    private double tarifaProrroga;
    private LocalDate fechaEntrega;
    private double cantKmSalida;
    private double cantKmLlegada;

    private String ciCliente;
    private String nombreCompletoCliente;
    private String matriculaMoto;
    private String marcaMoto;
    private String modeloMoto;

    public Contrato(double cantKmLlegada, double cantKmSalida, Integer idCliente, int diasProrroga,
                    LocalDate fechaEntrega, LocalDate fechaFin, LocalDate fechaInicio,
                    FormaPago formaPago, Integer idMoto, boolean seguroAdicional,
                    double tarifaNormal, double tarifaProrroga) {
        setCantKmLlegada(cantKmLlegada);
        setCantKmSalida(cantKmSalida);
        setIdCliente(idCliente);
        setDiasProrroga(diasProrroga);
        setFechaEntrega(fechaEntrega);
        setFechaFin(fechaFin);
        setFechaInicio(fechaInicio);
        setFormaPago(formaPago);
        setIdMoto(idMoto);
        setSeguroAdicional(seguroAdicional);
        setTarifaNormal(tarifaNormal);
        setTarifaProrroga(tarifaProrroga);
    }

    public Integer getIdContrato() { return idContrato; }
    public void setIdContrato(Integer idContrato) { this.idContrato = idContrato; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) {
        Validator.validateLocalDate(fechaInicio);
        this.fechaInicio = fechaInicio;
    }

    public Integer getIdMoto() { return idMoto; }
    public void setIdMoto(Integer idMoto) {
        Validator.nonNull(idMoto);
        this.idMoto = idMoto;
    }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) {
        Validator.validateLocalDate(fechaFin);
        this.fechaFin = fechaFin;
    }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) {
        Validator.nonNull(idCliente);
        this.idCliente = idCliente;
    }

    public FormaPago getFormaPago() { return formaPago; }
    public void setFormaPago(FormaPago formaPago) {
        Validator.nonNull(formaPago);
        this.formaPago = formaPago;
    }

    public int getDiasProrroga() { return diasProrroga; }
    public void setDiasProrroga(int diasProrroga) {
        boolean valid = diasProrroga >= 0;
        if (!valid) {
            throw new IllegalArgumentException(
                    "Los días de prórroga no pueden ser negativos. Recibido: " + diasProrroga);
        }
        this.diasProrroga = diasProrroga;
    }

    public boolean isSeguroAdicional() { return seguroAdicional; }
    public void setSeguroAdicional(boolean seguroAdicional) { this.seguroAdicional = seguroAdicional; }

    public double getTarifaNormal() { return tarifaNormal; }
    public void setTarifaNormal(double tarifaNormal) {
        Validator.validatePositive(tarifaNormal);
        this.tarifaNormal = tarifaNormal;
    }

    public double getTarifaProrroga() { return tarifaProrroga; }
    public void setTarifaProrroga(double tarifaProrroga) {
        boolean valid = tarifaProrroga >= 0;
        if (!valid) {
            throw new IllegalArgumentException(
                    "La tarifa de prórroga no puede ser negativa. Recibida: " + tarifaProrroga);
        }
        this.tarifaProrroga = tarifaProrroga;
    }

    public LocalDate getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDate fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public double getCantKmSalida() { return cantKmSalida; }
    public void setCantKmSalida(double cantKmSalida) {
        boolean valid = cantKmSalida >= 0;
        if (!valid) {
            throw new IllegalArgumentException(
                    "Los km de salida no pueden ser negativos. Recibidos: " + cantKmSalida);
        }
        this.cantKmSalida = cantKmSalida;
    }

    public double getCantKmLlegada() { return cantKmLlegada; }
    public void setCantKmLlegada(double cantKmLlegada) {
        boolean valid = cantKmLlegada >= 0;
        if (!valid) {
            throw new IllegalArgumentException(
                    "Los km de llegada no pueden ser negativos. Recibidos: " + cantKmLlegada);
        }
        this.cantKmLlegada = cantKmLlegada;
    }

    public int calcularDiasPactados() {
        int diasPactados = 0;
        boolean tieneFechasValidas = false;
        if (fechaInicio != null && fechaFin != null && !fechaFin.isBefore(fechaInicio)) {
            tieneFechasValidas = true;
        }
        if (tieneFechasValidas) {
            long diferenciaDias = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
            if (diferenciaDias == 0) {
                diasPactados = 1;
            } else {
                diasPactados = (int) diferenciaDias;
            }
        }
        return diasPactados;
    }

    public double calcularImporteBase() {
        int diasPactados = calcularDiasPactados();
        double importeBase = 0.0;
        if (diasPactados > 0) {
            double tarifa = getTarifaNormal();
            if (isSeguroAdicional()) { tarifa = tarifa * 2; }
            importeBase = diasPactados * tarifa;
        }
        return importeBase;
    }

    public int calcularDiasProrrogaReal() {
        int diasProrrogaReal = 0;
        if (fechaEntrega != null && fechaFin != null && fechaEntrega.isAfter(fechaFin)) {
            diasProrrogaReal = (int) ChronoUnit.DAYS.between(fechaFin, fechaEntrega);
        }
        return diasProrrogaReal;
    }

    public double calcularRecargoProrroga() {
        int diasProrrogaReal = calcularDiasProrrogaReal();
        double recargoProrroga = 0.0;
        if (diasProrrogaReal > 0) {
            double tarifa = getTarifaProrroga();
            if (isSeguroAdicional()) { tarifa = tarifa * 2; }
            recargoProrroga = diasProrrogaReal * tarifa;
        }
        return recargoProrroga;
    }

    public double calcularImporteTotalTeorico() {
        return calcularImporteBase() + calcularRecargoProrroga();
    }


    public String getCiCliente() { return ciCliente; }
    public void setCiCliente(String ciCliente) { this.ciCliente = ciCliente; }

    public String getNombreCompletoCliente() { return nombreCompletoCliente; }
    public void setNombreCompletoCliente(String nombreCompletoCliente) { this.nombreCompletoCliente = nombreCompletoCliente; }

    public String getMatriculaMoto() { return matriculaMoto; }
    public void setMatriculaMoto(String matriculaMoto) { this.matriculaMoto = matriculaMoto; }

    public String getMarcaMoto() { return marcaMoto; }
    public void setMarcaMoto(String marcaMoto) { this.marcaMoto = marcaMoto; }

    public String getModeloMoto() { return modeloMoto; }
    public void setModeloMoto(String modeloMoto) { this.modeloMoto = modeloMoto; }
}