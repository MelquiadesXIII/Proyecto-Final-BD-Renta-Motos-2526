package org.proyectobdmotos.utils;



import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import javazoom.jl.player.Player;

public class Reproductor {

    private static Reproductor instancia;
    private int indiceDeAntesDeCambio;
    private Player reproductorMP3;
    private Thread hiloReproduccion;
    private ArrayList<Cancion> canciones;
    private int indiceActual;
    private boolean enReproduccion;
    private int volumenSistema = 15;

    private Reproductor() {
        this.canciones = new ArrayList<Cancion>();
        this.indiceActual = 0;
        this.enReproduccion = true;

        File f = new File("Gracias Por Jugar.mp3");
        Cancion c = new Cancion("final",f);
        canciones.add(c);
    }


    public static Reproductor getInstancia() {
        if (instancia == null) {
            instancia = new Reproductor();
        }
        return instancia;
    }


    public static void reiniciarInstancia() {
        if (instancia != null) {
            instancia.detenerCancion();
        }
        instancia = new Reproductor();
    }


    public static boolean existeInstancia() {
        return instancia != null;
    }


    public void cambiarVolumen(float vol) {
        int porcentaje = (int) (vol * 100);
        if (porcentaje == 0) {
            this.volumenSistema = Math.max(0, Math.min(100, 20));
            ControlVolumen.setVolumenSistema(volumenSistema);
            detenerCancion();
        } else if (porcentaje != 0) {
            if (enReproduccion) {
                this.volumenSistema = Math.max(0, Math.min(100, porcentaje));
                ControlVolumen.setVolumenSistema(volumenSistema);
            } else {
                activarMusica();
                this.volumenSistema = Math.max(0, Math.min(100, porcentaje));
                ControlVolumen.setVolumenSistema(volumenSistema);
            }
        }
    }

    public void subirVolumen() {
        int nuevoVolumen = volumenSistema + 10;
        if (nuevoVolumen > 100) {
            nuevoVolumen = 100;
        }
        cambiarVolumen(nuevoVolumen / 100.0f);
        System.out.println(" Volumen: " + volumenSistema + "%");
    }

    public void bajarVolumen() {
        int nuevoVolumen = volumenSistema - 10;
        if (nuevoVolumen < 0) {
            nuevoVolumen = 0;
        }
        cambiarVolumen(nuevoVolumen / 100.0f);
        System.out.println(" Volumen: " + volumenSistema + "%");
    }

    public void setVolumen(float nuevoVolumen) {
        cambiarVolumen(nuevoVolumen);
    }

    public float getVolumen() {
        return volumenSistema / 100.0f;
    }


    // Getters
    public Player getReproductorMP3() {
        return reproductorMP3;
    }

    public Thread getHiloReproduccion() {
        return hiloReproduccion;
    }

    public ArrayList<Cancion> getCanciones() {
        return canciones;
    }

    public int getIndiceActual() {
        return indiceActual;
    }

    public boolean isEnReproduccion() {
        return enReproduccion;
    }

    public int getIndiceDeAntesDeCambio() {
        return indiceDeAntesDeCambio;
    }

    public void setIndiceDeAntesDeCambio(int indiceDeAntesDeCambio) {
        this.indiceDeAntesDeCambio = indiceDeAntesDeCambio;
    }

    // Setters
    public void setReproductorMP3(Player reproductorMP3) {
        this.reproductorMP3 = reproductorMP3;
    }

    public void setHiloReproduccion(Thread hiloReproduccion) {
        this.hiloReproduccion = hiloReproduccion;
    }

    public void setCanciones(ArrayList<Cancion> canciones) {
        this.canciones = canciones;
    }

    public void setIndiceActual(int indiceActual) {
        this.indiceActual = indiceActual;
    }

    public void setEnReproduccion(boolean enReproduccion) {
        this.enReproduccion = enReproduccion;
    }

    // Metodos

    public void iniciarMusica() {
        if (enReproduccion) {
            try {
                // Detener hilo anterior si existe
                if (hiloReproduccion != null && hiloReproduccion.isAlive()) {
                    hiloReproduccion.interrupt();
                }

                hiloReproduccion = new Thread(() -> {
                    try {
                        while (enReproduccion && !Thread.currentThread().isInterrupted()) {
                            Cancion c = canciones.get(indiceActual);
                            FileInputStream stream = new FileInputStream(c.getFile());


                            reproductorMP3 = new Player(stream);
                            reproductorMP3.play();
                            stream.close();

                            // Pequeña pausa antes de repetir
                            if (enReproduccion) {
                                Thread.sleep(500);
                            }
                        }
                    } catch (Exception e) {
                        if (enReproduccion) {
                            System.err.println("Error reproduciendo: " + e.getMessage());
                        }
                    }
                });
                hiloReproduccion.start();
                System.out.println("Música iniciada");

            } catch (Exception e) {
                System.err.println("Error iniciando música: " + e.getMessage());
            }
        }
    }


    // Se cambia a la cancion que se quiera en especifico por indice
    public void cambiarMusicaIndice(int i) {
        try {
            // Detener el reproductor
            if (reproductorMP3 != null) {
                reproductorMP3.close();
            }

            if (enReproduccion) {
                indiceActual = i;

                // Reiniciar la reproduccion
                if (hiloReproduccion != null && hiloReproduccion.isAlive()) {
                    hiloReproduccion.interrupt();
                }

                iniciarMusica();
            }
        } catch (Exception e) {
            System.err.println("Error cambiando música: " + e.getMessage());
        }
    }


    public void cambiarMusicaNombre(String nombre) {
        try {
            indiceDeAntesDeCambio = indiceActual;
            boolean encontrado = false;
            for (int i = 0; i < canciones.size() && !encontrado; i++) {
                if (canciones.get(i).getNombre().equals(nombre)) {
                    encontrado = true;
                    indiceActual = i;
                }
            }

            if (encontrado) {
                cambiarMusicaIndice(indiceActual);
            } else {
                throw new IllegalArgumentException("La canción '" + nombre + "' no fue encontrada");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    public void cambiarMusicaSiguiente() {
        indiceDeAntesDeCambio = indiceActual;
        if (indiceActual == canciones.size() - 1) {
            cambiarMusicaIndice(0);
        }
        cambiarMusicaIndice(indiceActual + 1);
    }

    public synchronized void detenerCancion() {
        if (!enReproduccion) {
            return;
        }

        enReproduccion = false;

        // Dar tiempo al hilo para que termine limpiamente
        if (hiloReproduccion != null && hiloReproduccion.isAlive()) {
            hiloReproduccion.interrupt();
            try {
                hiloReproduccion.join(1000); // Esperar máximo 1 segundo
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (reproductorMP3 != null) {
            try {
                reproductorMP3.close();
            } catch (Exception e) {

            } finally {
                reproductorMP3 = null;
            }
        }

        hiloReproduccion = null;
    }

    public void activarMusica() {
        enReproduccion = true;
        iniciarMusica();
    }

    public void desactivarMusica() {
        reproductorMP3.close();
        enReproduccion = false;
    }

    public void agregarReproucirCancionDelFinal()
    {
        File f = new File("Musica/Canciones/GraciasPorJugar.mp3");
        Cancion c = new Cancion("Creditos", f);
        canciones.add(c);
        cambiarMusicaNombre("Creditos");
        activarMusica();
    }


    // prueba para ver si funciona: Lo dejare para futuras comprobaciones.
    public void diagnosticar() {
        System.out.println("=== DIAGNÓSTICO REPRODUCTOR ===");
        System.out.println(" Canciones cargadas: " + canciones.size());
        System.out.println(" En reproducción: " + enReproduccion);
        System.out.println(" Hilo activo: " + (hiloReproduccion != null && hiloReproduccion.isAlive()));

        if (!canciones.isEmpty()) {
            File archivo = canciones.get(0).getFile();
            System.out.println("Archivo: " + archivo.getAbsolutePath());
            System.out.println("Existe: " + archivo.exists());
            System.out.println(" Tamaño: " + archivo.length() + " bytes");
        }

        // Probar reproducción simple
        System.out.println(" Probando reproducción directa...");
        probarReproduccionDirecta();
    }


    private void probarReproduccionDirecta() {
        try {
            if (!canciones.isEmpty()) {
                FileInputStream stream = new FileInputStream(canciones.get(0).getFile());
                Player playerTest = new Player(stream);

                new Thread(() -> {
                    try {
                        System.out.println(" INICIANDO PRUEBA DE SONIDO...");
                        playerTest.play();
                        System.out.println("PRUEBA COMPLETADA");
                    } catch (Exception e) {
                        System.err.println("Error en prueba: " + e.getMessage());
                    }
                }).start();

                // Detiene la musica  después de 3 segundos
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        playerTest.close();
                        System.out.println("Prueba detenida");
                    } catch (Exception e) {}
                }).start();
            }
        } catch (Exception e) {
            System.err.println(" No se pudo realizar la prueba: " + e.getMessage());
        }
    }

    public void musicaTension()
    {
        indiceDeAntesDeCambio = indiceActual;
        cambiarMusicaNombre("Tension");
    }

    public void musicaDeBusqueda()
    {
        indiceDeAntesDeCambio = indiceActual;
        cambiarMusicaNombre("Sombras en el Viento");
    }
    public void cambiarMusicaAnterior()
    {
        cambiarMusicaIndice(indiceDeAntesDeCambio);
    }

    public String nombreDeLaCancionActual()
    {
        return canciones.get(indiceActual).getNombre();
    }

    public void cambiarMusicaAnterioPorIndice() {
        indiceDeAntesDeCambio = indiceActual;
        if (indiceActual == 0 ) {
            cambiarMusicaIndice(canciones.size()-1);
        }
        cambiarMusicaIndice(indiceActual - 1);
    }
}