package org.proyectobdmotos.controller;

import java.time.LocalDate;
import java.util.List;

import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.models.FormaPago;
import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.services.ClienteService;
import org.proyectobdmotos.services.ContratoService;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.services.exceptions.ValidationException;
import org.proyectobdmotos.utils.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

public class ContratoFormController {

    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private ComboBox<Moto> comboMoto;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<FormaPago> comboPago;   
    @FXML private Label labelPrecio;

    private final ContratoService contratoService;
    private final ClienteService clienteService;
    private final MotoService motoService;

    public ContratoFormController(ContratoService contratoService,
                                  ClienteService clienteService,
                                  MotoService motoService) {
        this.contratoService = contratoService;
        this.clienteService = clienteService;
        this.motoService = motoService;
    }

    @FXML
    private void initialize() {
        cargarClientes();
        cargarMotosDisponibles();
        configurarComboCliente();
        configurarComboMoto();
        configurarComboPago();

        dateInicio.valueProperty().addListener((obs, oldDate, newDate) -> actualizarPrecioEstimado());
        dateFin.valueProperty().addListener((obs, oldDate, newDate) -> actualizarPrecioEstimado());
        comboMoto.valueProperty().addListener((obs, oldMoto, newMoto) -> actualizarPrecioEstimado());
    }

    @FXML
    private void onGuardar() {
        Cliente clienteSeleccionado = comboCliente.getValue();
        Moto motoSeleccionada = comboMoto.getValue();
        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();
        FormaPago formaPagoSeleccionada = comboPago.getValue();

        boolean datosValidos = true;

        if (clienteSeleccionado == null || motoSeleccionada == null ||
            inicio == null || fin == null || formaPagoSeleccionada == null) {
            new Alert(Alert.AlertType.ERROR,
                      "Todos los campos obligatorios deben estar completos.").showAndWait();
            datosValidos = false;
        }

        if (datosValidos) {
            boolean fechasValidas = inicio.isBefore(fin) || inicio.isEqual(fin);
            if (!fechasValidas) {
                new Alert(Alert.AlertType.ERROR,
                          "La fecha de inicio debe ser anterior o igual a la fecha fin.").showAndWait();
                datosValidos = false;
            }
        }

        if (datosValidos) {
            try {
                Contrato nuevoContrato = new Contrato(
                        0.0,                    
                        0.0,                    
                        clienteSeleccionado.getIdCliente(),
                        0,                      
                        null,                   
                        fin,
                        inicio,
                        formaPagoSeleccionada,
                        motoSeleccionada.getIdMoto(),
                        false,                  
                        0.0,                    
                        0.0                     
                );

                contratoService.crearContrato(nuevoContrato);

                new Alert(Alert.AlertType.INFORMATION,
                          "Contrato creado correctamente.").showAndWait();
                cerrarVentana();
            } catch (ValidationException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            } catch (Exception e) {
                Logger.logError("Error al guardar contrato: " + e.getMessage());
                new Alert(Alert.AlertType.ERROR,
                          "Error inesperado al guardar el contrato.").showAndWait();
            }
        }
    }

    @FXML
    private void onCancelar() {
        cerrarVentana();
    }

    // ======================== MÉTODOS PRIVADOS ========================

    private void cargarClientes() {
        List<Cliente> clientes = clienteService.listarTodos();
        comboCliente.getItems().setAll(clientes);
    }

    private void cargarMotosDisponibles() {
        List<Moto> todasLasMotos = motoService.listarTodos();
        comboMoto.getItems().clear();
        for (Moto m : todasLasMotos) {
            boolean disponible = motoService.estaDisponible(m.getMatriculaMoto());
            if (disponible) {
                comboMoto.getItems().add(m);
            }
        }
    }

    private void configurarComboCliente() {
        comboCliente.setCellFactory(param -> new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombreCliente() + " (" + item.getCiCliente() + ")");
                }
            }
        });
        comboCliente.setConverter(new StringConverter<Cliente>() {
            @Override
            public String toString(Cliente cliente) {
                return (cliente != null) ? cliente.getNombreCliente() + " (" + cliente.getCiCliente() + ")" : "";
            }
            @Override
            public Cliente fromString(String string) { return null; }
        });
    }

    private void configurarComboMoto() {
        comboMoto.setCellFactory(param -> new ListCell<Moto>() {
            @Override
            protected void updateItem(Moto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getMatriculaMoto() + " (Modelo #" + item.getIdModelo() + ")");
                }
            }
        });
        comboMoto.setConverter(new StringConverter<Moto>() {
            @Override
            public String toString(Moto moto) {
                return (moto != null) ? moto.getMatriculaMoto() + " (Modelo #" + moto.getIdModelo() + ")" : "";
            }
            @Override
            public Moto fromString(String string) { return null; }
        });
    }

    private void configurarComboPago() {
        // Cargar todos los valores del enum FormaPago
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
            public String toString(FormaPago fp) {
                return (fp != null) ? fp.name() : "";
            }
            @Override
            public FormaPago fromString(String string) { return null; }
        });
        comboPago.getSelectionModel().selectFirst();
    }

    private void actualizarPrecioEstimado() {
        Moto motoSeleccionada = comboMoto.getValue();
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

    private void cerrarVentana() {
        javafx.stage.Stage stage = (javafx.stage.Stage) comboCliente.getScene().getWindow();
        stage.close();
    }
}