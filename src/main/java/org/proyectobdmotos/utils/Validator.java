package org.proyectobdmotos.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.proyectobdmotos.database.DatabaseConnection;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;

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
     * Valida carnet de identidad cubano (11 dígitos).
     *
     * <p>Estructura del CI:</p>
     * <ul>
     *   <li>Dígitos 1-2: año de nacimiento (últimos dos dígitos)</li>
     *   <li>Dígitos 3-4: mes de nacimiento (01-12)</li>
     *   <li>Dígitos 5-6: día de nacimiento (01-31)</li>
     *   <li>Dígito 7: siglo (9=XIX, 0-5=XX, 6-8=XXI)</li>
     *   <li>Dígitos 8-9: número secuencial (00-99)</li>
     *   <li>Dígito 10: sexo (par=varón, impar=hembra)</li>
     *   <li>Dígito 11: dígito de control</li>
     * </ul>
     */
    public static boolean validateCI(String ci) {
        if (ci == null || !ci.matches("^\\d{11}$")) {
            throw new ValidationException(
                BusinessErrorCode.FORMATO_INVALIDO,
                "Carnet de identidad inválido: debe tener exactamente 11 dígitos."
                + " Recibido: \"" + ci + "\""
            );
        }

        int year2digits   = Integer.parseInt(ci.substring(0, 2));
        int month         = Integer.parseInt(ci.substring(2, 4));
        int day           = Integer.parseInt(ci.substring(4, 6));
        int centuryDigit  = Integer.parseInt(ci.substring(6, 7));

        // Siglo
        int fullYear;
        if (centuryDigit == 9) {
            fullYear = 1800 + year2digits;
        } else if (centuryDigit <= 5) {      // 0-5 → 1900s
            fullYear = 1900 + year2digits;
        } else {                             // 6-8 → 2000s
            fullYear = 2000 + year2digits;
        }

        // Si el año calculado supera el año actual es imposible (nadie nace en el futuro).
        // Ocurre cuando los dos primeros dígitos son grandes (ej. 96) y el dígito de siglo
        // apunta al siglo siguiente. Se retrocede 100 años para obtener el año correcto.
        if (fullYear > LocalDate.now().getYear()) {
            fullYear -= 100;
        }

        // Fecha
        try {
            LocalDate.of(fullYear, month, day);
        } catch (DateTimeException e) {
            throw new ValidationException(
                BusinessErrorCode.FORMATO_INVALIDO,
                "Carnet de identidad inválido: fecha de nacimiento inválida ("
                + fullYear + "-" + String.format("%02d", month)
                + "-" + String.format("%02d", day)
                + "). Recibido: \"" + ci + "\""
            );
        }

        return true;
    }

    /**
     * Valida que el dígito de sexo del CI (posición 10, índice 9) coincida con el sexo seleccionado.
     * Dígito par → Masculino; dígito impar → Femenino.
     * Debe llamarse solo con un CI que ya pasó {@link #validateCI(String)}.
     */
    public static void validateCISexo(String ci, String sexoSeleccionado) {
        int sexDigit = Integer.parseInt(ci.substring(9, 10));
        boolean ciEsMasculino = sexDigit % 2 == 0;
        boolean seleccionadoEsMasculino = "Masculino".equalsIgnoreCase(sexoSeleccionado);
        if (ciEsMasculino != seleccionadoEsMasculino) {
            String sexoCi = ciEsMasculino ? "Masculino" : "Femenino";
            throw new ValidationException(
                BusinessErrorCode.FORMATO_INVALIDO,
                "El dígito de sexo del carnet (posición 10: " + sexDigit + ") indica \""
                + sexoCi + "\", pero se seleccionó \"" + sexoSeleccionado + "\"."
            );
        }
    }

    /**
     * Calcula la edad en años a partir de un CI cubano válido.
     * Debe llamarse solo con un CI que ya pasó {@link #validateCI(String)}.
     */
    public static int calcularEdadDesdeCI(String ci) {
        int year2digits  = Integer.parseInt(ci.substring(0, 2));
        int month        = Integer.parseInt(ci.substring(2, 4));
        int day          = Integer.parseInt(ci.substring(4, 6));
        int centuryDigit = Integer.parseInt(ci.substring(6, 7));

        int fullYear;
        if (centuryDigit == 9) {
            fullYear = 1800 + year2digits;
        } else if (centuryDigit <= 5) {
            fullYear = 1900 + year2digits;
        } else {
            fullYear = 2000 + year2digits;
        }

        // Misma corrección que en validateCI: si el año es futuro, retroceder 100 años.
        if (fullYear > LocalDate.now().getYear()) {
            fullYear -= 100;
        }

        LocalDate fechaNacimiento = LocalDate.of(fullYear, month, day);
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
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

        try {
            if (connection == null || connection.isClosed()) {
                connection = DatabaseConnection.getInstance();
            }
        } catch (SQLException e) {
            throw new ValidationException(
                BusinessErrorCode.SIN_CONEXION_BD,
                "Validator: no se pudo obtener una conexión a la base de datos: " + e.getMessage(), e);
        }

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
        boolean valid = texto != null && texto.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 -]{2,25}$");
        if (!valid) {
            throw new ValidationException(
                    BusinessErrorCode.TEXTO_INVALIDO,
                    "Texto inválido: debe tener entre 3 y 25 caracteres, solo letras, números, espacios y guiones. Recibido: \"" + texto + "\""
            );
        }
    }
}
