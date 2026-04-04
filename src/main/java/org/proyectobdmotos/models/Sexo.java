package org.proyectobdmotos.models;

public enum Sexo {
  MASCULINO("masculino"),
  FEMENINO("femenino");

  private final String valor;

  Sexo(String valor) {
    this.valor = valor;
  }

  public String getValor() {
    return valor;
  }

  public static Sexo fromValor(String valor) {
    for (Sexo s : values()) {
      if (s.valor.equalsIgnoreCase(valor)) return s;
    }
    throw new IllegalArgumentException("Sexo inválido: " + valor);
  }
}
