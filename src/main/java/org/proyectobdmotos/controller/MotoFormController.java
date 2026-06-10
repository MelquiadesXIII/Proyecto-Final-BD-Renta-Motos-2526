package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.proyectobdmotos.models.*;
import org.proyectobdmotos.services.MarcaService;
import org.proyectobdmotos.services.ModeloService;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.utils.Logger;

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
    private final MarcaService marcaService;
    private final ModeloService modeloService;

    private static Moto motoAEditarStatic;

    public static void setMotoAEditarStatic(Moto m) { motoAEditarStatic = m; }

    public MotoFormController(MotoService motoService,
                              AgenciaStore agenciaStore,
                              ReferenceDataStore referenceDataStore,
                              MarcaService marcaService,
                              ModeloService modeloService) {
        this.motoService = motoService;
        this.agenciaStore = agenciaStore;
        this.referenceDataStore = referenceDataStore;
        this.marcaService = marcaService;
        this.modeloService = modeloService;
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
            @Override public String toString(Marca m) { return m != null ? m.getNombreMarca() : ""; }
            @Override public Marca fromString(String s) { return null; }
        });

        comboMarca.getSelectionModel().selectedItemProperty().addListener((obs, old, newMarca) -> {
            comboModelo.getItems().clear();
            if (newMarca != null) {
                ArrayList<Modelo> modelos = motoService.listarModelosPorMarca(newMarca.getIdMarca());
                if (modelos != null) comboModelo.getItems().setAll(modelos);
            }
        });

        comboModelo.setCellFactory(param -> new ListCell<Modelo>() {
            @Override protected void updateItem(Modelo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreModelo());
            }
        });
        comboModelo.setConverter(new StringConverter<Modelo>() {
            @Override public String toString(Modelo m) { return m != null ? m.getNombreModelo() : ""; }
            @Override public Modelo fromString(String s) { return null; }
        });

        ArrayList<Color> colores = motoService.listarColores();
        comboColor.getItems().setAll(colores);
        comboColor.setCellFactory(param -> new ListCell<Color>() {
            @Override protected void updateItem(Color item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreColor());
            }
        });
        comboColor.setConverter(new StringConverter<Color>() {
            @Override public String toString(Color c) { return c != null ? c.getNombreColor() : ""; }
            @Override public Color fromString(String s) { return null; }
        });

        comboModelo.disableProperty().bind(comboMarca.getSelectionModel().selectedItemProperty().isNull());

        if (motoAEditarStatic != null) {
            setModoEdicion(motoAEditarStatic);
            motoAEditarStatic = null;
        }
    }

    public void setModoEdicion(Moto m) {
        campoMatricula.setText(m.getMatriculaMoto());
        campoKilometros.setText(String.valueOf(m.getCantKmRecorridos()));

        int idModelo = m.getIdModelo();
        Modelo modelo = motoService.obtenerModeloPorId(idModelo);
        if (modelo != null) {
            Marca marca = motoService.obtenerMarcaPorId(modelo.getIdMarca());
            if (marca != null) comboMarca.getSelectionModel().select(marca);
            comboModelo.getSelectionModel().select(modelo);

            int idColor = m.getIdColor();
            for (Color c : comboColor.getItems()) {
                if (c.getIdColor() == idColor) { comboColor.getSelectionModel().select(c); break; }
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "No se pudo cargar el modelo de la moto.").showAndWait();
        }
    }

    @FXML
    private void onGuardar() {
        String matricula = campoMatricula.getText().trim();
        String kmTexto = campoKilometros.getText().trim();
        Modelo modeloSel = comboModelo.getValue();
        Color colorSel = comboColor.getValue();

        if (matricula.isEmpty() || modeloSel == null || colorSel == null) {
            new Alert(Alert.AlertType.ERROR, "Todos los campos obligatorios deben estar completos.").showAndWait();
            return;
        }
        try {
            double km = Double.parseDouble(kmTexto);
            Moto nueva = new Moto(null, matricula, modeloSel.getIdModelo(), Situacion.DISPONIBLE, km, colorSel.getIdColor());
            motoService.crearMoto(nueva);
            new Alert(Alert.AlertType.INFORMATION, "Moto guardada correctamente.").showAndWait();
            MainController.getInstance().onGoBack();
        } catch (NumberFormatException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Los kilómetros deben ser un número válido.").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al guardar la moto: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onCancelar() {
        MainController.getInstance().onGoBack();
    }

}