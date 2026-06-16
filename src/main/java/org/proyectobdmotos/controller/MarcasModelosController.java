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
import org.proyectobdmotos.dto.ModeloConMarcaDTO;
import org.proyectobdmotos.services.ModeloService;

public class MarcasModelosController {

    @FXML private TableView<ModeloConMarcaDTO> tabla;
    @FXML private TableColumn<ModeloConMarcaDTO, String> colModelo, colMarca;
    @FXML private StackPane rootPane;

    private final ModeloService modeloService;

    public MarcasModelosController(ModeloService modeloService) {
        this.modeloService = modeloService;
    }

    @FXML
    private void initialize() {
        if (rootPane != null) {
            rootPane.setStyle(
                    "-fx-background-image: url('"
                            + getClass().getResource("/Utiles/algo.jpg").toExternalForm()
                            + "');"
                            + "-fx-background-size: cover;"
                            + "-fx-background-position: center center;"
                            + "-fx-background-repeat: no-repeat;"
            );
        }
        colModelo.setCellValueFactory(new PropertyValueFactory<>("nombreModelo"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("nombreMarca"));
        cargarTabla();
        fijarColumnas(tabla);
    }

    private void cargarTabla() {
        List<ModeloConMarcaDTO> lista = modeloService.listarModelosConMarca();
        tabla.getItems().setAll(lista);
        ajustarColumnas(tabla, colModelo, colMarca);
    }

    @FXML
    private void onNuevo() {
        MainController.getInstance().cargarVista("/fxml/modelo-form.fxml", "Nuevo Modelo");
    }

    @FXML
    private void onEditar() {
        ModeloConMarcaDTO seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            EditarMarcaModeloController.setIdModeloPreseleccionado(seleccionado.getIdModelo());
        } else {
            EditarMarcaModeloController.setIdModeloPreseleccionado(null);
        }
        MainController.getInstance().cargarVista("/fxml/editar-marca-modelo.fxml", "Editar Marca/Modelo");
    }

    @FXML
    private void onEliminar() {
        MainController.getInstance().cargarVista("/fxml/eliminar-marca-modelo.fxml", "Eliminar Marca/Modelo");
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