package org.proyectobdmotos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.database.DatabaseConnection;
import org.proyectobdmotos.dto.CliRepDTO;
import org.proyectobdmotos.dto.ClienteDTO;
import org.proyectobdmotos.dto.IncumpDTO;
import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.models.Sexo;
import org.proyectobdmotos.utils.Logger;

public class ClienteDAO extends AbstractGenericDAO<Cliente, Integer> implements IClienteDAO {

    public ClienteDAO(Connection connection) {
        super(connection);
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO cliente (ci_cliente, nombre_cliente, primer_apellido, "
                + "segundo_apellido, edad, id_sexo, numero_contacto, id_municipio) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE cliente SET ci_cliente = ?, nombre_cliente = ?, primer_apellido = ?, "
                + "segundo_apellido = ?, edad = ?, id_sexo = ?, numero_contacto = ?, "
                + "id_municipio = ? WHERE id_cliente = ?";
    }

    @Override
    protected String getDeleteSQL() {
        return "DELETE FROM cliente WHERE id_cliente = ?";
    }

    @Override
    protected String getFindByIdSQL() {
        return "SELECT * FROM cliente WHERE id_cliente = ?";
    }

    @Override
    protected String getFindAllSQL() {
        return "SELECT * FROM cliente ORDER BY nombre_cliente, primer_apellido";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Cliente cliente) throws SQLException {
        ps.setString(1, cliente.getCiCliente());
        ps.setString(2, cliente.getNombreCliente());
        ps.setString(3, cliente.getPrimerApellido());
        ps.setString(4, cliente.getSegundoApellido());
        ps.setInt(5, cliente.getEdad());
        ps.setInt(6, cliente.getSexo().getId());
        ps.setString(7, cliente.getNumeroContacto());
        ps.setInt(8, cliente.getIdMunicipio());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Cliente cliente) throws SQLException {
        ps.setString(1, cliente.getCiCliente());
        ps.setString(2, cliente.getNombreCliente());
        ps.setString(3, cliente.getPrimerApellido());
        ps.setString(4, cliente.getSegundoApellido());
        ps.setInt(5, cliente.getEdad());
        ps.setInt(6, cliente.getSexo().getId());
        ps.setString(7, cliente.getNumeroContacto());
        ps.setInt(8, cliente.getIdMunicipio());
        ps.setInt(9, cliente.getIdCliente());
    }

    @Override
    protected void setIdParameter(PreparedStatement ps, Integer id) throws SQLException {
        ps.setInt(1, id);
    }

    @Override
    protected Cliente mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id_cliente"),
                rs.getString("ci_cliente"),
                rs.getString("nombre_cliente"),
                rs.getString("primer_apellido"),
                rs.getString("segundo_apellido"),
                rs.getInt("edad"),
                Sexo.fromId(rs.getInt("id_sexo")),
                rs.getString("numero_contacto"),
                rs.getInt("id_municipio"));
    }

    @Override
    public void insertar(Cliente cliente) {
        try (PreparedStatement ps = getConnection().prepareStatement(getInsertSQL(), Statement.RETURN_GENERATED_KEYS)) {
            setInsertParameters(ps, cliente);
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cliente.setIdCliente(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al insertar cliente: " + e.getMessage());
            throw new RuntimeException("Error al insertar cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorCi(String ci) {
        String sql = "SELECT * FROM cliente WHERE ci_cliente = ?";
        Optional<Cliente> resultado = Optional.empty();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, ci);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultado = Optional.of(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al buscar cliente por CI: " + e.getMessage());
            throw new RuntimeException("Error al buscar cliente por CI: " + e.getMessage(), e);
        }
        return resultado;
    }

    @Override
    public List<ClienteDTO> listarClientesPorMunicipio() {
        String sql = """
                SELECT c.ci_cliente,
                       c.nombre_cliente || ' ' || c.primer_apellido AS nombre_completo,
                       m.nombre_municipio,
                       COUNT(co.id_moto) AS cantidad_alquileres
                FROM cliente c
                JOIN municipio m ON c.id_municipio = m.id_municipio
                LEFT JOIN contrato co ON c.id_cliente = co.id_cliente
                GROUP BY c.ci_cliente, nombre_completo, m.nombre_municipio
                ORDER BY m.nombre_municipio, nombre_completo
                """;

        List<ClienteDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hasMore = rs.next();
            while (hasMore) {
                lista.add(new ClienteDTO(
                        rs.getString("ci_cliente"),
                        rs.getString("nombre_completo"),
                        rs.getString("nombre_municipio"),
                        rs.getInt("cantidad_alquileres")));
                hasMore = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error al listar clientes por municipio: " + e.getMessage());
            throw new RuntimeException("Error al listar clientes por municipio: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public List<Cliente> obtenerClientesIncumplidores() {
        String sql = """
                SELECT DISTINCT c.*
                FROM cliente c
                JOIN contrato co ON c.id_cliente = co.id_cliente
                WHERE co.fecha_entrega IS NOT NULL
                  AND co.fecha_entrega > co.fecha_fin
                ORDER BY c.nombre_cliente
                """;

        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hasMore = rs.next();
            while (hasMore) {
                lista.add(mapResultSetToEntity(rs));
                hasMore = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error al obtener clientes incumplidores: " + e.getMessage());
            throw new RuntimeException("Error al obtener clientes incumplidores: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public void eliminarConCascada(Integer idCliente) {
        try {
            Connection conn = getConnection();
            conn.setAutoCommit(false);

            String deleteContratos = "DELETE FROM contrato WHERE id_cliente = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteContratos)) {
                ps.setInt(1, idCliente);
                ps.executeUpdate();
            }

            String deleteCliente = "DELETE FROM cliente WHERE id_cliente = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteCliente)) {
                ps.setInt(1, idCliente);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException rollbackEx) {
                Logger.logError("Error en rollback: " + rollbackEx.getMessage());
            }
            Logger.logError("Error al eliminar cliente con cascada: " + e.getMessage());
            throw new RuntimeException("Error al eliminar cliente con cascada: " + e.getMessage(), e);
        }
        try {
            getConnection().setAutoCommit(true);
        } catch (SQLException e) {
            Logger.logError("Error al restaurar autoCommit: " + e.getMessage());
        }
    }

    public Cliente insert(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO cliente (ci_cliente, nombre_cliente, primer_apellido, segundo_apellido, edad, sexo, numero_contacto, municipio, id_usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_cliente";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, cliente.getCiCliente());
            ps.setInt(8, cliente.getIdMunicipio());
            ps.setString(2, cliente.getNombreCliente());
            ps.setString(3, cliente.getPrimerApellido());
            ps.setString(4, cliente.getSegundoApellido());
            ps.setInt(5, cliente.getEdad());
            ps.setString(6, cliente.getSexo().name());
            ps.setString(7, cliente.getNumeroContacto());
            ps.setInt(9, cliente.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cliente.setIdCliente(rs.getInt("id_cliente"));
                }
            }
        }
        return cliente;
    }

    // ===================== REPORTES =====================

    public List<CliRepDTO> listarClientesReporte() {
        String sql = "SELECT * FROM listado_clientes()";
        List<CliRepDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hayFila = rs.next();
            while (hayFila) {
                lista.add(new CliRepDTO(
                        rs.getDate("Fecha de hoy") != null ? rs.getDate("Fecha de hoy").toLocalDate() : null,
                        rs.getString("Municipio"),
                        rs.getString("Nombre"),
                        rs.getString("CI"),
                        rs.getInt("Cantidad de Contratos contratados"),
                        rs.getDouble("Total de Dinero gastado")
                ));
                hayFila = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error al listar clientes reporte: " + e.getMessage());
            throw new RuntimeException("Error al listar clientes reporte", e);
        }
        return lista;
    }

    public List<IncumpDTO> listarIncumplidores() {
        String sql = "SELECT * FROM lista_incumplidores()";
        List<IncumpDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hayFila = rs.next();
            while (hayFila) {
                lista.add(new IncumpDTO(
                        rs.getDate("Fecha actual") != null ? rs.getDate("Fecha actual").toLocalDate() : null,
                        rs.getString("Nombres y apellidos"),
                        rs.getDate("Fecha fin del contrato") != null ? rs.getDate("Fecha fin del contrato").toLocalDate() : null,
                        rs.getDate("Fecha de entrega") != null ? rs.getDate("Fecha de entrega").toLocalDate() : null
                ));
                hayFila = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error al listar incumplidores: " + e.getMessage());
            throw new RuntimeException("Error al listar incumplidores", e);
        }
        return lista;
    }
}