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
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.services.exceptions.ValidationException;

public class ContratoUsuarioFormController {

    @FXML private ComboBox<MotoDisponibleDTO> comboMoto;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<FormaPago> comboPago;
    @FXML private Label labelPrecio;
    @FXML private Label labelSinMotos;

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

    @FXML
    private void initialize() {
        configurarComboMoto();
        configurarComboPago();

        dateInicio.valueProperty().addListener((obs, oldDate, newDate) -> cargarMotosSegunFechas());
        dateFin.valueProperty().addListener((obs, oldDate, newDate) -> cargarMotosSegunFechas());

        comboMoto.valueProperty().addListener((obs, oldMoto, newMoto) -> actualizarPrecioEstimado());
        dateInicio.valueProperty().addListener((obs, oldDate, newDate) -> actualizarPrecioEstimado());
        dateFin.valueProperty().addListener((obs, oldDate, newDate) -> actualizarPrecioEstimado());
    }

    private void cargarMotosSegunFechas() {
        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();
        labelSinMotos.setVisible(false);
        labelSinMotos.setManaged(false);

        if (inicio != null && fin != null && !fin.isBefore(inicio)) {
            List<MotoDisponibleDTO> disponibles = motoService.listarMotosDisponiblesDetalle(inicio, fin);
            comboMoto.getItems().setAll(disponibles);
            if (disponibles.isEmpty()) {
                comboMoto.setPromptText("No hay motos disponibles");
                labelSinMotos.setVisible(true);
                labelSinMotos.setManaged(true);
            } else {
                comboMoto.setPromptText("Seleccione una moto");
            }
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

        if (motoSeleccionada == null || inicio == null || fin == null || formaPago == null) {
            mostrarError("Todos los campos obligatorios deben estar completos.");
        } else if (inicio.isAfter(fin)) {
            mostrarError("La fecha de inicio debe ser anterior o igual a la fecha fin.");
        } else {
            Cliente cliente = agenciaStore.getClienteActual();
            if (cliente == null) {
                mostrarError("No se ha identificado al cliente.");
            } else {
                try {
                    Contrato nuevoContrato = new Contrato(
                            0.0, 0.0,
                            cliente.getIdCliente(),
                            0, null,
                            fin, inicio,
                            formaPago,
                            motoSeleccionada.getIdMoto(),
                            false, 20.0, 40.0
                    );
                    contratoService.crearContrato(nuevoContrato);
                    mostrarInfo("Contrato creado correctamente.");
                    UserMainController.getInstance().onGoBack();
                } catch (ValidationException e) {
                    mostrarError(e.getMessage());
                } catch (Exception e) {
                    Logger.logError("Error al guardar contrato: " + e.getMessage());
                    mostrarError("Error inesperado al guardar el contrato.");
                }
            }
        }
    }

    @FXML
    private void onCancelar() {
        UserMainController.getInstance().onGoBack();
    }

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

    private void actualizarPrecioEstimado() {
        MotoDisponibleDTO motoSeleccionada = comboMoto.getValue();
        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();

        if (motoSeleccionada != null && inicio != null && fin != null) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fin) + 1;
            labelPrecio.setText(String.format("%.2f CUP", dias * 20.0));
        } else {
            labelPrecio.setText("0.00 CUP");
        }
    }

    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje).showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        new Alert(Alert.AlertType.INFORMATION, mensaje).showAndWait();
    }
}