package org.proyectobdmotos.models;

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
    for (FormaPago forma : values()) {
      if (forma.id == id) return forma;
    }
    throw new IllegalArgumentException("ID de forma de pago inválido: " + id);
  }

  public static FormaPago fromValor(String valor) {
    for (FormaPago forma : values()) {
      if (forma.valor.equalsIgnoreCase(valor)) return forma;
    }
    throw new IllegalArgumentException("Forma de pago inválida: " + valor);
  }
}
