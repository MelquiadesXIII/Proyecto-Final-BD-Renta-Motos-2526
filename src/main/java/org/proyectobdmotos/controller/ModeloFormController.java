package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.proyectobdmotos.models.Marca;
import org.proyectobdmotos.models.Modelo;
import org.proyectobdmotos.services.MarcaService;
import org.proyectobdmotos.services.ModeloService;
import org.proyectobdmotos.services.MotoService;

import java.util.List;

public class ModeloFormController {

    @FXML private ComboBox<Marca> comboMarca;
    @FXML private TextField campoNombreModelo;

    private final MotoService motoService;
    private final MarcaService marcaService;
    private final ModeloService modeloService;

    private Modelo modeloEditando;

    private static Modelo modeloEditarStatic;

    public static void setModeloEditarStatic(Modelo m) { modeloEditarStatic = m; }

    public ModeloFormController(MotoService motoService, MarcaService marcaService, ModeloService modeloService) {
        this.motoService = motoService;
        this.marcaService = marcaService;
        this.modeloService = modeloService;
    }

    @FXML
    private void initialize() {
        cargarMarcas();
        comboMarca.setConverter(new StringConverter<Marca>() {
            @Override public String toString(Marca m) { return m != null ? m.getNombreMarca() : ""; }
            @Override public Marca fromString(String s) { return null; }
        });
        comboMarca.setCellFactory(param -> new ListCell<Marca>() {
            @Override protected void updateItem(Marca item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreMarca());
            }
        });

        if (modeloEditarStatic != null) {
            setModoEdicion(modeloEditarStatic);
            modeloEditarStatic = null;
        }
    }

    private void cargarMarcas() {
        List<Marca> marcas = motoService.listarMarcas();
        comboMarca.getItems().setAll(marcas);
    }

    public void setModoEdicion(Modelo modelo) {
        this.modeloEditando = modelo;
        // Seleccionar la marca en el combo
        boolean encontrado = false;
        int i = 0;
        while (!encontrado && i < comboMarca.getItems().size()) {
            Marca m = comboMarca.getItems().get(i);
            if (m.getIdMarca() == modelo.getIdMarca()) {
                comboMarca.getSelectionModel().select(m);
                encontrado = true;
            }
            i++;
        }
        campoNombreModelo.setText(modelo.getNombreModelo());
    }

    @FXML
    private void onGuardar() {
        Marca marca = comboMarca.getValue();
        String nombre = campoNombreModelo.getText().trim();
        if (marca == null || nombre.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Complete todos los campos.").showAndWait();
            return;
        }
        try {
            if (modeloEditando != null) {
                // Actualizar modelo existente
                modeloEditando.setNombreModelo(nombre);
                modeloEditando.setIdMarca(marca.getIdMarca());
                modeloService.actualizarModelo(modeloEditando);
            } else {
                modeloService.crearModelo(marca.getIdMarca(), nombre);
            }
            MainController.getInstance().onGoBack();
        } catch (RuntimeException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onCancelar() {
        MainController.getInstance().onGoBack();
    }

    @FXML
    private void onCrearMarca() {
        MainController.getInstance().cargarVista("/fxml/marca-form.fxml", "Nueva Marca");
    }
}