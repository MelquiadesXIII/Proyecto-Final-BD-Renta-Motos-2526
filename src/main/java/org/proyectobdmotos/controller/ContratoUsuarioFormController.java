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
import org.proyectobdmotos.utils.AlertUtils;
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

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    /**
     * Configura los componentes visuales y enlaza los eventos de cambio
     * en las fechas y la moto para mantener actualizado el formulario.
     */
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

    // -----------------------------------------------------------------
    // Carga de motos según fechas
    // -----------------------------------------------------------------

    /**
     * Consulta las motos disponibles en el rango de fechas seleccionado
     * y llena el combo. Si no hay motos, muestra un mensaje informativo.
     */
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

    // -----------------------------------------------------------------
    // Acción Guardar
    // -----------------------------------------------------------------

    /**
     * Orquesta el guardado del contrato: valida los campos y, si todo es correcto,
     * crea el contrato asociado al cliente actual.
     */
    @FXML
    private void onGuardar() {
        if (validarFormulario()) {
            crearContrato();
        }
    }

    /**
     * Comprueba que los campos obligatorios estén rellenados y que la fecha
     * de inicio no sea posterior a la de fin.
     * @return true si los datos son válidos, false en caso contrario.
     */
    private boolean validarFormulario() {
        MotoDisponibleDTO motoSeleccionada = comboMoto.getValue();
        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();
        FormaPago formaPago = comboPago.getValue();

        if (motoSeleccionada == null || inicio == null || fin == null || formaPago == null) {
            mostrarError("Todos los campos obligatorios deben estar completos.");
            return false;
        }
        if (inicio.isAfter(fin)) {
            mostrarError("La fecha de inicio debe ser anterior o igual a la fecha fin.");
            return false;
        }
        return true;
    }

    /**
     * Construye el objeto Contrato con los datos del formulario y lo envía al servicio.
     * Si ocurre un error, muestra el mensaje correspondiente.
     */
    private void crearContrato() {
        Cliente cliente = agenciaStore.getClienteActual();
        if (cliente == null) {
            mostrarError("No se ha identificado al cliente.");
            return;
        }

        try {
            Contrato nuevoContrato = new Contrato(
                    0.0, 0.0,
                    cliente.getIdCliente(),
                    0, null,
                    dateFin.getValue(), dateInicio.getValue(),
                    comboPago.getValue(),
                    comboMoto.getValue().getIdMoto(),
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

    // -----------------------------------------------------------------
    // Cancelar
    // -----------------------------------------------------------------

    /**
     * Vuelve a la pantalla anterior sin guardar cambios.
     */
    @FXML
    private void onCancelar() {
        UserMainController.getInstance().onGoBack();
    }

    // -----------------------------------------------------------------
    // Configuración de componentes visuales
    // -----------------------------------------------------------------

    /**
     * Configura el combo de motos para mostrar marca, modelo y color.
     */
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

    /**
     * Llena el combo de forma de pago con los valores del enumerado FormaPago
     * y selecciona la primera opción por defecto.
     */
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

    /**
     * Calcula el precio estimado en función de la moto y el número de días.
     * Muestra 0.00 CUP si falta algún dato.
     */
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

    // -----------------------------------------------------------------
    // Alertas
    // -----------------------------------------------------------------

    /**
     * Muestra un mensaje de error en un cuadro de diálogo.
     */
    private void mostrarError(String mensaje) {
        AlertUtils.mostrarError(mensaje);
    }

    /**
     * Muestra un mensaje informativo en un cuadro de diálogo.
     */
    private void mostrarInfo(String mensaje) {
        AlertUtils.mostrarInfo(mensaje);
    }
}