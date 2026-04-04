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
    Sexo sex = null;
    int i = 0;
    Sexo[] valores = values();
    while (i < valores.length && sex == null) {
        if (valores[i].valor.equalsIgnoreCase(valor))
            sex = valores[i];
      i++;
    }

    if (sex == null) throw new IllegalArgumentException(
      "Sexo inválido: " + valor
    );
    return sex;
  }
}
