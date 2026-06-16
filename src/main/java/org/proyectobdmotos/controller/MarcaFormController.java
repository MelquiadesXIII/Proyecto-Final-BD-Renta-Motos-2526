package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.proyectobdmotos.services.MarcaService;

public class MarcaFormController {

    @FXML private TextField campoNombreMarca;
    @FXML private StackPane rootPane;

    private final MarcaService marcaService;

    public MarcaFormController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    /**
     * Inicializa la pantalla aplicando el fondo.
     */

    @FXML
    private void initialize() {
        if (rootPane != null) {
            rootPane.setStyle(
                    "-fx-background-image: url('"
                            + getClass().getResource("/Utiles/algoDos.jpeg").toExternalForm()
                            + "');"
                            + "-fx-background-size: cover;"
                            + "-fx-background-position: center center;"
                            + "-fx-background-repeat: no-repeat;"
            );
        }
    }

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

    @FXML
    private void onCancelar() {
        MainController.getInstance().onGoBack();
    }
}