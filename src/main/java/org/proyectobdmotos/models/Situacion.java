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
    Situacion situation = null;
    int i = 0;
    Situacion[] valores = values();
    while (i < valores.length && situation == null) {
      if (valores[i].valor.equalsIgnoreCase(valor))
        situation = valores[i];
      i++;
    }

    if (situation == null) throw new IllegalArgumentException(
      "Situación inválida: " + valor
    );
    return situation;
  }
}
