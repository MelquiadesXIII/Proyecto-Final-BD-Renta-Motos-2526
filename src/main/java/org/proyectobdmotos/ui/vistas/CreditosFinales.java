package org.proyectobdmotos.ui.vistas;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.proyectobdmotos.utils.Logger;

import java.util.List;
import java.util.Objects;

public class CreditosFinales extends StackPane {

    private final Runnable onFinCallback;

    private Label labelCabecera;
    private Label labelCuerpo;
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

    private final double ancho;
    private final double alto;

    public CreditosFinales(Runnable onFinCallback) {
        this.onFinCallback = onFinCallback;

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        ancho = bounds.getWidth();
        alto = bounds.getHeight();

        inicializarComponentes();
        iniciarAnimacion();

        /*
        try {
            // Reproductor.getInstancia().agregarReproducirCancionDelFinal();
            Logger.logInfo("Funcionalidad de audio comentada temporalmente.");
        } catch (Exception e) {
            Logger.logError("No se pudo reproducir la canción de los créditos: " + e.getMessage());
        }
        */
    }

    private void inicializarComponentes() {
        // Fondo de respaldo
        setStyle("-fx-background-color: blue;");

        // Capa 1: Imagen de fondo
        try {
            Image imagenFondo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Creditos.png")));
            ImageView imageView = new ImageView(imagenFondo);
            imageView.setFitWidth(ancho);
            imageView.setFitHeight(alto);
            imageView.setPreserveRatio(false);
            getChildren().add(imageView);
        } catch (NullPointerException e) {
            Logger.logError("No se pudo cargar la imagen de fondo: Creditos.png");
        }

        // Capa 2: Panel con los textos
        Pane paneTextos = new Pane();

        // Label Cabecera (rol)
        labelCabecera = new Label();
        labelCabecera.setLayoutX(ancho * 0.10);
        labelCabecera.setLayoutY(alto * 0.20);
        labelCabecera.setPrefWidth(ancho * 0.80);
        labelCabecera.setPrefHeight(alto * 0.20);
        labelCabecera.setFont(Font.font("Segoe UI", ancho * 0.085));
        labelCabecera.setAlignment(Pos.CENTER);
        labelCabecera.setTextFill(Color.rgb(250, 250, 250, 0.0));

        // Label Cuerpo (nombre)
        labelCuerpo = new Label("Fin"); // Texto inicial
        labelCuerpo.setLayoutX(0);
        labelCuerpo.setLayoutY(alto * 0.41);
        labelCuerpo.setPrefWidth(ancho);
        labelCuerpo.setPrefHeight(alto * 0.20);
        labelCuerpo.setFont(Font.font("Segoe UI", ancho * 0.057));
        labelCuerpo.setAlignment(Pos.CENTER);
        labelCuerpo.setTextFill(Color.rgb(250, 250, 250, 0.0));

        paneTextos.getChildren().addAll(labelCabecera, labelCuerpo);
        getChildren().add(paneTextos);
    }

    private void iniciarAnimacion() {
        ponerSiguienteString();
        timelinePrincipal = new Timeline(
                new KeyFrame(Duration.millis(21), e -> actualizarAnimacion())
        );
        timelinePrincipal.setCycleCount(Timeline.INDEFINITE);
        timelinePrincipal.play();
    }

    private void actualizarAnimacion() {
        desfile++;

        // Fade IN
        if (desfile >= 0 && desfile <= 48 && desfile % 4 == 0 && repetible) {
            double alpha = (desfile / 4.0) * (20.0 / 255.0);
            setAlphaTextos(alpha);
        }

        // En el tick 48, se revisa si hay que detenerse o si el texto se repite
        if (desfile == 48) {
            revisarRepetible();
            if (fin) {
                timelinePrincipal.stop();
                PauseTransition pausa = new PauseTransition(Duration.seconds(7));
                pausa.setOnFinished(e -> salirAlMenu());
                pausa.play();
                return; // Evita que siga la ejecución del tick
            }
        }

        // Fade OUT
        if (desfile >= 248 && desfile <= 292 && desfile % 4 == 0 && repetible) {
            double alpha = ((292 - desfile) / 4.0) * (20.0 / 255.0);
            setAlphaTextos(alpha);
        }

        // Cambio de texto
        if (desfile >= 310) {
            desfile = 0;
            if (!fin) {
                ponerSiguienteString();
            }
        }
    }

    private void setAlphaTextos(double alpha) {
        alpha = Math.max(0.0, Math.min(1.0, alpha)); // Clamping
        labelCabecera.setTextFill(Color.rgb(250, 250, 250, alpha));
        labelCuerpo.setTextFill(Color.rgb(250, 250, 250, alpha));
    }

    private void ponerSiguienteString() {
        boolean continuar = cabezaActual < cabeceras.size();
        if (continuar) {
            String nuevaCabecera = cabeceras.get(cabezaActual);
            String nuevoCuerpo = cuerpos.get(cabezaActual);

            if (!Objects.equals(labelCabecera.getText(), nuevaCabecera)) {
                labelCabecera.setText(nuevaCabecera);
                repetible = true;

                if ("Muchas gracias".equals(nuevaCabecera)) {
                    labelCuerpo.setLayoutX(ancho * 0.10);
                    labelCuerpo.setLayoutY(alto * 0.42);
                    labelCuerpo.setPrefWidth(ancho * 0.80);
                    labelCuerpo.setFont(Font.font("Segoe UI", ancho * 0.085));
                }
            } else {
                repetible = false;
            }

            labelCuerpo.setText(nuevoCuerpo);

            if ("por su atención".equals(nuevoCuerpo)) {
                fin = true;
            }

            cabezaActual++;
        }
    }

    private void revisarRepetible() {
        boolean continuar = cabezaActual < cabeceras.size();
        if (continuar) {
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
