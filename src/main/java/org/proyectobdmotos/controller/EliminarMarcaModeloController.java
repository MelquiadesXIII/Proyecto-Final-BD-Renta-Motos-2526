package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.proyectobdmotos.models.Marca;
import org.proyectobdmotos.models.Modelo;
import org.proyectobdmotos.services.MarcaService;
import org.proyectobdmotos.services.ModeloService;
import org.proyectobdmotos.utils.*;


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

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    /**
     * Llena los combos y configura la exclusividad de selección:
     * al elegir un modelo se limpia la marca y viceversa.
     */
    @FXML
    private void initialize() {
        cargarModelos();
        cargarMarcas();
        enlazarExclusividadDeSeleccion();
    }

    /**
     * Añade listeners a ambos combos para que, al seleccionar un
     * elemento, se limpie automáticamente la selección del otro.
     */
    private void enlazarExclusividadDeSeleccion() {
        comboModelo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) comboMarca.getSelectionModel().clearSelection();
        });
        comboMarca.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) comboModelo.getSelectionModel().clearSelection();
        });
    }

    // -----------------------------------------------------------------
    // Carga de combos
    // -----------------------------------------------------------------

    /**
     * Llena el combo de modelos y configura la visualización
     * para que muestre el nombre de cada modelo.
     */
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

    /**
     * Llena el combo de marcas y configura la visualización
     * para que muestre el nombre de cada marca.
     */
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

    // -----------------------------------------------------------------
    // Acción Eliminar
    // -----------------------------------------------------------------

    /**
     * Orquesta la eliminación según el elemento seleccionado:
     * modelo o marca. Si no se selecciona nada, muestra un mensaje.
     */
    @FXML
    private void onEliminar() {
        Modelo modelo = comboModelo.getValue();
        Marca marca = comboMarca.getValue();

        if (modelo == null && marca == null) {
            AlertUtils.mostrarError("Seleccione un modelo o una marca");
        } else if (modelo != null) {
            eliminarModeloSiEsPosible(modelo);
        } else {
            eliminarMarcaSiEsPosible(marca);
        }
    }

    /**
     * Verifica si el modelo tiene motos asociadas. Si no las tiene,
     * pide confirmación y, si se acepta, lo elimina.
     */
    private void eliminarModeloSiEsPosible(Modelo modelo) {
        if (modeloService.existeMotoConModelo(modelo.getIdModelo())) {
            AlertUtils.mostrarError("No se puede eliminar: hay motos que usan este modelo");
        } else {
            boolean confirmado = AlertUtils.mostrarConfirmacion(
                    "Eliminar modelo",
                    "¿Eliminar el modelo \"" + modelo.getNombreModelo() + "\"?",
                    "Esta acción no se puede deshacer."
            );
            if (confirmado) {
                modeloService.eliminarModelo(modelo.getIdModelo());
                MainController.getInstance().onGoBack();
            }
        }
    }

    /**
     * Verifica si la marca tiene modelos o motos asociadas. Si no las tiene,
     * pide confirmación y, si se acepta, la elimina.
     */
    private void eliminarMarcaSiEsPosible(Marca marca) {
        boolean tieneDependencias = marcaService.existenModelosConMarca(marca.getIdMarca())
                || marcaService.existenMotosConMarca(marca.getIdMarca());
        if (tieneDependencias) {
            AlertUtils.mostrarError("No se puede eliminar: la marca tiene modelos o motos asociadas");
        } else {
            boolean confirmado = AlertUtils.mostrarConfirmacion(
                    "Eliminar marca",
                    "¿Eliminar la marca \"" + marca.getNombreMarca() + "\"?",
                    "Esta acción no se puede deshacer."
            );
            if (confirmado) {
                marcaService.eliminarMarca(marca.getIdMarca());
                MainController.getInstance().onGoBack();
            }
        }
    }

    // -----------------------------------------------------------------
    // Cancelar
    // -----------------------------------------------------------------

    /**
     * Vuelve a la pantalla anterior sin eliminar nada.
     */
    @FXML
    private void onCancelar() {
        MainController.getInstance().onGoBack();
    }
}