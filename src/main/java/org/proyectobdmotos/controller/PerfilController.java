package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.proyectobdmotos.dto.MisContratosDTO;
import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.models.Usuario;
import org.proyectobdmotos.services.ClienteService;
import org.proyectobdmotos.services.ContratoService;
import org.proyectobdmotos.utils.Logger;

import java.util.List;
import java.util.Optional;

public class PerfilController {

    private final ClienteService clienteService;
    private final ContratoService contratoService;

    @FXML private Label labelNombre, labelCI, labelTelefono, labelMunicipio;
    @FXML private Label labelUsuario, labelGmail;

    @FXML private TableView<MisContratosDTO> tablaMisContratos;
    @FXML private TableColumn<MisContratosDTO, Integer> colIdContrato;
    @FXML private TableColumn<MisContratosDTO, String> colMatricula;
    @FXML private TableColumn<MisContratosDTO, String> colMarca;
    @FXML private TableColumn<MisContratosDTO, String> colModelo;
    @FXML private TableColumn<MisContratosDTO, String> colInicio;
    @FXML private TableColumn<MisContratosDTO, String> colFin;
    @FXML private TableColumn<MisContratosDTO, String> colEstado;
    @FXML private TableColumn<MisContratosDTO, Double> colImporte;

    private Usuario usuario;

    public PerfilController(ClienteService clienteService, ContratoService contratoService) {
        this.clienteService = clienteService;
        this.contratoService = contratoService;
    }

    @FXML
    private void initialize() {
        configurarColumnas();
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        labelUsuario.setText(usuario.getNombreUsuario());
        labelGmail.setText(usuario.getGmail());

        Optional<Cliente> optCliente = clienteService.buscarPorIdUsuario(usuario.getId());
        if (optCliente.isPresent()) {
            Cliente c = optCliente.get();
            labelNombre.setText(c.getNombreCliente() + " " + c.getPrimerApellido() + " " + c.getSegundoApellido());
            labelCI.setText(c.getCiCliente());
            labelTelefono.setText(c.getNumeroContacto());
            labelMunicipio.setText(clienteService.obtenerNombreMunicipio(c.getIdMunicipio()));
            cargarContratos(c.getIdCliente());
        }
    }

    private void configurarColumnas() {
        colIdContrato.setCellValueFactory(new PropertyValueFactory<>("idContrato"));
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matriculaMoto"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFin.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colImporte.setCellValueFactory(new PropertyValueFactory<>("importe"));
    }

    private void cargarContratos(int idCliente) {
        try {
            List<MisContratosDTO> lista = contratoService.listarMisContratos(idCliente);
            tablaMisContratos.getItems().setAll(lista);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error al cargar contratos del perfil: " + e.getMessage());
        }
    }

    @FXML
    private void onCerrarSesion() {
        try {
            MainController.getInstance().cargarVista("/fxml/login.fxml", "Login");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError("Error al cerrar sesión: " + e.getMessage());
        }
    }
}