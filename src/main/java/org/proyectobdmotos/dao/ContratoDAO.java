package org.proyectobdmotos.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.models.ContratoID;
import org.proyectobdmotos.models.FormaPago;
import org.proyectobdmotos.utils.Logger;

public class ContratoDAO extends AbstractGenericDAO<Contrato, ContratoID> implements IContratoDAO {

    private final IFormaPagoDAO formaPagoDAO;

    public ContratoDAO(Connection connection, IFormaPagoDAO formaPagoDAO) {
        super(connection);
        this.formaPagoDAO = formaPagoDAO;
    }

    // ===== MÉTODOS TEMPLATE =====

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO contrato (fecha_inicio, matricula_moto, ci_cliente, "
             + "id_forma_pago, fecha_fin, dias_prorroga, seguro_adicional, "
             + "tarifa_normal, tarifa_prorroga, fecha_entrega, "
             + "cant_km_salida, cant_km_llegada) "
             + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE contrato SET ci_cliente = ?, id_forma_pago = ?, "
             + "fecha_fin = ?, dias_prorroga = ?, seguro_adicional = ?, "
             + "tarifa_normal = ?, tarifa_prorroga = ?, fecha_entrega = ?, "
             + "cant_km_salida = ?, cant_km_llegada = ? "
             + "WHERE fecha_inicio = ? AND matricula_moto = ?";
    }

    @Override
    protected String getDeleteSQL() {
        return "DELETE FROM contrato WHERE fecha_inicio = ? AND matricula_moto = ?";
    }

    @Override
    protected String getFindByIdSQL() {
        return "SELECT co.*, fp.nombre AS forma_pago_nombre "
             + "FROM contrato co "
             + "JOIN forma_pago fp ON co.id_forma_pago = fp.id_forma_pago "
             + "WHERE co.fecha_inicio = ? AND co.matricula_moto = ?";
    }

    @Override
    protected String getFindAllSQL() {
        return "SELECT co.*, fp.nombre AS forma_pago_nombre "
             + "FROM contrato co "
             + "JOIN forma_pago fp ON co.id_forma_pago = fp.id_forma_pago "
             + "ORDER BY co.fecha_inicio DESC";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Contrato contrato) throws SQLException {
        int idFormaPago = formaPagoDAO.findIdByNombre(contrato.getFormaPago().getValor());
        ps.setDate(1, Date.valueOf(contrato.getContratoID().getFechaInicio()));
        ps.setString(2, contrato.getContratoID().getMatriculaMoto());
        ps.setString(3, contrato.getCiCliente());
        ps.setInt(4, idFormaPago);
        ps.setDate(5, Date.valueOf(contrato.getFechaFin()));
        ps.setInt(6, contrato.getDiasProrroga());
        ps.setBoolean(7, contrato.isSeguroAdicional());
        ps.setDouble(8, contrato.getTarifaNormal());
        ps.setDouble(9, contrato.getTarifaProrroga());
        Date fechaEntrega = null;
        if (contrato.getFechaEntrega() != null) {
            fechaEntrega = Date.valueOf(contrato.getFechaEntrega());
        }
        ps.setDate(10, fechaEntrega);
        ps.setDouble(11, contrato.getCantKmSalida());
        ps.setDouble(12, contrato.getCantKmLlegada());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Contrato contrato) throws SQLException {
        int idFormaPago = formaPagoDAO.findIdByNombre(contrato.getFormaPago().getValor());
        ps.setString(1, contrato.getCiCliente());
        ps.setInt(2, idFormaPago);
        ps.setDate(3, Date.valueOf(contrato.getFechaFin()));
        ps.setInt(4, contrato.getDiasProrroga());
        ps.setBoolean(5, contrato.isSeguroAdicional());
        ps.setDouble(6, contrato.getTarifaNormal());
        ps.setDouble(7, contrato.getTarifaProrroga());
        Date fechaEntrega = null;
        if (contrato.getFechaEntrega() != null) {
            fechaEntrega = Date.valueOf(contrato.getFechaEntrega());
        }
        ps.setDate(8, fechaEntrega);
        ps.setDouble(9, contrato.getCantKmSalida());
        ps.setDouble(10, contrato.getCantKmLlegada());
        ps.setDate(11, Date.valueOf(contrato.getContratoID().getFechaInicio()));
        ps.setString(12, contrato.getContratoID().getMatriculaMoto());
    }

    @Override
    protected void setIdParameter(PreparedStatement ps, ContratoID id) throws SQLException {
        ps.setDate(1, Date.valueOf(id.getFechaInicio()));
        ps.setString(2, id.getMatriculaMoto());
    }

    @Override
    protected Contrato mapResultSetToEntity(ResultSet rs) throws SQLException {
        java.sql.Date fechaEntregaSql = rs.getDate("fecha_entrega");
        java.time.LocalDate fechaEntrega = null;
        if (fechaEntregaSql != null) {
            fechaEntrega = fechaEntregaSql.toLocalDate();
        }

        double cantKmSalida = rs.getDouble("cant_km_salida");
        double cantKmLlegada = rs.getDouble("cant_km_llegada");
        double tarifaNormal = rs.getDouble("tarifa_normal");
        double tarifaProrroga = rs.getDouble("tarifa_prorroga");

        return new Contrato(
            cantKmLlegada,
            cantKmSalida,
            rs.getString("ci_cliente"),
            rs.getInt("dias_prorroga"),
            fechaEntrega,
            rs.getDate("fecha_fin").toLocalDate(),
            rs.getDate("fecha_inicio").toLocalDate(),
            FormaPago.fromValor(rs.getString("forma_pago_nombre")),
            rs.getString("matricula_moto"),
            rs.getBoolean("seguro_adicional"),
            tarifaNormal,
            tarifaProrroga
        );
    }

    // ===== MÉTODOS ESPECÍFICOS =====

    @Override
    public List<Contrato> listarContratosCompletos() {
        String sql = """
            SELECT co.*, fp.nombre AS forma_pago_nombre
            FROM contrato co
            JOIN forma_pago fp ON co.id_forma_pago = fp.id_forma_pago
            JOIN cliente c ON co.ci_cliente = c.ci_cliente
            JOIN moto m ON co.matricula_moto = m.matricula_moto
            ORDER BY co.fecha_inicio DESC
            """;

        List<Contrato> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hasMore = rs.next();
            while (hasMore) {
                lista.add(mapResultSetToEntity(rs));
                hasMore = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error al listar contratos completos: " + e.getMessage());
            throw new RuntimeException("Error al listar contratos completos: " + e.getMessage(), e);
        }
        return lista;
    }
}
