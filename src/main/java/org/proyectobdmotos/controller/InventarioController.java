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

    @FXML
    private void initialize() {
        Logger.log("Inicializando InventarioController...");
        configurarColumnas();
        cargarDatos();
    }

    private void configurarColumnas() {
        colMatriculaInv.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colMarcaInv.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colSituacionInv.setCellValueFactory(cellData -> {
            // El DTO tiene un enum Situacion; lo mostramos como texto
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getSituacion().getValor()
            );
        });
        colFechaFinInv.setCellValueFactory(cellData -> {
            var fecha = cellData.getValue().getFechaFinContrato();
            return new javafx.beans.property.SimpleStringProperty(
                    fecha != null ? fecha.toString() : "—"
            );
        });
    }

    private void cargarDatos() {
        try {
            List<SituacionMotoDTO> lista = motoService.listarSituacionMotos();
            tablaInventario.getItems().setAll(lista);
            Logger.logInfo("Inventario cargado: " + lista.size() + " motos");
        } catch (Exception e) {
            Logger.logError("Error al cargar inventario: " + e.getMessage());
        }
    }
}