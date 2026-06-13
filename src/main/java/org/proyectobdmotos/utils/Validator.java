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
        boolean valid = false;

        if (nonNull(ci) && ci.matches("^\\d{11}$")) {
            boolean dateValid = false;
            boolean centuryValid = false;
            boolean sexValid = false;
            boolean controlDigitValid = false;

            int year2digits = Integer.parseInt(ci.substring(0, 2));
            int month = Integer.parseInt(ci.substring(2, 4));
            int day = Integer.parseInt(ci.substring(4, 6));
            int centuryDigit = Integer.parseInt(ci.substring(6, 7));
            int sexDigit = Integer.parseInt(ci.substring(9, 10));
            int controlDigit = Integer.parseInt(ci.substring(10, 11));

            int fullYear = 0;
            if (centuryDigit == 9) {
                fullYear = 1800 + year2digits;
                centuryValid = true;
            } else if (centuryDigit >= 0 && centuryDigit <= 5) {
                fullYear = 1900 + year2digits;
                centuryValid = true;
            } else if (centuryDigit >= 6 && centuryDigit <= 8) {
                fullYear = 2000 + year2digits;
                centuryValid = true;
            }

            if (centuryValid) {
                try {
                    java.time.LocalDate date = java.time.LocalDate.of(fullYear, month, day);
                    dateValid = true;
                } catch (java.time.DateTimeException e) {
                    dateValid = false;
                }
            }

            sexValid = true;

            int expectedControlDigit = calculateCIControlDigit(ci);
            controlDigitValid = (controlDigit == expectedControlDigit);

            valid = centuryValid && dateValid && sexValid && controlDigitValid;

            if (!valid) {
                StringBuilder errorMsg = new StringBuilder("Carnet de identidad inválido:");

                if (!centuryValid) {
                    errorMsg.append(" dígito de siglo inválido (").append(centuryDigit).append(").");
                }
                if (!dateValid) {
                    errorMsg.append(" fecha de nacimiento inválida (")
                            .append(fullYear).append("-")
                            .append(String.format("%02d", month)).append("-")
                            .append(String.format("%02d", day)).append(").");
                }
                if (!controlDigitValid) {
                    errorMsg.append(" dígito de control inválido (esperado ")
                            .append(expectedControlDigit).append(", recibido ").append(controlDigit).append(").");
                }
                errorMsg.append(" Recibido: \"").append(ci).append("\"");

                throw new ValidationException(
                    BusinessErrorCode.FORMATO_INVALIDO,
                    errorMsg.toString()
                );
            }
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
     * Calcula el dígito de control del carnet de identidad cubano.
     *
     * <p>Algoritmo: se multiplican los 10 primeros dígitos por pesos cíclicos
     * [2, 3, 4, 5, 6, 7, 8, 9, 2, 3], se suman los productos, y se calcula
     * {@code 11 - (suma % 11)}. Si el resultado es 10 o 11, el dígito es 0.</p>
     *
     * @param ci los 11 dígitos del carnet de identidad
     * @return el dígito de control esperado (0-9)
     */
    private static int calculateCIControlDigit(String ci) {
        int[] weights = {2, 3, 4, 5, 6, 7, 8, 9, 2, 3};
        int sum = 0;

        int i = 0;
        boolean shouldCalculate = true;
        while (shouldCalculate && i < 10) {
            int digit = Integer.parseInt(ci.substring(i, i + 1));
            sum += digit * weights[i];
            i++;
            shouldCalculate = (i < 10);
        }

        int remainder = sum % 11;
        int result = 11 - remainder;

        boolean isOverTen = (result == 10 || result == 11);

        return isOverTen ? 0 : result;
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
        boolean valid = texto != null && texto.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 -]{2,25}$");
        if (!valid) {
            throw new ValidationException(
                    BusinessErrorCode.TEXTO_INVALIDO,
                    "Texto inválido: debe tener entre 3 y 25 caracteres, solo letras, números, espacios y guiones. Recibido: \"" + texto + "\""
            );
        }
    }
}
