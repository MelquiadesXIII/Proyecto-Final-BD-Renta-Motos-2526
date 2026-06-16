package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import org.proyectobdmotos.models.Marca;
import org.proyectobdmotos.models.Modelo;
import org.proyectobdmotos.services.MarcaService;
import org.proyectobdmotos.services.ModeloService;
import org.proyectobdmotos.services.MotoService;

import java.util.List;

public class ModeloFormController {

    @FXML private ComboBox<Marca> comboMarca;
    @FXML private TextField campoNombreModelo;

    private final MotoService motoService;
    private final MarcaService marcaService;
    private final ModeloService modeloService;

    private Modelo modeloEditando;

    private static Modelo modeloEditarStatic;
    @FXML private StackPane rootPane;





    /**
     * Establece el modelo que se editará al abrir el formulario.
     * Método estático para comunicación entre controladores.
     */
    public static void setModeloEditarStatic(Modelo m) {
        modeloEditarStatic = m;
    }

    public ModeloFormController(MotoService motoService, MarcaService marcaService, ModeloService modeloService) {
        this.motoService = motoService;
        this.marcaService = marcaService;
        this.modeloService = modeloService;
    }

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    /**
     * Configura el combo de marcas, carga los datos y, si se ha indicado
     * un modelo para editar, prepara el formulario en modo edición.
     */
    @FXML
    private void initialize() {
        if (rootPane != null) {
            rootPane.setStyle(
                    "-fx-background-image: url('"
                            + getClass().getResource("/Utiles/Modelos.jpeg").toExternalForm()
                            + "');"
                            + "-fx-background-size: cover;"
                            + "-fx-background-position: center center;"
                            + "-fx-background-repeat: no-repeat;"
            );
        }
        configurarComboMarca();
        cargarMarcas();
        preseleccionarModoEdicion();
    }

    /**
     * Configura el ComboBox de marcas para mostrar el nombre de cada marca
     * y establecer un StringConverter adecuado.
     */
    private void configurarComboMarca() {
        comboMarca.setConverter(new StringConverter<Marca>() {
            @Override
            public String toString(Marca m) {
                return m != null ? m.getNombreMarca() : "";
            }
            @Override
            public Marca fromString(String s) {
                return null;
            }
        });
        comboMarca.setCellFactory(param -> new ListCell<Marca>() {
            @Override
            protected void updateItem(Marca item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombreMarca());
                }
            }
        });
    }

    /**
     * Carga la lista de marcas disponibles desde el servicio de motos.
     */
    private void cargarMarcas() {
        List<Marca> marcas = motoService.listarMarcas();
        comboMarca.getItems().setAll(marcas);
    }

    /**
     * Si hay un modelo estático pendiente de edición, lo carga en el formulario
     * y limpia la variable estática.
     */
    private void preseleccionarModoEdicion() {
        if (modeloEditarStatic != null) {
            setModoEdicion(modeloEditarStatic);
            modeloEditarStatic = null;
        }
    }

    // -----------------------------------------------------------------
    // Modo edición
    // -----------------------------------------------------------------

    /**
     * Coloca el formulario en modo edición con los datos del modelo dado.
     * Selecciona la marca correspondiente en el combo y rellena el campo de nombre.
     */
    public void setModoEdicion(Modelo modelo) {
        this.modeloEditando = modelo;
        seleccionarMarcaEnCombo(modelo.getIdMarca());
        campoNombreModelo.setText(modelo.getNombreModelo());
    }

    /**
     * Busca la marca con el id indicado en el combo y la selecciona.
     * Utiliza un bucle controlado por una variable booleana para evitar break/continue.
     */
    private void seleccionarMarcaEnCombo(int idMarca) {
        boolean encontrado = false;
        int indice = 0;
        List<Marca> items = comboMarca.getItems();
        while (!encontrado && indice < items.size()) {
            Marca m = items.get(indice);
            if (m.getIdMarca() == idMarca) {
                comboMarca.getSelectionModel().select(m);
                encontrado = true;
            }
            indice++;
        }
    }

    // -----------------------------------------------------------------
    // Acción Guardar
    // -----------------------------------------------------------------

    /**
     * Orquesta el guardado del modelo: valida los campos y, si son correctos,
     * ejecuta la creación o actualización según el modo.
     */
    @FXML
    private void onGuardar() {
        if (validarFormulario()) {
            ejecutarGuardado();
        }
    }

    /**
     * Verifica que se haya seleccionado una marca y que el nombre del modelo
     * no esté vacío. Muestra un mensaje de error si falta algún campo.
     * @return true si los datos son válidos, false en caso contrario.
     */
    private boolean validarFormulario() {
        Marca marca = comboMarca.getValue();
        String nombre = campoNombreModelo.getText().trim();
        if (marca == null || nombre.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Complete todos los campos.").showAndWait();
            return false;
        }
        return true;
    }

    /**
     * Decide si se debe crear un nuevo modelo o actualizar uno existente,
     * y ejecuta la operación correspondiente.
     */
    private void ejecutarGuardado() {
        Marca marca = comboMarca.getValue();
        String nombre = campoNombreModelo.getText().trim();
        try {
            if (modeloEditando != null) {
                actualizarModeloExistente(marca, nombre);
            } else {
                crearNuevoModelo(marca, nombre);
            }
            MainController.getInstance().onGoBack();
        } catch (RuntimeException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    /**
     * Actualiza el modelo en edición con los nuevos datos y lo persiste.
     */
    private void actualizarModeloExistente(Marca marca, String nombre) {
        modeloEditando.setNombreModelo(nombre);
        modeloEditando.setIdMarca(marca.getIdMarca());
        modeloService.actualizarModelo(modeloEditando);
    }

    /**
     * Crea un nuevo modelo con la marca y nombre indicados.
     */
    private void crearNuevoModelo(Marca marca, String nombre) {
        modeloService.crearModelo(marca.getIdMarca(), nombre);
    }

    // -----------------------------------------------------------------
    // Navegación
    // -----------------------------------------------------------------

    /**
     * Cancela el formulario y vuelve a la pantalla anterior.
     */
    @FXML
    private void onCancelar() {
        MainController.getInstance().onGoBack();
    }

    /**
     * Abre el formulario para crear una nueva marca.
     */
    @FXML
    private void onCrearMarca() {
        MainController.getInstance().cargarVista("/fxml/marca-form.fxml", "Nueva Marca");
    }
}