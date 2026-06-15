package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.proyectobdmotos.dto.ModeloConMarcaDTO;
import org.proyectobdmotos.models.Modelo;
import org.proyectobdmotos.services.ModeloService;

import java.util.List;

public class MarcasModelosController {

    @FXML private TableView<ModeloConMarcaDTO> tabla;
    @FXML private TableColumn<ModeloConMarcaDTO, String> colModelo, colMarca;

    private final ModeloService modeloService;

    public MarcasModelosController(ModeloService modeloService) {
        this.modeloService = modeloService;
    }

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    @FXML
    private void initialize() {
        colModelo.setCellValueFactory(new PropertyValueFactory<>("nombreModelo"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("nombreMarca"));
        cargarTabla();
        fijarColumnas(tabla);
    }

    private void cargarTabla() {
        List<ModeloConMarcaDTO> lista = modeloService.listarModelosConMarca();
        tabla.getItems().setAll(lista);
    }

    // -----------------------------------------------------------------
    // Navegación
    // -----------------------------------------------------------------

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

    // -----------------------------------------------------------------
    // Método para fijar las columnas
    // -----------------------------------------------------------------

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