package org.proyectobdmotos.utils;
import java.io.File;

public class Cancion
{
    private String nombre;
    private File file;

    public Cancion(String nombre, File file)
    {
        this.nombre = nombre;
        this.file = file;
    }
    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public File getFile()
    {
        return file;
    }

    public void setFile(File file)
    {
        this.file = file;
    }
}
