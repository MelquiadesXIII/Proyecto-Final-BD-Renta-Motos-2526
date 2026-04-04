package org.proyectobdmotos.models;

@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")

public class Moto {

  private String matriculaMoto;
  private String idModelo;
  private Situacion situacion;
  private double cantKmRecorridos;
  private String idColor;

  public Moto(String matriculaMoto, String idModelo, Situacion situacion, double cantKmRecorridos, String idColor) {
    setCantKmRecorridos(cantKmRecorridos);
    setIdColor(idColor);
    setIdModelo(idModelo);
    setMatriculaMoto(matriculaMoto);
    setSituacion(situacion);
  }

  public String getMatriculaMoto() {
    return matriculaMoto;
  }

  public void setMatriculaMoto(String matriculaMoto) {
    this.matriculaMoto = matriculaMoto;
  }

  public String getIdModelo() {
    return idModelo;
  }

  public void setIdModelo(String idModelo) {
    if (idModelo == null) throw new IllegalArgumentException(
      "El id no puede ser null"
    );

    this.idModelo = idModelo;
  }

  public Situacion getSituacion() {
    return situacion;
  }

  public void setSituacion(Situacion situacion) {
    this.situacion = situacion;
  }

  public double getCantKmRecorridos() {
    return cantKmRecorridos;
  }

  public void setCantKmRecorridos(double cantKmRecorridos) {
    this.cantKmRecorridos = cantKmRecorridos;
  }

  public String getIdColor() {
    return idColor;
  }

  public void setIdColor(String idColor) {
    this.idColor = idColor;
  }
}
