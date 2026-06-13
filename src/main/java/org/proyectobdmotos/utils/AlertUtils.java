package org.proyectobdmotos.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.util.Optional;

public final class AlertUtils {

    private AlertUtils() {}

    /**
     * Muestra un cuadro de diálogo de error con un mensaje que se ajusta al tamaño del texto.
     * @param mensaje el mensaje a mostrar.
     */
    public static void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(crearContenido(mensaje));
        alert.showAndWait();
    }


    public static void mostrarErrorConTitulo(String cabecera,String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(cabecera);
        alert.getDialogPane().setContent(crearContenido(mensaje));
        alert.showAndWait();
    }

    /**
     * Muestra un cuadro de diálogo informativo con un mensaje que se ajusta al tamaño del texto.
     * @param mensaje el mensaje a mostrar.
     */
    public static void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(crearContenido(mensaje));
        alert.showAndWait();
    }

    public static void mostrarInfoCabecera(String cab,String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(cab);
        alert.getDialogPane().setContent(crearContenido(mensaje));
        alert.showAndWait();
    }


    /**
     * Muestra un error con cabecera personalizada, ideal para fallos de navegación.
     * El mensaje se ajusta al ancho del diálogo.
     * @param viewName Nombre descriptivo de la vista que no se pudo abrir.
     * @param exception Excepción ocurrida (se usará su mensaje).
     */
    public static void mostrarErrorCabecera(String viewName, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de navegación");
        alert.setHeaderText("No se pudo abrir la vista de " + viewName);
        alert.getDialogPane().setContent(crearContenido(exception.getMessage()));
        alert.showAndWait();
    }


    private static Label crearContenido(String mensaje) {
        Label label = new Label(mensaje);
        label.setWrapText(true);
        label.setMaxWidth(450);
        return label;
    }

    /**
     * Muestra un cuadro de diálogo de advertencia con un mensaje que se ajusta al tamaño del texto.
     * @param mensaje el mensaje a mostrar.
     */
    public static void mostrarAdvertencia(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(crearContenido(mensaje));
        alert.showAndWait();
    }

    public static  void mostrarErrorTitulo(String titulo, String mensaje)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.getDialogPane().setContent(crearContenido(mensaje));
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de confirmación con botones Aceptar/Cancelar.
     * @param titulo    título de la ventana.
     * @param encabezado texto del encabezado (puede ser null).
     * @param mensaje   contenido del mensaje.
     * @return true si el usuario elige Aceptar, false en caso contrario.
     */
    public static boolean mostrarConfirmacion(String titulo, String encabezado, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.getDialogPane().setContent(crearContenido(mensaje));

        Optional<ButtonType> resultado = alert.showAndWait();
        boolean aceptado = resultado.isPresent() && resultado.get() == ButtonType.OK;
        return aceptado;
    }

}