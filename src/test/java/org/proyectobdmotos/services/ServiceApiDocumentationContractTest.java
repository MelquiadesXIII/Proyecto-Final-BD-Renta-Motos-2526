package org.proyectobdmotos.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ServiceApiDocumentationContractTest extends TestCase {

    private static final String DOCUMENTO_CONTRATO = "docs/contrato-integracion-ui.md";

    public ServiceApiDocumentationContractTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(ServiceApiDocumentationContractTest.class);
    }

    public void testDocumentoDeclaraFirmasCanonicasVigentes() throws IOException {
        String documento = Files.readString(Path.of(DOCUMENTO_CONTRATO));

        boolean tieneClienteCanonico = documento.contains("Optional<Cliente> buscarPorCi(String ci)");
        boolean tieneMotoCanonico = documento.contains("Optional<Moto> buscarPorMatricula(String matricula)");
        boolean tieneListarClientes = documento.contains("List<Cliente> listarTodos()");
        boolean tieneListarMotos = documento.contains("List<Moto> listarTodos()");
        boolean existeBuscarPorCi = false;
        boolean existeBuscarPorMatricula = false;
        boolean existeListarTodosCliente = false;
        boolean existeListarTodosMoto = false;

        try {
            java.lang.reflect.Method metodoBuscarPorCi = ClienteService.class.getMethod("buscarPorCi", String.class);
            if (metodoBuscarPorCi != null) {
                existeBuscarPorCi = true;
            }
        } catch (NoSuchMethodException ex) {
            existeBuscarPorCi = false;
        }

        try {
            java.lang.reflect.Method metodoBuscarPorMatricula = MotoService.class.getMethod("buscarPorMatricula", String.class);
            if (metodoBuscarPorMatricula != null) {
                existeBuscarPorMatricula = true;
            }
        } catch (NoSuchMethodException ex) {
            existeBuscarPorMatricula = false;
        }

        try {
            java.lang.reflect.Method metodoListarTodosCliente = ClienteService.class.getMethod("listarTodos");
            if (metodoListarTodosCliente != null) {
                existeListarTodosCliente = true;
            }
        } catch (NoSuchMethodException ex) {
            existeListarTodosCliente = false;
        }

        try {
            java.lang.reflect.Method metodoListarTodosMoto = MotoService.class.getMethod("listarTodos");
            if (metodoListarTodosMoto != null) {
                existeListarTodosMoto = true;
            }
        } catch (NoSuchMethodException ex) {
            existeListarTodosMoto = false;
        }

        assertTrue(tieneClienteCanonico);
        assertTrue(tieneMotoCanonico);
        assertTrue(tieneListarClientes);
        assertTrue(tieneListarMotos);
        assertTrue(existeBuscarPorCi);
        assertTrue(existeBuscarPorMatricula);
        assertTrue(existeListarTodosCliente);
        assertTrue(existeListarTodosMoto);
    }

    public void testDocumentoDeclaraAliasesRemovidosYReemplazos() throws IOException {
        String documento = Files.readString(Path.of(DOCUMENTO_CONTRATO));

        boolean declaraPoliticaFreeze = documento.contains(
            "No se permiten cambios de firma pública de `services` sin PR excepcional."
        );
        boolean declaraUsoSoloMetodosCongelados = documento.contains(
            "La UI debe consumir únicamente los métodos listados en este documento."
        );
        boolean noExponeAliasCliente = false;
        boolean noExponeAliasMotoBuscar = false;
        boolean noExponeAliasMotoListar = false;

        try {
            ClienteService.class.getMethod("buscarPorId", String.class);
        } catch (NoSuchMethodException ex) {
            noExponeAliasCliente = true;
        }

        try {
            MotoService.class.getMethod("buscarPorId", String.class);
        } catch (NoSuchMethodException ex) {
            noExponeAliasMotoBuscar = true;
        }

        try {
            MotoService.class.getMethod("listarTodas");
        } catch (NoSuchMethodException ex) {
            noExponeAliasMotoListar = true;
        }

        assertTrue(declaraPoliticaFreeze);
        assertTrue(declaraUsoSoloMetodosCongelados);
        assertTrue(noExponeAliasCliente);
        assertTrue(noExponeAliasMotoBuscar);
        assertTrue(noExponeAliasMotoListar);
    }

    public void testDeteccionDesalineacionFallaCuandoFirmaDocumentadaNoExisteEnApi() throws IOException {
        String documento = Files.readString(Path.of(DOCUMENTO_CONTRATO));

        boolean firmaCanonicaAlineada = firmaYApiCanonicaCoinciden(
                documento,
                "Optional<Moto> buscarPorMatricula(String matricula)",
                MotoService.class,
                "buscarPorMatricula",
                String.class);
        boolean firmaInexistenteSeDetectaComoMismatch = firmaYApiCanonicaCoinciden(
                documento,
                "Optional<Moto> buscarPorPatente(String patente)",
                MotoService.class,
                "buscarPorPatente",
                String.class);

        assertTrue(firmaCanonicaAlineada);
        assertFalse(firmaInexistenteSeDetectaComoMismatch);
    }

    private boolean firmaYApiCanonicaCoinciden(
            String documento,
            String firmaDocumentada,
            Class<?> claseServicio,
            String nombreMetodo,
            Class<?> tipoParametro) {
        boolean firmaEnDocumento = documento.contains(firmaDocumentada);
        boolean metodoExisteEnApi = false;
        boolean coincide = false;

        try {
            java.lang.reflect.Method metodo = claseServicio.getMethod(nombreMetodo, tipoParametro);
            if (metodo != null) {
                metodoExisteEnApi = true;
            }
        } catch (NoSuchMethodException ex) {
            metodoExisteEnApi = false;
        }

        if (firmaEnDocumento && metodoExisteEnApi) {
            coincide = true;
        }

        return coincide;
    }
}
