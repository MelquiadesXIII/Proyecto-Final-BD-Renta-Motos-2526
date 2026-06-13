package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.proyectobdmotos.dto.SituacionMotoDTO;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.utils.Logger;

import java.util.List;

public class InventarioController {

    private final MotoService motoService;

    @FXML private TableView<SituacionMotoDTO> tablaInventario;
    @FXML private TableColumn<SituacionMotoDTO, String> colMatriculaInv;
    @FXML private TableColumn<SituacionMotoDTO, String> colMarcaInv;
    @FXML private TableColumn<SituacionMotoDTO, String> colSituacionInv;
    @FXML private TableColumn<SituacionMotoDTO, String> colFechaFinInv;

    public InventarioController(MotoService motoService) {
        this.motoService = motoService;
    }

    /**
     * Inicializa la pantalla: configura las columnas de la tabla
     * y carga los datos del inventario desde el servicio.
     */
    @FXML
    private void initialize() {
        Logger.log("Inicializando InventarioController...");
        configurarColumnas();
        cargarDatos();
    }

    /**
     * Establece las fábricas de valores para cada columna de la tabla.
     * Las columnas de matrícula y marca se obtienen directamente del DTO;
     * la situación se muestra como texto legible y la fecha fin se
     * presenta como cadena, mostrando "—" si no existe.
     */
    private void configurarColumnas() {
        configurarColumnaMatricula();
        configurarColumnaMarca();
        configurarColumnaSituacion();
        configurarColumnaFechaFin();
    }

    /**
     * Asigna la fábrica para la columna de matrícula.
     */
    private void configurarColumnaMatricula() {
        colMatriculaInv.setCellValueFactory(new PropertyValueFactory<>("matricula"));
    }

    /**
     * Asigna la fábrica para la columna de marca.
     */
    private void configurarColumnaMarca() {
        colMarcaInv.setCellValueFactory(new PropertyValueFactory<>("marca"));
    }

    /**
     * Configura la columna de situación para mostrar el valor
     * del enumerado Situacion como texto legible.
     */
    private void configurarColumnaSituacion() {
        colSituacionInv.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getSituacion().getValor()
                )
        );
    }

    /**
     * Configura la columna de fecha fin de contrato. Si no hay fecha,
     * muestra un guion largo (—) para indicar que no aplica.
     */
    private void configurarColumnaFechaFin() {
        colFechaFinInv.setCellValueFactory(cellData -> {
            var fecha = cellData.getValue().getFechaFinContrato();
            return new javafx.beans.property.SimpleStringProperty(
                    fecha != null ? fecha.toString() : "—"
            );
        });
    }

    /**
     * Obtiene la lista de situación de motos del servicio
     * y la muestra en la tabla. Si ocurre un error, lo registra.
     */
    private void cargarDatos() {
        try {
            List<SituacionMotoDTO> lista = motoService.listarSituacionMotos();
            tablaInventario.getItems().setAll(lista);
            Logger.logInfo("Inventario cargado: " + lista.size() + " motos");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error al cargar inventario: " + e.getMessage());

        }
    }
}