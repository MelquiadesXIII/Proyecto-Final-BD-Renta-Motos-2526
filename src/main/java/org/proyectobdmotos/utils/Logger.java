package org.proyectobdmotos.utils;

/**
 * Logger: clase abstracta estática para registrar mensajes en consola con colores ANSI.
 * No se pueden crear instancias.
 * Los métodos de log detectan automáticamente la clase que los invoca usando StackTrace.
 */
public abstract class Logger {

    // Códigos ANSI para colores
    private static final String RESET = "\033[0m";
    private static final String GREEN = "\033[32m";
    private static final String RED = "\033[31m";
    private static final String ORANGE = "\033[33m"; 
    private static final String CYAN = "\033[36m";

    /**
     * Constructor privado para evitar instanciación.
     */
    private Logger() {
    }

    /**
     * Registra un mensaje de información en verde.
     * Formato: [ClassName] message
     * 
     * @param text Mensaje a registrar
     */
    public static void log(String text) {
        logInfo(text);
    }

    /**
     * Registra un mensaje de información en verde.
     * 
     * @param text Mensaje a registrar
     */
    public static void logInfo(String text) {
        String className = getCallerClassName();
        String formattedMessage = formatMessage(className, text, GREEN);
        System.out.println(formattedMessage);
    }

    /**
     * Registra un mensaje de error en rojo.
     * 
     * @param text Mensaje a registrar
     */
    public static void logError(String text) {
        String className = getCallerClassName();
        String formattedMessage = formatMessage(className, text, RED);
        System.out.println(formattedMessage);
    }

    /**
     * Registra un mensaje de advertencia en naranja.
     * 
     * @param text Mensaje a registrar
     */
    public static void logWarn(String text) {
        String className = getCallerClassName();
        String formattedMessage = formatMessage(className, text, ORANGE);
        System.out.println(formattedMessage);
    }

    /**
     * Obtiene el nombre de la clase que invocó el método de log.
     * Usa StackTraceElement para inspeccionar la pila de llamadas.
     * Busca el primer elemento que NO sea Logger ni clases del sistema Java.
     * 
     * @return Nombre simple de la clase que llamó a log
     */
    private static String getCallerClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        String className = "Unknown";
        String loggerClassName = Logger.class.getName();
        
        boolean found = false;
        int i = 0;
        
        while (!found && i < stackTrace.length) {
            String fullClassName = stackTrace[i].getClassName();
            
            boolean isLogger = fullClassName.equals(loggerClassName);
            boolean isJavaSystem = fullClassName.startsWith("java.");
            boolean isSunSystem = fullClassName.startsWith("sun.");
            boolean shouldSkip = isLogger || isJavaSystem || isSunSystem;
            
            if (!shouldSkip) {
                className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
                found = true;
            }
            
            i++;
        }
        
        return className;
    }

    /**
     * Formatea el mensaje con color ANSI.
     * Formato: [ORANGE[className]RESET] COLOR_text RESET
     * 
     * @param className Nombre de la clase
     * @param text Texto del mensaje
     * @param color Código ANSI del color
     * @return Mensaje formateado con colores
     */
    private static String formatMessage(String className, String text, String color) {
        return CYAN + "[" + className + "] " + RESET + color + text + RESET;
    }
}
