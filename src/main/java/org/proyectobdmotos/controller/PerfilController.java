package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.proyectobdmotos.models.Usuario;

public class PerfilController {

    @FXML
    private Label labelNombreUsuario;
    @FXML
    private ImageView fondoPerfil;   // para el binding

    private Usuario usuarioActual;

    @FXML
    private void initialize() {
        // Responsive background
        if (fondoPerfil != null) {
            StackPane parent = (StackPane) fondoPerfil.getParent();
            fondoPerfil.fitWidthProperty().bind(parent.widthProperty());
            fondoPerfil.fitHeightProperty().bind(parent.heightProperty());
        }
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        if (labelNombreUsuario != null) {
            labelNombreUsuario.setText("Bienvenido, " + usuario.getNombreUsuario());
        }
    }
}