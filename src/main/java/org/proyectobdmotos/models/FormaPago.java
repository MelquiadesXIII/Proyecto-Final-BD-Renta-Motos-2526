package org.proyectobdmotos.models;

public enum FormaPago {
  EFECTIVO("efectivo"),
  CHEQUE("cheque"),
  CREDITO("credito");

  private final String valor;

  FormaPago(String valor) {
    this.valor = valor;
  }

  public String getValor() {
    return valor;
  }

  public static FormaPago fromValor(String valor) {
    for (FormaPago fp : values()) {
      if (fp.valor.equalsIgnoreCase(valor)) return fp;
    }
    throw new IllegalArgumentException("Forma de pago inválida: " + valor);
  }
}
