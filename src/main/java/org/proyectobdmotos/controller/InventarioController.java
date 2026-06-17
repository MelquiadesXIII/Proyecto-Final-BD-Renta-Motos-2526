package org.proyectobdmotos.controller;

import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import org.proyectobdmotos.dto.SituacionMotoDTO;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.utils.Logger;

public class InventarioController {

    private final MotoService motoService;

    @FXML private TableView<SituacionMotoDTO> tablaInventario;
    @FXML private TableColumn<SituacionMotoDTO, String> colMatriculaInv;
    @FXML private TableColumn<SituacionMotoDTO, String> colMarcaInv;
    @FXML private TableColumn<SituacionMotoDTO, String> colSituacionInv;
    @FXML private TableColumn<SituacionMotoDTO, String> colFechaFinInv;
    @FXML private StackPane rootPane;

    public InventarioController(MotoService motoService) {
        this.motoService = motoService;
    }

    @FXML
    private void initialize() {
        if (rootPane != null) {
            rootPane.setStyle(
                    "-fx-background-image: url('"
                            + getClass().getResource("/Utiles/fondoTablas.png").toExternalForm()
                            + "');"
                            + "-fx-background-size: cover;"
                            + "-fx-background-position: center center;"
                            + "-fx-background-repeat: no-repeat;"
            );
        }
        Logger.log("Inicializando InventarioController...");
        configurarColumnas();
        fijarColumnas(tablaInventario);
        cargarDatos();
    }

    private void configurarColumnas() {
        colMatriculaInv.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colMarcaInv.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colSituacionInv.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSituacion().getValor()));
        colFechaFinInv.setCellValueFactory(cellData -> {
            var fecha = cellData.getValue().getFechaFinContrato();
            return new javafx.beans.property.SimpleStringProperty(fecha != null ? fecha.toString() : "—");
        });
    }

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

    private void fijarColumnas(TableView<?> tabla) {
        for (TableColumn<?, ?> columna : tabla.getColumns()) {
            columna.setReorderable(false);
        }
        tabla.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                Platform.runLater(() -> tabla.getColumns().forEach(c -> c.setResizable(false)));
            }
        });
    }
}