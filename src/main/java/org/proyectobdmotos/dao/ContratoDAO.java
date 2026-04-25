package org.proyectobdmotos.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.models.FormaPago;
import org.proyectobdmotos.utils.Logger;

public class ContratoDAO extends AbstractGenericDAO<Contrato, Integer> implements IContratoDAO {

    public ContratoDAO(Connection connection) {
        super(connection);
    }

    private int buscarIdFormaPago(String valor) throws SQLException {
        String sql = "SELECT id_forma_pago FROM forma_pago WHERE LOWER(nombre_forma_pago) = LOWER(?)";
        int id = -1;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("id_forma_pago");
                }
            }
        }
        if (id == -1) {
            throw new SQLException("FormaPago no encontrada: " + valor);
        }
        return id;
    }

    // ===== MÉTODOS TEMPLATE =====

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO contrato (fecha_inicio, id_moto, id_cliente, "
             + "id_forma_pago, fecha_fin, dias_prorroga, seguro_adicional, "
             + "tarifa_normal, tarifa_prorroga, fecha_entrega, "
             + "cant_km_salida, cant_km_llegada) "
             + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE contrato SET id_cliente = ?, id_forma_pago = ?, "
             + "fecha_fin = ?, dias_prorroga = ?, seguro_adicional = ?, "
             + "tarifa_normal = ?, tarifa_prorroga = ?, fecha_entrega = ?, "
             + "cant_km_salida = ?, cant_km_llegada = ? "
             + "WHERE id_contrato = ?";
    }

    @Override
    protected String getDeleteSQL() {
        return "DELETE FROM contrato WHERE id_contrato = ?";
    }

    @Override
    protected String getFindByIdSQL() {
        return "SELECT co.*, fp.nombre_forma_pago AS forma_pago_nombre "
             + "FROM contrato co "
             + "JOIN forma_pago fp ON co.id_forma_pago = fp.id_forma_pago "
             + "WHERE co.id_contrato = ?";
    }

    @Override
    protected String getFindAllSQL() {
        return "SELECT co.*, fp.nombre_forma_pago AS forma_pago_nombre "
             + "FROM contrato co "
             + "JOIN forma_pago fp ON co.id_forma_pago = fp.id_forma_pago "
             + "ORDER BY co.fecha_inicio DESC";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Contrato contrato) throws SQLException {
        int idFormaPago = buscarIdFormaPago(contrato.getFormaPago().getValor());
        ps.setDate(1, Date.valueOf(contrato.getFechaInicio()));
        ps.setInt(2, contrato.getIdMoto());
        ps.setInt(3, contrato.getIdCliente());
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
        int idFormaPago = buscarIdFormaPago(contrato.getFormaPago().getValor());
        ps.setInt(1, contrato.getIdCliente());
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
        ps.setInt(11, contrato.getIdContrato());
    }

    @Override
    protected void setIdParameter(PreparedStatement ps, Integer id) throws SQLException {
        ps.setInt(1, id);
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

        Contrato contrato = new Contrato(
            cantKmLlegada,
            cantKmSalida,
            rs.getInt("id_cliente"),
            rs.getInt("dias_prorroga"),
            fechaEntrega,
            rs.getDate("fecha_fin").toLocalDate(),
            rs.getDate("fecha_inicio").toLocalDate(),
            FormaPago.fromValor(rs.getString("forma_pago_nombre")),
            rs.getInt("id_moto"),
            rs.getBoolean("seguro_adicional"),
            tarifaNormal,
            tarifaProrroga
        );
        contrato.setIdContrato(rs.getInt("id_contrato"));
        return contrato;
    }

    // ===== MÉTODOS ESPECÍFICOS =====

    @Override
    public List<Contrato> listarContratosCompletos() {
        String sql = """
            SELECT co.*, fp.nombre_forma_pago AS forma_pago_nombre
            FROM contrato co
            JOIN forma_pago fp ON co.id_forma_pago = fp.id_forma_pago
            JOIN cliente c ON co.id_cliente = c.id_cliente
            JOIN moto m ON co.id_moto = m.id_moto
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
