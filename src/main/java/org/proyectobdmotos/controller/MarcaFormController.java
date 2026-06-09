package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.proyectobdmotos.services.MarcaService;

public class MarcaFormController {

    @FXML private TextField campoNombreMarca;

    private final MarcaService marcaService;

    public MarcaFormController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @FXML
    private void onGuardar() {
        String nombre = campoNombreMarca.getText().trim();
        if (nombre.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "El nombre de la marca no puede estar vacío.").showAndWait();
            return;
        }
        try {
            marcaService.crearMarca(nombre);
            MainController.getInstance().onGoBack();
        } catch (RuntimeException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onCancelar() {
        MainController.getInstance().onGoBack();
    }
}