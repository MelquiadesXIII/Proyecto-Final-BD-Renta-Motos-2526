package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.proyectobdmotos.models.*;
import org.proyectobdmotos.services.MarcaService;
import org.proyectobdmotos.services.ModeloService;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.utils.Logger;

import java.util.ArrayList;

public class MotoFormController {

    @FXML private TextField campoMatricula;
    @FXML private ComboBox<Marca> comboMarca;
    @FXML private ComboBox<Modelo> comboModelo;
    @FXML private ComboBox<Color> comboColor;
    @FXML private TextField campoKilometros;

    private final MotoService motoService;
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;
    private final MarcaService marcaService;
    private final ModeloService modeloService;

    private static Moto motoAEditarStatic;

    /**
     * Establece la moto que se editará al abrir el formulario.
     * Método estático para comunicación entre controladores.
     */
    public static void setMotoAEditarStatic(Moto m) {
        motoAEditarStatic = m;
    }

    public MotoFormController(MotoService motoService,
                              AgenciaStore agenciaStore,
                              ReferenceDataStore referenceDataStore,
                              MarcaService marcaService,
                              ModeloService modeloService) {
        this.motoService = motoService;
        this.agenciaStore = agenciaStore;
        this.referenceDataStore = referenceDataStore;
        this.marcaService = marcaService;
        this.modeloService = modeloService;
    }

    // -----------------------------------------------------------------
    // Inicialización
    // -----------------------------------------------------------------

    /**
     * Configura todos los combos del formulario, enlaza el filtro
     * de modelos por marca y, si hay una moto para editar, carga sus datos.
     */
    @FXML
    private void initialize() {
        configurarComboMarca();
        configurarComboModelo();
        configurarComboColor();
        enlazarFiltroModelosPorMarca();
        preseleccionarModoEdicion();
    }

    /**
     * Carga las marcas en el combo y configura su visualización.
     */
    private void configurarComboMarca() {
        ArrayList<Marca> marcas = motoService.listarMarcas();
        comboMarca.getItems().setAll(marcas);

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
    }

    /**
     * Configura la visualización del combo de modelos.
     * El combo se llena dinámicamente al seleccionar una marca.
     */
    private void configurarComboModelo() {
        comboModelo.setCellFactory(param -> new ListCell<Modelo>() {
            @Override
            protected void updateItem(Modelo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombreModelo());
                }
            }
        });
        comboModelo.setConverter(new StringConverter<Modelo>() {
            @Override
            public String toString(Modelo m) {
                return m != null ? m.getNombreModelo() : "";
            }
            @Override
            public Modelo fromString(String s) {
                return null;
            }
        });
    }

    /**
     * Carga los colores en el combo y configura su visualización.
     */
    private void configurarComboColor() {
        ArrayList<Color> colores = motoService.listarColores();
        comboColor.getItems().setAll(colores);
        comboColor.setCellFactory(param -> new ListCell<Color>() {
            @Override
            protected void updateItem(Color item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombreColor());
                }
            }
        });
        comboColor.setConverter(new StringConverter<Color>() {
            @Override
            public String toString(Color c) {
                return c != null ? c.getNombreColor() : "";
            }
            @Override
            public Color fromString(String s) {
                return null;
            }
        });
    }

    /**
     * Cuando se selecciona una marca, limpia el combo de modelos
     * y lo llena con los modelos de esa marca. Deshabilita el combo
     * de modelos si no hay marca seleccionada.
     */
    private void enlazarFiltroModelosPorMarca() {
        comboMarca.getSelectionModel().selectedItemProperty().addListener((obs, old, newMarca) -> {
            comboModelo.getItems().clear();
            if (newMarca != null) {
                ArrayList<Modelo> modelos = motoService.listarModelosPorMarca(newMarca.getIdMarca());
                if (modelos != null) {
                    comboModelo.getItems().setAll(modelos);
                }
            }
        });
        comboModelo.disableProperty().bind(comboMarca.getSelectionModel().selectedItemProperty().isNull());
    }

    /**
     * Si hay una moto estática pendiente de edición, la carga en el formulario
     * y limpia la variable estática.
     */
    private void preseleccionarModoEdicion() {
        if (motoAEditarStatic != null) {
            setModoEdicion(motoAEditarStatic);
            motoAEditarStatic = null;
        }
    }

    // -----------------------------------------------------------------
    // Modo edición
    // -----------------------------------------------------------------

    /**
     * Carga los datos de la moto proporcionada en los campos del formulario.
     * Selecciona la marca, el modelo y el color correspondientes.
     */
    public void setModoEdicion(Moto m) {
        campoMatricula.setText(m.getMatriculaMoto());
        campoKilometros.setText(String.valueOf(m.getCantKmRecorridos()));

        int idModelo = m.getIdModelo();
        Modelo modelo = motoService.obtenerModeloPorId(idModelo);
        if (modelo != null) {
            Marca marca = motoService.obtenerMarcaPorId(modelo.getIdMarca());
            if (marca != null) {
                comboMarca.getSelectionModel().select(marca);
            }
            comboModelo.getSelectionModel().select(modelo);
            seleccionarColorPorId(m.getIdColor());
        } else {
            new Alert(Alert.AlertType.WARNING, "No se pudo cargar el modelo de la moto.").showAndWait();
        }
    }

    /**
     * Selecciona el color con el id indicado en el combo de colores.
     * Utiliza un bucle con bandera booleana para evitar break/continue.
     */
    private void seleccionarColorPorId(int idColor) {
        boolean encontrado = false;
        int indice = 0;
        while (!encontrado && indice < comboColor.getItems().size()) {
            Color c = comboColor.getItems().get(indice);
            if (c.getIdColor() == idColor) {
                comboColor.getSelectionModel().select(c);
                encontrado = true;
            }
            indice++;
        }
    }

    // -----------------------------------------------------------------
    // Acción Guardar
    // -----------------------------------------------------------------

    /**
     * Orquesta el guardado de la moto: valida los campos y, si son correctos,
     * crea la moto a través del servicio.
     */
    @FXML
    private void onGuardar() {
        if (validarFormulario()) {
            guardarMoto();
        }
    }

    /**
     * Verifica que los campos obligatorios estén completos.
     * Muestra un mensaje de error si algún campo falta.
     * @return true si todos los campos son válidos, false en caso contrario.
     */
    private boolean validarFormulario() {
        String matricula = campoMatricula.getText().trim();
        Modelo modeloSel = comboModelo.getValue();
        Color colorSel = comboColor.getValue();

        if (matricula.isEmpty() || modeloSel == null || colorSel == null) {
            new Alert(Alert.AlertType.ERROR, "Todos los campos obligatorios deben estar completos.").showAndWait();
            return false;
        }
        return true;
    }

    /**
     * Construye un objeto Moto con los datos del formulario y lo envía al servicio.
     * Si la operación falla, muestra un mensaje de error; si tiene éxito,
     * notifica al usuario y regresa a la pantalla anterior.
     */
    private void guardarMoto() {
        String matricula = campoMatricula.getText().trim();
        String kmTexto = campoKilometros.getText().trim();
        Modelo modeloSel = comboModelo.getValue();
        Color colorSel = comboColor.getValue();

        try {
            double km = Double.parseDouble(kmTexto);
            Moto nueva = new Moto(null, matricula, modeloSel.getIdModelo(), Situacion.DISPONIBLE, km, colorSel.getIdColor());
            motoService.crearMoto(nueva);
            new Alert(Alert.AlertType.INFORMATION, "Moto guardada correctamente.").showAndWait();
            MainController.getInstance().onGoBack();
        } catch (NumberFormatException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Los kilómetros deben ser un número válido.").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al guardar la moto: " + e.getMessage()).showAndWait();
        }
    }

    // -----------------------------------------------------------------
    // Cancelar
    // -----------------------------------------------------------------

    /**
     * Cancela el formulario y vuelve a la pantalla anterior.
     */
    @FXML
    private void onCancelar() {
        MainController.getInstance().onGoBack();
    }
}