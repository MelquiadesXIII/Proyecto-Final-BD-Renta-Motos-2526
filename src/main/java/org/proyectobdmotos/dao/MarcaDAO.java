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

    // -----------------------------------------------------------------
    // Métodos template (AbstractGenericDAO)
    // -----------------------------------------------------------------

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

    // -----------------------------------------------------------------
    // Operaciones específicas de Marca
    // -----------------------------------------------------------------

    /**
     * Crea una nueva marca utilizando la función SQL insertar_marca().
     * @return la marca creada con su ID asignado, o null si no se pudo crear.
     */
    public Marca crearMarca(String nombre) {
        String sql = "SELECT insertar_marca(?) AS id_marca";
        Marca nuevaMarca = null;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_marca");
                    nuevaMarca = new Marca(id, nombre);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.logError("Error al crear marca: " + e.getMessage());
            throw new RuntimeException("Error al crear marca", e);
        }
        return nuevaMarca;
    }

    /**
     * Verifica si ya existe una marca con el nombre dado.
     * @return true si la marca existe, false en caso contrario.
     */
    public boolean existeMarca(String nombre) {
        String sql = "SELECT existe_marca(?)";
        boolean existe = false;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            existe = rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar marca", e);
        }
        return existe;
    }

    /**
     * Verifica si una marca tiene modelos asociados.
     * @return true si al menos un modelo pertenece a esta marca.
     */
    public boolean existenModelosConMarca(int idMarca) {
        String sql = "SELECT existen_modelos_con_marca(?)";
        boolean existen = false;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMarca);
            ResultSet rs = ps.executeQuery();
            existen = rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar modelos con marca", e);
        }
        return existen;
    }

    /**
     * Verifica si hay motos que referencien directamente esta marca.
     * @return true si al menos una moto usa esta marca.
     */
    public boolean existenMotosConMarca(int idMarca) {
        String sql = "SELECT existen_motos_con_marca(?)";
        boolean existen = false;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMarca);
            ResultSet rs = ps.executeQuery();
            existen = rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar motos con marca", e);
        }
        return existen;
    }

    /**
     * Elimina una marca por su identificador.
     */
    public void eliminarMarca(int idMarca) {
        String sql = "DELETE FROM marca WHERE id_marca = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMarca);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar marca", e);
        }
    }

    /**
     * Actualiza los datos de una marca existente.
     * Delega en el método genérico actualizar() de la superclase.
     */
    public void actualizarMarca(Marca marca) {
        actualizar(marca);
    }
}