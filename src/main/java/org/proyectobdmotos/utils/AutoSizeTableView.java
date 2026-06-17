package org.proyectobdmotos.utils;

import javafx.scene.control.TableView;

/**
 * TableView que activa CONSTRAINED_RESIZE_POLICY por defecto,
 * haciendo que las columnas siempre llenen el ancho completo de la tabla
 * de forma proporcional a sus prefWidth.
 */
public class AutoSizeTableView<S> extends TableView<S> {

    public AutoSizeTableView() {
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }
}
