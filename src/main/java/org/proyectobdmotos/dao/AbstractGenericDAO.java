package org.proyectobdmotos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.database.DatabaseConnection;
import org.proyectobdmotos.utils.Logger;

public abstract class AbstractGenericDAO<T, ID> implements GenericDAO<T, ID> {

    protected Connection connection;

    public AbstractGenericDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Obtiene una conexión viva. Si la original se cerró, la reabre automáticamente.
     */
    protected Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DatabaseConnection.getInstance();
        }
        return connection;
    }

    protected abstract String getInsertSQL();
    protected abstract String getUpdateSQL();
    protected abstract String getDeleteSQL();
    protected abstract String getFindByIdSQL();
    protected abstract String getFindAllSQL();
    protected abstract void setInsertParameters(PreparedStatement ps, T entity) throws SQLException;
    protected abstract void setUpdateParameters(PreparedStatement ps, T entity) throws SQLException;
    protected abstract void setIdParameter(PreparedStatement ps, ID id) throws SQLException;
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;

    @Override
    public void insertar(T entity) {
        try (PreparedStatement ps = getConnection().prepareStatement(getInsertSQL())) {
            setInsertParameters(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.logError("Error al insertar entidad: " + e.getMessage());
            throw new RuntimeException("Error al insertar entidad: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(T entity) {
        try (PreparedStatement ps = getConnection().prepareStatement(getUpdateSQL())) {
            setUpdateParameters(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.logError("Error al actualizar entidad: " + e.getMessage());
            throw new RuntimeException("Error al actualizar entidad: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(ID id) {
        try (PreparedStatement ps = getConnection().prepareStatement(getDeleteSQL())) {
            setIdParameter(ps, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.logError("Error al eliminar entidad: " + e.getMessage());
            throw new RuntimeException("Error al eliminar entidad: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<T> buscarPorId(ID id) {
        Optional<T> resultado = Optional.empty();
        try (PreparedStatement ps = getConnection().prepareStatement(getFindByIdSQL())) {
            setIdParameter(ps, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultado = Optional.of(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al buscar entidad por ID: " + e.getMessage());
            throw new RuntimeException("Error al buscar entidad por ID: " + e.getMessage(), e);
        }
        return resultado;
    }

    @Override
    public List<T> listarTodos() {
        List<T> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(getFindAllSQL());
             ResultSet rs = ps.executeQuery()) {
            boolean hasMore = rs.next();
            while (hasMore) {
                lista.add(mapResultSetToEntity(rs));
                hasMore = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error al listar entidades: " + e.getMessage());
            throw new RuntimeException("Error al listar entidades: " + e.getMessage(), e);
        }
        return lista;
    }
}