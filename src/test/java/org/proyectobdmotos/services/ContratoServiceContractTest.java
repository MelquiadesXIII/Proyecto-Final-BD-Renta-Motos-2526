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
        assertEquals(0, motoDAO.buscarPorIdCount);
    }

    public void testCrearContratoLanzaCodigoMotoNoDisponibleCuandoNoEstaDisponible() {
        FakeContratoDAO contratoDAO = new FakeContratoDAO();
        FakeClienteDAO clienteDAO = new FakeClienteDAO();
        FakeMotoDAO motoDAO = new FakeMotoDAO();
        clienteDAO.cliente = Optional.of(new Cliente("C1", "Ana", "Perez", "Lopez", 30, Sexo.FEMENINO, "555", "M1"));
        motoDAO.moto = Optional.of(crearMoto("M1"));
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
        motoDAO.moto = Optional.of(crearMoto("M1"));
        motoDAO.disponible = true;
        ContratoService contratoService = new ContratoService(contratoDAO, clienteDAO, motoDAO);
        Contrato contrato = crearContrato("C1", "M1");

        contratoService.crearContrato(contrato);

        assertEquals(1, contratoDAO.insertCount);
        assertEquals("C1", contratoDAO.ultimoContratoInsertado.getCiCliente());
    }

    public void testFinalizarContratoLanzaCodigoContratoNoEncontradoSinActualizarNiCambiarEstado() {
        FakeContratoDAO contratoDAO = new FakeContratoDAO();
        FakeClienteDAO clienteDAO = new FakeClienteDAO();
        FakeMotoDAO motoDAO = new FakeMotoDAO();
        ContratoService contratoService = new ContratoService(contratoDAO, clienteDAO, motoDAO);
        Contrato contrato = crearContrato("C1", "M1");

        ValidationException exception = null;
        try {
            contratoService.finalizarContrato(contrato);
        } catch (ValidationException ex) {
            exception = ex;
        }

        assertNotNull(exception);
        assertEquals(BusinessErrorCode.CONTRATO_NO_ENCONTRADO, exception.getErrorCode());
        assertEquals(0, contratoDAO.updateCount);
        assertEquals(0, motoDAO.cambiarEstadoCount);
        assertEquals(0, motoDAO.buscarPorIdCount);
    }

    public void testFinalizarContratoLanzaCodigoContratoYaFinalizadoSinActualizarNiCambiarEstado() {
        FakeContratoDAO contratoDAO = new FakeContratoDAO();
        FakeClienteDAO clienteDAO = new FakeClienteDAO();
        FakeMotoDAO motoDAO = new FakeMotoDAO();
        Contrato contratoPersistido = crearContrato("C1", "M1");
        contratoPersistido.setFechaEntrega(LocalDate.of(2026, 4, 21));
        contratoDAO.contrato = Optional.of(contratoPersistido);
        ContratoService contratoService = new ContratoService(contratoDAO, clienteDAO, motoDAO);
        Contrato contrato = crearContrato("C1", "M1");

        ValidationException exception = null;
        try {
            contratoService.finalizarContrato(contrato);
        } catch (ValidationException ex) {
            exception = ex;
        }

        assertNotNull(exception);
        assertEquals(BusinessErrorCode.CONTRATO_YA_FINALIZADO, exception.getErrorCode());
        assertEquals(0, contratoDAO.updateCount);
        assertEquals(0, motoDAO.cambiarEstadoCount);
        assertEquals(0, motoDAO.buscarPorIdCount);
    }

    public void testFinalizarContratoLanzaCodigoMotoNoEncontradaSinActualizarNiCambiarEstado() {
        FakeContratoDAO contratoDAO = new FakeContratoDAO();
        FakeClienteDAO clienteDAO = new FakeClienteDAO();
        FakeMotoDAO motoDAO = new FakeMotoDAO();
        contratoDAO.contrato = Optional.of(crearContrato("C1", "M1"));
        motoDAO.moto = Optional.empty();
        ContratoService contratoService = new ContratoService(contratoDAO, clienteDAO, motoDAO);
        Contrato contrato = crearContrato("C1", "M1");

        ValidationException exception = null;
        try {
            contratoService.finalizarContrato(contrato);
        } catch (ValidationException ex) {
            exception = ex;
        }

        assertNotNull(exception);
        assertEquals(BusinessErrorCode.MOTO_NO_ENCONTRADA, exception.getErrorCode());
        assertEquals(0, contratoDAO.updateCount);
        assertEquals(0, motoDAO.cambiarEstadoCount);
    }

    public void testActualizarContratoLanzaCodigoContratoNoEncontradoSinActualizar() {
        FakeContratoDAO contratoDAO = new FakeContratoDAO();
        FakeClienteDAO clienteDAO = new FakeClienteDAO();
        FakeMotoDAO motoDAO = new FakeMotoDAO();
        ContratoService contratoService = new ContratoService(contratoDAO, clienteDAO, motoDAO);
        Contrato contrato = crearContrato("C1", "M1");

        ValidationException exception = null;
        try {
            contratoService.actualizarContrato(contrato);
        } catch (ValidationException ex) {
            exception = ex;
        }

        assertNotNull(exception);
        assertEquals(BusinessErrorCode.CONTRATO_NO_ENCONTRADO, exception.getErrorCode());
        assertEquals(0, contratoDAO.updateCount);
    }

    public void testEliminarContratoLanzaCodigoContratoNoEncontradoSinEliminar() {
        FakeContratoDAO contratoDAO = new FakeContratoDAO();
        FakeClienteDAO clienteDAO = new FakeClienteDAO();
        FakeMotoDAO motoDAO = new FakeMotoDAO();
        ContratoService contratoService = new ContratoService(contratoDAO, clienteDAO, motoDAO);
        ContratoID id = new ContratoID(LocalDate.of(2026, 4, 19), "M1");

        ValidationException exception = null;
        try {
            contratoService.eliminarContrato(id);
        } catch (ValidationException ex) {
            exception = ex;
        }

        assertNotNull(exception);
        assertEquals(BusinessErrorCode.CONTRATO_NO_ENCONTRADO, exception.getErrorCode());
        assertEquals(0, contratoDAO.deleteCount);
    }

    private Moto crearMoto(String matricula) {
        return new Moto(matricula, "MDL1", Situacion.DISPONIBLE, 0.0, "BLANCO");
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
        private int updateCount;
        private int deleteCount;
        private Optional<Contrato> contrato = Optional.empty();
        private Contrato ultimoContratoInsertado;

        @Override
        public void insertar(Contrato entity) {
            insertCount = insertCount + 1;
            ultimoContratoInsertado = entity;
        }

        @Override
        public void actualizar(Contrato entity) {
            updateCount = updateCount + 1;
        }

        @Override
        public void eliminar(ContratoID id) {
            deleteCount = deleteCount + 1;
        }

        @Override
        public Optional<Contrato> buscarPorId(ContratoID id) {
            return contrato;
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
        private int buscarPorIdCount;
        private int cambiarEstadoCount;
        private Optional<Moto> moto = Optional.empty();

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
            buscarPorIdCount = buscarPorIdCount + 1;
            return moto;
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
            cambiarEstadoCount = cambiarEstadoCount + 1;
        }

        @Override
        public boolean estaDisponible(String matricula) {
            return disponible;
        }
    }
}
