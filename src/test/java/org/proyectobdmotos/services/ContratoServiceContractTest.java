package org.proyectobdmotos.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dao.IClienteDAO;
import org.proyectobdmotos.dao.IContratoDAO;
import org.proyectobdmotos.dao.IMotoDAO;
import org.proyectobdmotos.dto.ClienteDTO;
import org.proyectobdmotos.dto.MotoDTO;
import org.proyectobdmotos.dto.SituacionMotoDTO;
import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.models.ContratoID;
import org.proyectobdmotos.models.FormaPago;
import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.models.Sexo;
import org.proyectobdmotos.models.Situacion;
import org.proyectobdmotos.services.exceptions.BusinessErrorCode;
import org.proyectobdmotos.services.exceptions.ValidationException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ContratoServiceContractTest extends TestCase {

    public ContratoServiceContractTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(ContratoServiceContractTest.class);
    }

    public void testCrearContratoLanzaCodigoClienteNoEncontradoCuandoNoExiste() {
        FakeContratoDAO contratoDAO = new FakeContratoDAO();
        FakeClienteDAO clienteDAO = new FakeClienteDAO();
        FakeMotoDAO motoDAO = new FakeMotoDAO();
        clienteDAO.cliente = Optional.empty();
        ContratoService contratoService = new ContratoService(contratoDAO, clienteDAO, motoDAO);
        Contrato contrato = crearContrato("C1", "M1");

        ValidationException exception = null;
        try {
            contratoService.crearContrato(contrato);
        } catch (ValidationException ex) {
            exception = ex;
        }

        assertNotNull(exception);
        assertEquals(BusinessErrorCode.CLIENTE_NO_ENCONTRADO, exception.getErrorCode());
        assertEquals(0, contratoDAO.insertCount);
    }

    public void testCrearContratoLanzaCodigoMotoNoDisponibleCuandoNoEstaDisponible() {
        FakeContratoDAO contratoDAO = new FakeContratoDAO();
        FakeClienteDAO clienteDAO = new FakeClienteDAO();
        FakeMotoDAO motoDAO = new FakeMotoDAO();
        clienteDAO.cliente = Optional.of(new Cliente("C1", "Ana", "Perez", "Lopez", 30, Sexo.FEMENINO, "555", "M1"));
        motoDAO.disponible = false;
        ContratoService contratoService = new ContratoService(contratoDAO, clienteDAO, motoDAO);
        Contrato contrato = crearContrato("C1", "M1");

        ValidationException exception = null;
        try {
            contratoService.crearContrato(contrato);
        } catch (ValidationException ex) {
            exception = ex;
        }

        assertNotNull(exception);
        assertEquals(BusinessErrorCode.MOTO_NO_DISPONIBLE, exception.getErrorCode());
        assertEquals(0, contratoDAO.insertCount);
    }

    public void testCrearContratoValidoInsertaContrato() {
        FakeContratoDAO contratoDAO = new FakeContratoDAO();
        FakeClienteDAO clienteDAO = new FakeClienteDAO();
        FakeMotoDAO motoDAO = new FakeMotoDAO();
        clienteDAO.cliente = Optional.of(new Cliente("C1", "Ana", "Perez", "Lopez", 30, Sexo.FEMENINO, "555", "M1"));
        motoDAO.disponible = true;
        ContratoService contratoService = new ContratoService(contratoDAO, clienteDAO, motoDAO);
        Contrato contrato = crearContrato("C1", "M1");

        contratoService.crearContrato(contrato);

        assertEquals(1, contratoDAO.insertCount);
        assertEquals("C1", contratoDAO.ultimoContratoInsertado.getCiCliente());
    }

    private Contrato crearContrato(String ciCliente, String matricula) {
        return new Contrato(
            0.0,
            0.0,
            ciCliente,
            0,
            null,
            LocalDate.of(2026, 4, 20),
            LocalDate.of(2026, 4, 19),
            FormaPago.EFECTIVO,
            matricula,
            false
        );
    }

    private static final class FakeContratoDAO implements IContratoDAO {
        private int insertCount;
        private Contrato ultimoContratoInsertado;

        @Override
        public void insertar(Contrato entity) {
            insertCount = insertCount + 1;
            ultimoContratoInsertado = entity;
        }

        @Override
        public void actualizar(Contrato entity) {
        }

        @Override
        public void eliminar(ContratoID id) {
        }

        @Override
        public Optional<Contrato> buscarPorId(ContratoID id) {
            return Optional.empty();
        }

        @Override
        public List<Contrato> listarTodos() {
            return new ArrayList<Contrato>();
        }

        @Override
        public List<Contrato> listarContratosCompletos() {
            return new ArrayList<Contrato>();
        }
    }

    private static final class FakeClienteDAO implements IClienteDAO {
        private Optional<Cliente> cliente = Optional.empty();

        @Override
        public void insertar(Cliente entity) {
        }

        @Override
        public void actualizar(Cliente entity) {
        }

        @Override
        public void eliminar(String id) {
        }

        @Override
        public Optional<Cliente> buscarPorId(String id) {
            return cliente;
        }

        @Override
        public List<Cliente> listarTodos() {
            return new ArrayList<Cliente>();
        }

        @Override
        public List<ClienteDTO> listarClientesPorMunicipio() {
            return new ArrayList<ClienteDTO>();
        }

        @Override
        public List<Cliente> obtenerClientesIncumplidores() {
            return new ArrayList<Cliente>();
        }

        @Override
        public void eliminarConCascada(String ci) {
        }
    }

    private static final class FakeMotoDAO implements IMotoDAO {
        private boolean disponible;

        @Override
        public void insertar(Moto entity) {
        }

        @Override
        public void actualizar(Moto entity) {
        }

        @Override
        public void eliminar(String id) {
        }

        @Override
        public Optional<Moto> buscarPorId(String id) {
            return Optional.empty();
        }

        @Override
        public List<Moto> listarTodos() {
            return new ArrayList<Moto>();
        }

        @Override
        public List<MotoDTO> listarMotosConKilometraje() {
            return new ArrayList<MotoDTO>();
        }

        @Override
        public List<SituacionMotoDTO> listarSituacionMotos() {
            return new ArrayList<SituacionMotoDTO>();
        }

        @Override
        public void cambiarEstado(String matricula, Situacion nuevaSituacion) {
        }

        @Override
        public boolean estaDisponible(String matricula) {
            return disponible;
        }
    }
}
