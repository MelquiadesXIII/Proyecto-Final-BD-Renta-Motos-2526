package org.proyectobdmotos.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import org.proyectobdmotos.dto.MisContratosDTO;
import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.models.Marca;
import org.proyectobdmotos.models.Modelo;
import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.services.ContratoService;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.utils.*;

public class MisContratosController {

    @FXML private StackPane rootPane;

    @FXML private TableView<Moto> tablaMotos;
    @FXML private TableColumn<Moto, String> colMatricula, colMarca, colModelo, colColor;
    @FXML private TableColumn<Moto, Double> colKm;

    @FXML private TableView<MisContratosDTO> tablaContratos;
    @FXML private TableColumn<MisContratosDTO, String> colContratoMoto, colFechaInicio, colFechaFin;
    @FXML private TableColumn<MisContratosDTO, Double> colCosto;
    @FXML private TableColumn<MisContratosDTO, String> colFechaEntrega;

    @FXML private Label labelSinContratos;
    @FXML private Button btnFinalizarContrato;

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

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    @FXML
    private void initialize() {

        if (rootPane != null) {
            rootPane.setStyle(
                    "-fx-background-image: url('"
                            + getClass().getResource("/Utiles/fondoMiscontratos.jpg").toExternalForm()
                            + "');"
                            + "-fx-background-size: cover;"
                            + "-fx-background-position: center center;"
                            + "-fx-background-repeat: no-repeat;"
            );
        }

        configurarColumnasMotos();
        configurarColumnasContratos();
        cargarMotos();
        cargarContratos();
        btnFinalizarContrato.setDisable(true);
        tablaContratos.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) ->
                btnFinalizarContrato.setDisable(newVal == null));

        fijarColumnas(tablaMotos);
        fijarColumnas(tablaContratos);
    }

    // -----------------------------------------------------------------
    // Configuración de columnas
    // -----------------------------------------------------------------

    private void configurarColumnasMotos() {
        configurarColumnaMatricula();
        configurarColumnaMarca();
        configurarColumnaModelo();
        configurarColumnaColor();
        configurarColumnaKm();
    }

    private void configurarColumnaMatricula() {
        colMatricula.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getMatriculaMoto()));
    }

    private void configurarColumnaMarca() {
        colMarca.setCellValueFactory(cellData -> {
            int idMarca = obtenerIdMarcaDeMoto(cellData.getValue());
            String nombreMarca = obtenerNombreMarca(idMarca);
            return new javafx.beans.property.SimpleStringProperty(nombreMarca);
        });
    }

    private void configurarColumnaModelo() {
        colModelo.setCellValueFactory(cellData -> {
            int idModelo = cellData.getValue().getIdModelo();
            String nombreModelo = obtenerNombreModelo(idModelo);
            return new javafx.beans.property.SimpleStringProperty(nombreModelo);
        });
    }

    private void configurarColumnaColor() {
        colColor.setCellValueFactory(cellData -> {
            int idColor = cellData.getValue().getIdColor();
            String nombreColor = obtenerNombreColor(idColor);
            return new javafx.beans.property.SimpleStringProperty(nombreColor);
        });
    }

    private void configurarColumnaKm() {
        colKm.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(
                        cellData.getValue().getCantKmRecorridos()).asObject());
    }

    private void configurarColumnasContratos() {
        colContratoMoto.setCellValueFactory(new PropertyValueFactory<>("motoInfo"));
        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFechaFin.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colCosto.setCellValueFactory(new PropertyValueFactory<>("costoTotal"));
        colFechaEntrega.setCellValueFactory(new PropertyValueFactory<>("fechaEntrega"));
    }

    // -----------------------------------------------------------------
    // Cachés para nombres
    // -----------------------------------------------------------------

    private int obtenerIdMarcaDeMoto(Moto moto) {
        int idModelo = moto.getIdModelo();
        Modelo modelo = null;
        try {
            modelo = motoService.obtenerModeloPorId(idModelo);
        } catch (Exception e) {
            e.printStackTrace();
            modelo = null;
        }
        int idMarca = -1;
        if (modelo != null) {
            idMarca = modelo.getIdMarca();
        }
        return idMarca;
    }

    private String obtenerNombreMarca(int idMarca) {
        String nombre = cacheMarcas.get(idMarca);
        if (nombre == null) {
            Marca marca = null;
            try {
                marca = motoService.obtenerMarcaPorId(idMarca);
            } catch (Exception e) {
                e.printStackTrace();
                marca = null;
            }
            nombre = (marca != null) ? marca.getNombreMarca() : "Desconocida";
            cacheMarcas.put(idMarca, nombre);
        }
        return nombre;
    }

    private String obtenerNombreModelo(int idModelo) {
        String nombre = cacheModelos.get(idModelo);
        if (nombre == null) {
            Modelo modelo = null;
            try {
                modelo = motoService.obtenerModeloPorId(idModelo);
            } catch (Exception e) {
                e.printStackTrace();
                modelo = null;
            }
            nombre = (modelo != null) ? modelo.getNombreModelo() : "Desconocido";
            cacheModelos.put(idModelo, nombre);
        }
        return nombre;
    }

    private String obtenerNombreColor(int idColor) {
        String nombre = cacheColores.get(idColor);
        if (nombre == null) {
            try {
                nombre = motoService.obtenerNombreColorPorId(idColor);
            } catch (Exception e) {
                e.printStackTrace();
                nombre = "Color #" + idColor;
            }
            cacheColores.put(idColor, nombre);
        }
        return nombre;
    }

    // -----------------------------------------------------------------
    // Carga de datos + Autoajuste de columnas
    // -----------------------------------------------------------------

    private void cargarMotos() {
        List<Moto> motos = motoService.listarTodos();
        tablaMotos.getItems().setAll(motos);
        ajustarAnchoColumnasMotos();
    }

    private void cargarContratos() {
        int idCliente = agenciaStore.getClienteActual().getIdCliente();
        List<MisContratosDTO> contratos = contratoService.listarMisContratos(idCliente);
        tablaContratos.getItems().setAll(contratos);
        boolean sinContratos = contratos.isEmpty();
        labelSinContratos.setVisible(sinContratos);
        labelSinContratos.setManaged(sinContratos);
        ajustarAnchoColumnasContratos();
    }

    /**
     * Calcula el ancho real del texto en píxeles, usando fuente 14,
     * bold para cabeceras y normal para datos.
     */
    private double medirAnchoTexto(String texto, boolean bold) {
        Font font = bold ? Font.font("System", FontWeight.BOLD, 14)
                : Font.font("System", 14);
        Text text = new Text(texto);
        text.setFont(font);
        return text.getLayoutBounds().getWidth() + 25;
    }

    private void ajustarAnchoColumnasMotos() {
        ajustarColumnaMoto(colMatricula);
        ajustarColumnaMoto(colMarca);
        ajustarColumnaMoto(colModelo);
        ajustarColumnaMoto(colColor);
        ajustarColumnaKm();

        Platform.runLater(() -> {
            double total = 0;
            for (TableColumn<Moto, ?> c : tablaMotos.getColumns()) {
                total += c.getPrefWidth();
            }
            tablaMotos.setPrefWidth(total + 10);
            tablaMotos.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        });
    }

    private void ajustarAnchoColumnasContratos() {
        ajustarColumnaContrato(colContratoMoto);
        ajustarColumnaContrato(colFechaInicio);
        ajustarColumnaContrato(colFechaFin);
        ajustarColumnaCosto();
        ajustarColumnaContrato(colFechaEntrega);

        Platform.runLater(() -> {
            double total = 0;
            for (TableColumn<MisContratosDTO, ?> c : tablaContratos.getColumns()) {
                total += c.getPrefWidth();
            }
            tablaContratos.setPrefWidth(total + 10);
            tablaContratos.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        });
    }

    private void ajustarColumnaMoto(TableColumn<Moto, String> col) {
        double max = medirAnchoTexto(col.getText(), true);
        for (Moto item : tablaMotos.getItems()) {
            String valor = col.getCellData(item);
            if (valor != null) {
                double w = medirAnchoTexto(valor, false);
                if (w > max) max = w;
            }
        }
        col.setPrefWidth(max);
        col.setMinWidth(max);
        col.setMaxWidth(max);
    }

    private void ajustarColumnaKm() {
        double max = medirAnchoTexto(colKm.getText(), true);
        for (Moto item : tablaMotos.getItems()) {
            Double valor = colKm.getCellData(item);
            if (valor != null) {
                String texto = String.format("%.1f km", valor);
                double w = medirAnchoTexto(texto, false);
                if (w > max) max = w;
            }
        }
        colKm.setPrefWidth(max);
        colKm.setMinWidth(max);
        colKm.setMaxWidth(max);
    }

    private void ajustarColumnaContrato(TableColumn<MisContratosDTO, String> col) {
        double max = medirAnchoTexto(col.getText(), true);
        for (MisContratosDTO item : tablaContratos.getItems()) {
            String valor = col.getCellData(item);
            if (valor != null) {
                double w = medirAnchoTexto(valor, false);
                if (w > max) max = w;
            }
        }
        col.setPrefWidth(max);
        col.setMinWidth(max);
        col.setMaxWidth(max);
    }

    private void ajustarColumnaCosto() {
        double max = medirAnchoTexto(colCosto.getText(), true);
        for (MisContratosDTO item : tablaContratos.getItems()) {
            Double valor = colCosto.getCellData(item);
            if (valor != null) {
                String texto = String.format("$%.2f", valor);
                double w = medirAnchoTexto(texto, false);
                if (w > max) max = w;
            }
        }
        colCosto.setPrefWidth(max);
        colCosto.setMinWidth(max);
        colCosto.setMaxWidth(max);
    }

    // -----------------------------------------------------------------
    // Acciones del usuario
    // -----------------------------------------------------------------

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
                    mostrarAlerta("Datos inválidos.");
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
        DatosFinalizacion datos = null;
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                double km = Double.parseDouble(kmField.getText().trim());
                if (datePicker.getValue() == null) {
                    mostrarAlerta("Debe seleccionar una fecha.");
                } else {
                    datos = new DatosFinalizacion(km, datePicker.getValue());
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                mostrarAlerta("Ingrese un número válido para los kilómetros.");
            }
        }
        return Optional.ofNullable(datos);
    }

    private void procesarFinalizacion(MisContratosDTO dto, double kmLlegada, LocalDate fechaEntrega) {
        Optional<Contrato> optContrato = contratoService.buscarPorId(dto.getIdContrato());
        if (optContrato.isEmpty()) {
            mostrarAlerta("El contrato ya no existe.");
        } else {
            Contrato contrato = optContrato.get();
            boolean kmValido = kmLlegada >= contrato.getCantKmSalida();
            boolean fechaValida = !fechaEntrega.isAfter(LocalDate.now());
            if (!kmValido) {
                mostrarAlerta("Los kilómetros de llegada no pueden ser menores que los de salida "
                        + contrato.getCantKmSalida() + " km).");
            } else if (!fechaValida) {
                mostrarAlerta("La fecha de entrega no puede ser posterior a hoy.");
            } else {
                contrato.setCantKmLlegada(kmLlegada);
                contrato.setFechaEntrega(fechaEntrega);
                try {
                    contratoService.finalizarContrato(contrato);
                    cargarContratos();
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarAlerta("Error al finalizar: " + e.getMessage());
                }
            }
        }
    }

    // -----------------------------------------------------------------
    // Clase interna para datos de finalización
    // -----------------------------------------------------------------

    private static class DatosFinalizacion {
        final double kmLlegada;
        final LocalDate fechaEntrega;
        DatosFinalizacion(double kmLlegada, LocalDate fechaEntrega) {
            this.kmLlegada = kmLlegada;
            this.fechaEntrega = fechaEntrega;
        }
    }

    // -----------------------------------------------------------------
    // Utilidades
    // -----------------------------------------------------------------

    private void mostrarAlerta(String mensaje) {
        AlertUtils.mostrarError(mensaje);
    }

    private void fijarColumnas(TableView<?> tabla) {
        int i = 0;
        while (i < tabla.getColumns().size()) {
            TableColumn<?, ?> columna = tabla.getColumns().get(i);
            columna.setResizable(false);
            columna.setReorderable(false);
            i++;
        }
    }
}