package org.proyectobdmotos.models;

import java.time.LocalDate;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ContratoIDTest extends TestCase {

    public ContratoIDTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(ContratoIDTest.class);
    }

    public void testEqualsDebeRetornarTrueCuandoEsMismaReferencia() {
        ContratoID contratoId = new ContratoID(LocalDate.of(2026, 4, 20), 101);

        assertTrue(contratoId.equals(contratoId));
    }

    public void testEqualsDebeRetornarFalseCuandoObjetoEsNull() {
        ContratoID contratoId = new ContratoID(LocalDate.of(2026, 4, 20), 101);

        assertFalse(contratoId.equals(null));
    }

    public void testEqualsDebeRetornarFalseCuandoTipoEsDistinto() {
        ContratoID contratoId = new ContratoID(LocalDate.of(2026, 4, 20), 101);

        assertFalse(contratoId.equals("no-es-contrato-id"));
    }

    public void testEqualsDebeCompararSoloCamposDeLlavePrimaria() {
        ContratoID contratoIdA = new ContratoID(LocalDate.of(2026, 4, 20), 101);
        ContratoID contratoIdB = new ContratoID(LocalDate.of(2026, 4, 20), 101);

        assertTrue(contratoIdA.equals(contratoIdB));
        assertTrue(contratoIdB.equals(contratoIdA));
    }

    public void testEqualsDebeSerNullSafeParaCamposDelId() {
        ContratoID contratoIdConNulosA = new ContratoID(null, null);
        ContratoID contratoIdConNulosB = new ContratoID(null, null);
        ContratoID contratoIdConIdMotoDistinto = new ContratoID(null, 202);

        assertTrue(contratoIdConNulosA.equals(contratoIdConNulosB));
        assertFalse(contratoIdConNulosA.equals(contratoIdConIdMotoDistinto));
    }

    public void testHashCodeDebeSerConsistenteConEquals() {
        ContratoID contratoIdA = new ContratoID(LocalDate.of(2026, 4, 20), 101);
        ContratoID contratoIdB = new ContratoID(LocalDate.of(2026, 4, 20), 101);

        assertTrue(contratoIdA.equals(contratoIdB));
        assertEquals(contratoIdA.hashCode(), contratoIdB.hashCode());
    }

    public void testHashCodeDebeSerDeterminista() {
        ContratoID contratoId = new ContratoID(LocalDate.of(2026, 4, 20), 101);
        int primerHash = contratoId.hashCode();
        int segundoHash = contratoId.hashCode();

        assertEquals(primerHash, segundoHash);
    }
}
