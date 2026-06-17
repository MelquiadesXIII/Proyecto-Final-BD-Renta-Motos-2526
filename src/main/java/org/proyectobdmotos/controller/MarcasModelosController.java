package org.proyectobdmotos.controller;

import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
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
        fijarColumnas(tabla);
        cargarTabla();
    }

    private void cargarTabla() {
        List<ModeloConMarcaDTO> lista = modeloService.listarModelosConMarca();
        tabla.getItems().setAll(lista);
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