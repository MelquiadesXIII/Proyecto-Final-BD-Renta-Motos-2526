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

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    /**
     * Configura las columnas de todas las tablas de reportes y carga los datos
     * desde los servicios correspondientes al abrir la pantalla.
     */
    @FXML
    private void initialize() {
        Logger.log("Inicializando ReportesController...");
        configurarColumnas();
        cargarDatos();
    }

    // -----------------------------------------------------------------
    // Configuración de columnas
    // -----------------------------------------------------------------

    /**
     * Asigna las fábricas de valores a cada columna de todas las tablas
     * de reportes. Cada columna extrae la propiedad del DTO correspondiente.
     */
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

    /** Configura las columnas de la tabla de clientes por municipio. */
    private void configurarColumnasClientes() {
        colFechaCli.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colMunicipioCli.setCellValueFactory(new PropertyValueFactory<>("municipio"));
        colNombreCli.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCiCli.setCellValueFactory(new PropertyValueFactory<>("ci"));
        colContratosCli.setCellValueFactory(new PropertyValueFactory<>("cantidadContratos"));
        colTotalCli.setCellValueFactory(new PropertyValueFactory<>("totalGastado"));
    }

    /** Configura las columnas de la tabla de reporte de motos. */
    private void configurarColumnasMotos() {
        colFechaMoto.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colMatriculaMoto.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colMarcaMoto.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colModeloMoto.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colColorMoto.setCellValueFactory(new PropertyValueFactory<>("color"));
        colKmMoto.setCellValueFactory(new PropertyValueFactory<>("kmRecorridos"));
    }

    /** Configura las columnas de la tabla de reporte de contratos. */
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

    /** Configura las columnas de la tabla de situación de motos. */
    private void configurarColumnasSituacionMotos() {
        colFechaSit.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colMatriculaMarcaSit.setCellValueFactory(new PropertyValueFactory<>("matriculaMarca"));
        colSituacionSit.setCellValueFactory(new PropertyValueFactory<>("situacion"));
        colFechaFinSit.setCellValueFactory(new PropertyValueFactory<>("fechaFinContrato"));
    }

    /** Configura las columnas de la tabla de clientes incumplidores. */
    private void configurarColumnasIncumplidores() {
        colFechaInc.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colNombreInc.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colFechaFinInc.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colFechaEntregaInc.setCellValueFactory(new PropertyValueFactory<>("fechaEntrega"));
    }

    /** Configura las columnas de la tabla de resumen por marcas y modelos. */
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

    /** Configura las columnas de la tabla de resumen por municipios. */
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

    /** Configura las columnas de la tabla de ingresos anuales. */
    private void configurarColumnasIngresosAnuales() {
        colFechaAnual.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTotalAnual.setCellValueFactory(new PropertyValueFactory<>("ingresoTotalAnual"));
        colMesAnual.setCellValueFactory(new PropertyValueFactory<>("mes"));
        colIngresoMensual.setCellValueFactory(new PropertyValueFactory<>("ingresoMensual"));
    }

    // -----------------------------------------------------------------
    // Carga de datos
    // -----------------------------------------------------------------

    /**
     * Carga los datos de todos los reportes desde los servicios.
     * Cada carga se envuelve en un try-catch para que un fallo en un reporte
     * no impida cargar el resto.
     */
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

    /** Carga los datos de la tabla de clientes por municipio. */
    private void cargarClientesReporte() {
        try {
            List<CliRepDTO> lista = clienteService.listarClientesReporte();
            tablaClientesMunicipio.getItems().setAll(lista);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error clientes reporte: " + e.getMessage());
        }
    }

    /** Carga los datos de la tabla de reporte de motos. */
    private void cargarMotosReporte() {
        try {
            List<MotoRepDTO> lista = motoService.listarMotosReporte();
            tablaMotos.getItems().setAll(lista);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error motos reporte: " + e.getMessage());
        }
    }

    /** Carga los datos de la tabla de reporte de contratos. */
    private void cargarContratosReporte() {
        try {
            List<ContRepDTO> lista = contratoService.listarContratosReporte();
            tablaContratos.getItems().setAll(lista);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error contratos reporte: " + e.getMessage());
        }
    }

    /** Carga los datos de la tabla de situación de motos. */
    private void cargarSituacionMotosReporte() {
        try {
            List<SitMotoRepDTO> lista = motoService.listarSituacionMotosReporte();
            tablaSituacionMotos.getItems().setAll(lista);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error situación motos: " + e.getMessage());
        }
    }

    /** Carga los datos de la tabla de clientes incumplidores. */
    private void cargarIncumplidores() {
        try {
            List<IncumpDTO> lista = clienteService.listarIncumplidores();
            tablaIncumplidores.getItems().setAll(lista);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error incumplidores: " + e.getMessage());
        }
    }

    /** Carga los datos de la tabla de resumen por marcas y modelos. */
    private void cargarResumenMarcaModelo() {
        try {
            List<ResMarModDTO> lista = contratoService.resumenMarcasModelos();
            tablaResumenMarcaModelo.getItems().setAll(lista);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error resumen MM: " + e.getMessage());
        }
    }

    /** Carga los datos de la tabla de resumen por municipios. */
    private void cargarResumenMunicipio() {
        try {
            List<ResMunDTO> lista = contratoService.resumenMunicipios();
            tablaResumenMunicipio.getItems().setAll(lista);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error resumen Mun: " + e.getMessage());
        }
    }

    /** Carga los datos de la tabla de ingresos anuales. */
    private void cargarIngresosAnuales() {
        try {
            List<IngAnualDTO> lista = contratoService.ingresosAnuales();
            tablaIngresosAnuales.getItems().setAll(lista);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error ingresos anuales: " + e.getMessage());
        }
    }
}