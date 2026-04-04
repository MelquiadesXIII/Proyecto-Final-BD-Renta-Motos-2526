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
    FormaPago form = null;
    int i = 0;
    FormaPago[] valores = values();
    while (i < valores.length && form == null) {
        if (valores[i].valor.equalsIgnoreCase(valor))
        form = valores[i];
      i++;
    }

    if (form == null) throw new IllegalArgumentException(
      "Forma de pago inválida: " + valor
    );
    return form;
  }
}
