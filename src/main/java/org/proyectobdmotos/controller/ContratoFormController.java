package org.proyectobdmotos.controller;

import java.time.LocalDate;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import org.proyectobdmotos.dto.MotoDisponibleDTO;
import org.proyectobdmotos.models.*;
import org.proyectobdmotos.services.*;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.services.exceptions.ValidationException;

public class ContratoFormController {

    @FXML private TextField campoBuscarCliente;
    @FXML private ListView<Cliente> listaResultados;

    @FXML private ComboBox<MotoDisponibleDTO> comboMoto;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<FormaPago> comboPago;
    @FXML private Label labelPrecio;

    private final ContratoService contratoService;
    private final ClienteService clienteService;
    private final MotoService motoService;

    private Cliente clienteSeleccionado = null;

    public ContratoFormController(ContratoService contratoService,
                                  ClienteService clienteService,
                                  MotoService motoService) {
        this.contratoService = contratoService;
        this.clienteService = clienteService;
        this.motoService = motoService;
    }

    @FXML
    private void initialize() {
        configurarListaClientes();
        configurarComboMoto();
        configurarComboPago();

        // Placeholder y filtro para evitar errores en lista vacía
        listaResultados.setPlaceholder(new Label("Escriba para buscar clientes"));
        listaResultados.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (listaResultados.getItems().isEmpty()) {
                event.consume();
            }
        });

        // Búsqueda de clientes
        campoBuscarCliente.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.trim().isEmpty()) {
                listaResultados.getItems().clear();
            } else {
                List<Cliente> resultados = clienteService.buscarClientesPorTexto(newText.trim());
                listaResultados.getItems().setAll(resultados);
            }
        });

        listaResultados.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                clienteSeleccionado = newVal;
                campoBuscarCliente.setText(newVal.getNombreCliente() + " (" + newVal.getCiCliente() + ")");
                listaResultados.getItems().clear();
            }
        });

        // Fechas -> cargar motos disponibles
        dateInicio.valueProperty().addListener((obs, oldDate, newDate) -> cargarMotosSegunFechas());
        dateFin.valueProperty().addListener((obs, oldDate, newDate) -> cargarMotosSegunFechas());

        // Precio estimado
        comboMoto.valueProperty().addListener((obs, oldMoto, newMoto) -> actualizarPrecioEstimado());
        dateInicio.valueProperty().addListener((obs, oldDate, newDate) -> actualizarPrecioEstimado());
        dateFin.valueProperty().addListener((obs, oldDate, newDate) -> actualizarPrecioEstimado());
    }

    private void cargarMotosSegunFechas() {
        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();
        if (inicio != null && fin != null && !fin.isBefore(inicio)) {
            List<MotoDisponibleDTO> disponibles = motoService.listarMotosDisponiblesDetalle(inicio, fin);
            comboMoto.getItems().setAll(disponibles);
            comboMoto.setPromptText("Seleccione una moto");
        } else {
            comboMoto.getItems().clear();
            comboMoto.setPromptText("Primero seleccione las fechas");
        }
    }

    @FXML
    private void onGuardar() {
        MotoDisponibleDTO motoSeleccionada = comboMoto.getValue();
        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();
        FormaPago formaPago = comboPago.getValue();

        boolean datosValidos = true;

        if (clienteSeleccionado == null) {
            mostrarError("Debe buscar y seleccionar un cliente.");
            datosValidos = false;
        }

        if (motoSeleccionada == null || inicio == null || fin == null || formaPago == null) {
            mostrarError("Todos los campos obligatorios deben estar completos.");
            datosValidos = false;
        }

        if (datosValidos) {
            boolean fechasValidas = inicio.isBefore(fin) || inicio.isEqual(fin);
            if (!fechasValidas) {
                mostrarError("La fecha de inicio debe ser anterior o igual a la fecha fin.");
                datosValidos = false;
            }
        }

        if (datosValidos) {
            try {
                Contrato nuevoContrato = new Contrato(
                        0.0, 0.0,
                        clienteSeleccionado.getIdCliente(),
                        0, null,
                        fin, inicio,
                        formaPago,
                        motoSeleccionada.getIdMoto(),
                        false, 20.0, 40.0
                );

                contratoService.crearContrato(nuevoContrato);
                mostrarInfo("Contrato creado correctamente.");
                MainController.getInstance().onGoBack();
            } catch (ValidationException e) {
                mostrarError(e.getMessage());
            } catch (Exception e) {
                Logger.logError("Error al guardar contrato: " + e.getMessage());
                mostrarError("Error inesperado al guardar el contrato.");
            }
        }
    }

    @FXML
    private void onCancelar() {
        MainController.getInstance().onGoBack();
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private void configurarListaClientes() {
        listaResultados.setCellFactory(param -> new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null :
                        item.getNombreCliente() + " " + item.getPrimerApellido() + " (" + item.getCiCliente() + ")");
            }
        });
    }

    private void configurarComboMoto() {
        // Mostramos Marca, Modelo y Color en el combo
        comboMoto.setCellFactory(param -> new ListCell<MotoDisponibleDTO>() {
            @Override
            protected void updateItem(MotoDisponibleDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null :
                        item.getMarca() + " " + item.getModelo() + " (" + item.getColor() + ")");
            }
        });
        comboMoto.setConverter(new StringConverter<MotoDisponibleDTO>() {
            @Override
            public String toString(MotoDisponibleDTO dto) {
                return (dto != null) ? dto.getMarca() + " " + dto.getModelo() + " (" + dto.getColor() + ")" : "";
            }
            @Override
            public MotoDisponibleDTO fromString(String string) { return null; }
        });
    }

    private void configurarComboPago() {
        comboPago.getItems().setAll(FormaPago.values());
        comboPago.setCellFactory(param -> new ListCell<FormaPago>() {
            @Override
            protected void updateItem(FormaPago item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        });
        comboPago.setConverter(new StringConverter<FormaPago>() {
            @Override
            public String toString(FormaPago fp) { return (fp != null) ? fp.name() : ""; }
            @Override
            public FormaPago fromString(String string) { return null; }
        });
        comboPago.getSelectionModel().selectFirst();
    }

    private void actualizarPrecioEstimado() {
        MotoDisponibleDTO motoSeleccionada = comboMoto.getValue();
        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();

        boolean puedeCalcular = motoSeleccionada != null && inicio != null && fin != null;
        double precio = 0.0;
        if (puedeCalcular) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fin) + 1;
            precio = dias * 20.0;
        }
        labelPrecio.setText(String.format("%.2f CUP", precio));
    }

    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje).showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        new Alert(Alert.AlertType.INFORMATION, mensaje).showAndWait();
    }
}