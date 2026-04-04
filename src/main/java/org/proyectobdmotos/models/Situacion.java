package org.proyectobdmotos.models;

public enum Situacion {
  DISPONIBLE("disponible"),
  ALQUILADA("alquilada"),
  TALLER("taller");

  private final String valor;

  Situacion(String valor) {
    this.valor = valor;
  }

  public String getValor() {
    return valor;
  }

  public static Situacion fromValor(String valor) {
    for (Situacion s : values()) {
      if (s.valor.equalsIgnoreCase(valor)) return s;
    }
    throw new IllegalArgumentException("Situación inválida: " + valor);
  }
}
