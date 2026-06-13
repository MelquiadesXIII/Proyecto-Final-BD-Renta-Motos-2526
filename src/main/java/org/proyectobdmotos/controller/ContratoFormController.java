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

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    /**
     * Prepara todos los componentes al cargar la pantalla:
     * configura listas, combos y enlaza los eventos de cambio de fechas y moto.
     */
    @FXML
    private void initialize() {
        configurarListaClientes();
        configurarComboMoto();
        configurarComboPago();

        // Mensaje que se muestra cuando la lista de resultados está vacía
        listaResultados.setPlaceholder(new Label("Escriba para buscar clientes"));

        // Evita que se pueda seleccionar un elemento fantasma al hacer clic en una lista vacía
        listaResultados.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (listaResultados.getItems().isEmpty()) {
                event.consume();
            }
        });

        // Al escribir en el buscador se actualiza la lista de resultados
        campoBuscarCliente.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.trim().isEmpty()) {
                listaResultados.getItems().clear();
            } else {
                List<Cliente> resultados = clienteService.buscarClientesPorTexto(newText.trim());
                listaResultados.getItems().setAll(resultados);
            }
        });

        // Cuando se selecciona un cliente de la lista, se guarda y se muestra su nombre
        listaResultados.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                clienteSeleccionado = newVal;
                campoBuscarCliente.setText(newVal.getNombreCliente() + " (" + newVal.getCiCliente() + ")");
                listaResultados.getItems().clear(); // limpia la lista tras la selección
            }
        });

        // Al cambiar cualquiera de las fechas se recarga el combo de motos disponibles
        dateInicio.valueProperty().addListener((obs, oldDate, newDate) -> cargarMotosSegunFechas());
        dateFin.valueProperty().addListener((obs, oldDate, newDate) -> cargarMotosSegunFechas());

        // Al cambiar la moto o las fechas se actualiza el precio estimado
        comboMoto.valueProperty().addListener((obs, oldMoto, newMoto) -> actualizarPrecioEstimado());
        dateInicio.valueProperty().addListener((obs, oldDate, newDate) -> actualizarPrecioEstimado());
        dateFin.valueProperty().addListener((obs, oldDate, newDate) -> actualizarPrecioEstimado());
    }

    // -----------------------------------------------------------------
    // Carga de motos según fechas
    // -----------------------------------------------------------------

    /**
     * Consulta las motos disponibles en el rango de fechas seleccionado
     * y llena el combo correspondiente. Si las fechas no son válidas
     * o están vacías, limpia el combo y muestra un texto informativo.
     */
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

    // -----------------------------------------------------------------
    // Acción de guardar
    // -----------------------------------------------------------------

    /**
     * Valida los campos y, si todo es correcto, crea el contrato.
     * Los mensajes de error se muestran sin detener el flujo (no se usa return).
     */
    @FXML
    private void onGuardar() {
        if (validarFormulario()) {
            crearContrato();
        }
    }

    /**
     * Comprueba que los campos obligatorios del formulario estén rellenados
     * y que las fechas sean coherentes. Muestra errores si algo falla.
     * @return true si todos los campos son válidos, false en caso contrario.
     */
    private boolean validarFormulario() {
        boolean valido = true;

        // Validar que se ha seleccionado un cliente
        if (clienteSeleccionado == null) {
            mostrarError("Debe buscar y seleccionar un cliente.");
            valido = false;
        }

        // Validar que el resto de campos obligatorios no estén vacíos
        MotoDisponibleDTO motoSeleccionada = comboMoto.getValue();
        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();
        FormaPago formaPago = comboPago.getValue();
        if (motoSeleccionada == null || inicio == null || fin == null || formaPago == null) {
            mostrarError("Todos los campos obligatorios deben estar completos.");
            valido = false;
        }

        // Validar que la fecha de inicio no sea posterior a la de fin
        if (valido) {
            boolean fechasValidas = inicio.isBefore(fin) || inicio.isEqual(fin);
            if (!fechasValidas) {
                mostrarError("La fecha de inicio debe ser anterior o igual a la fecha fin.");
                valido = false;
            }
        }

        return valido;
    }

    /**
     * Construye el objeto Contrato con los datos del formulario y lo envía al servicio.
     * Si la operación falla, muestra un mensaje de error; si tiene éxito,
     * notifica al usuario y regresa a la pantalla anterior.
     */
    private void crearContrato() {
        try {
            Contrato nuevoContrato = new Contrato(
                    0.0, 0.0,
                    clienteSeleccionado.getIdCliente(),
                    0, null,
                    dateFin.getValue(), dateInicio.getValue(),
                    comboPago.getValue(),
                    comboMoto.getValue().getIdMoto(),
                    false, 20.0, 40.0
            );

            contratoService.crearContrato(nuevoContrato);
            mostrarInfo("Contrato creado correctamente.");
            MainController.getInstance().onGoBack();
        } catch (ValidationException e) {
            e.printStackTrace();
            mostrarError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
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
        MainController.getInstance().onGoBack();
    }

    // -----------------------------------------------------------------
    // Configuración de componentes visuales
    // -----------------------------------------------------------------

    /**
     * Configura la lista de clientes para que muestre nombre, apellido y CI.
     */
    private void configurarListaClientes() {
        listaResultados.setCellFactory(param -> new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombreCliente() + " " + item.getPrimerApellido() + " (" + item.getCiCliente() + ")");
                }
            }
        });
    }

    /**
     * Configura el combo de motos para mostrar marca, modelo y color.
     * Usa un StringConverter para que el texto mostrado coincida con el formato deseado.
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
                return (dto != null) ? dto.getMarca() + " " + dto.getModelo() + " (" + dto.getColor() + ")" : "";
            }
            @Override
            public MotoDisponibleDTO fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Llena el combo de forma de pago con los valores del enumerado FormaPago,
     * muestra el nombre de cada opción y selecciona la primera por defecto.
     */
    private void configurarComboPago() {
        comboPago.getItems().setAll(FormaPago.values());
        comboPago.setCellFactory(param -> new ListCell<FormaPago>() {
            @Override
            protected void updateItem(FormaPago item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.name());
                }
            }
        });
        comboPago.setConverter(new StringConverter<FormaPago>() {
            @Override
            public String toString(FormaPago fp) {
                return (fp != null) ? fp.name() : "";
            }
            @Override
            public FormaPago fromString(String string) {
                return null;
            }
        });
        comboPago.getSelectionModel().selectFirst();
    }

    // -----------------------------------------------------------------
    // Cálculo dinámico del precio
    // -----------------------------------------------------------------

    /**
     * Calcula el precio estimado del alquiler en función de la moto seleccionada
     * y el número de días entre las fechas (incluyendo ambos extremos).
     * Si falta algún dato, muestra 0.00 CUP.
     */
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

    // -----------------------------------------------------------------
    // Utilidades de alertas
    // -----------------------------------------------------------------

    /**
     * Muestra un diálogo de error con el mensaje indicado.
     */
    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje).showAndWait();
    }

    /**
     * Muestra un diálogo informativo con el mensaje indicado.
     */
    private void mostrarInfo(String mensaje) {
        new Alert(Alert.AlertType.INFORMATION, mensaje).showAndWait();
    }
}