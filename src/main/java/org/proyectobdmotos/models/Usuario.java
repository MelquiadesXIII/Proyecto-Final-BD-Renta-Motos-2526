package org.proyectobdmotos.models;


public class Usuario {
    private Integer id;
    private String nombreUsuario;
    private String password;   // texto plano
    private String gmail;
    private boolean esAdmin;

    public Usuario() {}

    public Usuario(Integer id, String nombreUsuario, String password, String gmail, boolean esAdmin) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.password = password;
        this.gmail = gmail;
        this.esAdmin = esAdmin;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getGmail() { return gmail; }
    public void setGmail(String gmail) { this.gmail = gmail; }
    public boolean isEsAdmin() { return esAdmin; }
    public void setEsAdmin(boolean esAdmin) { this.esAdmin = esAdmin; }
}
