package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.proyectobdmotos.models.Marca;
import org.proyectobdmotos.models.Modelo;
import org.proyectobdmotos.services.MarcaService;
import org.proyectobdmotos.services.ModeloService;

import java.util.List;

public class EliminarMarcaModeloController {

    @FXML private ComboBox<Modelo> comboModelo;
    @FXML private ComboBox<Marca> comboMarca;

    private final ModeloService modeloService;
    private final MarcaService marcaService;

    public EliminarMarcaModeloController(ModeloService modeloService, MarcaService marcaService) {
        this.modeloService = modeloService;
        this.marcaService = marcaService;
    }

    @FXML
    private void initialize() {
        cargarModelos();
        cargarMarcas();

        comboModelo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) comboMarca.getSelectionModel().clearSelection();
        });
        comboMarca.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) comboModelo.getSelectionModel().clearSelection();
        });
    }

    private void cargarModelos() {
        List<Modelo> modelos = modeloService.listarTodos();
        comboModelo.getItems().setAll(modelos);
        comboModelo.setCellFactory(param -> new ListCell<Modelo>() {
            @Override
            protected void updateItem(Modelo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreModelo());
            }
        });
        comboModelo.setButtonCell(new ListCell<Modelo>() {
            @Override
            protected void updateItem(Modelo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreModelo());
            }
        });
    }

    private void cargarMarcas() {
        List<Marca> marcas = marcaService.listarTodas();
        comboMarca.getItems().setAll(marcas);
        comboMarca.setCellFactory(param -> new ListCell<Marca>() {
            @Override
            protected void updateItem(Marca item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreMarca());
            }
        });
        comboMarca.setButtonCell(new ListCell<Marca>() {
            @Override
            protected void updateItem(Marca item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreMarca());
            }
        });
    }

    @FXML
    private void onEliminar() {
        Modelo modelo = comboModelo.getValue();
        Marca marca = comboMarca.getValue();

        if (modelo == null && marca == null) {
            new Alert(Alert.AlertType.ERROR, "Seleccione un modelo o una marca").showAndWait();
        } else if (modelo != null) {
            if (modeloService.existeMotoConModelo(modelo.getIdModelo())) {
                new Alert(Alert.AlertType.ERROR, "No se puede eliminar: hay motos que usan este modelo").showAndWait();
            } else {
                modeloService.eliminarModelo(modelo.getIdModelo());
                MainController.getInstance().onGoBack();
            }
        } else {
            if (marcaService.existenModelosConMarca(marca.getIdMarca()) || marcaService.existenMotosConMarca(marca.getIdMarca())) {
                new Alert(Alert.AlertType.ERROR, "No se puede eliminar: la marca tiene modelos o motos asociadas").showAndWait();
            } else {
                marcaService.eliminarMarca(marca.getIdMarca());
                MainController.getInstance().onGoBack();
            }
        }
    }

    @FXML
    private void onCancelar() {
        MainController.getInstance().onGoBack();
    }
}