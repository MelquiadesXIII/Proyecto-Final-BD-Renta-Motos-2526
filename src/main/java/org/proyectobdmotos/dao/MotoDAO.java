package org.proyectobdmotos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.proyectobdmotos.dto.MotoDTO;
import org.proyectobdmotos.dto.SituacionMotoDTO;
import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.models.Situacion;
import org.proyectobdmotos.utils.Logger;

public class MotoDAO extends AbstractGenericDAO<Moto, String> implements IMotoDAO {

    public MotoDAO(Connection connection) {
        super(connection);
    }

    // ===== MÉTODOS TEMPLATE =====

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO moto (matricula_moto, id_modelo, situacion, "
             + "cant_km_recorridos, id_color) VALUES (?, ?, ?::tipo_situacion, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE moto SET id_modelo = ?, situacion = ?::tipo_situacion, "
             + "cant_km_recorridos = ?, id_color = ? WHERE matricula_moto = ?";
    }

    @Override
    protected String getDeleteSQL() {
        return "DELETE FROM moto WHERE matricula_moto = ?";
    }

    @Override
    protected String getFindByIdSQL() {
        return "SELECT * FROM moto WHERE matricula_moto = ?";
    }

    @Override
    protected String getFindAllSQL() {
        return "SELECT * FROM moto ORDER BY matricula_moto";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Moto moto) throws SQLException {
        ps.setString(1, moto.getMatriculaMoto());
        ps.setString(2, moto.getIdModelo());
        ps.setString(3, moto.getSituacion().getValor());
        ps.setDouble(4, moto.getCantKmRecorridos());
        ps.setString(5, moto.getIdColor());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Moto moto) throws SQLException {
        ps.setString(1, moto.getIdModelo());
        ps.setString(2, moto.getSituacion().getValor());
        ps.setDouble(3, moto.getCantKmRecorridos());
        ps.setString(4, moto.getIdColor());
        ps.setString(5, moto.getMatriculaMoto());
    }

    @Override
    protected void setIdParameter(PreparedStatement ps, String matricula) throws SQLException {
        ps.setString(1, matricula);
    }

    @Override
    protected Moto mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new Moto(
            rs.getString("matricula_moto"),
            String.valueOf(rs.getInt("id_modelo")),
            Situacion.fromValor(rs.getString("situacion")),
            rs.getDouble("cant_km_recorridos"),
            String.valueOf(rs.getInt("id_color"))
        );
    }

    // ===== MÉTODOS ESPECÍFICOS =====

    @Override
    public List<MotoDTO> listarMotosConKilometraje() {
        String sql = """
            SELECT m.matricula_moto,
                   ma.nombre_marca,
                   mo.nombre_modelo,
                   m.cant_km_recorridos
            FROM moto m
            JOIN modelo mo ON m.id_modelo = mo.id_modelo
            JOIN marca ma ON mo.id_marca = ma.id_marca
            ORDER BY m.cant_km_recorridos DESC
            """;

        List<MotoDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hasMore = rs.next();
            while (hasMore) {
                lista.add(new MotoDTO(
                    rs.getString("matricula_moto"),
                    rs.getString("nombre_marca"),
                    rs.getString("nombre_modelo"),
                    rs.getDouble("cant_km_recorridos")
                ));
                hasMore = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error al listar motos con kilometraje: " + e.getMessage());
            throw new RuntimeException("Error al listar motos con kilometraje: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public List<SituacionMotoDTO> listarSituacionMotos() {
        String sql = """
            SELECT m.matricula_moto,
                   ma.nombre_marca,
                   m.situacion,
                   co.fecha_fin
            FROM moto m
            JOIN modelo mo ON m.id_modelo = mo.id_modelo
            JOIN marca ma ON mo.id_marca = ma.id_marca
            LEFT JOIN contrato co ON m.matricula_moto = co.matricula_moto
                AND co.fecha_entrega IS NULL
            ORDER BY m.situacion, m.matricula_moto
            """;

        List<SituacionMotoDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hasMore = rs.next();
            while (hasMore) {
                java.sql.Date fechaFinSql = rs.getDate("fecha_fin");
                LocalDate fechaFin = null;
                if (fechaFinSql != null) {
                    fechaFin = fechaFinSql.toLocalDate();
                }
                lista.add(new SituacionMotoDTO(
                    rs.getString("matricula_moto"),
                    rs.getString("nombre_marca"),
                    Situacion.fromValor(rs.getString("situacion")),
                    fechaFin
                ));
                hasMore = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error al listar situación de motos: " + e.getMessage());
            throw new RuntimeException("Error al listar situación de motos: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public void cambiarEstado(String matricula, Situacion nuevaSituacion) {
        String sql = "UPDATE moto SET situacion = ?::tipo_situacion WHERE matricula_moto = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nuevaSituacion.getValor());
            ps.setString(2, matricula);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.logError("Error al cambiar estado de moto: " + e.getMessage());
            throw new RuntimeException("Error al cambiar estado de moto: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean estaDisponible(String matricula) {
        String sql = "SELECT situacion FROM moto WHERE matricula_moto = ?";
        boolean disponible = false;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    disponible = "disponible".equalsIgnoreCase(rs.getString("situacion"));
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al verificar disponibilidad: " + e.getMessage());
            throw new RuntimeException("Error al verificar disponibilidad: " + e.getMessage(), e);
        }
        return disponible;
    }
}
