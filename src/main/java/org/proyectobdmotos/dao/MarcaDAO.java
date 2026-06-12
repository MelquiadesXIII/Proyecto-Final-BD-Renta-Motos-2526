package org.proyectobdmotos.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.proyectobdmotos.models.Marca;
import org.proyectobdmotos.utils.Logger;

public class MarcaDAO extends AbstractGenericDAO<Marca, Integer> implements IMarcaDAO {

    public MarcaDAO(Connection connection) {
        super(connection);
    }

    @Override
    protected String getInsertSQL() { return "INSERT INTO marca (nombre_marca) VALUES (?)"; }
    @Override
    protected String getUpdateSQL() { return "UPDATE marca SET nombre_marca = ? WHERE id_marca = ?"; }
    @Override
    protected String getDeleteSQL() { return "DELETE FROM marca WHERE id_marca = ?"; }
    @Override
    protected String getFindByIdSQL() { return "SELECT * FROM marca WHERE id_marca = ?"; }
    @Override
    protected String getFindAllSQL() { return "SELECT * FROM marca ORDER BY nombre_marca"; }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Marca marca) throws SQLException {
        ps.setString(1, marca.getNombreMarca());
    }
    @Override
    protected void setUpdateParameters(PreparedStatement ps, Marca marca) throws SQLException {
        ps.setString(1, marca.getNombreMarca());
        ps.setInt(2, marca.getIdMarca());
    }
    @Override
    protected void setIdParameter(PreparedStatement ps, Integer id) throws SQLException {
        ps.setInt(1, id);
    }
    @Override
    protected Marca mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new Marca(rs.getInt("id_marca"), rs.getString("nombre_marca"));
    }

    public Marca crearMarca(String nombre) {
        String sql = "SELECT insertar_marca(?) AS id_marca";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_marca");
                    return new Marca(id, nombre);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.logError("Error al crear marca: " + e.getMessage());
            throw new RuntimeException("Error al crear marca", e);
        }
        return null;
    }

    public boolean existeMarca(String nombre) {
        String sql = "SELECT existe_marca(?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar marca", e);
        }
    }

    public boolean existenModelosConMarca(int idMarca) {
        String sql = "SELECT existen_modelos_con_marca(?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMarca);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar modelos con marca", e);
        }
    }

    public boolean existenMotosConMarca(int idMarca) {
        String sql = "SELECT existen_motos_con_marca(?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMarca);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar motos con marca", e);
        }
    }

    public void eliminarMarca(int idMarca) {
        String sql = "DELETE FROM marca WHERE id_marca = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMarca);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar marca", e);
        }
    }

    public void actualizarMarca(Marca marca) {
        actualizar(marca);
    }
}