package org.proyectobdmotos.services.exceptions;

public class BusinessException extends RuntimeException {

    private final BusinessErrorCode errorCode;

    public BusinessException(BusinessErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessErrorCode getErrorCode() {
        return errorCode;
    }
}
