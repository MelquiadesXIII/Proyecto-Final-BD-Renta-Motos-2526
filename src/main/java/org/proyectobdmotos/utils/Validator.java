package org.proyectobdmotos.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.proyectobdmotos.services.exceptions.BusinessErrorCode;
import org.proyectobdmotos.services.exceptions.ValidationException;

public abstract class Validator {

    private static Connection connection;

    /**
     * Inyecta la conexión a la base de datos utilizada por las validaciones
     * que requieren acceso al almacenamiento (unicidad de campos únicos).
     * Debe llamarse desde AppCompositionRoot justo después de crear la Connection.
     */
    public static void setConnection(Connection conn) {
        connection = conn;
    }

    /**
     * Verifica que el objeto no sea nulo.
     * Lanza {@link IllegalArgumentException} si el valor es nulo.
     */
    public static boolean nonNull(Object o) {
        boolean valid = o != null;
        if (!valid) {
            throw new ValidationException(
                BusinessErrorCode.CAMPO_REQUERIDO,
                "El campo es requerido y no puede ser nulo."
            );
        }
        return valid;
    }

    /**
     * Valida texto: entre 3 y 25 caracteres, solo letras
     * (incluyendo tildes y ñ) y espacios; sin números ni símbolos.
     */
    public static boolean validateText(String text) {
        boolean valid = false;
        if (nonNull(text) && text.length() >= 3 && text.length() <= 25
                && text.matches("[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]+")) {
            valid = true;
        }
        if (!valid) {
            throw new ValidationException(
                BusinessErrorCode.FORMATO_INVALIDO,
                "Texto inválido: debe tener entre 3 y 25 caracteres, sin números ni caracteres especiales."
                + " Recibido: \"" + text + "\"");
        }
        return valid;
    }

    /**
     * Valida edad: entero entre 18 y 99 años.
     */
    public static boolean validateAge(Integer age) {
        boolean valid = false;
        if (nonNull(age) && age >= 18 && age <= 99) {
            valid = true;
        }
        if (!valid) {
            throw new ValidationException(
                BusinessErrorCode.RANGO_INVALIDO,
                "Edad inválida: debe estar entre 18 y 99 años. Recibida: " + age);
        }
        return valid;
    }

    /**
     * Valida número de teléfono cubano: exactamente 8 dígitos comenzando con 5 o 6.
     */
    public static boolean validateTelephoneNumber(String number) {
        boolean valid = false;
        if (nonNull(number) && number.matches("^[56]\\d{7}$")) {
            valid = true;
        }
        if (!valid) {
            throw new ValidationException(
                BusinessErrorCode.FORMATO_INVALIDO,
                "Número de teléfono inválido: debe tener 8 dígitos y comenzar con 5 o 6."
                + " Recibido: \"" + number + "\"");
        }
        return valid;
    }

    /**
     * Valida carnet de identidad. La lógica completa se implementará posteriormente.
     * El carnet de identidad cubano debe tener 11 digitos
     */
    public static boolean validateCI(String ci) {
        boolean valid = false;
        if (nonNull(ci) && ci.matches("^\\d{11}$")) {
            valid = true;
        }
        if (!valid) {
            throw new ValidationException(
                BusinessErrorCode.FORMATO_INVALIDO,
                "Carnet de identidad inválido: debe tener exactamente 11 dígitos."
                + " Recibido: \"" + ci + "\""
            );
        }
        return valid;
    }

    /**
     * Valida que el objeto LocalDate pasado no sea nulo.
     */
    public static boolean validateLocalDate(LocalDate ld) {
        boolean valid = ld != null;
        if (!valid) {
            throw new ValidationException(
                BusinessErrorCode.FECHA_INVALIDA,
                "La fecha no puede ser nula."
            );
        }
        return valid;
    }

    /**
     * Valida que el número sea estrictamente positivo (> 0).
     */
    public static boolean validatePositive(Double number) {
        boolean valid = false;
        if (nonNull(number) && number > 0) {
            valid = true;
        }
        if (!valid) {
            throw new ValidationException(
                BusinessErrorCode.NUMERO_NO_POSITIVO,
                "El número debe ser positivo (mayor que cero). Recibido: " + number);
        }
        return valid;
    }

    /**
     * Valida matrícula: exactamente 6 caracteres alfanuméricos (letras o dígitos).
     */
    public static boolean validatePlate(String plate) {
        boolean valid = false;
        if (nonNull(plate) && plate.matches("^[a-zA-Z0-9]{6}$")) {
            valid = true;
        }
        if (!valid) {
            throw new ValidationException(
                BusinessErrorCode.FORMATO_INVALIDO,
                "Matrícula inválida: debe tener exactamente 6 caracteres alfanuméricos."
                + " Recibida: \"" + plate + "\"");
        }
        return valid;
    }

    /**
     * Verifica que el valor indicado no exista ya en la base de datos.
     *
     * <p>Campos soportados:</p>
     * <ul>
     *   <li>{@code "ci"}        → busca en {@code cliente.ci_cliente}</li>
     *   <li>{@code "matricula"} → busca en {@code moto.matricula_moto}</li>
     * </ul>
     *
     * @param field identificador del campo a consultar ({@code "ci"} o {@code "matricula"})
     * @param value valor a verificar
     * @throws IllegalArgumentException si el valor ya existe en la BD
     * @throws IllegalStateException    si la conexión no fue inicializada
     */
    public static boolean validateUniqueField(String field, String value) {
        boolean isUnique = true;

        if (connection == null) {
            throw new ValidationException(
                BusinessErrorCode.SIN_CONEXION_BD,
                "Validator: la conexión a la base de datos no fue inicializada. "
                + "Llama a Validator.setConnection(conn) desde AppCompositionRoot.");
        }

        String sql = null;
        String fieldLabel = null;

        if ("ci".equalsIgnoreCase(field)) {
            sql = "SELECT COUNT(*) FROM cliente WHERE ci_cliente = ?";
            fieldLabel = "Carnet de identidad";
        } else if ("matricula".equalsIgnoreCase(field)) {
            sql = "SELECT COUNT(*) FROM moto WHERE matricula_moto = ?";
            fieldLabel = "Matrícula";
        }

        boolean fieldRecognized = sql != null;
        if (!fieldRecognized) {
            throw new ValidationException(
                BusinessErrorCode.UNICIDAD_INVALIDA,
                "Campo de unicidad desconocido: \"" + field + "\". "
                + "Valores aceptados: \"ci\", \"matricula\".");
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                boolean hasRow = rs.next();
                if (hasRow && rs.getInt(1) > 0) {
                    isUnique = false;
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al verificar unicidad del campo \"" + field + "\": " + e.getMessage());
            throw new ValidationException(
                BusinessErrorCode.SIN_CONEXION_BD,
                "Error al verificar unicidad del campo \"" + field + "\": " + e.getMessage(),
                e
            );
        }

        if (!isUnique) {
            throw new ValidationException(
                BusinessErrorCode.UNICIDAD_INVALIDA,
                fieldLabel + " \"" + value + "\" ya existe en el sistema.");
        }

        return isUnique;
    }

    // Esto es para los modelos y marcas... que me estan saltando muchos erroes
    // y dolores de cabeza... al final funciono. Amen.
    public static void validateTextWithNumbers(String texto) {
        boolean valid = texto != null && texto.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 -]{3,25}$");
        if (!valid) {
            throw new ValidationException(
                    BusinessErrorCode.TEXTO_INVALIDO,
                    "Texto inválido: debe tener entre 3 y 25 caracteres, solo letras, números, espacios y guiones. Recibido: \"" + texto + "\""
            );
        }
    }
}
