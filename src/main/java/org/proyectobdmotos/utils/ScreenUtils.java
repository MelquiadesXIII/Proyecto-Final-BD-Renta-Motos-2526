package org.proyectobdmotos.utils;

import javafx.stage.Screen;

/**
 * Utilidad para obtener las dimensiones de la pantalla principal.
 * Se carga una sola vez al iniciar la aplicacion.
 */
public final class ScreenUtils {

    private static final double WIDTH;
    private static final double HEIGHT;

    static {
        WIDTH = Screen.getPrimary().getVisualBounds().getWidth();
        HEIGHT = Screen.getPrimary().getVisualBounds().getHeight();
    }

    private ScreenUtils() {
        
    }

    public static double getWidth() {
        return WIDTH;
    }

    public static double getHeight() {
        return HEIGHT;
    }
}