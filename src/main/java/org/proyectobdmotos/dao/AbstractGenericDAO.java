package org.proyectobdmotos.dao;

import java.sql.Connection;

public abstract class AbstractGenericDAO<T, ID> implements GenericDAO<T, ID> {

    protected final Connection connection;

    public AbstractGenericDAO(Connection connection) {
        this.connection = connection;
    }
    
    /*protected abstract String getInsertSQL();
    
    protected abstract String getUpdateSQL();
    
    protected abstract String getDeleteSQL();
    
    protected abstract String getFindByIdSQL();
    
    protected abstract String getFindAllSQL();
    
    protected abstract void setInsertParameters(PreparedStatement ps, T entity) throws SQLException;
    
    protected abstract void setUpdateParameters(PreparedStatement ps, T entity) throws SQLException;
    
    protected abstract void setIdParameter(PreparedStatement ps, ID id) throws SQLException;
    
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;*/
    
    // ===== IMPLEMENTACIÓN DEL FLUJO COMÚN =====
    
    /*@Override
    public void insertar(T entity) {
        try (PreparedStatement ps = connection.prepareStatement(getInsertSQL())) {
            setInsertParameters(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar entidad: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void actualizar(T entity) {
        try (PreparedStatement ps = connection.prepareStatement(getUpdateSQL())) {
            setUpdateParameters(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar entidad: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void eliminar(ID id) {
        try (PreparedStatement ps = connection.prepareStatement(getDeleteSQL())) {
            setIdParameter(ps, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar entidad: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<T> buscarPorId(ID id) {
        try (PreparedStatement ps = connection.prepareStatement(getFindByIdSQL())) {
            setIdParameter(ps, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEntity(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar entidad por ID: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<T> listarTodos() {
        List<T> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(getFindAllSQL());
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(mapResultSetToEntity(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar entidades: " + e.getMessage(), e);
        }
    }*/
}
