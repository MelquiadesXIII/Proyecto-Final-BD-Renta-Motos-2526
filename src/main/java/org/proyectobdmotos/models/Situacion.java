package org.proyectobdmotos.models;

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
    for (Situacion situacion : values()) {
      if (situacion.id == id) return situacion;
    }
    throw new IllegalArgumentException("ID de situación inválido: " + id);
  }

  public static Situacion fromValor(String valor) {
    for (Situacion situacion : values()) {
      if (situacion.valor.equalsIgnoreCase(valor)) return situacion;
    }
    throw new IllegalArgumentException("Situación inválida: " + valor);
  }
}
