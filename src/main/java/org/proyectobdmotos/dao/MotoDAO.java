package org.proyectobdmotos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.proyectobdmotos.database.DatabaseConnection;
import org.proyectobdmotos.dto.MotoDTO;
import org.proyectobdmotos.dto.SituacionMotoDTO;
import org.proyectobdmotos.models.*;
import org.proyectobdmotos.utils.Logger;

public class MotoDAO extends AbstractGenericDAO<Moto, Integer> implements IMotoDAO {

    public MotoDAO(Connection connection) {
        super(connection);
    }

    // ===== MÉTODOS TEMPLATE =====

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO moto (matricula_moto, id_modelo, id_situacion, "
             + "cant_km_recorridos, id_color) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE moto SET matricula_moto = ?, id_modelo = ?, id_situacion = ?, "
             + "cant_km_recorridos = ?, id_color = ? WHERE id_moto = ?";
    }

    @Override
    protected String getDeleteSQL() {
        return "DELETE FROM moto WHERE id_moto = ?";
    }

    @Override
    protected String getFindByIdSQL() {
        return "SELECT * FROM moto WHERE id_moto = ?";
    }

    @Override
    protected String getFindAllSQL() {
        return "SELECT * FROM moto ORDER BY matricula_moto";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Moto moto) throws SQLException {
        ps.setString(1, moto.getMatriculaMoto());
        ps.setInt(2, moto.getIdModelo());
        ps.setInt(3, moto.getSituacion().getId());
        ps.setDouble(4, moto.getCantKmRecorridos());
        ps.setInt(5, moto.getIdColor());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Moto moto) throws SQLException {
        ps.setString(1, moto.getMatriculaMoto());
        ps.setInt(2, moto.getIdModelo());
        ps.setInt(3, moto.getSituacion().getId());
        ps.setDouble(4, moto.getCantKmRecorridos());
        ps.setInt(5, moto.getIdColor());
        ps.setInt(6, moto.getIdMoto());
    }

    @Override
    protected void setIdParameter(PreparedStatement ps, Integer id) throws SQLException {
        ps.setInt(1, id);
    }

    @Override
    protected Moto mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new Moto(
            rs.getInt("id_moto"),
            rs.getString("matricula_moto"),
            rs.getInt("id_modelo"),
            Situacion.fromId(rs.getInt("id_situacion")),
            rs.getDouble("cant_km_recorridos"),
            rs.getInt("id_color")
        );
    }

    /**
     * Override para capturar el id_moto generado (SERIAL) tras el INSERT.
     */
    @Override
    public void insertar(Moto moto) {
        try (PreparedStatement ps = connection.prepareStatement(getInsertSQL(), Statement.RETURN_GENERATED_KEYS)) {
            setInsertParameters(ps, moto);
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    moto.setIdMoto(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al insertar moto: " + e.getMessage());
            throw new RuntimeException("Error al insertar moto: " + e.getMessage(), e);
        }
    }

    // ===== MÉTODOS ESPECÍFICOS =====

    @Override
    public Optional<Moto> buscarPorMatricula(String matricula) {
        String sql = "SELECT * FROM moto WHERE matricula_moto = ?";
        Optional<Moto> resultado = Optional.empty();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultado = Optional.of(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al buscar moto por matrícula: " + e.getMessage());
            throw new RuntimeException("Error al buscar moto por matrícula: " + e.getMessage(), e);
        }
        return resultado;
    }

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
                   si.nombre_situacion AS situacion_nombre,
                   co.fecha_fin
            FROM moto m
            JOIN situacion si ON m.id_situacion = si.id_situacion
            JOIN modelo mo ON m.id_modelo = mo.id_modelo
            JOIN marca ma ON mo.id_marca = ma.id_marca
            LEFT JOIN contrato co ON m.id_moto = co.id_moto
                AND co.fecha_entrega IS NULL
            ORDER BY si.nombre, m.matricula_moto
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
                    Situacion.fromValor(rs.getString("situacion_nombre")),
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
    public void cambiarEstado(Integer idMoto, Situacion nuevaSituacion) {
        String sql = "UPDATE moto SET id_situacion = ? WHERE id_moto = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, nuevaSituacion.getId());
            ps.setInt(2, idMoto);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.logError("Error al cambiar estado de moto: " + e.getMessage());
            throw new RuntimeException("Error al cambiar estado de moto: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean estaDisponible(Integer idMoto) {
        String sql = "SELECT id_situacion FROM moto WHERE id_moto = ?";
        boolean disponible = false;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idMoto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    disponible = rs.getInt("id_situacion") == Situacion.DISPONIBLE.getId();
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al verificar disponibilidad: " + e.getMessage());
            Logger.logError("Error en la funcion de estaDisponible en MotoDAO " + e.getMessage());
            throw new RuntimeException("Error al verificar disponibilidad: " + e.getMessage(), e);
        }
        return disponible;
    }

    // Nota: Respetare como se colocaron las cosas en las anteriores
    // lineas
    public ArrayList<Color> obtenerColores() throws SQLException {
        String sql = "SELECT * FROM obtener_colores()";
        ArrayList<Color> colores = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // Construir el objeto Color con los datos completos
                Color color = new Color(
                        rs.getInt("id_color"),
                        rs.getString("nombre_color")
                );
                colores.add(color);
            }
        } catch (SQLException e) {
            Logger.logError("Error en obtenerColores: " + e.getMessage());
            throw new RuntimeException("Error al cargar los colores", e);
        }
        return colores;
    }

    // ===== MÉTODOS PARA MARCAS Y MODELOS =====



    public ArrayList<Marca> obtenerMarcas() throws SQLException {
        String sql = "SELECT * FROM obtener_marcas()";
        ArrayList<Marca> marcas = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                marcas.add(new Marca(
                        rs.getInt("id_marca"),
                        rs.getString("nombre_marca")
                ));
            }
        } catch (SQLException e) {
            Logger.logError("Error al obtener marcas: " + e.getMessage());
            throw new RuntimeException("Error al obtener marcas", e);
        }
        return marcas;
    }


    public ArrayList<Modelo> obtenerModelosPorMarca(int idMarca) throws SQLException {
        String sql = "SELECT * FROM obtener_modelos_por_marca(?)";
        ArrayList<Modelo> modelos = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idMarca);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelos.add(new Modelo(
                            rs.getInt("id_modelo"),
                            rs.getInt("id_marca"),
                            rs.getString("nombre_modelo")
                    ));
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al obtener modelos por marca: " + e.getMessage());
            throw new RuntimeException("Error al obtener modelos por marca", e);
        }
        return modelos;
    }

    public Modelo obtenerModeloPorId(int idModelo) throws SQLException {
        String sql = "SELECT * FROM obtener_modelo_por_id(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idModelo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Modelo(
                            rs.getInt("id_modelo"),
                            rs.getInt("id_marca"),
                            rs.getString("nombre_modelo")
                    );
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al obtener modelo por id: " + e.getMessage());
            throw new RuntimeException("Error al obtener modelo por id", e);
        }
        return null;
    }

    public Marca obtenerMarcaPorId(int idMarca) throws SQLException {
        String sql = "SELECT * FROM obtener_marca_por_id(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idMarca);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Marca(
                            rs.getInt("id_marca"),
                            rs.getString("nombre_marca")
                    );
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al obtener marca por id: " + e.getMessage());
            throw new RuntimeException("Error al obtener marca por id", e);
        }
        return null;
    }

    public int obtenerIdColorPorNombre(String nombreColor) throws SQLException {
        String sql = "SELECT obtener_id_color_por_nombre(?) AS id_color";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nombreColor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_color");
                    if (rs.wasNull()) {
                        throw new RuntimeException("Color no encontrado: " + nombreColor);
                    }
                    return id;
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al obtener id color por nombre: " + e.getMessage());
            throw new RuntimeException("Error al obtener id color por nombre", e);
        }
        throw new RuntimeException("Color no encontrado: " + nombreColor);
    }

    public String obtenerNombreColorPorId(int idColor) throws SQLException {
        String sql = "SELECT obtener_nombre_color_por_id(?) AS nombre_color";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idColor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("nombre_color");
                    if (rs.wasNull()) {
                        throw new RuntimeException("Color no encontrado con id: " + idColor);
                    }
                    return nombre;
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al obtener nombre color por id: " + e.getMessage());
            throw new RuntimeException("Error al obtener nombre color por id", e);
        }
        throw new RuntimeException("Color no encontrado con id: " + idColor);
    }

}
