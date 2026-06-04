package org.proyectobdmotos.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.services.ContratoService;
import org.proyectobdmotos.services.exceptions.BusinessException;
import org.proyectobdmotos.services.exceptions.ValidationException;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.utils.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ContratoController {

    private final ContratoService contratoService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;

    @FXML private TableView<Contrato> tablaContratos;
    @FXML private TableColumn<Contrato, Integer> colId;
    @FXML private TableColumn<Contrato, String> colCliente;
    @FXML private TableColumn<Contrato, String> colMoto;
    @FXML private TableColumn<Contrato, LocalDate> colFechaInicio;
    @FXML private TableColumn<Contrato, LocalDate> colFechaFin;
    @FXML private TableColumn<Contrato, String> colEstado;
    @FXML private TableColumn<Contrato, String> colImporte;

    public ContratoController(
            ContratoService contratoService,
            AgenciaStore agenciaStore,
            ReferenceDataStore referenceDataStore
    ) {
        this.contratoService = contratoService;
        this.agenciaStore = agenciaStore;
        this.referenceDataStore = referenceDataStore;
    }

    @FXML
    private void initialize() {
        Logger.log("Inicializando ContratoController...");
        configureTableColumns();
        bindStore();
        loadContratos();
    }

    @FXML
    private void onEliminarContrato() {
        Contrato contratoSeleccionado = tablaContratos.getSelectionModel().getSelectedItem();
        if (contratoSeleccionado == null) {
            mostrarAlerta("Seleccione un contrato de la tabla para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar el contrato #" + contratoSeleccionado.getIdContrato() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                contratoService.eliminarContrato(contratoSeleccionado.getIdContrato());
                loadContratos();
                mostrarAlerta("Contrato eliminado correctamente.");
            } catch (ValidationException e) {
                mostrarAlerta("Error al eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onActualizarLista() {
        loadContratos();
    }

    @FXML
    private void onFinalizarContrato() {
        Contrato contrato = tablaContratos.getSelectionModel().getSelectedItem();

        if (contrato == null) {
            mostrarAlerta("Seleccione un contrato de la tabla para finalizar.");
            return;
        }
        if (contrato.getFechaEntrega() != null) {
            mostrarAlerta("Este contrato ya está finalizado.");
            return;
        }

        Stage dialogo = new Stage();
        dialogo.initModality(Modality.APPLICATION_MODAL);
        dialogo.initOwner(tablaContratos.getScene().getWindow());
        dialogo.initStyle(StageStyle.UTILITY);
        dialogo.setResizable(false);
        dialogo.setTitle("Finalizar Contrato #" + contrato.getIdContrato());

        DatePicker dpFechaEntrega = new DatePicker();
        TextField tfKmLlegada = new TextField();
        tfKmLlegada.setPromptText("Kilómetros de llegada");

        Button btnAceptar = new Button("Aceptar");
        Button btnCancelar = new Button("Cancelar");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("Fecha de entrega:"), 0, 0);
        grid.add(dpFechaEntrega, 1, 0);
        grid.add(new Label("Km de llegada:"), 0, 1);
        grid.add(tfKmLlegada, 1, 1);

        HBox botones = new HBox(10, btnAceptar, btnCancelar);
        VBox root = new VBox(15, grid, botones);
        root.setPadding(new Insets(15));
        Scene scene = new Scene(root);
        dialogo.setScene(scene);

        btnAceptar.setOnAction(e -> {
            LocalDate fechaEntrega = dpFechaEntrega.getValue();
            String kmTexto = tfKmLlegada.getText().trim();

            if (fechaEntrega == null || kmTexto.isEmpty()) {
                mostrarAlerta("Complete todos los campos.");
                return;
            }

            try {
                double kmLlegada = Double.parseDouble(kmTexto);
                contrato.setFechaEntrega(fechaEntrega);
                contrato.setCantKmLlegada(kmLlegada);

                contratoService.finalizarContrato(contrato);

                double importeTotal = contrato.calcularImporteTotalTeorico();

                new Alert(Alert.AlertType.INFORMATION,
                        "Contrato finalizado.\nImporte total: " +
                                String.format("%.2f CUP", importeTotal)).showAndWait();

                loadContratos();
                dialogo.close();
            } catch (NumberFormatException ex) {
                mostrarAlerta("Kilómetros inválidos. Debe ser un número.");
            } catch (ValidationException ex) {
                mostrarAlerta("Error de negocio: " + ex.getMessage());
            } catch (Exception ex) {
                Logger.logError("Error al finalizar contrato: " + ex.getMessage());
                mostrarAlerta("Error inesperado al finalizar el contrato.");
            }
        });

        btnCancelar.setOnAction(e -> dialogo.close());
        dialogo.showAndWait();
    }

    private void configureTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idContrato"));

        colCliente.setCellValueFactory(cellData ->
                new SimpleStringProperty("Cliente #" + cellData.getValue().getIdCliente()));
        colMoto.setCellValueFactory(cellData ->
                new SimpleStringProperty("Moto #" + cellData.getValue().getIdMoto()));

        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFechaFin.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));

        colEstado.setCellValueFactory(cellData -> {
            String estado = "Activo";
            if (cellData.getValue().getFechaEntrega() != null) {
                estado = "Finalizado";
            }
            return new SimpleStringProperty(estado);
        });

        colImporte.setCellValueFactory(cellData -> {
            double importe = cellData.getValue().calcularImporteTotalTeorico();
            return new SimpleStringProperty(String.format("%.2f CUP", importe));
        });
    }

    private void bindStore() {
        tablaContratos.setItems(agenciaStore.getContratos());
    }

    private void loadContratos() {
        Task<List<Contrato>> loadTask = new Task<>() {
            @Override
            protected List<Contrato> call() {
                return contratoService.listarTodos();
            }
        };

        loadTask.setOnSucceeded(event -> {
            List<Contrato> contratos = loadTask.getValue();
            if (contratos != null) {
                agenciaStore.setContratos(contratos);
                Logger.logInfo("Contratos cargados: " + contratos.size());
            }
        });

        loadTask.setOnFailed(event -> {
            Throwable throwable = loadTask.getException();
            String message = throwable != null ? throwable.getMessage() : "Sin detalle";
            boolean isBusinessError = throwable instanceof BusinessException;
            if (isBusinessError) {
                Logger.logError("Error de negocio cargando contratos: " + message);
                showError("No se pudieron cargar los contratos", message);
            } else {
                Logger.logError("Error inesperado cargando contratos: " + message);
                showError("Error cargando contratos", message);
            }
        });

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void showError(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}