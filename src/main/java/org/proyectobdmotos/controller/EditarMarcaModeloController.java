package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.proyectobdmotos.models.Marca;
import org.proyectobdmotos.models.Modelo;
import org.proyectobdmotos.services.MarcaService;
import org.proyectobdmotos.services.ModeloService;
import org.proyectobdmotos.services.MotoService;

import java.util.List;

public class EditarMarcaModeloController {

    @FXML private RadioButton radioModelo, radioMarca;
    @FXML private ComboBox<Modelo> comboModelo;
    @FXML private ComboBox<Marca> comboMarca;
    @FXML private TextField campoNombreModelo, campoNombreMarca;
    @FXML private Label valorMarcaAsociada;
    @FXML private Label labelMarcaAsociada;
    @FXML private ToggleGroup grupoTipo;

    private final ModeloService modeloService;
    private final MarcaService marcaService;
    private final MotoService motoService;

    private static Integer idModeloPreseleccionado;

    public static void setIdModeloPreseleccionado(Integer id) {
        idModeloPreseleccionado = id;
    }

    public EditarMarcaModeloController(ModeloService modeloService, MarcaService marcaService, MotoService motoService) {
        this.modeloService = modeloService;
        this.marcaService = marcaService;
        this.motoService = motoService;
    }

    @FXML
    private void initialize() {
        cargarCombos();

        grupoTipo.selectedToggleProperty().addListener((obs, oldVal, newVal) -> actualizarVisibilidad());

        if (idModeloPreseleccionado != null) {
            radioModelo.setSelected(true);
            Modelo encontrado = null;
            for (Modelo m : comboModelo.getItems()) {
                if (m.getIdModelo() == idModeloPreseleccionado) {
                    encontrado = m;
                }
            }
            if (encontrado != null) {
                comboModelo.getSelectionModel().select(encontrado);
            }
            idModeloPreseleccionado = null;
        } else {
            radioModelo.setSelected(true);
        }
        actualizarVisibilidad();
    }

    private void cargarCombos() {
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

    private void actualizarVisibilidad() {
        boolean esModelo = radioModelo.isSelected();

        comboModelo.setVisible(esModelo);
        comboModelo.setManaged(esModelo);
        campoNombreModelo.setVisible(esModelo);
        campoNombreModelo.setManaged(esModelo);
        labelMarcaAsociada.setVisible(esModelo);
        labelMarcaAsociada.setManaged(esModelo);
        valorMarcaAsociada.setVisible(esModelo);
        valorMarcaAsociada.setManaged(esModelo);

        comboMarca.setVisible(!esModelo);
        comboMarca.setManaged(!esModelo);
        campoNombreMarca.setVisible(!esModelo);
        campoNombreMarca.setManaged(!esModelo);

        if (esModelo) {
            Modelo seleccionado = comboModelo.getValue();
            if (seleccionado != null) {
                campoNombreModelo.setText(seleccionado.getNombreModelo());
                actualizarMarcaAsociada(seleccionado.getIdMarca());
            } else {
                campoNombreModelo.clear();
                valorMarcaAsociada.setText("");
            }
        } else {
            Marca seleccionada = comboMarca.getValue();
            if (seleccionada != null) {
                campoNombreMarca.setText(seleccionada.getNombreMarca());
            } else {
                campoNombreMarca.clear();
            }
        }

        comboModelo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && radioModelo.isSelected()) {
                campoNombreModelo.setText(newVal.getNombreModelo());
                actualizarMarcaAsociada(newVal.getIdMarca());
            }
        });
        comboMarca.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && radioMarca.isSelected()) {
                campoNombreMarca.setText(newVal.getNombreMarca());
            }
        });
    }

    private void actualizarMarcaAsociada(int idMarca) {
        String nombre = "";
        for (Marca m : marcaService.listarTodas()) {
            if (m.getIdMarca() == idMarca) {
                nombre = m.getNombreMarca();
            }
        }
        valorMarcaAsociada.setText(nombre);
    }

    @FXML
    private void onGuardar() {
        if (radioModelo.isSelected()) {
            Modelo modelo = comboModelo.getValue();
            String nuevoNombre = campoNombreModelo.getText().trim();

            if (modelo == null || nuevoNombre.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Complete todos los campos.").showAndWait();
            } else {
                int idMarca = modelo.getIdMarca();
                if (modeloService.existeModelo(idMarca, nuevoNombre) &&
                        !modelo.getNombreModelo().equalsIgnoreCase(nuevoNombre)) {
                    new Alert(Alert.AlertType.ERROR, "El modelo ya existe en esa marca.").showAndWait();
                } else {
                    modelo.setNombreModelo(nuevoNombre);
                    modeloService.actualizarModelo(modelo);
                    MainController.getInstance().onGoBack();
                }
            }
        } else {
            Marca marca = comboMarca.getValue();
            String nuevoNombre = campoNombreMarca.getText().trim();

            if (marca == null || nuevoNombre.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Complete todos los campos.").showAndWait();
            } else {
                if (marcaService.existeMarca(nuevoNombre) &&
                        !marca.getNombreMarca().equalsIgnoreCase(nuevoNombre)) {
                    new Alert(Alert.AlertType.ERROR, "La marca ya existe.").showAndWait();
                } else {
                    marca.setNombreMarca(nuevoNombre);
                    marcaService.actualizarMarca(marca);
                    MainController.getInstance().onGoBack();
                }
            }
        }
    }

    @FXML
    private void onCancelar() {
        MainController.getInstance().onGoBack();
    }
}