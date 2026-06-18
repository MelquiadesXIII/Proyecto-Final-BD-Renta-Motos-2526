package org.proyectobdmotos.controller;

import java.time.LocalDate;
import java.util.HashMap;
import javafx.application.Platform;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

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
    @FXML private Button btnCancelarContrato;

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
        fijarColumnas(tablaMotos);
        fijarColumnas(tablaContratos);
        cargarMotos();
        cargarContratos();
        btnCancelarContrato.setDisable(true);
        tablaContratos.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            boolean sinSeleccion = newVal == null;
            boolean yaFinalizado = newVal != null && !"Sin entregar".equals(newVal.getFechaEntrega());
            btnCancelarContrato.setDisable(sinSeleccion || yaFinalizado);
        });
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
        try {
            List<Moto> motos = motoService.listarTodos();
            tablaMotos.getItems().setAll(motos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarContratos() {
        if (agenciaStore.getClienteActual() == null) return;
        try {
            int idCliente = agenciaStore.getClienteActual().getIdCliente();
            List<MisContratosDTO> contratos = contratoService.listarMisContratos(idCliente);
            tablaContratos.getItems().setAll(contratos);
            boolean sinContratos = contratos.isEmpty();
            labelSinContratos.setVisible(sinContratos);
            labelSinContratos.setManaged(sinContratos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------------
    // Acciones del usuario
    // -----------------------------------------------------------------

    @FXML
    private void onCrearNuevoContrato() {
        UserMainController.getInstance().cargarVista("/fxml/contrato-usuario-form.fxml", "Nuevo Contrato");
    }

    @FXML
    private void onCancelarContrato() {
        MisContratosDTO seleccionado = tablaContratos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        LocalDate fechaInicioContrato = LocalDate.parse(seleccionado.getFechaInicio());

        if (fechaInicioContrato.isAfter(LocalDate.now())) {
            // Contrato que aún no ha comenzado: cancelar sin necesidad de KM ni fecha de entrega
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Cancelar contrato");
            confirmacion.setHeaderText("¿Cancelar el contrato con " + seleccionado.getMotoInfo() + "?");
            confirmacion.setContentText("El contrato no ha comenzado (inicio: "
                    + seleccionado.getFechaInicio() + "). Se eliminará y la moto quedará disponible.");
            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                try {
                    contratoService.eliminarContrato(seleccionado.getIdContrato());
                    cargarContratos();
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarAlerta("Error al cancelar el contrato: " + e.getMessage());
                }
            }
        } else {
            // Contrato ya iniciado: devolución anticipada (requiere KM y fecha)
            Optional<DatosFinalizacion> datos = mostrarDialogoDevolucion(fechaInicioContrato);
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

    private Optional<DatosFinalizacion> mostrarDialogoDevolucion(LocalDate fechaInicioContrato) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Devolución anticipada");
        dialog.setHeaderText("Ingrese los datos de devolución:");

        TextField kmField = new TextField();
        kmField.setPromptText("Kilometraje actual");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isAfter(LocalDate.now()) || date.isBefore(fechaInicioContrato)) {
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
            boolean fechaNoFutura = !fechaEntrega.isAfter(LocalDate.now());
            boolean fechaNoAntesDeSalida = !fechaEntrega.isBefore(contrato.getFechaInicio());
            if (!kmValido) {
                mostrarAlerta("Los kilómetros de llegada no pueden ser menores que los de salida ("
                        + contrato.getCantKmSalida() + " km).");
            } else if (!fechaNoFutura) {
                mostrarAlerta("La fecha de entrega no puede ser posterior a hoy.");
            } else if (!fechaNoAntesDeSalida) {
                mostrarAlerta("La fecha de entrega no puede ser anterior a la fecha de inicio del contrato ("
                        + contrato.getFechaInicio() + ").");
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
        for (TableColumn<?, ?> columna : tabla.getColumns()) {
            columna.setReorderable(false);
        }
        tabla.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                Platform.runLater(() -> tabla.getColumns().forEach(c -> c.setResizable(false)));
            }
        });
    }
}