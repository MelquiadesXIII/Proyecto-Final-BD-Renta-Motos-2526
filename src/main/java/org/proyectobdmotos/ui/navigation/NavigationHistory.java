package org.proyectobdmotos.ui.navigation;

import java.util.Stack;
import javafx.scene.Parent;
/**
 * Historial global de navegación entre pantallas.
 * Permite volver a la pantalla anterior desde cualquier controlador.
 */
public final class NavigationHistory {

    private static final Stack<String> historial = new Stack<>();

    private NavigationHistory() {}

    /**
     * Guarda la pantalla actual antes de cambiar a otra.
     */
    public static void push(String fxmlActual) {
        historial.push(fxmlActual);
    }

    /**
     * Saca la última pantalla del historial.
     * @return la ruta FXML de la pantalla anterior, o null si el historial está vacío.
     */
    public static String pop() {
        if (historial.isEmpty()) {
            return null;
        }
        return historial.pop();
    }

    /**
     * Vuelve a la pantalla anterior usando el ScreenLoader.
     * @param screenLoader el cargador de pantallas
     * @return el Parent cargado, o null si no hay historial o error
     */
    public static Parent goBack(ScreenLoader screenLoader) {
        String fxmlAnterior = pop();
        if (fxmlAnterior == null) {
            return null;
        }
        try {
            return screenLoader.load(fxmlAnterior);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}