package org.proyectobdmotos.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.proyectobdmotos.database.DatabaseConnection;
import org.proyectobdmotos.dto.*;
import org.proyectobdmotos.models.Contrato;
import org.proyectobdmotos.models.FormaPago;
import org.proyectobdmotos.utils.Logger;

public class ContratoDAO extends AbstractGenericDAO<Contrato, Integer> implements IContratoDAO {

    public ContratoDAO(Connection connection) {
        super(connection);
    }

    // -----------------------------------------------------------------
    // Métodos template (AbstractGenericDAO)
    // -----------------------------------------------------------------

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

    /**
     * Convierte una fila de ResultSet en un objeto Contrato.
     * Maneja correctamente los campos nulos (fecha de entrega) y los tipos de datos.
     */
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

    // -----------------------------------------------------------------
    // Listados y reportes
    // -----------------------------------------------------------------

    /**
     * Obtiene todos los contratos con información completa (joins a cliente y moto).
     */
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
            e.printStackTrace();
            Logger.logError("Error al listar contratos completos: " + e.getMessage());
            throw new RuntimeException("Error al listar contratos completos: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Obtiene el reporte de contratos desde la función listado_contratos().
     */
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
            e.printStackTrace();
            Logger.logError("Error al listar contratos reporte: " + e.getMessage());
            throw new RuntimeException("Error al listar contratos reporte", e);
        }
        return lista;
    }

    /**
     * Obtiene el resumen de contratos por marcas y modelos.
     */
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
            e.printStackTrace();
            Logger.logError("Error en resumen marcas/modelos: " + e.getMessage());
            throw new RuntimeException("Error en resumen marcas/modelos", e);
        }
        return lista;
    }

    /**
     * Obtiene el resumen de contratos por municipios.
     */
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
            e.printStackTrace();
            Logger.logError("Error en resumen municipios: " + e.getMessage());
            throw new RuntimeException("Error en resumen municipios", e);
        }
        return lista;
    }

    /**
     * Obtiene el reporte de ingresos anuales.
     */
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
            e.printStackTrace();
            Logger.logError("Error en ingresos anuales: " + e.getMessage());
            throw new RuntimeException("Error en ingresos anuales", e);
        }
        return lista;
    }

    /**
     * Obtiene los contratos de un cliente específico, incluyendo información de la moto,
     * fechas y costos. La fecha de entrega se muestra como texto ("Sin entregar" si es nula).
     */
    public List<MisContratosDTO> listarMisContratos(int idCliente) {
        String sql = """
        SELECT co.id_contrato,
               m.matricula_moto || ' - ' || ma.nombre_marca || ' ' || mo.nombre_modelo AS moto_info,
               co.fecha_inicio,
               co.fecha_fin,
               co.fecha_entrega,
               (co.tarifa_normal * (co.fecha_fin - co.fecha_inicio + 1) +
                co.tarifa_prorroga * co.dias_prorroga) AS importe
        FROM contrato co
        JOIN moto m ON co.id_moto = m.id_moto
        JOIN modelo mo ON m.id_modelo = mo.id_modelo
        JOIN marca ma ON mo.id_marca = ma.id_marca
        WHERE co.id_cliente = ?
        ORDER BY co.fecha_inicio DESC
        """;

        List<MisContratosDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                boolean hayFila = rs.next();
                while (hayFila) {
                    String fechaEntregaStr = rs.getDate("fecha_entrega") != null ?
                            rs.getDate("fecha_entrega").toLocalDate().toString() : "Sin entregar";
                    lista.add(new MisContratosDTO(
                            rs.getInt("id_contrato"),
                            rs.getString("moto_info"),
                            rs.getDate("fecha_inicio").toLocalDate().toString(),
                            rs.getDate("fecha_fin").toLocalDate().toString(),
                            rs.getDouble("importe"),
                            fechaEntregaStr
                    ));
                    hayFila = rs.next();
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al listar mis contratos: " + e.getMessage());
            throw new RuntimeException("Error al listar mis contratos", e);
        }
        return lista;
    }

    @Override
    public boolean tieneContratoAnteriorActivo(int idMoto, int idContratoActual) {
        String sql = "SELECT COUNT(*) FROM contrato " +
                "WHERE id_moto = ? AND id_contrato <> ? " +
                "AND fecha_inicio < (SELECT fecha_inicio FROM contrato WHERE id_contrato = ?) " +
                "AND fecha_entrega IS NULL";
        try (
             PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMoto);
            ps.setInt(2, idContratoActual);
            ps.setInt(3, idContratoActual);
            try (ResultSet rs = ps.executeQuery()) {
                boolean hayFila = rs.next();
                if (hayFila) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al verificar contrato anterior activo: " + e.getMessage());
            throw new RuntimeException("Error al verificar contrato anterior activo", e);
        }
        return false;
    }

    public List<Contrato> listarTodos() {
        String sql = "SELECT * FROM listar_contratos_completos()";
        List<Contrato> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Contrato contrato = new Contrato(
                        rs.getDouble("cant_km_llegada"),
                        rs.getDouble("cant_km_salida"),
                        rs.getInt("id_cliente"),
                        rs.getInt("dias_prorroga"),
                        rs.getDate("fecha_entrega") != null ? rs.getDate("fecha_entrega").toLocalDate() : null,
                        rs.getDate("fecha_fin").toLocalDate(),
                        rs.getDate("fecha_inicio").toLocalDate(),
                        FormaPago.fromId(rs.getInt("id_forma_pago")),
                        rs.getInt("id_moto"),
                        rs.getBoolean("seguro_adicional"),
                        rs.getDouble("tarifa_normal"),
                        rs.getDouble("tarifa_prorroga")
                );
                contrato.setIdContrato(rs.getInt("id_contrato"));
                contrato.setCiCliente(rs.getString("ci_cliente"));
                contrato.setNombreCompletoCliente(rs.getString("nombre_completo_cliente"));
                contrato.setMatriculaMoto(rs.getString("matricula_moto"));
                contrato.setMarcaMoto(rs.getString("marca_moto"));
                contrato.setModeloMoto(rs.getString("modelo_moto"));
                lista.add(contrato);
            }
        } catch (SQLException e) {
            Logger.logError("Error al listar contratos completos: " + e.getMessage());
            throw new RuntimeException("Error al listar contratos completos", e);
        }
        return lista;
    }
}