package org.proyectobdmotos.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dao.IClienteDAO;
import org.proyectobdmotos.dto.CliRepDTO;
import org.proyectobdmotos.dto.ClienteDTO;
import org.proyectobdmotos.dto.ClienteUsuarioDTO;
import org.proyectobdmotos.dto.IncumpDTO;
import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.models.Municipio;
import org.proyectobdmotos.models.Sexo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ClienteServiceContractTest extends TestCase {

    public ClienteServiceContractTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(ClienteServiceContractTest.class);
    }

    public void testBuscarPorCiRetornaResultadoDelDAO() {
        FakeClienteDAO clienteDAO = new FakeClienteDAO();
        Cliente cliente = new Cliente(1, "99123112351", "Ana", "Perez", "Lopez", 30, Sexo.FEMENINO, "55512345", 1);
        clienteDAO.clienteBuscado = Optional.of(cliente);
        ClienteService clienteService = new ClienteService(clienteDAO);

        Optional<Cliente> resultado = clienteService.buscarPorCi("99123112351");

        assertEquals(clienteDAO.clienteBuscado, resultado);
        assertEquals("99123112351", clienteDAO.ultimoCiBuscado);
    }

    public void testBuscarPorCiRetornaVacioCuandoNoExisteRegistro() {
        FakeClienteDAO clienteDAO = new FakeClienteDAO();
        ClienteService clienteService = new ClienteService(clienteDAO);

        Optional<Cliente> resultado = clienteService.buscarPorCi("00010160006");

        assertFalse(resultado.isPresent());
        assertEquals("00010160006", clienteDAO.ultimoCiBuscado);
    }

    public void testBuscarPorCiPropagaExcepcionDelDAO() {
        FakeClienteDAO clienteDAO = new FakeClienteDAO();
        clienteDAO.excepcionEnBuscar = new IllegalStateException("fallo dao");
        ClienteService clienteService = new ClienteService(clienteDAO);

        IllegalStateException excepcion = null;

        try {
            clienteService.buscarPorCi("404");
        } catch (IllegalStateException ex) {
            excepcion = ex;
        }

        assertNotNull(excepcion);
        assertEquals("fallo dao", excepcion.getMessage());
    }

    public void testClienteServiceNoExponeBuscarPorIdEnAPI() {
        boolean tieneBuscarPorId = false;
        int indiceMetodo = 0;
        java.lang.reflect.Method[] metodos = ClienteService.class.getDeclaredMethods();

        while (indiceMetodo < metodos.length) {
            java.lang.reflect.Method metodo = metodos[indiceMetodo];
            if ("buscarPorId".equals(metodo.getName())) {
                tieneBuscarPorId = true;
            }
            indiceMetodo++;
        }

        assertTrue(tieneBuscarPorId);
    }

    public void testConsumoLegadoFallaPorAliasRemovidoEnClienteService() {
        boolean faltaBuscarPorId = false;
        boolean tieneBuscarPorCiCanonico = false;

        try {
            ClienteService.class.getMethod("buscarPorId", String.class);
        } catch (NoSuchMethodException ex) {
            faltaBuscarPorId = true;
        }

        try {
            java.lang.reflect.Method metodoCanonico = ClienteService.class.getMethod("buscarPorCi", String.class);
            if (metodoCanonico != null) {
                tieneBuscarPorCiCanonico = true;
            }
        } catch (NoSuchMethodException ex) {
            tieneBuscarPorCiCanonico = false;
        }

        assertTrue(faltaBuscarPorId);
        assertTrue(tieneBuscarPorCiCanonico);
    }

    private static final class FakeClienteDAO implements IClienteDAO {
        private Optional<Cliente> clienteBuscado = Optional.empty();
        private RuntimeException excepcionEnBuscar;
        private String ultimoCiBuscado;

        @Override
        public void insertar(Cliente entity) {
        }

        @Override
        public void actualizar(Cliente entity) {
        }

        @Override
        public void eliminar(Integer id) {
        }

        @Override
        public Optional<Cliente> buscarPorId(Integer id) {
            return Optional.empty();
        }

        @Override
        public Optional<Cliente> buscarPorCi(String ci) {
            Optional<Cliente> resultado = Optional.empty();
            ultimoCiBuscado = ci;
            if (excepcionEnBuscar != null) {
                throw excepcionEnBuscar;
            }
            if (clienteBuscado.isPresent()) {
                resultado = clienteBuscado;
            }
            return resultado;
        }

        @Override
        public List<Cliente> listarTodos() {
            return new ArrayList<>();
        }

        @Override
        public List<ClienteDTO> listarClientesPorMunicipio() {
            return new ArrayList<>();
        }

        @Override
        public List<Cliente> obtenerClientesIncumplidores() {
            return new ArrayList<>();
        }

        @Override
        public void eliminarConCascada(Integer idCliente) {
        }

        @Override
        public List<CliRepDTO> listarClientesReporte() {
            return new ArrayList<>();
        }

        @Override
        public List<IncumpDTO> listarIncumplidores() {
            return new ArrayList<>();
        }

        @Override
        public Optional<Cliente> buscarPorId(int idCliente) {
            return clienteBuscado;
        }

        @Override
        public Optional<Cliente> buscarPorIdUsuario(int idUsuario) {
            return Optional.empty();
        }

        @Override
        public List<ClienteUsuarioDTO> listarClientesConUsuario() {
            return new ArrayList<>();
        }

        @Override
        public String obtenerNombreMunicipio(int idMunicipio) {
            return "";
        }

        @Override
        public List<Cliente> buscarClientesPorTexto(String texto) {
            return new ArrayList<>();
        }

        @Override
        public List<Municipio> listarMunicipios() {
            return new ArrayList<>();
        }
    }
}