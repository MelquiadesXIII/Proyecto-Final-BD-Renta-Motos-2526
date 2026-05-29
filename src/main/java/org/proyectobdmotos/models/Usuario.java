package org.proyectobdmotos.models;


public class Usuario {
    private Integer id_usuario;
    private String nombreUsuario;
    private String password;   
    private String gmail;
    private boolean esAdmin;


    public Usuario(){}
    
    public Usuario(Integer id_usuario, String nombreUsuario, String password, String gmail, boolean esAdmin) {
        this.id_usuario = id_usuario;
        this.nombreUsuario = nombreUsuario;
        this.password = password;
        this.gmail = gmail;
        this.esAdmin = esAdmin;
    }

    public Integer getId() { return id_usuario; }
    public void setId(Integer id) { this.id_usuario = id_usuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getGmail() { return gmail; }
    public void setGmail(String gmail) { this.gmail = gmail; }
    public boolean isEsAdmin() { return esAdmin; }
    public void setEsAdmin(boolean esAdmin) { this.esAdmin = esAdmin; }
}
