package org.proyectobdmotos.controller;

import java.util.List;
import org.proyectobdmotos.dto.*;
import org.proyectobdmotos.services.ClienteService;
import org.proyectobdmotos.services.ContratoService;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.utils.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ReportesController {

    private final ClienteService clienteService;
    private final MotoService motoService;
    private final ContratoService contratoService;

    @FXML private TableView<CliRepDTO> tablaClientesMunicipio;
    @FXML private TableColumn<CliRepDTO, String> colFechaCli, colMunicipioCli, colNombreCli, colCiCli;
    @FXML private TableColumn<CliRepDTO, Integer> colContratosCli;
    @FXML private TableColumn<CliRepDTO, Double> colTotalCli;

    @FXML private TableView<MotoRepDTO> tablaMotos;
    @FXML private TableColumn<MotoRepDTO, String> colFechaMoto, colMatriculaMoto, colMarcaMoto, colModeloMoto, colColorMoto;
    @FXML private TableColumn<MotoRepDTO, Double> colKmMoto;

    @FXML private TableView<ContRepDTO> tablaContratos;
    @FXML private TableColumn<ContRepDTO, String> colClienteCont, colMatriculaCont, colMarcaCont, colModeloCont, colPagoCont, colInicioCont, colFinCont, colSeguroCont;
    @FXML private TableColumn<ContRepDTO, Integer> colProrrogaCont;
    @FXML private TableColumn<ContRepDTO, Double> colImporteCont;

    @FXML private TableView<SitMotoRepDTO> tablaSituacionMotos;
    @FXML private TableColumn<SitMotoRepDTO, String> colFechaSit, colMatriculaMarcaSit, colSituacionSit, colFechaFinSit;

    @FXML private TableView<IncumpDTO> tablaIncumplidores;
    @FXML private TableColumn<IncumpDTO, String> colFechaInc, colNombreInc, colFechaFinInc, colFechaEntregaInc;

    @FXML private TableView<ResMarModDTO> tablaResumenMarcaModelo;
    @FXML private TableColumn<ResMarModDTO, String> colFechaResMM, colMarcaResMM, colModeloResMM;
    @FXML private TableColumn<ResMarModDTO, Long> colCantMotosResMM;
    @FXML private TableColumn<ResMarModDTO, Double> colDiasResMM, colIngTarjetaResMM, colIngChequeResMM, colIngEfectivoResMM, colTotalMarcaResMM, colTotalGeneralResMM;

    @FXML private TableView<ResMunDTO> tablaResumenMunicipio;
    @FXML private TableColumn<ResMunDTO, String> colFechaResMun, colMunicipioResMun, colMarcaResMun, colModeloResMun;
    @FXML private TableColumn<ResMunDTO, Double> colDiasAlqResMun, colDiasProrrogaResMun, colEfectivoResMun, colTotalResMun;

    @FXML private TableView<IngAnualDTO> tablaIngresosAnuales;
    @FXML private TableColumn<IngAnualDTO, String> colFechaAnual, colMesAnual;
    @FXML private TableColumn<IngAnualDTO, Double> colTotalAnual, colIngresoMensual;

    public ReportesController(ClienteService clienteService, MotoService motoService, ContratoService contratoService) {
        this.clienteService = clienteService;
        this.motoService = motoService;
        this.contratoService = contratoService;
    }

    @FXML
    private void initialize() {
        Logger.log("Inicializando ReportesController...");
        configurarColumnas();
        cargarDatos();
    }

    private void configurarColumnas() {
        colFechaCli.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colMunicipioCli.setCellValueFactory(new PropertyValueFactory<>("municipio"));
        colNombreCli.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCiCli.setCellValueFactory(new PropertyValueFactory<>("ci"));
        colContratosCli.setCellValueFactory(new PropertyValueFactory<>("cantidadContratos"));
        colTotalCli.setCellValueFactory(new PropertyValueFactory<>("totalGastado"));

        colFechaMoto.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colMatriculaMoto.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colMarcaMoto.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colModeloMoto.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colColorMoto.setCellValueFactory(new PropertyValueFactory<>("color"));
        colKmMoto.setCellValueFactory(new PropertyValueFactory<>("kmRecorridos"));

        colClienteCont.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colMatriculaCont.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colMarcaCont.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colModeloCont.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colPagoCont.setCellValueFactory(new PropertyValueFactory<>("formaPago"));
        colInicioCont.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFinCont.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colProrrogaCont.setCellValueFactory(new PropertyValueFactory<>("prorrogaDias"));
        colSeguroCont.setCellValueFactory(new PropertyValueFactory<>("seguroAdicional"));
        colImporteCont.setCellValueFactory(new PropertyValueFactory<>("importeTotal"));

        colFechaSit.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colMatriculaMarcaSit.setCellValueFactory(new PropertyValueFactory<>("matriculaMarca"));
        colSituacionSit.setCellValueFactory(new PropertyValueFactory<>("situacion"));
        colFechaFinSit.setCellValueFactory(new PropertyValueFactory<>("fechaFinContrato"));

        colFechaInc.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colNombreInc.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colFechaFinInc.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colFechaEntregaInc.setCellValueFactory(new PropertyValueFactory<>("fechaEntrega"));

        colFechaResMM.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colMarcaResMM.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colModeloResMM.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colCantMotosResMM.setCellValueFactory(new PropertyValueFactory<>("cantidadMotos"));
        colDiasResMM.setCellValueFactory(new PropertyValueFactory<>("diasTotales"));
        colIngTarjetaResMM.setCellValueFactory(new PropertyValueFactory<>("ingresosTarjeta"));
        colIngChequeResMM.setCellValueFactory(new PropertyValueFactory<>("ingresosCheque"));
        colIngEfectivoResMM.setCellValueFactory(new PropertyValueFactory<>("ingresosEfectivo"));
        colTotalMarcaResMM.setCellValueFactory(new PropertyValueFactory<>("totalIngresosMarca"));
        colTotalGeneralResMM.setCellValueFactory(new PropertyValueFactory<>("totalGeneral"));

        colFechaResMun.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colMunicipioResMun.setCellValueFactory(new PropertyValueFactory<>("municipio"));
        colMarcaResMun.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colModeloResMun.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colDiasAlqResMun.setCellValueFactory(new PropertyValueFactory<>("diasAlquilados"));
        colDiasProrrogaResMun.setCellValueFactory(new PropertyValueFactory<>("diasProrroga"));
        colEfectivoResMun.setCellValueFactory(new PropertyValueFactory<>("valorEfectivo"));
        colTotalResMun.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));

        colFechaAnual.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTotalAnual.setCellValueFactory(new PropertyValueFactory<>("ingresoTotalAnual"));
        colMesAnual.setCellValueFactory(new PropertyValueFactory<>("mes"));
        colIngresoMensual.setCellValueFactory(new PropertyValueFactory<>("ingresoMensual"));
    }

    private void cargarDatos() {
        try { tablaClientesMunicipio.getItems().setAll(clienteService.listarClientesReporte()); } catch (Exception e) { Logger.logError("Error clientes reporte: " + e.getMessage()); }
        try { tablaMotos.getItems().setAll(motoService.listarMotosReporte()); } catch (Exception e) { Logger.logError("Error motos reporte: " + e.getMessage()); }
        try { tablaContratos.getItems().setAll(contratoService.listarContratosReporte()); } catch (Exception e) { Logger.logError("Error contratos reporte: " + e.getMessage()); }
        try { tablaSituacionMotos.getItems().setAll(motoService.listarSituacionMotosReporte()); } catch (Exception e) { Logger.logError("Error situación motos: " + e.getMessage()); }
        try { tablaIncumplidores.getItems().setAll(clienteService.listarIncumplidores()); } catch (Exception e) { Logger.logError("Error incumplidores: " + e.getMessage()); }
        try { tablaResumenMarcaModelo.getItems().setAll(contratoService.resumenMarcasModelos()); } catch (Exception e) { Logger.logError("Error resumen MM: " + e.getMessage()); }
        try { tablaResumenMunicipio.getItems().setAll(contratoService.resumenMunicipios()); } catch (Exception e) { Logger.logError("Error resumen Mun: " + e.getMessage()); }
        try { tablaIngresosAnuales.getItems().setAll(contratoService.ingresosAnuales()); } catch (Exception e) { Logger.logError("Error ingresos anuales: " + e.getMessage()); }
    }
}