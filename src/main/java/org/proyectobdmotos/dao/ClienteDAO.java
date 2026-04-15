package org.proyectobdmotos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.proyectobdmotos.dto.ClienteDTO;
import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.models.Sexo;
import org.proyectobdmotos.utils.Logger;

public class ClienteDAO extends AbstractGenericDAO<Cliente, String> implements IClienteDAO {

    public ClienteDAO(Connection connection) {
        super(connection);
    }

    // ===== MÉTODOS TEMPLATE =====

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO cliente (ci_cliente, nombre_cliente, primer_apellido, "
             + "segundo_apellido, edad, sexo, numero_contacto, id_municipio) "
             + "VALUES (?, ?, ?, ?, ?, ?::tipo_sexo, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE cliente SET nombre_cliente = ?, primer_apellido = ?, "
             + "segundo_apellido = ?, edad = ?, sexo = ?::tipo_sexo, numero_contacto = ?, "
             + "id_municipio = ? WHERE ci_cliente = ?";
    }

    @Override
    protected String getDeleteSQL() {
        return "DELETE FROM cliente WHERE ci_cliente = ?";
    }

    @Override
    protected String getFindByIdSQL() {
        return "SELECT * FROM cliente WHERE ci_cliente = ?";
    }

    @Override
    protected String getFindAllSQL() {
        return "SELECT * FROM cliente ORDER BY nombre_cliente, primer_apellido";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Cliente cliente) throws SQLException {
        ps.setString(1, cliente.getCiCliente());
        ps.setString(2, cliente.getNombreCLiente());
        ps.setString(3, cliente.getPrimerApellido());
        ps.setString(4, cliente.getSegundoApellido());
        ps.setInt(5, cliente.getEdad());
        ps.setString(6, cliente.getSexo().getValor());
        ps.setString(7, cliente.getNumeroContacto());
        ps.setString(8, cliente.getIdMunicipio());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Cliente cliente) throws SQLException {
        ps.setString(1, cliente.getNombreCLiente());
        ps.setString(2, cliente.getPrimerApellido());
        ps.setString(3, cliente.getSegundoApellido());
        ps.setInt(4, cliente.getEdad());
        ps.setString(5, cliente.getSexo().getValor());
        ps.setString(6, cliente.getNumeroContacto());
        ps.setString(7, cliente.getIdMunicipio());
        ps.setString(8, cliente.getCiCliente());
    }

    @Override
    protected void setIdParameter(PreparedStatement ps, String ci) throws SQLException {
        ps.setString(1, ci);
    }

    @Override
    protected Cliente mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getString("ci_cliente"),
            rs.getString("nombre_cliente"),
            rs.getString("primer_apellido"),
            rs.getString("segundo_apellido"),
            rs.getInt("edad"),
            Sexo.fromValor(rs.getString("sexo")),
            rs.getString("numero_contacto"),
            String.valueOf(rs.getInt("id_municipio"))
        );
    }

    // ===== MÉTODOS ESPECÍFICOS =====

    @Override
    public List<ClienteDTO> listarClientesPorMunicipio() {
        String sql = """
            SELECT c.ci_cliente,
                   c.nombre_cliente || ' ' || c.primer_apellido AS nombre_completo,
                   m.nombre_municipio,
                   COUNT(co.matricula_moto) AS cantidad_alquileres
            FROM cliente c
            JOIN municipio m ON c.id_municipio = m.id_municipio
            LEFT JOIN contrato co ON c.ci_cliente = co.ci_cliente
            GROUP BY c.ci_cliente, nombre_completo, m.nombre_municipio
            ORDER BY m.nombre_municipio, nombre_completo
            """;

        List<ClienteDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hasMore = rs.next();
            while (hasMore) {
                lista.add(new ClienteDTO(
                    rs.getString("ci_cliente"),
                    rs.getString("nombre_completo"),
                    rs.getString("nombre_municipio"),
                    rs.getInt("cantidad_alquileres")
                ));
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
            JOIN contrato co ON c.ci_cliente = co.ci_cliente
            WHERE co.fecha_entrega IS NOT NULL
              AND co.fecha_entrega > co.fecha_fin
            ORDER BY c.nombre_cliente
            """;

        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
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
    public void eliminarConCascada(String ci) {
        try {
            connection.setAutoCommit(false);

            String deleteContratos = "DELETE FROM contrato WHERE ci_cliente = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteContratos)) {
                ps.setString(1, ci);
                ps.executeUpdate();
            }

            String deleteCliente = "DELETE FROM cliente WHERE ci_cliente = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteCliente)) {
                ps.setString(1, ci);
                ps.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logError("Error en rollback: " + rollbackEx.getMessage());
            }
            Logger.logError("Error al eliminar cliente con cascada: " + e.getMessage());
            throw new RuntimeException("Error al eliminar cliente con cascada: " + e.getMessage(), e);
        }
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            Logger.logError("Error al restaurar autoCommit: " + e.getMessage());
        }
    }
}
