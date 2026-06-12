package org.proyectobdmotos.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.proyectobdmotos.dto.MisContratosDTO;
import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.models.Marca;
import org.proyectobdmotos.models.Modelo;
import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.services.ContratoService;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.stores.AgenciaStore;

public class MisContratosController {

    @FXML private TableView<Moto> tablaMotos;
    @FXML private TableColumn<Moto, String> colMatricula, colMarca, colModelo, colColor;
    @FXML private TableColumn<Moto, Double> colKm;

    @FXML private TableView<MisContratosDTO> tablaContratos;
    @FXML private TableColumn<MisContratosDTO, String> colContratoMoto, colFechaInicio, colFechaFin;
    @FXML private TableColumn<MisContratosDTO, Double> colCosto;

    @FXML private Label labelSinContratos;
    @FXML private Button btnFinalizarContrato;

    @FXML private TableColumn<MisContratosDTO, String> colFechaEntrega;

    private final MotoService motoService;
    private final ContratoService contratoService;
    private final AgenciaStore agenciaStore;

    private final Map<Integer, String> cacheMarcas = new HashMap<>();
    private final Map<Integer, String> cacheModelos = new HashMap<>();
    private final Map<Integer, String> cacheColores = new HashMap<>();

    public MisContratosController(MotoService motoService, ContratoService contratoService,
                                  AgenciaStore agenciaStore) {
        this.motoService = motoService;
        this.contratoService = contratoService;
        this.agenciaStore = agenciaStore;
    }

    @FXML
    private void initialize() {
        configurarColumnasMotos();
        configurarColumnasContratos();
        cargarMotos();
        cargarContratos();
        btnFinalizarContrato.setDisable(true);

        tablaContratos.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            btnFinalizarContrato.setDisable(newVal == null);
        });
    }

    private void configurarColumnasMotos() {
        colMatricula.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMatriculaMoto()));

        colMarca.setCellValueFactory(cellData -> {
            int idMarca = obtenerIdMarcaDeMoto(cellData.getValue());
            return new javafx.beans.property.SimpleStringProperty(obtenerNombreMarca(idMarca));
        });

        colModelo.setCellValueFactory(cellData -> {
            int idModelo = cellData.getValue().getIdModelo();
            return new javafx.beans.property.SimpleStringProperty(obtenerNombreModelo(idModelo));
        });

        colColor.setCellValueFactory(cellData -> {
            int idColor = cellData.getValue().getIdColor();
            return new javafx.beans.property.SimpleStringProperty(obtenerNombreColor(idColor));
        });

        colKm.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getCantKmRecorridos()).asObject());
    }

    private int obtenerIdMarcaDeMoto(Moto moto) {
        int idModelo = moto.getIdModelo();
        Modelo modelo = null;
        try {
            modelo = motoService.obtenerModeloPorId(idModelo);
        } catch (Exception e) {
            modelo = null;
        }
        if (modelo != null) {
            return modelo.getIdMarca();
        }
        return -1;
    }

    private String obtenerNombreMarca(int idMarca) {
        if (cacheMarcas.containsKey(idMarca)) {
            return cacheMarcas.get(idMarca);
        }
        Marca marca = null;
        try {
            marca = motoService.obtenerMarcaPorId(idMarca);
        } catch (Exception e) {
            marca = null;
        }
        String nombre = (marca != null) ? marca.getNombreMarca() : "Desconocida";
        cacheMarcas.put(idMarca, nombre);
        return nombre;
    }

    private String obtenerNombreModelo(int idModelo) {
        if (cacheModelos.containsKey(idModelo)) {
            return cacheModelos.get(idModelo);
        }
        Modelo modelo = null;
        try {
            modelo = motoService.obtenerModeloPorId(idModelo);
        } catch (Exception e) {
            modelo = null;
        }
        String nombre = (modelo != null) ? modelo.getNombreModelo() : "Desconocido";
        cacheModelos.put(idModelo, nombre);
        return nombre;
    }

    private String obtenerNombreColor(int idColor) {
        if (cacheColores.containsKey(idColor)) {
            return cacheColores.get(idColor);
        }
        String nombre = "Color #" + idColor;
        try {
            nombre = motoService.obtenerNombreColorPorId(idColor);
        } catch (Exception e) {

        }
        cacheColores.put(idColor, nombre);
        return nombre;
    }



    private void configurarColumnasContratos() {
        colContratoMoto.setCellValueFactory(new PropertyValueFactory<>("motoInfo"));
        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFechaFin.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colCosto.setCellValueFactory(new PropertyValueFactory<>("costoTotal"));
        colFechaEntrega.setCellValueFactory(new PropertyValueFactory<>("fechaEntrega"));  // nueva
    }

    private void cargarMotos() {
        List<Moto> motos = motoService.listarTodos();
        tablaMotos.getItems().setAll(motos);
    }

    private void cargarContratos() {
        int idCliente = agenciaStore.getClienteActual().getIdCliente();
        List<MisContratosDTO> contratos = contratoService.listarMisContratos(idCliente);
        tablaContratos.getItems().setAll(contratos);
        boolean sinContratos = contratos.isEmpty();
        labelSinContratos.setVisible(sinContratos);
        labelSinContratos.setManaged(sinContratos);
    }

    @FXML
    private void onCrearNuevoContrato() {
        UserMainController.getInstance().cargarVista("/fxml/contrato-usuario-form.fxml", "Nuevo Contrato");
    }

    @FXML
    private void onFinalizarContrato() {
        MisContratosDTO seleccionado = tablaContratos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            Optional<DatosFinalizacion> datos = mostrarDialogoFinalizacion();
            if (datos.isPresent()) {
                DatosFinalizacion d = datos.get();
                if (d.kmLlegada < 0 || d.fechaEntrega == null) {
                    new Alert(Alert.AlertType.ERROR, "Datos inválidos.").showAndWait();
                } else {
                    procesarFinalizacion(seleccionado, d.kmLlegada, d.fechaEntrega);
                }
            }
        }
    }

    private Optional<DatosFinalizacion> mostrarDialogoFinalizacion() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Finalizar contrato");
        dialog.setHeaderText("Ingrese los datos de entrega:");

        TextField kmField = new TextField();
        kmField.setPromptText("Kilometraje actual");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isAfter(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });

        VBox vbox = new VBox(10,
                new Label("Kilometraje de llegada:"), kmField,
                new Label("Fecha de entrega:"), datePicker);
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> resultado = dialog.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                double km = Double.parseDouble(kmField.getText().trim());
                if (datePicker.getValue() == null) {
                    new Alert(Alert.AlertType.ERROR, "Debe seleccionar una fecha.").showAndWait();
                    return Optional.empty();
                }
                return Optional.of(new DatosFinalizacion(km, datePicker.getValue()));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Ingrese un número válido para los kilómetros.").showAndWait();
            }
        }
        return Optional.empty();
    }

    private void procesarFinalizacion(MisContratosDTO dto, double kmLlegada, LocalDate fechaEntrega) {
        Optional<Contrato> optContrato = contratoService.buscarPorId(dto.getIdContrato());
        if (optContrato.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "El contrato ya no existe.").showAndWait();
        } else {
            Contrato contrato = optContrato.get();
            if (kmLlegada < contrato.getCantKmSalida()) {
                new Alert(Alert.AlertType.ERROR,
                        "Los kilómetros de llegada no pueden ser menores que los de salida ("
                                + contrato.getCantKmSalida() + " km).").showAndWait();
                return;
            }
            if (fechaEntrega.isAfter(LocalDate.now())) {
                new Alert(Alert.AlertType.ERROR,
                        "La fecha de entrega no puede ser posterior a hoy.").showAndWait();
                return;
            }

            contrato.setCantKmLlegada(kmLlegada);
            contrato.setFechaEntrega(fechaEntrega);
            try {
                contratoService.finalizarContrato(contrato);
                cargarContratos();
            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Error al finalizar: " + e.getMessage()).showAndWait();
            }
        }
    }

    private static class DatosFinalizacion {
        final double kmLlegada;
        final LocalDate fechaEntrega;

        DatosFinalizacion(double kmLlegada, LocalDate fechaEntrega) {
            this.kmLlegada = kmLlegada;
            this.fechaEntrega = fechaEntrega;
        }
    }
}