package org.proyectobdmotos.services;

import org.proyectobdmotos.services.exceptions.BusinessErrorCode;
import org.proyectobdmotos.services.exceptions.BusinessException;
import org.proyectobdmotos.services.exceptions.ValidationException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BusinessExceptionContractTest extends TestCase {

    public BusinessExceptionContractTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(BusinessExceptionContractTest.class);
    }

    public void testBusinessExceptionExponeCodigoYMensajeContractual() {
        BusinessException exception = new BusinessException(
            BusinessErrorCode.CONTRATO_VALIDACION_FALLIDA,
            "Mensaje contractual"
        );

        assertEquals(BusinessErrorCode.CONTRATO_VALIDACION_FALLIDA, exception.getErrorCode());
        assertEquals("Mensaje contractual", exception.getMessage());
    }

    public void testValidationExceptionConservaContratoDeBusinessException() {
        ValidationException exception = new ValidationException(
            BusinessErrorCode.MOTO_NO_DISPONIBLE,
            "Moto sin disponibilidad"
        );

        assertEquals(BusinessErrorCode.MOTO_NO_DISPONIBLE, exception.getErrorCode());
        assertEquals("Moto sin disponibilidad", exception.getMessage());
    }

    public void testBusinessErrorCodeIncluyeCodigosEstablesDeFaseUno() {
        assertEquals(BusinessErrorCode.CLIENTE_NO_ENCONTRADO, BusinessErrorCode.valueOf("CLIENTE_NO_ENCONTRADO"));
        assertEquals(BusinessErrorCode.MOTO_NO_DISPONIBLE, BusinessErrorCode.valueOf("MOTO_NO_DISPONIBLE"));
        assertEquals(
            BusinessErrorCode.CONTRATO_VALIDACION_FALLIDA,
            BusinessErrorCode.valueOf("CONTRATO_VALIDACION_FALLIDA")
        );
    }
}
