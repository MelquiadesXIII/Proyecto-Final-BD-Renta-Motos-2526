package org.proyectobdmotos.ui.vistas;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.utils.Reproductor;

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

    private ImageView imageView;
    private Pane paneTextos;

    public CreditosFinales(Runnable onFinCallback) {
        this.onFinCallback = onFinCallback;
        Reproductor.getInstancia().activarMusica();
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setStyle("-fx-background-color: blue;");

        // Capa 1: Imagen de fondo (se redimensionará cuando el panel tenga tamaño)
        imageView = new ImageView();
        imageView.setPreserveRatio(false);
        try {
            Image imagenFondo = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/Utiles/Creditos-Momentanea.jpg")));
            imageView.setImage(imagenFondo);
        } catch (NullPointerException e) {
            Logger.logError("No se pudo cargar la imagen de fondo: Creditos.png");
        }
        getChildren().add(imageView);

        // Capa 2: Panel con textos
        paneTextos = new Pane();
        labelCabecera = new Label();
        labelCabecera.setAlignment(Pos.CENTER);
        labelCabecera.setTextFill(Color.rgb(250, 250, 250, 0.0));

        labelCuerpo = new Label("Fin");
        labelCuerpo.setAlignment(Pos.CENTER);
        labelCuerpo.setTextFill(Color.rgb(250, 250, 250, 0.0));

        paneTextos.getChildren().addAll(labelCabecera, labelCuerpo);
        getChildren().add(paneTextos);

        // Cuando el panel se muestre y tenga tamaño, iniciamos la animación
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    ajustarATamañoActual();
                    iniciarAnimacion();
                });
            }
        });

        // Si el tamaño cambia después (redimensión), ajustamos los componentes
        widthProperty().addListener((obs, oldVal, newVal) -> ajustarATamañoActual());
        heightProperty().addListener((obs, oldVal, newVal) -> ajustarATamañoActual());
    }

    private void ajustarATamañoActual() {
        double ancho = getWidth();
        double alto = getHeight();
        if (ancho <= 0 || alto <= 0) return;

        imageView.setFitWidth(ancho);
        imageView.setFitHeight(alto);

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
        if (timelinePrincipal != null) {
            timelinePrincipal.stop();
        }
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

        // Fade IN
        if (desfile >= 0 && desfile <= 48 && desfile % 4 == 0 && repetible) {
            double alpha = (desfile / 4.0) * (20.0 / 255.0);
            setAlphaTextos(alpha);
        }

        if (desfile == 48) {
            revisarRepetible();
            if (fin) {
                timelinePrincipal.stop();
                PauseTransition pausa = new PauseTransition(Duration.seconds(7));
                pausa.setOnFinished(e -> salirAlMenu());
                pausa.play();
                return;
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

            if ("por su atención".equals(nuevoCuerpo)) {
                fin = true;
            }

            cabezaActual++;
        }
    }

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