package org.proyectobdmotos.controller;



import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.services.exceptions.BusinessException;
import org.proyectobdmotos.services.exceptions.ValidationException;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.utils.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;


/**
 * MotoController: maneja eventos de la UI de motos.
 * Delega operaciones a MotoService y actualiza/observa AgenciaStore.
 */

public class MotoController {

    private final MotoService motoService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;

    // Nombres coincidentes con moto-lista.fxml
    @FXML private TableView<Moto> tablaMotos;
    @FXML private TableColumn<Moto, String> colMatricula;
    @FXML private TableColumn<Moto, Integer> colModelo;
    @FXML private TableColumn<Moto, Integer> colColor;
    @FXML private TableColumn<Moto, Double> colKilometros;

    public MotoController(
            MotoService motoService,
            AgenciaStore agenciaStore,
            ReferenceDataStore referenceDataStore
    ) {
        this.motoService = motoService;
        this.agenciaStore = agenciaStore;
        this.referenceDataStore = referenceDataStore;
    }

    @FXML
    private void initialize() {
        Logger.log("Inicializando MotoController...");
        configureTableColumns();
        bindStore();
        loadMotos();
    }

    // ===================== MÉTODOS DE LOS BOTONES =====================

    @FXML
    private void onCrearMoto() {
        abrirFormulario(null);
    }

    @FXML
    private void onEditarMoto() {
        Moto motoSeleccionada = tablaMotos.getSelectionModel().getSelectedItem();
        if (motoSeleccionada == null) {
            mostrarAlerta("Seleccione una moto de la tabla para editar.");
        } else {
            abrirFormulario(motoSeleccionada);
        }
    }

    @FXML
    private void onEliminarMoto() {
        Moto motoSeleccionada = tablaMotos.getSelectionModel().getSelectedItem();
        if (motoSeleccionada == null) {
            mostrarAlerta("Seleccione una moto de la tabla para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar la moto con matrícula " + motoSeleccionada.getMatriculaMoto() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                motoService.eliminarMoto(motoSeleccionada.getMatriculaMoto());
                loadMotos();
                mostrarAlerta("Moto eliminada correctamente.");
            } catch (ValidationException e) {
                mostrarAlerta("Error al eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onActualizarLista() {
        loadMotos();
    }

    // ===================== MÉTODOS PRIVADOS =====================

    private void configureTableColumns() {
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matriculaMoto"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("idModelo"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("idColor"));
        colKilometros.setCellValueFactory(new PropertyValueFactory<>("cantKmRecorridos"));
    }

    private void bindStore() {
        tablaMotos.setItems(agenciaStore.getMotos());
    }

    private void loadMotos() {
        Task<List<Moto>> loadTask = new Task<>() {
            @Override
            protected List<Moto> call() {
                return motoService.listarTodos();
            }
        };

        loadTask.setOnSucceeded(event -> {
            List<Moto> motos = loadTask.getValue();
            if (motos != null) {
                agenciaStore.setMotos(motos);
                Logger.logInfo("Motos cargadas: " + motos.size());
            }
        });

        loadTask.setOnFailed(event -> {
            Throwable throwable = loadTask.getException();
            String message = throwable != null ? throwable.getMessage() : "Sin detalle";
            boolean isBusinessError = throwable instanceof BusinessException;
            if (isBusinessError) {
                Logger.logError("Error de negocio cargando motos: " + message);
                showError("No se pudieron cargar las motos", message);
            } else {
                Logger.logError("Error inesperado cargando motos: " + message);
                showError("Error cargando motos", message);
            }
        });

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void abrirFormulario(Moto moto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/moto-form.fxml"));
            MotoFormController formController = new MotoFormController(motoService, agenciaStore, referenceDataStore);
            loader.setController(formController);

            Parent root = loader.load();

            if (moto != null) {
                formController.setModoEdicion(moto);
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(moto == null ? "Nueva Moto" : "Editar Moto");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tablaMotos.getScene().getWindow());
            stage.showAndWait();

            loadMotos();
        } catch (IOException e) {
            Logger.logError("Error al cargar formulario de moto: " + e.getMessage());
            mostrarAlerta("No se pudo abrir el formulario.");
        }
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