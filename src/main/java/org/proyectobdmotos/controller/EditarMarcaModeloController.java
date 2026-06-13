package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.proyectobdmotos.models.Marca;
import org.proyectobdmotos.models.Modelo;
import org.proyectobdmotos.services.MarcaService;
import org.proyectobdmotos.services.ModeloService;
import org.proyectobdmotos.services.MotoService;

import java.util.List;

public class EditarMarcaModeloController {

    @FXML private RadioButton radioModelo, radioMarca;
    @FXML private ComboBox<Modelo> comboModelo;
    @FXML private ComboBox<Marca> comboMarca;
    @FXML private TextField campoNombreModelo, campoNombreMarca;
    @FXML private Label valorMarcaAsociada;
    @FXML private Label labelMarcaAsociada;
    @FXML private ToggleGroup grupoTipo;

    private final ModeloService modeloService;
    private final MarcaService marcaService;
    private final MotoService motoService;

    private static Integer idModeloPreseleccionado;

    public static void setIdModeloPreseleccionado(Integer id) {
        idModeloPreseleccionado = id;
    }

    public EditarMarcaModeloController(ModeloService modeloService, MarcaService marcaService, MotoService motoService) {
        this.modeloService = modeloService;
        this.marcaService = marcaService;
        this.motoService = motoService;
    }

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    /**
     * Prepara la pantalla al abrirse: carga los combos, configura
     * la visibilidad y preselecciona el modelo si se indicó uno.
     */
    @FXML
    private void initialize() {
        cargarCombos();
        configurarCambioDeTipo();
        preseleccionarModeloSiCorresponde();
        actualizarVisibilidad();
    }

    /**
     * Enlaza el cambio de selección de los RadioButtons con la
     * actualización de la visibilidad de los campos.
     */
    private void configurarCambioDeTipo() {
        grupoTipo.selectedToggleProperty().addListener((obs, oldVal, newVal) -> actualizarVisibilidad());
    }

    /**
     * Si se pasó un id de modelo preseleccionado, se activa el modo modelo,
     * se selecciona el modelo correspondiente y se limpia la variable estática.
     * Si no hay preselección, se deja el modo modelo por defecto.
     */
    private void preseleccionarModeloSiCorresponde() {
        if (idModeloPreseleccionado != null) {
            radioModelo.setSelected(true);
            seleccionarModeloPorId(idModeloPreseleccionado);
            idModeloPreseleccionado = null;
        } else {
            radioModelo.setSelected(true);
        }
    }

    /**
     * Busca el modelo con el id indicado en el combo y lo selecciona.
     * Utiliza un bucle con variable booleana para evitar break/continue.
     */
    private void seleccionarModeloPorId(int id) {
        boolean encontrado = false;
        int indice = 0;
        List<Modelo> items = comboModelo.getItems();
        while (!encontrado && indice < items.size()) {
            Modelo m = items.get(indice);
            if (m.getIdModelo() == id) {
                comboModelo.getSelectionModel().select(m);
                encontrado = true;
            }
            indice++;
        }
    }

    // -----------------------------------------------------------------
    // Carga de combos
    // -----------------------------------------------------------------

    /**
     * Llena los ComboBox de modelo y marca con los datos del servicio,
     * configurando las fábricas de celdas para que muestren el nombre.
     */
    private void cargarCombos() {
        cargarComboModelos();
        cargarComboMarcas();
    }

    /**
     * Carga la lista de modelos y configura la visualización de sus nombres.
     */
    private void cargarComboModelos() {
        List<Modelo> modelos = modeloService.listarTodos();
        comboModelo.getItems().setAll(modelos);
        comboModelo.setCellFactory(param -> new ListCell<Modelo>() {
            @Override
            protected void updateItem(Modelo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreModelo());
            }
        });
        comboModelo.setButtonCell(new ListCell<Modelo>() {
            @Override
            protected void updateItem(Modelo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreModelo());
            }
        });
    }

    /**
     * Carga la lista de marcas y configura la visualización de sus nombres.
     */
    private void cargarComboMarcas() {
        List<Marca> marcas = marcaService.listarTodas();
        comboMarca.getItems().setAll(marcas);
        comboMarca.setCellFactory(param -> new ListCell<Marca>() {
            @Override
            protected void updateItem(Marca item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreMarca());
            }
        });
        comboMarca.setButtonCell(new ListCell<Marca>() {
            @Override
            protected void updateItem(Marca item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreMarca());
            }
        });
    }

    // -----------------------------------------------------------------
    // Visibilidad condicional
    // -----------------------------------------------------------------

    /**
     * Muestra u oculta los campos según el tipo de elemento seleccionado
     * (modelo o marca). También inicializa los listeners de cambio de valor.
     */
    private void actualizarVisibilidad() {
        boolean esModelo = radioModelo.isSelected();

        // Visibilidad de los campos de modelo
        comboModelo.setVisible(esModelo);
        comboModelo.setManaged(esModelo);
        campoNombreModelo.setVisible(esModelo);
        campoNombreModelo.setManaged(esModelo);
        labelMarcaAsociada.setVisible(esModelo);
        labelMarcaAsociada.setManaged(esModelo);
        valorMarcaAsociada.setVisible(esModelo);
        valorMarcaAsociada.setManaged(esModelo);

        // Visibilidad de los campos de marca
        comboMarca.setVisible(!esModelo);
        comboMarca.setManaged(!esModelo);
        campoNombreMarca.setVisible(!esModelo);
        campoNombreMarca.setManaged(!esModelo);

        // Sincroniza los campos de texto con la selección actual
        sincronizarCamposConSeleccion(esModelo);

        // Escucha cambios posteriores en los combos
        escucharCambiosEnCombos(esModelo);
    }

    /**
     * Sincroniza los campos de texto y la etiqueta de marca asociada
     * con el elemento actualmente seleccionado en los combos.
     */
    private void sincronizarCamposConSeleccion(boolean esModelo) {
        if (esModelo) {
            Modelo seleccionado = comboModelo.getValue();
            if (seleccionado != null) {
                campoNombreModelo.setText(seleccionado.getNombreModelo());
                actualizarMarcaAsociada(seleccionado.getIdMarca());
            } else {
                campoNombreModelo.clear();
                valorMarcaAsociada.setText("");
            }
        } else {
            Marca seleccionada = comboMarca.getValue();
            if (seleccionada != null) {
                campoNombreMarca.setText(seleccionada.getNombreMarca());
            } else {
                campoNombreMarca.clear();
            }
        }
    }

    /**
     * Añade listeners a los combos para que, al cambiar la selección,
     * se actualicen los campos de texto correspondientes.
     */
    private void escucharCambiosEnCombos(boolean esModelo) {
        comboModelo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && radioModelo.isSelected()) {
                campoNombreModelo.setText(newVal.getNombreModelo());
                actualizarMarcaAsociada(newVal.getIdMarca());
            }
        });
        comboMarca.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && radioMarca.isSelected()) {
                campoNombreMarca.setText(newVal.getNombreMarca());
            }
        });
    }

    /**
     * Actualiza la etiqueta de marca asociada buscando el nombre
     * de la marca por su id. Itera sin break/continue.
     */
    private void actualizarMarcaAsociada(int idMarca) {
        String nombre = "";
        boolean encontrado = false;
        int indice = 0;
        List<Marca> marcas = marcaService.listarTodas();
        while (!encontrado && indice < marcas.size()) {
            Marca m = marcas.get(indice);
            if (m.getIdMarca() == idMarca) {
                nombre = m.getNombreMarca();
                encontrado = true;
            }
            indice++;
        }
        valorMarcaAsociada.setText(nombre);
    }

    // -----------------------------------------------------------------
    // Acción Guardar
    // -----------------------------------------------------------------

    /**
     * Orquesta el guardado según el tipo de elemento seleccionado
     * (modelo o marca). Valida los campos y ejecuta la actualización.
     */
    @FXML
    private void onGuardar() {
        if (radioModelo.isSelected()) {
            guardarCambiosModelo();
        } else {
            guardarCambiosMarca();
        }
    }

    /**
     * Valida los campos del modelo y, si son correctos, actualiza el modelo.
     */
    private void guardarCambiosModelo() {
        Modelo modelo = comboModelo.getValue();
        String nuevoNombre = campoNombreModelo.getText().trim();

        if (modelo == null || nuevoNombre.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Complete todos los campos.").showAndWait();
        } else {
            int idMarca = modelo.getIdMarca();
            boolean nombreDuplicado = modeloService.existeModelo(idMarca, nuevoNombre)
                    && !modelo.getNombreModelo().equalsIgnoreCase(nuevoNombre);
            if (nombreDuplicado) {
                new Alert(Alert.AlertType.ERROR, "El modelo ya existe en esa marca.").showAndWait();
            } else {
                modelo.setNombreModelo(nuevoNombre);
                modeloService.actualizarModelo(modelo);
                MainController.getInstance().onGoBack();
            }
        }
    }

    /**
     * Valida los campos de la marca y, si son correctos, actualiza la marca.
     */
    private void guardarCambiosMarca() {
        Marca marca = comboMarca.getValue();
        String nuevoNombre = campoNombreMarca.getText().trim();

        if (marca == null || nuevoNombre.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Complete todos los campos.").showAndWait();
        } else {
            boolean nombreDuplicado = marcaService.existeMarca(nuevoNombre)
                    && !marca.getNombreMarca().equalsIgnoreCase(nuevoNombre);
            if (nombreDuplicado) {
                new Alert(Alert.AlertType.ERROR, "La marca ya existe.").showAndWait();
            } else {
                marca.setNombreMarca(nuevoNombre);
                marcaService.actualizarMarca(marca);
                MainController.getInstance().onGoBack();
            }
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
}