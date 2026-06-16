package org.proyectobdmotos.controller;

import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.proyectobdmotos.dto.SituacionMotoDTO;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.utils.Logger;

public class InventarioController {

    private final MotoService motoService;

    @FXML
    private TableView<SituacionMotoDTO> tablaInventario;
    @FXML
    private TableColumn<SituacionMotoDTO, String> colMatriculaInv;
    @FXML
    private TableColumn<SituacionMotoDTO, String> colMarcaInv;
    @FXML
    private TableColumn<SituacionMotoDTO, String> colSituacionInv;
    @FXML
    private TableColumn<SituacionMotoDTO, String> colFechaFinInv;
    @FXML
    private StackPane rootPane;

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
        cargarDatos();
        fijarColumnas(tablaInventario);
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
            ajustarColumnas(tablaInventario, colMatriculaInv, colMarcaInv, colSituacionInv, colFechaFinInv);
            Logger.logInfo("Inventario cargado: " + lista.size() + " motos");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error al cargar inventario: " + e.getMessage());
        }
    }

    // ---------------------------
    // Autoajuste
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
                    } catch (Exception ignored2) {
                    }
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