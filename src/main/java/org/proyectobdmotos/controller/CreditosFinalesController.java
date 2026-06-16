package org.proyectobdmotos.controller;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.utils.Reproductor;

import java.util.List;
import java.util.Objects;

public class CreditosFinalesController {

    @FXML private StackPane rootPane;
    @FXML private Pane paneTextos;
    @FXML private Label labelCabecera;
    @FXML private Label labelCuerpo;

    private Timeline timelinePrincipal;
    private int desfile = 0;
    private int cabezaActual = 0;
    private boolean fin = false;
    private boolean repetible = true;

    private final List<String> cabeceras = List.of(
            "Fullstack",
            "Fullstack",
            "Fullstack"
    );

    private final List<String> cuerpos = List.of(
            "Eriet Dario",
            "Lian Carlos Gene Galvez",
            "Darell Perdomo"
    );

    private static Runnable onFinCallback;

    public static void setOnFinCallback(Runnable callback) {
        onFinCallback = callback;
    }

    @FXML
    private void initialize() {
        // FONDO EXACTAMENTE COMO EN MisContratosController
        if (rootPane != null) {
            rootPane.setStyle(
                "-fx-background-image: url('"
                    + getClass().getResource("/Utiles/Creditos-Momentanea.jpg").toExternalForm()
                    + "');"
                    + "-fx-background-size: cover;"
                    + "-fx-background-position: center center;"
                    + "-fx-background-repeat: no-repeat;"
            );
        }

        // Los labels ya tienen estilos definidos en el CSS (creditos.css)
        labelCabecera.setAlignment(Pos.CENTER);
        labelCuerpo.setAlignment(Pos.CENTER);

        // Ajustar posiciones cuando el panel tenga tamaño
        rootPane.widthProperty().addListener((obs, oldW, newW) -> ajustarPosiciones());
        rootPane.heightProperty().addListener((obs, oldH, newH) -> ajustarPosiciones());

        // Iniciar música
        try {
            Logger.logInfo("Música de créditos iniciada");
        } catch (Exception e) {
            Logger.logError("No se pudo iniciar la música: " + e.getMessage());
        }

        Platform.runLater(() -> {
            ajustarPosiciones();
            iniciarAnimacion();
        });
    }

    private void ajustarPosiciones() {
        double ancho = rootPane.getWidth();
        double alto = rootPane.getHeight();
        if (ancho <= 0 || alto <= 0) return;

        labelCabecera.setLayoutX(ancho * 0.10);
        labelCabecera.setLayoutY(alto * 0.20);
        labelCabecera.setPrefWidth(ancho * 0.80);
        labelCabecera.setPrefHeight(alto * 0.20);
        labelCabecera.setFont(Font.font("Segoe UI", ancho * 0.085));

        labelCuerpo.setLayoutX(0);
        labelCuerpo.setLayoutY(alto * 0.41);
        labelCuerpo.setPrefWidth(ancho);
        labelCuerpo.setPrefHeight(alto * 0.20);
        labelCuerpo.setFont(Font.font("Segoe UI", ancho * 0.057));
    }

    private void iniciarAnimacion() {
        if (timelinePrincipal != null) timelinePrincipal.stop();
        desfile = 0;
        cabezaActual = 0;
        fin = false;
        repetible = true;
        ponerSiguienteString();
        timelinePrincipal = new Timeline(
                new KeyFrame(Duration.millis(21), e -> actualizarAnimacion())
        );
        timelinePrincipal.setCycleCount(Timeline.INDEFINITE);
        timelinePrincipal.play();
    }

    private void actualizarAnimacion() {
        desfile++;
        if (desfile >= 0 && desfile <= 48 && desfile % 4 == 0 && repetible) {
            double alpha = (desfile / 4.0) * (20.0 / 255.0);
            setAlphaTextos(alpha);
        }
        if (desfile == 48) {
            revisarRepetible();
            if (fin) {
                timelinePrincipal.stop();
                Reproductor.getInstancia().detenerCancion();
                PauseTransition pausa = new PauseTransition(Duration.seconds(7));
                pausa.setOnFinished(e -> salirAlMenu());
                pausa.play();
                return;
            }
        }
        if (desfile >= 248 && desfile <= 292 && desfile % 4 == 0 && repetible) {
            double alpha = ((292 - desfile) / 4.0) * (20.0 / 255.0);
            setAlphaTextos(alpha);
        }
        if (desfile >= 310) {
            desfile = 0;
            if (!fin) ponerSiguienteString();
        }
    }

    private void setAlphaTextos(double alpha) {
        alpha = Math.max(0.0, Math.min(1.0, alpha));
        labelCabecera.setTextFill(Color.rgb(250, 250, 250, alpha));
        labelCuerpo.setTextFill(Color.rgb(250, 250, 250, alpha));
    }

    private void ponerSiguienteString() {
        if (cabezaActual < cabeceras.size()) {
            String nuevaCabecera = cabeceras.get(cabezaActual);
            String nuevoCuerpo = cuerpos.get(cabezaActual);

            if (!Objects.equals(labelCabecera.getText(), nuevaCabecera)) {
                labelCabecera.setText(nuevaCabecera);
                repetible = true;
                if ("Muchas gracias".equals(nuevaCabecera)) {
                    labelCuerpo.setLayoutX(getWidth() * 0.10);
                    labelCuerpo.setLayoutY(getHeight() * 0.42);
                    labelCuerpo.setPrefWidth(getWidth() * 0.80);
                    labelCuerpo.setFont(Font.font("Segoe UI", getWidth() * 0.085));
                }
            } else {
                repetible = false;
            }

            labelCuerpo.setText(nuevoCuerpo);
            if ("por su atención".equals(nuevoCuerpo)) fin = true;
            cabezaActual++;
        }
    }

    private double getWidth() { return rootPane.getWidth(); }
    private double getHeight() { return rootPane.getHeight(); }

    private void revisarRepetible() {
        if (cabezaActual < cabeceras.size()) {
            String proximaCabecera = cabeceras.get(cabezaActual);
            repetible = !proximaCabecera.equals(labelCabecera.getText());
        }
    }

    private void salirAlMenu() {
        if (onFinCallback != null) {
            Platform.runLater(onFinCallback);
        }
    }
}