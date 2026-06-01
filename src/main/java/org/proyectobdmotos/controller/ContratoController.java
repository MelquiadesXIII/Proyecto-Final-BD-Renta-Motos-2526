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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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

    // ===================== MÉTODOS DE LOS BOTONES =====================

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
        mostrarAlerta("Funcionalidad de finalizar contrato en desarrollo.");
    }

    // ===================== MÉTODOS PRIVADOS =====================

    private void configureTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idContrato"));

        // Convertir IDs a texto para mostrar en las columnas
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

        colImporte.setCellValueFactory(cellData ->
                new SimpleStringProperty("Pendiente"));
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