package org.proyectobdmotos.controller;

import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.proyectobdmotos.dto.*;
import org.proyectobdmotos.services.ClienteService;
import org.proyectobdmotos.services.ContratoService;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.utils.Logger;

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
        fijarColumnas(tablaClientesMunicipio);
        fijarColumnas(tablaMotos);
        fijarColumnas(tablaContratos);
        fijarColumnas(tablaSituacionMotos);
        fijarColumnas(tablaIncumplidores);
        fijarColumnas(tablaResumenMarcaModelo);
        fijarColumnas(tablaResumenMunicipio);
        fijarColumnas(tablaIngresosAnuales);
    }

    private void configurarColumnas() {
        configurarColumnasClientes();
        configurarColumnasMotos();
        configurarColumnasContratos();
        configurarColumnasSituacionMotos();
        configurarColumnasIncumplidores();
        configurarColumnasResumenMarcaModelo();
        configurarColumnasResumenMunicipio();
        configurarColumnasIngresosAnuales();
    }

    private void configurarColumnasClientes() {
        colFechaCli.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colMunicipioCli.setCellValueFactory(new PropertyValueFactory<>("municipio"));
        colNombreCli.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCiCli.setCellValueFactory(new PropertyValueFactory<>("ci"));
        colContratosCli.setCellValueFactory(new PropertyValueFactory<>("cantidadContratos"));
        colTotalCli.setCellValueFactory(new PropertyValueFactory<>("totalGastado"));
    }

    private void configurarColumnasMotos() {
        colFechaMoto.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colMatriculaMoto.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colMarcaMoto.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colModeloMoto.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colColorMoto.setCellValueFactory(new PropertyValueFactory<>("color"));
        colKmMoto.setCellValueFactory(new PropertyValueFactory<>("kmRecorridos"));
    }

    private void configurarColumnasContratos() {
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
    }

    private void configurarColumnasSituacionMotos() {
        colFechaSit.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colMatriculaMarcaSit.setCellValueFactory(new PropertyValueFactory<>("matriculaMarca"));
        colSituacionSit.setCellValueFactory(new PropertyValueFactory<>("situacion"));
        colFechaFinSit.setCellValueFactory(new PropertyValueFactory<>("fechaFinContrato"));
    }

    private void configurarColumnasIncumplidores() {
        colFechaInc.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colNombreInc.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colFechaFinInc.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colFechaEntregaInc.setCellValueFactory(new PropertyValueFactory<>("fechaEntrega"));
    }

    private void configurarColumnasResumenMarcaModelo() {
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
    }

    private void configurarColumnasResumenMunicipio() {
        colFechaResMun.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colMunicipioResMun.setCellValueFactory(new PropertyValueFactory<>("municipio"));
        colMarcaResMun.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colModeloResMun.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colDiasAlqResMun.setCellValueFactory(new PropertyValueFactory<>("diasAlquilados"));
        colDiasProrrogaResMun.setCellValueFactory(new PropertyValueFactory<>("diasProrroga"));
        colEfectivoResMun.setCellValueFactory(new PropertyValueFactory<>("valorEfectivo"));
        colTotalResMun.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));
    }

    private void configurarColumnasIngresosAnuales() {
        colFechaAnual.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTotalAnual.setCellValueFactory(new PropertyValueFactory<>("ingresoTotalAnual"));
        colMesAnual.setCellValueFactory(new PropertyValueFactory<>("mes"));
        colIngresoMensual.setCellValueFactory(new PropertyValueFactory<>("ingresoMensual"));
    }

    private void cargarDatos() {
        cargarClientesReporte();
        cargarMotosReporte();
        cargarContratosReporte();
        cargarSituacionMotosReporte();
        cargarIncumplidores();
        cargarResumenMarcaModelo();
        cargarResumenMunicipio();
        cargarIngresosAnuales();
    }

    private void cargarClientesReporte() {
        try {
            List<CliRepDTO> lista = clienteService.listarClientesReporte();
            tablaClientesMunicipio.getItems().setAll(lista);
            ajustarColumnas(tablaClientesMunicipio, colFechaCli, colMunicipioCli, colNombreCli, colCiCli, colContratosCli, colTotalCli);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error clientes reporte: " + e.getMessage());
        }
    }

    private void cargarMotosReporte() {
        try {
            List<MotoRepDTO> lista = motoService.listarMotosReporte();
            tablaMotos.getItems().setAll(lista);
            ajustarColumnas(tablaMotos, colFechaMoto, colMatriculaMoto, colMarcaMoto, colModeloMoto, colColorMoto, colKmMoto);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error motos reporte: " + e.getMessage());
        }
    }

    private void cargarContratosReporte() {
        try {
            List<ContRepDTO> lista = contratoService.listarContratosReporte();
            tablaContratos.getItems().setAll(lista);
            ajustarColumnas(tablaContratos, colClienteCont, colMatriculaCont, colMarcaCont, colModeloCont,
                    colPagoCont, colInicioCont, colFinCont, colSeguroCont, colProrrogaCont, colImporteCont);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error contratos reporte: " + e.getMessage());
        }
    }

    private void cargarSituacionMotosReporte() {
        try {
            List<SitMotoRepDTO> lista = motoService.listarSituacionMotosReporte();
            tablaSituacionMotos.getItems().setAll(lista);
            ajustarColumnas(tablaSituacionMotos, colFechaSit, colMatriculaMarcaSit, colSituacionSit, colFechaFinSit);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error situación motos: " + e.getMessage());
        }
    }

    private void cargarIncumplidores() {
        try {
            List<IncumpDTO> lista = clienteService.listarIncumplidores();
            tablaIncumplidores.getItems().setAll(lista);
            ajustarColumnas(tablaIncumplidores, colFechaInc, colNombreInc, colFechaFinInc, colFechaEntregaInc);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error incumplidores: " + e.getMessage());
        }
    }

    private void cargarResumenMarcaModelo() {
        try {
            List<ResMarModDTO> lista = contratoService.resumenMarcasModelos();
            tablaResumenMarcaModelo.getItems().setAll(lista);
            ajustarColumnas(tablaResumenMarcaModelo, colFechaResMM, colMarcaResMM, colModeloResMM,
                    colCantMotosResMM, colDiasResMM, colIngTarjetaResMM, colIngChequeResMM, colIngEfectivoResMM,
                    colTotalMarcaResMM, colTotalGeneralResMM);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error resumen MM: " + e.getMessage());
        }
    }

    private void cargarResumenMunicipio() {
        try {
            List<ResMunDTO> lista = contratoService.resumenMunicipios();
            tablaResumenMunicipio.getItems().setAll(lista);
            ajustarColumnas(tablaResumenMunicipio, colFechaResMun, colMunicipioResMun, colMarcaResMun, colModeloResMun,
                    colDiasAlqResMun, colDiasProrrogaResMun, colEfectivoResMun, colTotalResMun);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error resumen Mun: " + e.getMessage());
        }
    }

    private void cargarIngresosAnuales() {
        try {
            List<IngAnualDTO> lista = contratoService.ingresosAnuales();
            tablaIngresosAnuales.getItems().setAll(lista);
            ajustarColumnas(tablaIngresosAnuales, colFechaAnual, colMesAnual, colTotalAnual, colIngresoMensual);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error ingresos anuales: " + e.getMessage());
        }
    }

    // ---------------------------
    // Autoajuste genérico
    // ---------------------------
    private double medirAnchoTexto(String texto, boolean bold) {
        Font font = bold ? Font.font("System", FontWeight.BOLD, 14) : Font.font("System", 14);
        Text text = new Text(texto);
        text.setFont(font);
        return text.getLayoutBounds().getWidth() + 25;
    }

    @SafeVarargs
    private void ajustarColumnas(TableView<?> tabla, TableColumn<?, ?>... columnas) {
        for (TableColumn<?, ?> col : columnas) {
            double max = medirAnchoTexto(col.getText(), true);
            for (Object item : tabla.getItems()) {
                Object valor = null;
                try {
                    valor = ((TableColumn) col).getCellData(item);
                } catch (Exception ignored) {
                    try {
                        javafx.beans.value.ObservableValue<?> obs = ((TableColumn) col).getCellObservableValue(item);
                        if (obs != null) valor = obs.getValue();
                    } catch (Exception ignored2) {}
                }
                if (valor != null) {
                    double w = medirAnchoTexto(valor.toString(), false);
                    if (w > max) max = w;
                }
            }
            col.setPrefWidth(max);
            col.setMinWidth(max);
            col.setMaxWidth(max);
        }
        Platform.runLater(() -> {
            double total = 0;
            for (TableColumn<?, ?> c : tabla.getColumns()) total += c.getPrefWidth();
            tabla.setPrefWidth(total + 10);
            tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        });
    }

    private void fijarColumnas(TableView<?> tabla) {
        int i = 0;
        while (i < tabla.getColumns().size()) {
            TableColumn<?, ?> columna = tabla.getColumns().get(i);
            columna.setResizable(false);
            columna.setReorderable(false);
            i++;
        }
    }
}