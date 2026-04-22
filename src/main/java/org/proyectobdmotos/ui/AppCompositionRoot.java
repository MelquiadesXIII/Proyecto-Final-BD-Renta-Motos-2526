package org.proyectobdmotos.ui;

import java.sql.Connection;
import java.sql.SQLException;

import org.proyectobdmotos.dao.ClienteDAO;
import org.proyectobdmotos.dao.ContratoDAO;
import org.proyectobdmotos.dao.FormaPagoDAO;
import org.proyectobdmotos.dao.MotoDAO;
import org.proyectobdmotos.dao.SexoDAO;
import org.proyectobdmotos.dao.SituacionDAO;
import org.proyectobdmotos.utils.Logger;
import org.proyectobdmotos.database.DatabaseConnection;
import org.proyectobdmotos.services.AgenciaService;
import org.proyectobdmotos.services.ClienteService;
import org.proyectobdmotos.services.ContratoService;
import org.proyectobdmotos.services.MotoService;
import org.proyectobdmotos.stores.AgenciaStore;
import org.proyectobdmotos.stores.ReferenceDataStore;
import org.proyectobdmotos.ui.navigation.ScreenLoader;

/**
 * Composition Root: construye y conecta el grafo completo de dependencias.
 * Esta es la única clase responsable de crear e inyectar dependencias.
 */
public final class AppCompositionRoot {

    // Capa de infraestructura
    private final Connection connection;

    // DAOs de catálogos lookup
    private final SexoDAO sexoDAO;
    private final SituacionDAO situacionDAO;
    private final FormaPagoDAO formaPagoDAO;

    // Capa de acceso a datos
    private final ClienteDAO clienteDAO;
    private final MotoDAO motoDAO;
    private final ContratoDAO contratoDAO;

    // Capa de servicios
    private final ClienteService clienteService;
    private final MotoService motoService;
    private final ContratoService contratoService;
    private final AgenciaService agenciaService;

    // Capa de estado observable (Stores)
    private final AgenciaStore agenciaStore;
    private final ReferenceDataStore referenceDataStore;

    // Navegación UI
    private final ScreenLoader screenLoader;

    public AppCompositionRoot() throws SQLException {
        Logger.log("Iniciando construcción del grafo de dependencias...");

        // 1. Connection
        Logger.log("✓ Creando Connection");
        this.connection = DatabaseConnection.getInstance();

        // 2. DAOs
        Logger.log("✓ Creando DAOs (ClienteDAO, MotoDAO, ContratoDAO)");
        this.sexoDAO = new SexoDAO(connection);
        this.situacionDAO = new SituacionDAO(connection);
        this.formaPagoDAO = new FormaPagoDAO(connection);
        this.clienteDAO = new ClienteDAO(connection, sexoDAO);
        this.motoDAO = new MotoDAO(connection, situacionDAO);
        this.contratoDAO = new ContratoDAO(connection, formaPagoDAO);

        // 3. Services
        Logger.log("✓ Creando Services (ClienteService, MotoService, ContratoService)");
        this.clienteService = new ClienteService(clienteDAO);
        this.motoService = new MotoService(motoDAO);
        this.contratoService = new ContratoService(contratoDAO, clienteDAO, motoDAO);

        Logger.log("✓ Creando AgenciaService (fachada)");
        this.agenciaService = new AgenciaService(clienteService, motoService, contratoService);

        // 4. Stores
        Logger.log("✓ Creando Stores (AgenciaStore, ReferenceDataStore)");
        this.agenciaStore = new AgenciaStore();
        this.referenceDataStore = new ReferenceDataStore();

        // 5. ScreenLoader
        Logger.log("✓ Creando ScreenLoader");
        this.screenLoader = new ScreenLoader(this);

        Logger.logInfo("Grafo de dependencias completo\n");
    }

    // Getters para acceso controlado desde ScreenLoader y otros componentes

    public Connection getConnection() {
        return connection;
    }

    public SexoDAO getSexoDAO() {
        return sexoDAO;
    }

    public SituacionDAO getSituacionDAO() {
        return situacionDAO;
    }

    public FormaPagoDAO getFormaPagoDAO() {
        return formaPagoDAO;
    }

    public ClienteDAO getClienteDAO() {
        return clienteDAO;
    }

    public MotoDAO getMotoDAO() {
        return motoDAO;
    }

    public ContratoDAO getContratoDAO() {
        return contratoDAO;
    }

    public ClienteService getClienteService() {
        return clienteService;
    }

    public MotoService getMotoService() {
        return motoService;
    }

    public ContratoService getContratoService() {
        return contratoService;
    }

    public AgenciaService getAgenciaService() {
        return agenciaService;
    }

    public AgenciaStore getAgenciaStore() {
        return agenciaStore;
    }

    public ReferenceDataStore getReferenceDataStore() {
        return referenceDataStore;
    }

    public ScreenLoader getScreenLoader() {
        return screenLoader;
    }
}
