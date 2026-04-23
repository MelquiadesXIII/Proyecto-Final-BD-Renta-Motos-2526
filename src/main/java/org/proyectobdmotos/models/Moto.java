package org.proyectobdmotos.models;

@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")

public class Moto {

  private Integer idMoto;
  private String matriculaMoto;
  private Integer idModelo;
  private Situacion situacion;
  private double cantKmRecorridos;
  private Integer idColor;

  public Moto(Integer idMoto, String matriculaMoto, Integer idModelo, Situacion situacion,
          double cantKmRecorridos, Integer idColor) {
    setIdMoto(idMoto);
    setMatriculaMoto(matriculaMoto);
    setIdModelo(idModelo);
    setSituacion(situacion);
    setCantKmRecorridos(cantKmRecorridos);
    setIdColor(idColor);
  }

  public Integer getIdMoto() {
    return idMoto;
  }

  public void setIdMoto(Integer idMoto) {
    this.idMoto = idMoto;
  }

  public String getMatriculaMoto() {
    return matriculaMoto;
  }

  public void setMatriculaMoto(String matriculaMoto) {
    this.matriculaMoto = matriculaMoto;
  }

  public Integer getIdModelo() {
    return idModelo;
  }

  public void setIdModelo(Integer idModelo) {
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

  public Integer getIdColor() {
    return idColor;
  }

  public void setIdColor(Integer idColor) {
    this.idColor = idColor;
  }
}
