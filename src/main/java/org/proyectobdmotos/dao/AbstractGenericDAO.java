package org.proyectobdmotos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.utils.Logger;

/**
 * Clase abstracta que implementa la lógica común del flujo CRUD.
 * Usa el patrón Template Method: define el esqueleto del algoritmo
 * y delega los detalles específicos (SQL, mapeo) a las subclases.
 *
 * @param <T>  Tipo de la entidad
 * @param <ID> Tipo de la clave primaria
 */
public abstract class AbstractGenericDAO<T, ID> implements GenericDAO<T, ID> {

    protected final Connection connection;

    public AbstractGenericDAO(Connection connection) {
        this.connection = connection;
    }

    // ===== MÉTODOS ABSTRACTOS (implementados por cada DAO concreto) =====

    protected abstract String getInsertSQL();

    protected abstract String getUpdateSQL();

    protected abstract String getDeleteSQL();

    protected abstract String getFindByIdSQL();

    protected abstract String getFindAllSQL();

    protected abstract void setInsertParameters(PreparedStatement ps, T entity) throws SQLException;

    protected abstract void setUpdateParameters(PreparedStatement ps, T entity) throws SQLException;

    protected abstract void setIdParameter(PreparedStatement ps, ID id) throws SQLException;

    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;

    // ===== IMPLEMENTACIÓN DEL FLUJO COMÚN =====

    @Override
    public void insertar(T entity) {
        try (PreparedStatement ps = connection.prepareStatement(getInsertSQL())) {
            setInsertParameters(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.logError("Error al insertar entidad: " + e.getMessage());
            throw new RuntimeException("Error al insertar entidad: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(T entity) {
        try (PreparedStatement ps = connection.prepareStatement(getUpdateSQL())) {
            setUpdateParameters(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.logError("Error al actualizar entidad: " + e.getMessage());
            throw new RuntimeException("Error al actualizar entidad: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(ID id) {
        try (PreparedStatement ps = connection.prepareStatement(getDeleteSQL())) {
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
        try (PreparedStatement ps = connection.prepareStatement(getFindByIdSQL())) {
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
        try (PreparedStatement ps = connection.prepareStatement(getFindAllSQL());
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
