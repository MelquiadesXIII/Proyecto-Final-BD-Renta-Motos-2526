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

import org.proyectobdmotos.dto.*;
import org.proyectobdmotos.models.*;
import org.proyectobdmotos.utils.Logger;

public class MotoDAO extends AbstractGenericDAO<Moto, Integer> implements IMotoDAO {

    public MotoDAO(Connection connection) {
        super(connection);
    }

    // -----------------------------------------------------------------
    // Métodos template (AbstractGenericDAO)
    // -----------------------------------------------------------------

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

    /**
     * Convierte una fila de ResultSet en un objeto Moto.
     * Mapea todas las columnas de la tabla moto.
     */
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

    // -----------------------------------------------------------------
    // Operaciones CRUD implementadas
    // -----------------------------------------------------------------

    /**
     * Inserta una nueva moto y le asigna el ID generado por la base de datos.
     */
    @Override
    public void insertar(Moto moto) {
        try (PreparedStatement ps = getConnection().prepareStatement(getInsertSQL(), Statement.RETURN_GENERATED_KEYS)) {
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

    /**
     * Busca una moto por su matrícula exacta.
     * @return Optional con la moto si existe, vacío si no se encuentra.
     */
    @Override
    public Optional<Moto> buscarPorMatricula(String matricula) {
        String sql = "SELECT * FROM moto WHERE matricula_moto = ?";
        Optional<Moto> resultado = Optional.empty();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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

    // -----------------------------------------------------------------
    // Listados y reportes de motos
    // -----------------------------------------------------------------

    /**
     * Lista las motos con su kilometraje, marca y modelo.
     * Ordenado por kilometraje descendente.
     */
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
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
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

    /**
     * Obtiene la situación actual de todas las motos (incluyendo la fecha de fin
     * del contrato activo si la moto está alquilada).
     */
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
            ORDER BY si.nombre_situacion, m.matricula_moto
            """;

        List<SituacionMotoDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
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

    /**
     * Cambia el estado (situación) de una moto.
     */
    @Override
    public void cambiarEstado(Integer idMoto, Situacion nuevaSituacion) {
        String sql = "UPDATE moto SET id_situacion = ? WHERE id_moto = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, nuevaSituacion.getId());
            ps.setInt(2, idMoto);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.logError("Error al cambiar estado de moto: " + e.getMessage());
            throw new RuntimeException("Error al cambiar estado de moto: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si una moto está en situación DISPONIBLE.
     * @return true si la moto está disponible, false en caso contrario.
     */
    @Override
    public boolean estaDisponible(Integer idMoto) {
        String sql = "SELECT id_situacion FROM moto WHERE id_moto = ?";
        boolean disponible = false;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMoto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    disponible = rs.getInt("id_situacion") == Situacion.DISPONIBLE.getId();
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al verificar disponibilidad: " + e.getMessage());
            throw new RuntimeException("Error al verificar disponibilidad: " + e.getMessage(), e);
        }
        return disponible;
    }

    // -----------------------------------------------------------------
    // Catálogos (colores, marcas, modelos)
    // -----------------------------------------------------------------

    /**
     * Obtiene todos los colores desde la función obtener_colores().
     */
    public ArrayList<Color> obtenerColores() throws SQLException {
        String sql = "SELECT * FROM obtener_colores()";
        ArrayList<Color> colores = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hayFila = rs.next();
            while (hayFila) {
                colores.add(new Color(
                        rs.getInt("id_color"),
                        rs.getString("nombre_color")
                ));
                hayFila = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error en obtenerColores: " + e.getMessage());
            throw new RuntimeException("Error al cargar los colores", e);
        }
        return colores;
    }

    /**
     * Obtiene todas las marcas desde la función obtener_marcas().
     */
    public ArrayList<Marca> obtenerMarcas() throws SQLException {
        String sql = "SELECT * FROM obtener_marcas()";
        ArrayList<Marca> marcas = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hayFila = rs.next();
            while (hayFila) {
                marcas.add(new Marca(
                        rs.getInt("id_marca"),
                        rs.getString("nombre_marca")
                ));
                hayFila = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error al obtener marcas: " + e.getMessage());
            throw new RuntimeException("Error al obtener marcas", e);
        }
        return marcas;
    }

    /**
     * Obtiene los modelos que pertenecen a una marca específica.
     */
    public ArrayList<Modelo> obtenerModelosPorMarca(int idMarca) throws SQLException {
        String sql = "SELECT * FROM obtener_modelos_por_marca(?)";
        ArrayList<Modelo> modelos = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMarca);
            try (ResultSet rs = ps.executeQuery()) {
                boolean hayFila = rs.next();
                while (hayFila) {
                    modelos.add(new Modelo(
                            rs.getInt("id_modelo"),
                            rs.getInt("id_marca"),
                            rs.getString("nombre_modelo")
                    ));
                    hayFila = rs.next();
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al obtener modelos por marca: " + e.getMessage());
            throw new RuntimeException("Error al obtener modelos por marca", e);
        }
        return modelos;
    }

    /**
     * Busca un modelo por su ID utilizando la función obtener_modelo_por_id().
     * @return el modelo encontrado, o null si no existe.
     */
    public Modelo obtenerModeloPorId(int idModelo) throws SQLException {
        String sql = "SELECT * FROM obtener_modelo_por_id(?)";
        Modelo modelo = null;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idModelo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    modelo = new Modelo(
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
        return modelo;
    }

    /**
     * Busca una marca por su ID utilizando la función obtener_marca_por_id().
     * @return la marca encontrada, o null si no existe.
     */
    public Marca obtenerMarcaPorId(int idMarca) throws SQLException {
        String sql = "SELECT * FROM obtener_marca_por_id(?)";
        Marca marca = null;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMarca);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    marca = new Marca(
                            rs.getInt("id_marca"),
                            rs.getString("nombre_marca")
                    );
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al obtener marca por id: " + e.getMessage());
            throw new RuntimeException("Error al obtener marca por id", e);
        }
        return marca;
    }

    /**
     * Obtiene el id de un color a partir de su nombre.
     * @return el id del color.
     * @throws RuntimeException si el color no existe.
     */
    public int obtenerIdColorPorNombre(String nombreColor) throws SQLException {
        String sql = "SELECT obtener_id_color_por_nombre(?) AS id_color";
        Integer idColor = null;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, nombreColor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_color");
                    if (!rs.wasNull()) {
                        idColor = id;
                    }
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al obtener id color por nombre: " + e.getMessage());
            throw new RuntimeException("Error al obtener id color por nombre", e);
        }
        if (idColor == null) {
            throw new RuntimeException("Color no encontrado: " + nombreColor);
        }
        return idColor;
    }

    /**
     * Obtiene el nombre de un color a partir de su id.
     * @return el nombre del color.
     * @throws RuntimeException si el color no existe.
     */
    public String obtenerNombreColorPorId(int idColor) throws SQLException {
        String sql = "SELECT obtener_nombre_color_por_id(?) AS nombre_color";
        String nombreColor = null;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idColor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("nombre_color");
                    if (!rs.wasNull()) {
                        nombreColor = nombre;
                    }
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al obtener nombre color por id: " + e.getMessage());
            throw new RuntimeException("Error al obtener nombre color por id", e);
        }
        if (nombreColor == null) {
            throw new RuntimeException("Color no encontrado con id: " + idColor);
        }
        return nombreColor;
    }

    // -----------------------------------------------------------------
    // Reportes
    // -----------------------------------------------------------------

    /**
     * Obtiene el reporte general de motos desde la función reporte_motos().
     */
    public List<MotoRepDTO> listarMotosReporte() {
        String sql = "SELECT * FROM reporte_motos()";
        List<MotoRepDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hayFila = rs.next();
            while (hayFila) {
                lista.add(new MotoRepDTO(
                        rs.getDate("fecha_reporte") != null ? rs.getDate("fecha_reporte").toLocalDate() : null,
                        rs.getString("matricula_moto"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getString("color"),
                        rs.getDouble("cant_km_recorridos")
                ));
                hayFila = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error al listar motos reporte: " + e.getMessage());
            throw new RuntimeException("Error al listar motos reporte", e);
        }
        return lista;
    }

    /**
     * Obtiene el reporte de situación de motos desde la función reporte_situacion_motos().
     */
    public List<SitMotoRepDTO> listarSituacionMotosReporte() {
        String sql = "SELECT * FROM reporte_situacion_motos()";
        List<SitMotoRepDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean hayFila = rs.next();
            while (hayFila) {
                lista.add(new SitMotoRepDTO(
                        rs.getDate("fecha_reporte") != null ? rs.getDate("fecha_reporte").toLocalDate() : null,
                        rs.getString("matricula_marca"),
                        rs.getString("situacion"),
                        rs.getDate("fecha_fin_contrato") != null ? rs.getDate("fecha_fin_contrato").toLocalDate() : null
                ));
                hayFila = rs.next();
            }
        } catch (SQLException e) {
            Logger.logError("Error al listar situación motos reporte: " + e.getMessage());
            throw new RuntimeException("Error al listar situación motos reporte", e);
        }
        return lista;
    }

    // -----------------------------------------------------------------
    // Disponibilidad y solapamiento
    // -----------------------------------------------------------------

    /**
     * Lista las motos que están disponibles en un rango de fechas,
     * utilizando la función motos_disponibles_entre().
     * @return lista de motos (solo datos básicos).
     */
    public List<Moto> listarMotosDisponiblesEntre(LocalDate inicio, LocalDate fin) {
        String sql = "SELECT * FROM motos_disponibles_entre(?, ?)";
        List<Moto> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(inicio));
            ps.setDate(2, java.sql.Date.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                boolean hayFila = rs.next();
                while (hayFila) {
                    lista.add(mapResultSetToEntity(rs));
                    hayFila = rs.next();
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al listar motos disponibles en rango: " + e.getMessage());
            throw new RuntimeException("Error al listar motos disponibles en rango", e);
        }
        return lista;
    }

    /**
     * Lista las motos disponibles en un rango de fechas con detalle de marca,
     * modelo y color, utilizando la función obtener_motos_libres().
     * @return lista de DTOs con información completa para mostrar en la interfaz.
     */
    public List<MotoDisponibleDTO> listarMotosDisponiblesDetalle(LocalDate inicio, LocalDate fin) {
        String sql = "SELECT * FROM obtener_motos_libres(?, ?)";
        List<MotoDisponibleDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(inicio));
            ps.setDate(2, java.sql.Date.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                boolean hayFila = rs.next();
                while (hayFila) {
                    lista.add(new MotoDisponibleDTO(
                            rs.getInt("id_moto"),
                            rs.getString("matricula_moto"),
                            rs.getString("nombre_marca"),
                            rs.getString("nombre_modelo"),
                            rs.getString("nombre_color")
                    ));
                    hayFila = rs.next();
                }
            }
        } catch (SQLException e) {
            Logger.logError("Error al listar motos disponibles detalle: " + e.getMessage());
            throw new RuntimeException("Error al listar motos disponibles detalle", e);
        }
        return lista;
    }

    /**
     * Verifica si una moto ya tiene un contrato que se solape con el período indicado.
     * @return true si existe solapamiento, false en caso contrario.
     */
    public boolean existeSolapamiento(int idMoto, LocalDate inicio, LocalDate fin) {
        String sql = "SELECT COUNT(*) FROM contrato WHERE id_moto = ? AND (fecha_inicio, fecha_fin) OVERLAPS (?, ?)";
        boolean solapamiento = false;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMoto);
            ps.setDate(2, java.sql.Date.valueOf(inicio));
            ps.setDate(3, java.sql.Date.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    solapamiento = rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.logError("Error al comprobar solapamiento: " + e.getMessage());
            throw new RuntimeException("Error al comprobar solapamiento", e);
        }
        return solapamiento;
    }
}