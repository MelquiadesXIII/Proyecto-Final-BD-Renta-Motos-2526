package org.proyectobdmotos.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.dao.IMotoDAO;
import org.proyectobdmotos.dto.MotoDTO;
import org.proyectobdmotos.dto.SituacionMotoDTO;
import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.models.Situacion;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MotoServiceContractTest extends TestCase {

    public MotoServiceContractTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(MotoServiceContractTest.class);
    }

    public void testBuscarPorMatriculaRetornaResultadoDelDAO() {
        FakeMotoDAO motoDAO = new FakeMotoDAO();
        Moto moto = new Moto(1, "ABC123", "M1", Situacion.DISPONIBLE, 123.0, "C1");
        motoDAO.motoBuscada = Optional.of(moto);
        MotoService motoService = new MotoService(motoDAO);

        Optional<Moto> resultado = motoService.buscarPorMatricula("ABC123");

        assertEquals(motoDAO.motoBuscada, resultado);
        assertEquals("ABC123", motoDAO.ultimaMatriculaBuscada);
    }

    public void testBuscarPorMatriculaRetornaVacioCuandoNoExisteRegistro() {
        FakeMotoDAO motoDAO = new FakeMotoDAO();
        MotoService motoService = new MotoService(motoDAO);

        Optional<Moto> resultado = motoService.buscarPorMatricula("NO-EXISTE");

        assertFalse(resultado.isPresent());
        assertEquals("NO-EXISTE", motoDAO.ultimaMatriculaBuscada);
    }

    public void testListarTodosRetornaColeccionDelDAO() {
        FakeMotoDAO motoDAO = new FakeMotoDAO();
        List<Moto> motos = new ArrayList<Moto>();
        motos.add(new Moto(1, "ABC123", "M1", Situacion.DISPONIBLE, 123.0, "C1"));
        motos.add(new Moto(2, "XYZ999", "M2", Situacion.ALQUILADA, 321.0, "C2"));
        motoDAO.listaMotos = motos;
        MotoService motoService = new MotoService(motoDAO);

        List<Moto> resultado = motoService.listarTodos();

        assertEquals(motos, resultado);
        assertEquals(2, resultado.size());
    }

    public void testBuscarPorMatriculaPropagaExcepcionDelDAO() {
        FakeMotoDAO motoDAO = new FakeMotoDAO();
        motoDAO.excepcionEnBuscar = new IllegalStateException("error buscar moto");
        MotoService motoService = new MotoService(motoDAO);

        IllegalStateException excepcion = null;

        try {
            motoService.buscarPorMatricula("XX");
        } catch (IllegalStateException ex) {
            excepcion = ex;
        }

        assertNotNull(excepcion);
        assertEquals("error buscar moto", excepcion.getMessage());
    }

    public void testMotoServiceNoExponeMetodosDeprecatedEnAPI() {
        boolean tieneBuscarPorId = false;
        boolean tieneListarTodas = false;
        int indiceMetodo = 0;
        java.lang.reflect.Method[] metodos = MotoService.class.getDeclaredMethods();

        while (indiceMetodo < metodos.length) {
            java.lang.reflect.Method metodo = metodos[indiceMetodo];
            if ("buscarPorId".equals(metodo.getName())) {
                tieneBuscarPorId = true;
            }
            if ("listarTodas".equals(metodo.getName())) {
                tieneListarTodas = true;
            }
            indiceMetodo++;
        }

        assertFalse(tieneBuscarPorId);
        assertFalse(tieneListarTodas);
    }

    public void testConsumoLegadoFallaPorAliasRemovidosEnMotoService() {
        boolean faltaBuscarPorId = false;
        boolean faltaListarTodas = false;
        boolean tieneBuscarPorMatriculaCanonico = false;

        try {
            MotoService.class.getMethod("buscarPorId", String.class);
        } catch (NoSuchMethodException ex) {
            faltaBuscarPorId = true;
        }

        try {
            MotoService.class.getMethod("listarTodas");
        } catch (NoSuchMethodException ex) {
            faltaListarTodas = true;
        }

        try {
            java.lang.reflect.Method metodoCanonico = MotoService.class.getMethod("buscarPorMatricula", String.class);
            if (metodoCanonico != null) {
                tieneBuscarPorMatriculaCanonico = true;
            }
        } catch (NoSuchMethodException ex) {
            tieneBuscarPorMatriculaCanonico = false;
        }

        assertTrue(faltaBuscarPorId);
        assertTrue(faltaListarTodas);
        assertTrue(tieneBuscarPorMatriculaCanonico);
    }

    private static final class FakeMotoDAO implements IMotoDAO {
        private Optional<Moto> motoBuscada = Optional.empty();
        private List<Moto> listaMotos = new ArrayList<Moto>();
        private RuntimeException excepcionEnBuscar;
        private String ultimaMatriculaBuscada;

        @Override
        public void insertar(Moto entity) {
        }

        @Override
        public void actualizar(Moto entity) {
        }

        @Override
        public void eliminar(Integer id) {
        }

        @Override
        public Optional<Moto> buscarPorId(Integer id) {
            return Optional.empty();
        }

        @Override
        public Optional<Moto> buscarPorMatricula(String matricula) {
            Optional<Moto> resultado = Optional.empty();
            ultimaMatriculaBuscada = matricula;
            if (excepcionEnBuscar != null) {
                throw excepcionEnBuscar;
            }
            if (motoBuscada.isPresent()) {
                resultado = motoBuscada;
            }
            return resultado;
        }

        @Override
        public List<Moto> listarTodos() {
            return listaMotos;
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
        public void cambiarEstado(Integer idMoto, Situacion nuevaSituacion) {
        }

        @Override
        public boolean estaDisponible(Integer idMoto) {
            return false;
        }
    }
}
