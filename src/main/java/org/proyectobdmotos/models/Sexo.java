package org.proyectobdmotos.models;

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
    for (Sexo sexo : values()) {
      if (sexo.id == id) return sexo;
    }
    throw new IllegalArgumentException("ID de sexo inválido: " + id);
  }

  public static Sexo fromValor(String valor) {
    for (Sexo sexo : values()) {
      if (sexo.valor.equalsIgnoreCase(valor)) return sexo;
    }
    throw new IllegalArgumentException("Sexo inválido: " + valor);
  }
}
