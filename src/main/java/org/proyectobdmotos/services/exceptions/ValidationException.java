package org.proyectobdmotos.services.exceptions;

public class ValidationException extends BusinessException {

    public ValidationException(BusinessErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ValidationException(BusinessErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
