package org.proyectobdmotos.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javafx.stage.Window;
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

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    /**
     * Configura la tabla, la enlaza con el store y lanza la carga
     * asíncrona de contratos al abrir la pantalla.
     */
    @FXML
    private void initialize() {
        Logger.log("Inicializando ContratoController...");
        configureTableColumns();
        bindStore();
        loadContratos();
    }

    // -----------------------------------------------------------------
    // Configuración de columnas
    // -----------------------------------------------------------------

    /**
     * Define cómo se muestra cada columna de la tabla de contratos.
     * Extrae los valores necesarios de cada objeto Contrato.
     */
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

    // -----------------------------------------------------------------
    // Vinculación con el store
    // -----------------------------------------------------------------

    /**
     * Enlaza la tabla con la lista observable de contratos del store.
     * Los cambios en el store se reflejarán automáticamente.
     */
    private void bindStore() {
        tablaContratos.setItems(agenciaStore.getContratos());
    }

    // -----------------------------------------------------------------
    // Carga asíncrona de contratos
    // -----------------------------------------------------------------

    /**
     * Inicia la carga en segundo plano de todos los contratos.
     * Muestra los datos cuando la tarea termina exitosamente.
     */
    private void loadContratos() {
        Task<List<Contrato>> loadTask = crearTareaCargaContratos();
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    /**
     * Construye la tarea que obtiene los contratos del servicio.
     * Define el manejo de éxito y fallo al finalizar.
     */
    private Task<List<Contrato>> crearTareaCargaContratos() {
        Task<List<Contrato>> loadTask = new Task<>() {
            @Override
            protected List<Contrato> call() {
                return contratoService.listarTodos();
            }
        };

        loadTask.setOnSucceeded(event -> manejarCargaExitosa(loadTask.getValue()));
        loadTask.setOnFailed(event -> manejarCargaFallida(loadTask.getException()));

        return loadTask;
    }

    /**
     * Procesa la lista de contratos obtenida y la coloca en el store.
     */
    private void manejarCargaExitosa(List<Contrato> contratos) {
        if (contratos != null) {
            agenciaStore.setContratos(contratos);
            Logger.logInfo("Contratos cargados: " + contratos.size());
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
            Logger.logError("Error de negocio cargando contratos: " + message);
            showError("No se pudieron cargar los contratos", message);
        } else {
            Logger.logError("Error inesperado cargando contratos: " + message);
            showError("Error cargando contratos", message);
        }
    }

    // -----------------------------------------------------------------
    // Eliminación de contrato
    // -----------------------------------------------------------------

    /**
     * Maneja la acción de eliminar un contrato seleccionado.
     * Verifica la selección, pide confirmación y ejecuta el borrado.
     */
    @FXML
    private void onEliminarContrato() {
        Contrato contratoSeleccionado = tablaContratos.getSelectionModel().getSelectedItem();
        if (contratoSeleccionado == null) {
            mostrarAlerta("Seleccione un contrato de la tabla para eliminar.");
        } else {
            boolean confirmado = confirmarEliminacion(contratoSeleccionado);
            if (confirmado) {
                ejecutarEliminacion(contratoSeleccionado);
            }
        }
    }

    /**
     * Muestra un diálogo de confirmación y devuelve true si se acepta.
     */
    private boolean confirmarEliminacion(Contrato contrato) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar el contrato #" + contrato.getIdContrato() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

    /**
     * Intenta eliminar el contrato usando el servicio.
     * Si la operación falla, muestra un mensaje; si tiene éxito, recarga la tabla.
     */
    private void ejecutarEliminacion(Contrato contrato) {
        try {
            contratoService.eliminarContrato(contrato.getIdContrato());
            loadContratos();
            mostrarAlerta("Contrato eliminado correctamente.");
        } catch (ValidationException e) {
            mostrarAlerta("Error al eliminar: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------
    // Recarga manual
    // -----------------------------------------------------------------

    /**
     * Fuerza la recarga de la lista de contratos desde el servicio.
     */
    @FXML
    private void onActualizarLista() {
        loadContratos();
    }

    // -----------------------------------------------------------------
    // Finalización de contrato
    // -----------------------------------------------------------------

    /**
     * Abre un diálogo modal para finalizar un contrato seleccionado.
     * Solo procede si el contrato no ha sido finalizado antes.
     */
    @FXML
    private void onFinalizarContrato() {
        Contrato contrato = tablaContratos.getSelectionModel().getSelectedItem();

        if (contrato == null) {
            mostrarAlerta("Seleccione un contrato de la tabla para finalizar.");
        } else if (contrato.getFechaEntrega() != null) {
            mostrarAlerta("Este contrato ya está finalizado.");
        } else {
            mostrarDialogoFinalizacion(contrato);
        }
    }

    /**
     * Crea y muestra el diálogo de finalización con todos sus componentes.
     */
    private void mostrarDialogoFinalizacion(Contrato contrato) {
        Stage dialogo = crearVentanaDialogo(contrato);
        VBox contenido = construirContenidoDialogo(contrato);
        Scene scene = new Scene(contenido);
        dialogo.setScene(scene);
        dialogo.showAndWait();
    }

    /**
     * Configura la ventana modal para el diálogo de finalización.
     */
    private Stage crearVentanaDialogo(Contrato contrato) {
        Stage dialogo = new Stage();
        dialogo.initModality(Modality.APPLICATION_MODAL);
        dialogo.initOwner(tablaContratos.getScene().getWindow());
        dialogo.initStyle(StageStyle.UTILITY);
        dialogo.setResizable(false);
        dialogo.setTitle("Finalizar Contrato #" + contrato.getIdContrato());
        return dialogo;
    }

    /**
     * Construye el contenido visual del diálogo: etiquetas, campos y botones.
     */
    private VBox construirContenidoDialogo(Contrato contrato) {
        Label labelId = new Label("Contrato #" + contrato.getIdContrato());
        Label labelMoto = new Label("Moto: " + contrato.getIdMoto());
        Label labelFechas = new Label("Inicio: " + contrato.getFechaInicio() + " | Fin: " + contrato.getFechaFin());

        DatePicker dpFechaEntrega = new DatePicker();
        TextField tfKmLlegada = new TextField();
        tfKmLlegada.setPromptText("Kilómetros de llegada");

        Label labelDiasBase = new Label("Días base: --");
        Label labelDiasProrroga = new Label("Días prórroga: --");
        Label labelImporteBase = new Label("Importe base: --");
        Label labelRecargoProrroga = new Label("Recargo prórroga: --");
        Label labelTotal = new Label("Total: --");

        // Vincula los cambios en los campos con el cálculo
        Runnable actualizar = () -> actualizarCalculo(contrato, dpFechaEntrega.getValue(), tfKmLlegada.getText().trim(),
                labelDiasBase, labelDiasProrroga, labelImporteBase, labelRecargoProrroga, labelTotal);
        dpFechaEntrega.valueProperty().addListener((obs, oldVal, newVal) -> actualizar.run());
        tfKmLlegada.textProperty().addListener((obs, oldVal, newVal) -> actualizar.run());

        Button btnAceptar = new Button("Aceptar");
        Button btnCancelar = new Button("Cancelar");

        btnAceptar.setOnAction(e -> procesarAceptacion(contrato, dpFechaEntrega.getValue(), tfKmLlegada.getText().trim()));
        btnCancelar.setOnAction(e -> cerrarVentanaDelBoton(btnCancelar));

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(
                labelId, labelMoto, labelFechas,
                new Label("Fecha de entrega:"), dpFechaEntrega,
                new Label("Km de llegada:"), tfKmLlegada,
                labelDiasBase, labelDiasProrroga, labelImporteBase, labelRecargoProrroga, labelTotal,
                new HBox(10, btnAceptar, btnCancelar)
        );
        return root;
    }

    /**
     * Actualiza las etiquetas de desglose según los valores ingresados.
     * Si los datos no son válidos, deja las etiquetas sin modificar.
     */
    private void actualizarCalculo(Contrato contrato, LocalDate fechaEntrega, String kmTexto,
                                   Label labelDiasBase, Label labelDiasProrroga,
                                   Label labelImporteBase, Label labelRecargoProrroga, Label labelTotal) {
        if (fechaEntrega != null && !kmTexto.isEmpty()) {
            try {
                double kmLlegada = Double.parseDouble(kmTexto);
                Contrato copia = construirCopiaContrato(contrato, fechaEntrega, kmLlegada);
                copia.setDiasProrroga(copia.calcularDiasProrrogaReal());
                double base = copia.calcularImporteBase();
                double recargo = copia.calcularRecargoProrroga();
                double total = base + recargo;

                labelDiasBase.setText("Días base: " + copia.calcularDiasPactados());
                labelDiasProrroga.setText("Días prórroga: " + copia.calcularDiasProrrogaReal());
                labelImporteBase.setText("Importe base: " + String.format("%.2f CUP", base));
                labelRecargoProrroga.setText("Recargo prórroga: " + String.format("%.2f CUP", recargo));
                labelTotal.setText("Total: " + String.format("%.2f CUP", total));
            } catch (NumberFormatException ignored) {
                // No actualiza las etiquetas si el formato del número es inválido.
            }
        }
    }

    /**
     * Crea una copia del contrato con la fecha de entrega y km de llegada
     * especificados, necesaria para los cálculos.
     */
    private Contrato construirCopiaContrato(Contrato original, LocalDate fechaEntrega, double kmLlegada) {
        return new Contrato(
                kmLlegada,
                original.getCantKmSalida(),
                original.getIdCliente(),
                original.getDiasProrroga(),
                fechaEntrega,
                original.getFechaFin(),
                original.getFechaInicio(),
                original.getFormaPago(),
                original.getIdMoto(),
                original.isSeguroAdicional(),
                original.getTarifaNormal(),
                original.getTarifaProrroga()
        );
    }

    /**
     * Procesa la aceptación del diálogo: valida campos, finaliza el contrato
     * y cierra la ventana. Si hay errores, muestra mensajes.
     */
    private void procesarAceptacion(Contrato contrato, LocalDate fechaEntrega, String kmTexto) {
        if (fechaEntrega == null || kmTexto.isEmpty()) {
            mostrarAlerta("Complete todos los campos.");
        } else {
            try {
                double kmLlegada = Double.parseDouble(kmTexto);
                contrato.setFechaEntrega(fechaEntrega);
                contrato.setCantKmLlegada(kmLlegada);

                contratoService.finalizarContrato(contrato);

                double total = contrato.calcularImporteTotalTeorico();
                new Alert(Alert.AlertType.INFORMATION,
                        "Contrato finalizado.\nImporte total: " + String.format("%.2f CUP", total)).showAndWait();

                loadContratos();
                cerrarDialogoDeFinalizacion();
            } catch (NumberFormatException ex) {
                mostrarAlerta("Kilómetros inválidos. Debe ser un número.");
            } catch (ValidationException ex) {
                mostrarAlerta("Error de negocio: " + ex.getMessage());
            } catch (Exception ex) {
                Logger.logError("Error al finalizar contrato: " + ex.getMessage());
                mostrarAlerta("Error inesperado al finalizar el contrato.");
            }
        }
    }

    /**
     * Cierra el diálogo modal de finalización.
     * Busca la ventana hija del owner que esté visible y la cierra.
     */
    private void cerrarDialogoDeFinalizacion() {
        Window owner = tablaContratos.getScene().getWindow();
        boolean cerrado = false;
        for (Window w : Stage.getWindows()) {
            if (!cerrado && w instanceof Stage) {
                Stage stage = (Stage) w;
                if (stage.getOwner() == owner && stage.isShowing()) {
                    stage.close();
                    cerrado = true;
                }
            }
        }
    }

    /**
     * Cierra la ventana que contiene al botón dado.
     */
    private void cerrarVentanaDelBoton(Button boton) {
        Stage stage = (Stage) boton.getScene().getWindow();
        stage.close();
    }

    // -----------------------------------------------------------------
    // Navegación
    // -----------------------------------------------------------------

    /**
     * Abre el formulario de creación de un nuevo contrato.
     */
    @FXML
    private void onCrearContrato() {
        MainController.getInstance().cargarVista("/fxml/contrato-form-view.fxml", "Nuevo Contrato");
    }

    // -----------------------------------------------------------------
    // Utilidades de alertas
    // -----------------------------------------------------------------

    /**
     * Muestra un diálogo de error con título y contenido.
     */
    private void showError(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo informativo con un solo texto.
     */
    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}