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

    /**
     * Configura las columnas de la tabla y carga los datos desde el servicio
     * al abrir la pantalla.
     */
    @FXML
    private void initialize() {
        colModelo.setCellValueFactory(new PropertyValueFactory<>("nombreModelo"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("nombreMarca"));
        cargarTabla();
    }

    /**
     * Obtiene la lista de modelos con sus marcas asociadas y la muestra en la tabla.
     */
    private void cargarTabla() {
        List<ModeloConMarcaDTO> lista = modeloService.listarModelosConMarca();
        tabla.getItems().setAll(lista);
    }

    // -----------------------------------------------------------------
    // Navegación
    // -----------------------------------------------------------------

    /**
     * Abre el formulario para crear un nuevo modelo.
     */
    @FXML
    private void onNuevo() {
        MainController.getInstance().cargarVista("/fxml/modelo-form.fxml", "Nuevo Modelo");
    }

    /**
     * Prepara el controlador de edición con el modelo seleccionado
     * (si hay alguno) y abre la vista correspondiente.
     */
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

    /**
     * Abre la vista para eliminar una marca o modelo.
     */
    @FXML
    private void onEliminar() {
        MainController.getInstance().cargarVista("/fxml/eliminar-marca-modelo.fxml", "Eliminar Marca/Modelo");
    }
}