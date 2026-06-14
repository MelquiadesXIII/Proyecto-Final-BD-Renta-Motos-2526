package org.proyectobdmotos.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

    @FXML private TableView<Moto> tablaMotos;
    @FXML private TableColumn<Moto, String> colMatricula, colMarca, colModelo, colColor;
    @FXML private TableColumn<Moto, Double> colKm;

    @FXML private TableView<MisContratosDTO> tablaContratos;
    @FXML private TableColumn<MisContratosDTO, String> colContratoMoto, colFechaInicio, colFechaFin;
    @FXML private TableColumn<MisContratosDTO, Double> colCosto;
    @FXML private TableColumn<MisContratosDTO, String> colFechaEntrega;

    @FXML private Label labelSinContratos;
    @FXML private Button btnFinalizarContrato;

    private final MotoService motoService;
    private final ContratoService contratoService;
    private final AgenciaStore agenciaStore;

    // Cachés para evitar múltiples consultas a la base de datos
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

    /**
     * Configura las columnas de ambas tablas, carga los datos iniciales
     * y deshabilita el botón de finalizar hasta que se seleccione un contrato.
     */
    @FXML
    private void initialize() {
        configurarColumnasMotos();
        configurarColumnasContratos();
        cargarMotos();
        cargarContratos();
        btnFinalizarContrato.setDisable(true);

        // Habilita el botón Finalizar solo cuando haya un contrato seleccionado
        tablaContratos.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            btnFinalizarContrato.setDisable(newVal == null);
        });
    }

    // -----------------------------------------------------------------
    // Configuración de columnas
    // -----------------------------------------------------------------

    /**
     * Configura la visualización de las cinco columnas de la tabla de motos.
     * Cada columna delega en un método privado que extrae el valor adecuado
     * del objeto Moto.
     */
    private void configurarColumnasMotos() {
        configurarColumnaMatricula();
        configurarColumnaMarca();
        configurarColumnaModelo();
        configurarColumnaColor();
        configurarColumnaKm();
    }

    /**
     * Columna Matrícula: muestra el valor directo de getMatriculaMoto().
     */
    private void configurarColumnaMatricula() {
        colMatricula.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getMatriculaMoto()));
    }

    /**
     * Columna Marca: obtiene el ID de la marca desde el modelo y luego el nombre.
     */
    private void configurarColumnaMarca() {
        colMarca.setCellValueFactory(cellData -> {
            int idMarca = obtenerIdMarcaDeMoto(cellData.getValue());
            String nombreMarca = obtenerNombreMarca(idMarca);
            return new javafx.beans.property.SimpleStringProperty(nombreMarca);
        });
    }

    /**
     * Columna Modelo: muestra el nombre del modelo a partir de su ID.
     */
    private void configurarColumnaModelo() {
        colModelo.setCellValueFactory(cellData -> {
            int idModelo = cellData.getValue().getIdModelo();
            String nombreModelo = obtenerNombreModelo(idModelo);
            return new javafx.beans.property.SimpleStringProperty(nombreModelo);
        });
    }

    /**
     * Columna Color: muestra el nombre del color a partir de su ID.
     */
    private void configurarColumnaColor() {
        colColor.setCellValueFactory(cellData -> {
            int idColor = cellData.getValue().getIdColor();
            String nombreColor = obtenerNombreColor(idColor);
            return new javafx.beans.property.SimpleStringProperty(nombreColor);
        });
    }

    /**
     * Columna Km: muestra el kilometraje (double) como objeto observable.
     */
    private void configurarColumnaKm() {
        colKm.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(
                        cellData.getValue().getCantKmRecorridos()).asObject());
    }

    /**
     * Configura las columnas de la tabla de contratos del usuario.
     */
    private void configurarColumnasContratos() {
        colContratoMoto.setCellValueFactory(new PropertyValueFactory<>("motoInfo"));
        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFechaFin.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colCosto.setCellValueFactory(new PropertyValueFactory<>("costoTotal"));
        colFechaEntrega.setCellValueFactory(new PropertyValueFactory<>("fechaEntrega"));
    }

    // -----------------------------------------------------------------
    // Métodos auxiliares para obtener nombres a partir de IDs
    // -----------------------------------------------------------------

    /**
     * Obtiene el ID de la marca asociada a una moto, a través de su modelo.
     * Si no se puede determinar, devuelve -1.
     * @return el ID de la marca, o -1 si no se encuentra.
     */
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

    /**
     * Obtiene el nombre de una marca a partir de su ID, usando caché.
     * Si no se encuentra, devuelve "Desconocida".
     * @return el nombre de la marca.
     */
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

    /**
     * Obtiene el nombre de un modelo a partir de su ID, usando caché.
     * Si no se encuentra, devuelve "Desconocido".
     * @return el nombre del modelo.
     */
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

    /**
     * Obtiene el nombre de un color a partir de su ID, usando caché.
     * Si no se encuentra, devuelve "Color #" seguido del ID.
     * @return el nombre del color.
     */
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
    // Carga de datos
    // -----------------------------------------------------------------

    /**
     * Carga la lista completa de motos y las muestra en la tabla superior.
     */
    private void cargarMotos() {
        List<Moto> motos = motoService.listarTodos();
        tablaMotos.getItems().setAll(motos);
    }

    /**
     * Carga los contratos del cliente actual y actualiza la tabla inferior.
     * Muestra u oculta el mensaje "Sin Ningún Contrato" según corresponda.
     */
    private void cargarContratos() {
        int idCliente = agenciaStore.getClienteActual().getIdCliente();
        List<MisContratosDTO> contratos = contratoService.listarMisContratos(idCliente);
        tablaContratos.getItems().setAll(contratos);
        boolean sinContratos = contratos.isEmpty();
        labelSinContratos.setVisible(sinContratos);
        labelSinContratos.setManaged(sinContratos);
    }

    // -----------------------------------------------------------------
    // Acciones del usuario
    // -----------------------------------------------------------------

    /**
     * Abre el formulario para crear un nuevo contrato.
     */
    @FXML
    private void onCrearNuevoContrato() {
        UserMainController.getInstance().cargarVista("/fxml/contrato-usuario-form.fxml", "Nuevo Contrato");
    }

    /**
     * Inicia el proceso de finalización de un contrato seleccionado.
     * Muestra un diálogo para ingresar los datos de entrega y, si son válidos,
     * procede a finalizar el contrato.
     */
    @FXML
    private void onFinalizarContrato() {
        MisContratosDTO seleccionado = tablaContratos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            Optional<DatosFinalizacion> datos = mostrarDialogoFinalizacion();
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

    /**
     * Muestra un cuadro de diálogo modal para que el usuario ingrese
     * el kilometraje de llegada y la fecha de entrega.
     * @return un Optional con los datos si el usuario acepta, Optional.empty() si cancela.
     */
    private Optional<DatosFinalizacion> mostrarDialogoFinalizacion() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Finalizar contrato");
        dialog.setHeaderText("Ingrese los datos de entrega:");

        TextField kmField = new TextField();
        kmField.setPromptText("Kilometraje actual");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isAfter(LocalDate.now())) {
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
                mostrarAlerta( "Ingrese un número válido para los kilómetros.");
            }
        }
        return Optional.ofNullable(datos);
    }

    /**
     * Ejecuta la lógica de finalización del contrato: valida los kilómetros y la fecha,
     * actualiza el contrato y recarga la tabla de contratos.
     */
    private void procesarFinalizacion(MisContratosDTO dto, double kmLlegada, LocalDate fechaEntrega) {
        Optional<Contrato> optContrato = contratoService.buscarPorId(dto.getIdContrato());
        if (optContrato.isEmpty()) {
            mostrarAlerta( "El contrato ya no existe.");
        } else {
            Contrato contrato = optContrato.get();
            boolean kmValido = kmLlegada >= contrato.getCantKmSalida();
            boolean fechaValida = !fechaEntrega.isAfter(LocalDate.now());
            if (!kmValido) {
                mostrarAlerta("Los kilómetros de llegada no pueden ser menores que los de salida "
                                + contrato.getCantKmSalida() + " km).");
            } else if (!fechaValida) {
                mostrarAlerta(
                        "La fecha de entrega no puede ser posterior a hoy.");
            } else {
                contrato.setCantKmLlegada(kmLlegada);
                contrato.setFechaEntrega(fechaEntrega);
                try {
                    contratoService.finalizarContrato(contrato);
                    cargarContratos();
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarAlerta( "Error al finalizar: " + e.getMessage());
                }
            }
        }
    }

    // -----------------------------------------------------------------
    // Clase interna para agrupar los datos del diálogo
    // -----------------------------------------------------------------

    /**
     * Almacena el kilometraje de llegada y la fecha de entrega
     * ingresados por el usuario en el diálogo de finalización.
     */
    private static class DatosFinalizacion {
        final double kmLlegada;
        final LocalDate fechaEntrega;

        DatosFinalizacion(double kmLlegada, LocalDate fechaEntrega) {
            this.kmLlegada = kmLlegada;
            this.fechaEntrega = fechaEntrega;
        }
    }

    private void mostrarAlerta(String mensaje)
    {
        AlertUtils.mostrarError(mensaje);
    }
}