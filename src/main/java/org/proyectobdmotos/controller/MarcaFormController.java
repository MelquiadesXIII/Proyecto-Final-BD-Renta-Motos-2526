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

    /**
     * Valida el nombre de la marca y, si es correcto, la crea a través del servicio.
     * Si el nombre está vacío o la creación falla, muestra un mensaje de error.
     * Si la creación tiene éxito, regresa a la pantalla anterior.
     */
    @FXML
    private void onGuardar() {
        String nombre = campoNombreMarca.getText().trim();
        if (nombre.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "El nombre de la marca no puede estar vacío.").showAndWait();
        } else {
            try {
                marcaService.crearMarca(nombre);
                MainController.getInstance().onGoBack();
            } catch (RuntimeException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }
        }
    }

    /**
     * Cancela la operación y vuelve a la pantalla anterior.
     */
    @FXML
    private void onCancelar() {
        MainController.getInstance().onGoBack();
    }
}