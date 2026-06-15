package org.proyectobdmotos.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.services.ContratoService;
import org.proyectobdmotos.services.exceptions.BusinessException;
import org.proyectobdmotos.services.exceptions.ValidationException;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.utils.*;
import org.proyectobdmotos.utils.Logger;

public class ContratoController {

    private final ContratoService contratoService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;

    @FXML private TableView<Contrato> tablaContratos;
    @FXML private TableColumn<Contrato, Integer> colId;
    @FXML private TableColumn<Contrato, String> colCiCliente;
    @FXML private TableColumn<Contrato, String> colNombreCliente;
    @FXML private TableColumn<Contrato, String> colMatriculaMoto;
    @FXML private TableColumn<Contrato, String> colMarcaMoto;
    @FXML private TableColumn<Contrato, String> colModeloMoto;
    @FXML private TableColumn<Contrato, Double> colKmSalida;
    @FXML private TableColumn<Contrato, Double> colKmLlegada;
    @FXML private TableColumn<Contrato, LocalDate> colFechaInicio;
    @FXML private TableColumn<Contrato, LocalDate> colFechaFin;
    @FXML private TableColumn<Contrato, String> colEstado;
    @FXML private TableColumn<Contrato, String> colImporte;

    public ContratoController(ContratoService contratoService, AgenciaStore agenciaStore, ReferenceDataStore referenceDataStore) {
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
        fijarColumnas(tablaContratos);
    }

    private void configureTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idContrato"));
        colCiCliente.setCellValueFactory(new PropertyValueFactory<>("ciCliente"));
        colNombreCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCompletoCliente"));
        colMatriculaMoto.setCellValueFactory(new PropertyValueFactory<>("matriculaMoto"));
        colMarcaMoto.setCellValueFactory(new PropertyValueFactory<>("marcaMoto"));
        colModeloMoto.setCellValueFactory(new PropertyValueFactory<>("modeloMoto"));
        colKmSalida.setCellValueFactory(new PropertyValueFactory<>("cantKmSalida"));
        colKmLlegada.setCellValueFactory(new PropertyValueFactory<>("cantKmLlegada"));
        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFechaFin.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFechaEntrega() != null ? "Finalizado" : "Activo"));
        colImporte.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f CUP", cellData.getValue().calcularImporteTotalTeorico())));
    }

    private void bindStore() {
        tablaContratos.setItems(agenciaStore.getContratos());
    }

    private void loadContratos() {
        Task<List<Contrato>> loadTask = crearTareaCargaContratos();
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

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

    private void manejarCargaExitosa(List<Contrato> contratos) {
        if (contratos != null) {
            agenciaStore.setContratos(contratos);
            ajustarColumnas(tablaContratos, colId, colCiCliente, colNombreCliente, colMatriculaMoto,
                    colMarcaMoto, colModeloMoto, colKmSalida, colKmLlegada,
                    colFechaInicio, colFechaFin, colEstado, colImporte);
            Logger.logInfo("Contratos cargados: " + contratos.size());
        }
    }

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

    private boolean confirmarEliminacion(Contrato contrato) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar el contrato #" + contrato.getIdContrato() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

    private void ejecutarEliminacion(Contrato contrato) {
        try {
            contratoService.eliminarContrato(contrato.getIdContrato());
            loadContratos();
            mostrarAlerta("Contrato eliminado correctamente.");
        } catch (ValidationException e) {
            e.printStackTrace();
            mostrarAlerta("Error al eliminar: " + e.getMessage());
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
        } else if (contrato.getFechaEntrega() != null) {
            mostrarAlerta("Este contrato ya está finalizado.");
        } else {
            mostrarDialogoFinalizacion(contrato);
        }
    }

    private void mostrarDialogoFinalizacion(Contrato contrato) {
        Stage dialogo = crearVentanaDialogo(contrato);
        VBox contenido = construirContenidoDialogo(contrato);
        Scene scene = new Scene(contenido);
        dialogo.setScene(scene);
        dialogo.showAndWait();
    }

    private Stage crearVentanaDialogo(Contrato contrato) {
        Stage dialogo = new Stage();
        dialogo.initModality(Modality.APPLICATION_MODAL);
        dialogo.initOwner(tablaContratos.getScene().getWindow());
        dialogo.initStyle(StageStyle.UTILITY);
        dialogo.setResizable(false);
        dialogo.setTitle("Finalizar Contrato #" + contrato.getIdContrato());
        return dialogo;
    }

    private VBox construirContenidoDialogo(Contrato contrato) {
        Label labelId = new Label("Contrato #" + contrato.getIdContrato());
        Label labelMoto = new Label("Moto: " + contrato.getMatriculaMoto() + " " + contrato.getMarcaMoto() + " " + contrato.getModeloMoto());
        Label labelFechas = new Label("Inicio: " + contrato.getFechaInicio() + " | Fin: " + contrato.getFechaFin());

        DatePicker dpFechaEntrega = new DatePicker();
        dpFechaEntrega.setEditable(false);
        TextField tfKmLlegada = new TextField();
        tfKmLlegada.setPromptText("Kilómetros de llegada");

        Label labelDiasBase = new Label("Días base: --");
        Label labelDiasProrroga = new Label("Días prórroga: --");
        Label labelImporteBase = new Label("Importe base: --");
        Label labelRecargoProrroga = new Label("Recargo prórroga: --");
        Label labelTotal = new Label("Total: --");

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
            } catch (NumberFormatException ignored) {}
        }
    }

    private Contrato construirCopiaContrato(Contrato original, LocalDate fechaEntrega, double kmLlegada) {
        return new Contrato(
                kmLlegada, original.getCantKmSalida(), original.getIdCliente(),
                original.getDiasProrroga(), fechaEntrega, original.getFechaFin(),
                original.getFechaInicio(), original.getFormaPago(), original.getIdMoto(),
                original.isSeguroAdicional(), original.getTarifaNormal(), original.getTarifaProrroga());
    }

    private void procesarAceptacion(Contrato contrato, LocalDate fechaEntrega, String kmTexto) {
        String mensajeError = validarDatos(contrato, fechaEntrega, kmTexto);
        if (mensajeError == null) {
            double kmLlegada = Double.parseDouble(kmTexto);
            ejecutarFinalizacion(contrato, fechaEntrega, kmLlegada);
        } else {
            mostrarAlerta(mensajeError);
        }
    }

    private String validarDatos(Contrato contrato, LocalDate fechaEntrega, String kmTexto) {
        if (fechaEntrega == null || kmTexto.isEmpty()) return "Complete todos los campos.";
        if (contrato.getFechaInicio() != null && fechaEntrega.isBefore(contrato.getFechaInicio()))
            return "La fecha de entrega no puede ser anterior a la fecha de inicio.";
        if (contratoService.tieneContratoAnteriorActivo(contrato.getIdMoto(), contrato.getIdContrato()))
            return "Existe un contrato anterior activo para la misma moto.";
        try {
            double kmLlegada = Double.parseDouble(kmTexto);
            if (kmLlegada < contrato.getCantKmSalida())
                return "Los kilómetros de llegada no pueden ser menores que los de salida.";
        } catch (NumberFormatException e) {
            return "Kilómetros inválidos.";
        }
        return null;
    }

    private void ejecutarFinalizacion(Contrato contrato, LocalDate fechaEntrega, double kmLlegada) {
        try {
            contrato.setFechaEntrega(fechaEntrega);
            contrato.setCantKmLlegada(kmLlegada);
            contratoService.finalizarContrato(contrato);
            double total = contrato.calcularImporteTotalTeorico();
            new Alert(Alert.AlertType.INFORMATION, "Contrato finalizado.\nImporte total: " + String.format("%.2f CUP", total)).showAndWait();
            loadContratos();
            cerrarDialogoSeguro();
        } catch (ValidationException ex) {
            ex.printStackTrace();
            mostrarAlerta("Error de negocio: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.logError("Error al finalizar contrato: " + ex.getMessage());
            mostrarAlerta("Error inesperado al finalizar el contrato.");
        }
    }

    private void cerrarDialogoSeguro() {
        try { cerrarDialogoDeFinalizacion(); } catch (Exception ignored) {}
    }

    private void cerrarDialogoDeFinalizacion() {
        Window owner = tablaContratos.getScene().getWindow();
        for (Window w : Stage.getWindows()) {
            if (w instanceof Stage stage && stage.getOwner() == owner && stage.isShowing()) {
                stage.close();
                break;
            }
        }
    }

    private void cerrarVentanaDelBoton(Button boton) {
        ((Stage) boton.getScene().getWindow()).close();
    }

    @FXML
    private void onCrearContrato() {
        MainController.getInstance().cargarVista("/fxml/contrato-form-view.fxml", "Nuevo Contrato");
    }

    private void showError(String headerText, String contentText) {
        AlertUtils.mostrarErrorTitulo(headerText, contentText);
    }

    private void mostrarAlerta(String mensaje) {
        AlertUtils.mostrarInfo(mensaje);
    }

    // ---------------------------
    // Autoajuste
    // ---------------------------
    private double medirAnchoTexto(String texto, boolean bold) {
        Font font = bold ? Font.font("System", FontWeight.BOLD, 14) : Font.font("System", 14);
        Text text = new Text(texto);
        text.setFont(font);
        return text.getLayoutBounds().getWidth() + 25;
    }

    @SafeVarargs
    private void ajustarColumnas(TableView<?> tabla, TableColumn<?, ?>... columnas) {
        for (TableColumn<?, ?> col : columnas) {
            double max = medirAnchoTexto(col.getText(), true);
            for (Object item : tabla.getItems()) {
                Object valor = null;
                try {
                    valor = ((TableColumn) col).getCellData(item);
                } catch (Exception ignored) {
                    try {
                        javafx.beans.value.ObservableValue<?> obs = ((TableColumn) col).getCellObservableValue(item);
                        if (obs != null) valor = obs.getValue();
                    } catch (Exception ignored2) {}
                }
                if (valor != null) {
                    double w = medirAnchoTexto(valor.toString(), false);
                    if (w > max) max = w;
                }
            }
            col.setPrefWidth(max);
            col.setMinWidth(max);
            col.setMaxWidth(max);
        }
        Platform.runLater(() -> {
            double total = 0;
            for (TableColumn<?, ?> c : tabla.getColumns()) total += c.getPrefWidth();
            tabla.setPrefWidth(total + 10);
            tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        });
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