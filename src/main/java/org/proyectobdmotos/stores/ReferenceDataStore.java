package org.proyectobdmotos.stores;


import java.util.Collection;

import org.proyectobdmotos.models.Color;
import org.proyectobdmotos.models.Marca;
import org.proyectobdmotos.models.Modelo;
import org.proyectobdmotos.models.Municipio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class ReferenceDataStore {

    private final ObservableList<Municipio> municipios;
    private final ObservableList<Color> colores;
    private final ObservableList<Marca> marcas;
    private final ObservableList<Modelo> modelos;


    public ReferenceDataStore() {

        municipios = FXCollections.observableArrayList();
        colores = FXCollections.observableArrayList();
        marcas = FXCollections.observableArrayList();
        modelos = FXCollections.observableArrayList();
    }

    public ObservableList<Municipio> getMunicipios() {
        return municipios;
    }
    public ObservableList<Color> getColores() {
        return colores;
    }
    public ObservableList<Marca> getMarcas() {
        return marcas;
    }
    public ObservableList<Modelo> getModelos() {
        return modelos;
    }

    public void setMunicipios(Collection<Municipio> nuevos) {
        municipios.setAll(nuevos);
    }

    public void setColores(Collection<Color> nuevos) {
        colores.setAll(nuevos);
    }

    public void setMarcas(Collection<Marca> nuevos) {
        marcas.setAll(nuevos);
    }

    public void setModelos(Collection<Modelo> nuevos) {
        modelos.setAll(nuevos);
    }
}
