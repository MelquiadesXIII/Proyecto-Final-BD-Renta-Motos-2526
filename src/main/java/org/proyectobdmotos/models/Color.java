package org.proyectobdmotos.models;

import org.proyectobdmotos.utils.Validator;

@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
public class Color {

    private int idColor;
    private String nombreColor;

    public Color(int idColor, String nombreColor) {
        setIdColor(idColor);
        setNombreColor(nombreColor);
    }

    public int getIdColor() {
        return idColor;
    }

    public void setIdColor(int idColor) {
        this.idColor = idColor;
    }

    public String getNombreColor() {
        return nombreColor;
    }

    public void setNombreColor(String nombreColor) {
        Validator.validateText(nombreColor);
        this.nombreColor = nombreColor;
    }
}
