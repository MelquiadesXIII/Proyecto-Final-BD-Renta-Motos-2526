package org.proyectobdmotos.models;

import org.proyectobdmotos.services.exceptions.BusinessErrorCode;
import org.proyectobdmotos.services.exceptions.ValidationException;

public enum Situacion {
  DISPONIBLE(1, "disponible"),
  ALQUILADA(2, "alquilada"),
  TALLER(3, "taller");

  private final int id;
  private final String valor;

  Situacion(int id, String valor) {
    this.id = id;
    this.valor = valor;
  }

  public int getId() {
    return id;
  }

  public String getValor() {
    return valor;
  }

  public static Situacion fromId(int id) {
    Situacion resultado = null;
    boolean encontrado = false;
    for (Situacion situacion : values()) {
      if (situacion.id == id) {
        resultado = situacion;
        encontrado = true;
      }
    }

    if (!encontrado) {
      throw new ValidationException(
          BusinessErrorCode.ID_INVALIDO,
          "ID de situación inválido: " + id);
    }

    return resultado;
  }

  public static Situacion fromValor(String valor) {
    Situacion resultado = null;
    boolean encontrado = false;
    for (Situacion situacion : values()) {
      if (situacion.valor.equalsIgnoreCase(valor)) {
        resultado = situacion;
        encontrado = true;
      }
    }

    if (!encontrado) {
      throw new ValidationException(
          BusinessErrorCode.FORMATO_INVALIDO,
          "Situación inválida: " + valor);
    }

    return resultado;
  }
}
