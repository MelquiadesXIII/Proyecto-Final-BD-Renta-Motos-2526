package org.proyectobdmotos.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.proyectobdmotos.dto.ContRepDTO;
import org.proyectobdmotos.dto.IngAnualDTO;
import org.proyectobdmotos.dto.ResMarModDTO;
import org.proyectobdmotos.dto.ResMunDTO;
import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.models.FormaPago;
import org.proyectobdmotos.utils.Logger;

public class ContratoDAO extends AbstractGenericDAO<Contrato, Integer> implements IContratoDAO {

    public ContratoDAO(Connection connection) {
        super(connection);
    }

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
        return "SELECT * FROM contrato WHERE id_contrato = ?";
    }

    @Override
    protected String getFindAllSQL() {
        return "SELECT * FROM contrato ORDER BY fecha_inicio DESC";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Contrato contrato) throws SQLException {
        ps.setDate(1, Date.valueOf(contrato.getFechaInicio()));
        ps.setInt(2, contrato.getIdMoto());
        ps.setInt(3, contrato.getIdCliente());
        ps.setInt(4, contrato.getFormaPago().getId());
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
        ps.setInt(1, contrato.getIdCliente());
        ps.setInt(2, contrato.getFormaPago().getId());
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
                FormaPago.fromId(rs.getInt("id_forma_pago")),
                rs.getInt("id_moto"),
                rs.getBoolean("seguro_adicional"),
                tarifaNormal,
                tarifaProrroga
        );
        contrato.setIdContrato(rs.getInt("id_contrato"));
        return contrato;
    }

    @Override
    public List<Contrato> listarContratosCompletos() {
        String sql = """
            SELECT co.*
            FROM contrato co
            JOIN cliente c ON co.id_cliente = c.id_cliente
            JOIN moto m ON co.id_moto = m.id_moto
            ORDER BY co.fecha_inicio DESC
            """;

        List<Contrato> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
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

    // ===================== REPORTES =====================

    public List<ContRepDTO> listarContratosReporte() {
        String sql = "SELECT * FROM listado_contratos()";
        List<ContRepDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hayFila = rs.next();
            while (hayFila) {
                lista.add(new ContRepDTO(
                        rs.getString("Nombre del cliente"),
                        rs.getString("Matrícula"),
                        rs.getString("Marca"),
                        rs.getString("Modelo"),
                        rs.getString("Forma de pago"),
                        rs.getDate("Fecha inicio") != null ? rs.getDate("Fecha inicio").toLocalDate() : null,
                        rs.getDate("Fecha fin") != null ? rs.getDate("Fecha fin").toLocalDate() : null,
                        rs.getInt("Prórroga (días)"),
                        rs.getString("Seguro adicional"),
                        rs.getDouble("Importe total")
                ));
                hayFila = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error al listar contratos reporte: " + e.getMessage());
            throw new RuntimeException("Error al listar contratos reporte", e);
        }
        return lista;
    }

    public List<ResMarModDTO> resumenMarcasModelos() {
        String sql = "SELECT * FROM resumen_contratos_por_marcas_modelos()";
        List<ResMarModDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hayFila = rs.next();
            while (hayFila) {
                lista.add(new ResMarModDTO(
                        rs.getDate("Fecha") != null ? rs.getDate("Fecha").toLocalDate() : null,
                        rs.getString("Marca"),
                        rs.getString("Modelo"),
                        rs.getLong("Cantidad de motos"),
                        rs.getDouble("Días totales"),
                        rs.getDouble("Ingresos tarjeta"),
                        rs.getDouble("Ingresos cheque"),
                        rs.getDouble("Ingresos efectivo"),
                        rs.getDouble("Total ingresos marca"),
                        rs.getDouble("Total general ingresos")
                ));
                hayFila = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error en resumen marcas/modelos: " + e.getMessage());
            throw new RuntimeException("Error en resumen marcas/modelos", e);
        }
        return lista;
    }

    public List<ResMunDTO> resumenMunicipios() {
        String sql = "SELECT * FROM resumen_contratos_por_municipios()";
        List<ResMunDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hayFila = rs.next();
            while (hayFila) {
                lista.add(new ResMunDTO(
                        rs.getDate("Fecha") != null ? rs.getDate("Fecha").toLocalDate() : null,
                        rs.getString("Municipio"),
                        rs.getString("Marca"),
                        rs.getString("Modelo"),
                        rs.getDouble("Días alquilados"),
                        rs.getDouble("Días de prórroga"),
                        rs.getDouble("Valor en efectivo"),
                        rs.getDouble("Valor total general")
                ));
                hayFila = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error en resumen municipios: " + e.getMessage());
            throw new RuntimeException("Error en resumen municipios", e);
        }
        return lista;
    }

    public List<IngAnualDTO> ingresosAnuales() {
        String sql = "SELECT * FROM listado_ingresos_anuales()";
        List<IngAnualDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hayFila = rs.next();
            while (hayFila) {
                lista.add(new IngAnualDTO(
                        rs.getDate("Fecha") != null ? rs.getDate("Fecha").toLocalDate() : null,
                        rs.getDouble("Ingreso total anual"),
                        rs.getString("Mes"),
                        rs.getDouble("Ingreso mensual")
                ));
                hayFila = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error en ingresos anuales: " + e.getMessage());
            throw new RuntimeException("Error en ingresos anuales", e);
        }
        return lista;
    }
}