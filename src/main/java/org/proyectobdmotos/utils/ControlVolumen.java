package org.proyectobdmotos.utils;

import java.io.IOException;

public class ControlVolumen
{
    public static void setVolumenSistema(int porcentaje) {
        try {
            int volumen = Math.max(0, Math.min(100, porcentaje));

            // Obtener la ruta absoluta del directorio actual del proyecto
            String projectPath = System.getProperty("user.dir");
            String nircmdPath = projectPath + "\\DatosAuxiliaresLogica\\nircmd.exe";
            
            // Comando para cambiar volumen del sistema usando NirCmd
            String command = "\"" + nircmdPath + "\" setsysvolume " + (volumen * 655.35);

            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

            System.out.println("Volumen del sistema cambiado a: " + porcentaje + "%");

        } catch (IOException | InterruptedException e) {
            System.err.println("Error cambiando volumen del sistema: " + e.getMessage());
        }
    }
}
