package org.proyectobdmotos.models;

import org.proyectobdmotos.services.exceptions.BusinessErrorCode;
import org.proyectobdmotos.services.exceptions.ValidationException;

public enum FormaPago {
  EFECTIVO(1, "efectivo"),
  CHEQUE(2, "cheque"),
  CREDITO(3, "credito");

  private final int id;
  private final String valor;

  FormaPago(int id, String valor) {
    this.id = id;
    this.valor = valor;
  }

  public int getId() {
    return id;
  }

  public String getValor() {
    return valor;
  }

  public static FormaPago fromId(int id) {
    FormaPago resultado = null;
    boolean encontrado = false;
    for (FormaPago forma : values()) {
      if (forma.id == id) {
        resultado = forma;
        encontrado = true;
      }
    }

    if (!encontrado) {
      throw new ValidationException(
          BusinessErrorCode.ID_INVALIDO,
          "ID de forma de pago inválido: " + id);
    }

    return resultado;
  }

  public static FormaPago fromValor(String valor) {
    FormaPago resultado = null;
    boolean encontrado = false;
    for (FormaPago forma : values()) {
      if (forma.valor.equalsIgnoreCase(valor)) {
        resultado = forma;
        encontrado = true;
      }
    }

    if (!encontrado) {
      throw new ValidationException(
          BusinessErrorCode.FORMATO_INVALIDO,
          "Forma de pago inválida: " + valor);
    }

    return resultado;
  }
}
