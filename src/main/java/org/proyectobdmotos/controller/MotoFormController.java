package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.proyectobdmotos.models.*;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;

import java.util.ArrayList;

public class MotoFormController {

    @FXML private TextField campoMatricula;
    @FXML private ComboBox<Marca> comboMarca;
    @FXML private ComboBox<Modelo> comboModelo;
    @FXML private ComboBox<Color> comboColor;
    @FXML private TextField campoKilometros;

    private final MotoService motoService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;

    public MotoFormController(MotoService motoService,
                              AgenciaStore agenciaStore,
                              ReferenceDataStore referenceDataStore) {
        this.motoService = motoService;
        this.agenciaStore = agenciaStore;
        this.referenceDataStore = referenceDataStore;
    }

    @FXML
    private void initialize() {
        ArrayList<Marca> marcas = motoService.listarMarcas();
        comboMarca.getItems().setAll(marcas);

        comboMarca.setCellFactory(param -> new ListCell<Marca>() {
            @Override
            protected void updateItem(Marca item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreMarca());
            }
        });
        comboMarca.setConverter(new StringConverter<Marca>() {
            @Override
            public String toString(Marca marca) {
                return (marca != null) ? marca.getNombreMarca() : "";
            }
            @Override
            public Marca fromString(String string) { return null; }
        });

        comboMarca.getSelectionModel().selectedItemProperty().addListener((obs, oldMarca, newMarca) -> {
            comboModelo.getItems().clear();
            if (newMarca != null) {
                ArrayList<Modelo> modelos = motoService.listarModelosPorMarca(newMarca.getIdMarca());
                if (modelos != null) {
                    comboModelo.getItems().setAll(modelos);
                }
            }
        });

        comboModelo.setCellFactory(param -> new ListCell<Modelo>() {
            @Override
            protected void updateItem(Modelo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreModelo());
            }
        });
        comboModelo.setConverter(new StringConverter<Modelo>() {
            @Override
            public String toString(Modelo modelo) {
                return (modelo != null) ? modelo.getNombreModelo() : "";
            }
            @Override
            public Modelo fromString(String string) { return null; }
        });

        ArrayList<Color> colores = motoService.listarColores();
        comboColor.getItems().setAll(colores);

        comboColor.setCellFactory(param -> new ListCell<Color>() {
            @Override
            protected void updateItem(Color item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreColor());
            }
        });
        comboColor.setConverter(new StringConverter<Color>() {
            @Override
            public String toString(Color color) {
                return (color != null) ? color.getNombreColor() : "";
            }
            @Override
            public Color fromString(String string) { return null; }
        });

        comboModelo.disableProperty().bind(
                comboMarca.getSelectionModel().selectedItemProperty().isNull()
        );
    }

    public void setModoEdicion(Moto m) {
        campoMatricula.setText(m.getMatriculaMoto());
        campoKilometros.setText(String.valueOf(m.getCantKmRecorridos()));

        int idModelo = m.getIdModelo();
        Modelo modelo = motoService.obtenerModeloPorId(idModelo);

        if (modelo != null) {
            int idMarca = modelo.getIdMarca();
            Marca marca = motoService.obtenerMarcaPorId(idMarca);
            if (marca != null) {
                comboMarca.getSelectionModel().select(marca);
            }
            comboModelo.getSelectionModel().select(modelo);

            int idColor = m.getIdColor();
            for (Color c : comboColor.getItems()) {
                if (c.getIdColor() == idColor) {
                    comboColor.getSelectionModel().select(c);
                    break;
                }
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "No se pudo cargar el modelo de la moto.").showAndWait();
        }
    }

    @FXML
    private void onGuardar() {
        String matricula = campoMatricula.getText().trim();
        String kmTexto = campoKilometros.getText().trim();
        Modelo modeloSeleccionado = comboModelo.getValue();
        Color colorSeleccionado = comboColor.getValue();

        boolean datosValidos = true;

        if (matricula.isEmpty() || modeloSeleccionado == null || colorSeleccionado == null) {
            new Alert(Alert.AlertType.ERROR, "Todos los campos obligatorios deben estar completos.").showAndWait();
            datosValidos = false;
        }

        if (datosValidos) {
            try {
                double kilometros = Double.parseDouble(kmTexto);
                int idModelo = modeloSeleccionado.getIdModelo();
                int idColor = colorSeleccionado.getIdColor();

                Moto nuevaMoto = new Moto(
                        null,
                        matricula,
                        idModelo,
                        Situacion.DISPONIBLE,
                        kilometros,
                        idColor
                );

                motoService.crearMoto(nuevaMoto);

                new Alert(Alert.AlertType.INFORMATION, "Moto guardada correctamente.").showAndWait();
                cerrarVentana();

            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Los kilómetros deben ser un número válido.").showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error al guardar la moto: " + e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    private void onCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) campoMatricula.getScene().getWindow();
        stage.close();
    }
}