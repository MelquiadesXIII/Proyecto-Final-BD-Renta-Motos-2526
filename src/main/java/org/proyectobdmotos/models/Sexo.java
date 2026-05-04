package org.proyectobdmotos.models;

import org.proyectobdmotos.services.exceptions.BusinessErrorCode;
import org.proyectobdmotos.services.exceptions.ValidationException;

public enum Sexo {
  MASCULINO(1, "masculino"),
  FEMENINO(2, "femenino");

  private final int id;
  private final String valor;

  Sexo(int id, String valor) {
    this.id = id;
    this.valor = valor;
  }

  public int getId() {
    return id;
  }

  public String getValor() {
    return valor;
  }

  public static Sexo fromId(int id) {
    Sexo resultado = null;
    boolean encontrado = false;
    for (Sexo sexo : values()) {
      if (sexo.id == id) {
        resultado = sexo;
        encontrado = true;
      }
    }

    if (!encontrado) {
      throw new ValidationException(
          BusinessErrorCode.ID_INVALIDO,
          "ID de sexo inválido: " + id);
    }

    return resultado;
  }

  public static Sexo fromValor(String valor) {
    Sexo resultado = null;
    boolean encontrado = false;
    for (Sexo sexo : values()) {
      if (sexo.valor.equalsIgnoreCase(valor)) {
        resultado = sexo;
        encontrado = true;
      }
    }

    if (!encontrado) {
      throw new ValidationException(
          BusinessErrorCode.FORMATO_INVALIDO,
          "Sexo inválido: " + valor);
    }

    return resultado;
  }
}
