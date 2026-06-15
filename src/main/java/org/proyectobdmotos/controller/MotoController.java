package org.proyectobdmotos.controller;

import java.util.List;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.models.Situacion;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.services.exceptions.BusinessException;
import org.proyectobdmotos.services.exceptions.ValidationException;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.utils.*;
import org.proyectobdmotos.utils.Logger;

import javafx.scene.control.ChoiceDialog;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class MotoController {

    private final MotoService motoService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;

    @FXML private TableView<Moto> tablaMotos;
    @FXML private TableColumn<Moto, String> colMatricula;
    @FXML private TableColumn<Moto, String> colMarca;
    @FXML private TableColumn<Moto, String> colModelo;
    @FXML private TableColumn<Moto, String> colColor;
    @FXML private TableColumn<Moto, Double> colKilometros;
    @FXML private TableColumn<Moto, String> colSituacion;

    public MotoController(
            MotoService motoService,
            AgenciaStore agenciaStore,
            ReferenceDataStore referenceDataStore
    ) {
        this.motoService = motoService;
        this.agenciaStore = agenciaStore;
        this.referenceDataStore = referenceDataStore;
    }

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    /**
     * Configura la tabla, la enlaza con el store y lanza la carga
     * asíncrona de motos al abrir la pantalla.
     */
    @FXML
    private void initialize() {
        Logger.log("Inicializando MotoController...");
        configureTableColumns();
        bindStore();
        loadMotos();
    }

    // -----------------------------------------------------------------
    // Configuración de columnas
    // -----------------------------------------------------------------

    /**
     * Asocia cada columna de la tabla con el atributo correspondiente
     * del modelo Moto.
     */
    private void configureTableColumns() {
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matriculaMoto"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("nombreMarca"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("nombreModelo"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("nombreColor"));
        colKilometros.setCellValueFactory(new PropertyValueFactory<>("cantKmRecorridos"));
        colSituacion.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSituacion().getValor())
        );
    }

    // -----------------------------------------------------------------
    // Vinculación con el store
    // -----------------------------------------------------------------

    /**
     * Enlaza la tabla con la lista observable de motos del store.
     * Los cambios en el store se reflejarán automáticamente.
     */
    private void bindStore() {
        tablaMotos.setItems(agenciaStore.getMotos());
    }

    // -----------------------------------------------------------------
    // Carga asíncrona de motos
    // -----------------------------------------------------------------

    /**
     * Inicia la carga en segundo plano de todas las motos.
     * Muestra los datos cuando la tarea termina exitosamente.
     */
    private void loadMotos() {
        Task<List<Moto>> loadTask = crearTareaCargaMotos();
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    /**
     * Construye la tarea que obtiene las motos del servicio.
     * Define el manejo de éxito y fallo al finalizar.
     */
    private Task<List<Moto>> crearTareaCargaMotos() {
        Task<List<Moto>> loadTask = new Task<>() {
            @Override
            protected List<Moto> call() {
                return motoService.listarTodos();
            }
        };

        loadTask.setOnSucceeded(event -> manejarCargaExitosa(loadTask.getValue()));
        loadTask.setOnFailed(event -> manejarCargaFallida(loadTask.getException()));

        return loadTask;
    }

    /**
     * Procesa la lista de motos obtenida y la coloca en el store.
     */
    private void manejarCargaExitosa(List<Moto> motos) {
        if (motos != null) {
            agenciaStore.setMotos(motos);
            Logger.logInfo("Motos cargadas: " + motos.size());
        }
    }

    /**
     * Muestra un mensaje de error apropiado si la carga falla,
     * diferenciando entre error de negocio y error técnico.
     */
    private void manejarCargaFallida(Throwable throwable) {
        String message = throwable != null ? throwable.getMessage() : "Sin detalle";
        boolean isBusinessError = throwable instanceof BusinessException;
        if (isBusinessError) {
            Logger.logError("Error de negocio cargando motos: " + message);
            showError("No se pudieron cargar las motos", message);
        } else {
            Logger.logError("Error inesperado cargando motos: " + message);
            showError("Error cargando motos", message);
        }
    }

    // -----------------------------------------------------------------
    // Navegación
    // -----------------------------------------------------------------

    /**
     * Abre el formulario para crear una nueva moto, sin datos previos.
     */
    @FXML
    private void onCrearMoto() {
        MotoFormController.setMotoAEditarStatic(null);
        MainController.getInstance().cargarVista("/fxml/moto-form-view.fxml", "Nueva Moto");
    }

    /**
     * Abre el formulario de moto en modo edición con los datos de la moto
     * seleccionada en la tabla. Si no hay selección, muestra un mensaje.
     */
    @FXML
    private void onEditarMoto() {
        Moto motoSeleccionada = tablaMotos.getSelectionModel().getSelectedItem();
        if (motoSeleccionada == null) {
            mostrarAlerta("Seleccione una moto de la tabla para editar.");
        } else {
            MotoFormController.setMotoAEditarStatic(motoSeleccionada);
            MainController.getInstance().cargarVista("/fxml/moto-form-view.fxml", "Editar Moto");
        }
    }

    // -----------------------------------------------------------------
    // Eliminación de moto
    // -----------------------------------------------------------------

    /**
     * Maneja la acción de eliminar la moto seleccionada.
     * Verifica la selección, pide confirmación y ejecuta el borrado.
     */
    @FXML
    private void onEliminarMoto() {
        Moto motoSeleccionada = tablaMotos.getSelectionModel().getSelectedItem();
        if (motoSeleccionada == null) {
            mostrarAlerta("Seleccione una moto de la tabla para eliminar.");
        } else {
            boolean confirmado = confirmarEliminacion(motoSeleccionada);
            if (confirmado) {
                ejecutarEliminacion(motoSeleccionada);
            }
        }
    }

    /**
     * Muestra un diálogo de confirmación y devuelve true si se acepta.
     */
    private boolean confirmarEliminacion(Moto moto) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar la moto con matrícula " + moto.getMatriculaMoto() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

    /**
     * Intenta eliminar la moto usando el servicio.
     * Si la operación falla, muestra un mensaje; si tiene éxito, recarga la tabla.
     */
    private void ejecutarEliminacion(Moto moto) {
        try {
            motoService.eliminarMoto(moto.getMatriculaMoto());
            loadMotos();
            mostrarAlerta("Moto eliminada correctamente.");
        } catch (ValidationException e) {
            e.printStackTrace();
            mostrarAlerta("Error al eliminar: " + e.getMessage());
        }
    }


    @FXML
    private void onCambiarEstado() {
        // Obtener la moto seleccionada
        Moto motoSeleccionada = tablaMotos.getSelectionModel().getSelectedItem();
        boolean haySeleccion = (motoSeleccionada != null);
        boolean puedeEjecutar = false;
        String errorSeleccion = null;

        if (!haySeleccion) {
            errorSeleccion = "Seleccione una moto de la tabla.";
        } else {
            puedeEjecutar = true;
        }

        // Si no hay selección, mostrar mensaje y detener el flujo sin return
        if (!puedeEjecutar) {
            if (errorSeleccion != null) {
                mostrarAlerta(errorSeleccion);
            }
        } else {
            // Construir el diálogo de opciones
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Disponible", "Disponible", "Taller");
            dialog.setTitle("Cambiar estado");
            dialog.setHeaderText("Moto: " + motoSeleccionada.getMatriculaMoto());
            dialog.setContentText("Seleccione el nuevo estado:");

            // Mostrar el diálogo y esperar
            Optional<String> resultado = dialog.showAndWait();
            boolean confirmado = resultado.isPresent();
            boolean operacionExitosa = false;
            String mensajeExito = null;
            String mensajeError = null;

            if (confirmado) {
                String estadoElegido = resultado.get();
                Situacion situacion = Situacion.fromValor(estadoElegido);

                try {
                    motoService.cambiarEstado(motoSeleccionada.getMatriculaMoto(), situacion);
                    loadMotos();
                    mensajeExito = "Estado cambiado a " + estadoElegido + ".";
                    operacionExitosa = true;
                } catch (ValidationException e) {
                    e.printStackTrace();
                    Logger.logError("Error al cambiar estado de moto: " + e.getMessage());
                    mensajeError = "Error al cambiar estado: " + e.getMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.logError("Error inesperado al cambiar estado: " + e.getMessage());
                    mensajeError = "Error inesperado al cambiar estado.";
                }
            }

            // Mostrar resultado final
            if (operacionExitosa && mensajeExito != null) {
                mostrarAlerta(mensajeExito);
            } else if (mensajeError != null) {
                mostrarAlerta(mensajeError);
            }
        }
    }


    // -----------------------------------------------------------------
    // Recarga manual
    // -----------------------------------------------------------------

    /**
     * Fuerza la recarga de la lista de motos desde el servicio.
     */
    @FXML
    private void onActualizarLista() {
        loadMotos();
    }

    // -----------------------------------------------------------------
    // Utilidades de alertas
    // -----------------------------------------------------------------

    /**
     * Muestra un diálogo de error con título y contenido.
     */
    private void showError(String headerText, String contentText) {
        AlertUtils.mostrarErrorConTitulo(headerText,contentText);
    }

    /**
     * Muestra un diálogo informativo con un solo texto.
     */
    private void mostrarAlerta(String mensaje) {
        AlertUtils.mostrarInfo(mensaje);
    }


}