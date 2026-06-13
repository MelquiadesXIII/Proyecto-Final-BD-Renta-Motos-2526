package org.proyectobdmotos.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.proyectobdmotos.dto.ModeloConMarcaDTO;
import org.proyectobdmotos.models.Modelo;
import org.proyectobdmotos.utils.Logger;

public class ModeloDAO extends AbstractGenericDAO<Modelo, Integer> implements IModeloDAO {

    public ModeloDAO(Connection connection) {
        super(connection);
    }

    // -----------------------------------------------------------------
    // Métodos template (AbstractGenericDAO)
    // -----------------------------------------------------------------

    @Override
    protected String getInsertSQL() { return "INSERT INTO modelo (id_marca, nombre_modelo) VALUES (?, ?)"; }
    @Override
    protected String getUpdateSQL() { return "UPDATE modelo SET id_marca = ?, nombre_modelo = ? WHERE id_modelo = ?"; }
    @Override
    protected String getDeleteSQL() { return "DELETE FROM modelo WHERE id_modelo = ?"; }
    @Override
    protected String getFindByIdSQL() { return "SELECT * FROM modelo WHERE id_modelo = ?"; }
    @Override
    protected String getFindAllSQL() { return "SELECT * FROM modelo ORDER BY nombre_modelo"; }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Modelo modelo) throws SQLException {
        ps.setInt(1, modelo.getIdMarca());
        ps.setString(2, modelo.getNombreModelo());
    }
    @Override
    protected void setUpdateParameters(PreparedStatement ps, Modelo modelo) throws SQLException {
        ps.setInt(1, modelo.getIdMarca());
        ps.setString(2, modelo.getNombreModelo());
        ps.setInt(3, modelo.getIdModelo());
    }
    @Override
    protected void setIdParameter(PreparedStatement ps, Integer id) throws SQLException {
        ps.setInt(1, id);
    }
    @Override
    protected Modelo mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new Modelo(rs.getInt("id_modelo"), rs.getInt("id_marca"), rs.getString("nombre_modelo"));
    }

    // -----------------------------------------------------------------
    // Operaciones específicas de Modelo
    // -----------------------------------------------------------------

    /**
     * Crea un nuevo modelo asociado a una marca utilizando la función SQL insertar_modelo().
     * @return el modelo creado con su ID asignado, o null si no se pudo crear.
     */
    public Modelo crearModelo(int idMarca, String nombre) {
        String sql = "SELECT insertar_modelo(?, ?) AS id_modelo";
        Modelo nuevoModelo = null;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMarca);
            ps.setString(2, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_modelo");
                    nuevoModelo = new Modelo(id, idMarca, nombre);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.logError("Error al crear modelo: " + e.getMessage());
            throw new RuntimeException("Error al crear modelo", e);
        }
        return nuevoModelo;
    }

    /**
     * Verifica si ya existe un modelo con el mismo nombre dentro de una marca.
     * La comparación es insensible a mayúsculas/minúsculas.
     * @return true si el modelo ya existe en esa marca, false en caso contrario.
     */
    public boolean existeModelo(int idMarca, String nombre) {
        String sql = "SELECT existe_modelo(?, ?) AS existe";
        boolean existe = false;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMarca);
            ps.setString(2, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    existe = rs.getBoolean("existe");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.logError("Error al verificar modelo: " + e.getMessage());
            throw new RuntimeException("Error al verificar modelo", e);
        }
        return existe;
    }

    /**
     * Verifica si hay alguna moto que use este modelo.
     * @return true si al menos una moto referencia este modelo.
     */
    public boolean existeMotoConModelo(int idModelo) {
        String sql = "SELECT existe_moto_con_modelo(?)";
        boolean existe = false;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idModelo);
            ResultSet rs = ps.executeQuery();
            existe = rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar moto con modelo", e);
        }
        return existe;
    }

    /**
     * Elimina un modelo por su identificador.
     */
    public void eliminarModelo(int idModelo) {
        String sql = "DELETE FROM modelo WHERE id_modelo = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idModelo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar modelo", e);
        }
    }

    /**
     * Obtiene todos los modelos junto con el nombre de su marca asociada.
     * @return lista de DTOs con id y nombre del modelo e id y nombre de la marca.
     */
    public List<ModeloConMarcaDTO> listarModelosConMarca() {
        String sql = "SELECT mo.id_modelo, mo.nombre_modelo, ma.id_marca, ma.nombre_marca FROM modelo mo JOIN marca ma ON mo.id_marca = ma.id_marca ORDER BY ma.nombre_marca, mo.nombre_modelo";
        List<ModeloConMarcaDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hayFila = rs.next();
            while (hayFila) {
                lista.add(new ModeloConMarcaDTO(
                        rs.getInt("id_modelo"), rs.getString("nombre_modelo"),
                        rs.getInt("id_marca"), rs.getString("nombre_marca")
                ));
                hayFila = rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar modelos con marca", e);
        }
        return lista;
    }

    /**
     * Actualiza los datos de un modelo existente (nombre y marca asociada).
     */
    public void actualizarModelo(Modelo modelo) {
        String sql = "UPDATE modelo SET id_marca = ?, nombre_modelo = ? WHERE id_modelo = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, modelo.getIdMarca());
            ps.setString(2, modelo.getNombreModelo());
            ps.setInt(3, modelo.getIdModelo());
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.logError("Error al actualizar modelo: " + e.getMessage());
            throw new RuntimeException("Error al actualizar modelo", e);
        }
    }
}