package org.proyectobdmotos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.proyectobdmotos.models.Usuario;

public class PerfilController {

    @FXML
    private Label labelNombreUsuario;  // Asegúrate de que tu perfil.fxml tenga un Label con fx:id="labelNombreUsuario"

    private Usuario usuarioActual;

    public void setUsuario(Usuario usuario) 
    {
        this.usuarioActual = usuario;
        if (labelNombreUsuario != null) {
            labelNombreUsuario.setText("Bienvenido, " + usuario.getNombreUsuario());
        }
    }
}