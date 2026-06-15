package org.proyectobdmotos.controller;

import java.time.LocalDate;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.proyectobdmotos.dto.MotoDisponibleDTO;
import org.proyectobdmotos.models.*;
import org.proyectobdmotos.services.*;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.utils.*;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.services.exceptions.ValidationException;

public class ContratoUsuarioFormController {

    @FXML private ComboBox<MotoDisponibleDTO> comboMoto;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<FormaPago> comboPago;
    @FXML private Label labelPrecio;
    @FXML private Label labelSinMotos;
    @FXML private CheckBox checkSeguroAdicional;

    private final ContratoService contratoService;
    private final MotoService motoService;
    private final AgenciaStore agenciaStore;

    public ContratoUsuarioFormController(ContratoService contratoService,
                                         MotoService motoService,
                                         AgenciaStore agenciaStore) {
        this.contratoService = contratoService;
        this.motoService = motoService;
        this.agenciaStore = agenciaStore;
    }

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    @FXML
    private void initialize() {
        configurarComboMoto();
        configurarComboPago();

        dateInicio.valueProperty().addListener((obs, oldDate, newDate) -> cargarMotosSegunFechas());
        dateFin.valueProperty().addListener((obs, oldDate, newDate) -> cargarMotosSegunFechas());

        comboMoto.valueProperty().addListener((obs, oldMoto, newMoto) -> actualizarPrecioEstimado());
        dateInicio.valueProperty().addListener((obs, oldDate, newDate) -> actualizarPrecioEstimado());
        dateFin.valueProperty().addListener((obs, oldDate, newDate) -> actualizarPrecioEstimado());
        checkSeguroAdicional.selectedProperty().addListener((obs, oldVal, newVal) -> actualizarPrecioEstimado());

        // Estado inicial
        comboMoto.setPromptText("Primero seleccione las fechas");
        labelSinMotos.setVisible(false);
        labelSinMotos.setManaged(false);
    }

    // -----------------------------------------------------------------
    // Carga de motos según fechas
    // -----------------------------------------------------------------

    private void cargarMotosSegunFechas() {
        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();
        labelSinMotos.setVisible(false);
        labelSinMotos.setManaged(false);

        boolean fechasValidas = inicio != null && fin != null && !fin.isBefore(inicio);
        if (fechasValidas) {
            List<MotoDisponibleDTO> disponibles = null;
            boolean cargado = false;
            String errorMsg = null;
            try {
                disponibles = motoService.listarMotosDisponiblesDetalle(inicio, fin);
                cargado = true;
            } catch (Exception e) {
                errorMsg = e.getMessage();
                Logger.logError("Error al cargar motos disponibles: " + errorMsg);
                e.printStackTrace();
            }
            if (cargado) {
                comboMoto.getItems().setAll(disponibles);
                comboMoto.getSelectionModel().clearSelection();
                boolean hayMotos = disponibles != null && !disponibles.isEmpty();
                if (hayMotos) {
                    comboMoto.setPromptText("Seleccione una moto");
                } else {
                    comboMoto.setPromptText("No hay motos disponibles");
                    labelSinMotos.setVisible(true);
                    labelSinMotos.setManaged(true);
                }
            } else {
                mostrarError("Error al cargar las motos disponibles. Revise el registro.");
            }
        } else {
            comboMoto.getItems().clear();
            comboMoto.setPromptText("Primero seleccione las fechas");
        }
    }

    // -----------------------------------------------------------------
    // Acción Guardar
    // -----------------------------------------------------------------

    @FXML
    private void onGuardar() {
        boolean puedeGuardar = validarFormulario();
        if (puedeGuardar) {
            crearContrato();
        }
    }

    private boolean validarFormulario() {
        MotoDisponibleDTO motoSeleccionada = comboMoto.getValue();
        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();
        FormaPago formaPago = comboPago.getValue();

        boolean camposCompletos = motoSeleccionada != null && inicio != null && fin != null && formaPago != null;
        boolean fechaPasada = inicio != null && inicio.isBefore(LocalDate.now());
        boolean fechasInvertidas = inicio != null && fin != null && inicio.isAfter(fin);
        boolean todoOk = false;

        if (!camposCompletos) {
            mostrarError("Todos los campos obligatorios deben estar completos.");
        }
        if (fechaPasada) {
            mostrarError("La fecha de inicio no puede ser anterior a hoy.");
        }
        if (fechasInvertidas) {
            mostrarError("La fecha de inicio debe ser anterior o igual a la fecha fin.");
        }

        if (camposCompletos && !fechaPasada && !fechasInvertidas) {
            todoOk = true;
        }
        return todoOk;
    }

    private void crearContrato() {
        Cliente cliente = agenciaStore.getClienteActual();
        boolean clienteExiste = cliente != null;
        if (clienteExiste) {
            try {
                Contrato nuevoContrato = new Contrato(
                        0.0, 0.0,
                        cliente.getIdCliente(),
                        0, null,
                        dateFin.getValue(), dateInicio.getValue(),
                        comboPago.getValue(),
                        comboMoto.getValue().getIdMoto(),
                        checkSeguroAdicional.isSelected(),
                        20.0, 40.0
                );
                contratoService.crearContrato(nuevoContrato);
                mostrarInfo("Contrato creado correctamente.");
                UserMainController.getInstance().onGoBack();
            } catch (ValidationException e) {
                e.printStackTrace();
                mostrarError(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                Logger.logError("Error al guardar contrato: " + e.getMessage());
                mostrarError("Error inesperado al guardar el contrato.");
            }
        } else {
            mostrarError("No se ha identificado al cliente.");
        }
    }

    // -----------------------------------------------------------------
    // Cancelar
    // -----------------------------------------------------------------

    @FXML
    private void onCancelar() {
        UserMainController.getInstance().onGoBack();
    }

    // -----------------------------------------------------------------
    // Configuración de componentes visuales
    // -----------------------------------------------------------------

    private void configurarComboMoto() {
        comboMoto.setCellFactory(param -> new ListCell<MotoDisponibleDTO>() {
            @Override
            protected void updateItem(MotoDisponibleDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getMarca() + " " + item.getModelo() + " (" + item.getColor() + ")");
                }
            }
        });
        comboMoto.setConverter(new StringConverter<MotoDisponibleDTO>() {
            @Override
            public String toString(MotoDisponibleDTO dto) {
                if (dto == null) return "";
                return dto.getMarca() + " " + dto.getModelo() + " (" + dto.getColor() + ")";
            }
            @Override
            public MotoDisponibleDTO fromString(String string) {
                return null;
            }
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

    // -----------------------------------------------------------------
    // Cálculo del precio estimado
    // -----------------------------------------------------------------

    private void actualizarPrecioEstimado() {
        MotoDisponibleDTO motoSeleccionada = comboMoto.getValue();
        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();

        boolean puedeCalcular = motoSeleccionada != null && inicio != null && fin != null;
        double precio = 0.0;
        if (puedeCalcular) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fin) + 1;
            double tarifa = 20.0;
            if (checkSeguroAdicional.isSelected()) {
                tarifa = tarifa * 2;
            }
            precio = dias * tarifa;
        }
        labelPrecio.setText(String.format("%.2f CUP", precio));
    }

    // -----------------------------------------------------------------
    // Alertas
    // -----------------------------------------------------------------

    private void mostrarError(String mensaje) {
        AlertUtils.mostrarError(mensaje);
    }

    private void mostrarInfo(String mensaje) {
        AlertUtils.mostrarInfo(mensaje);
    }
}