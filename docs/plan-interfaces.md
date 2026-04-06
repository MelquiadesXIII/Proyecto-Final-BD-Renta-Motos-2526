# Plan de Interfaces y Clases Abstractas para DAOs

## Índice
1. [Análisis y Conclusión](#análisis-y-conclusión)
2. [Principio de Segregación de Interfaces (ISP)](#principio-de-segregación-de-interfaces-isp)
3. [Por qué usar `<T, ID>` en lugar de solo `<E>` o `Object`](#por-qué-usar-t-id-en-lugar-de-solo-e-o-object)
4. [Arquitectura Propuesta](#arquitectura-propuesta)
5. [Implementación con Clase Abstracta (Opción 1)](#implementación-con-clase-abstracta-opción-1)
6. [Implementación sin Clase Abstracta (Opción 2)](#implementación-sin-clase-abstracta-opción-2)
7. [Comparación de Enfoques](#comparación-de-enfoques)
8. [Recomendación Final](#recomendación-final)

---

## Análisis y Conclusión

Después de analizar los requisitos del sistema Renta Motos Habana y los 3 DAOs principales (ClienteDAO, MotoDAO, ContratoDAO), se identificaron los siguientes comportamientos:

### Operaciones Comunes (CRUD Básico)
Estas operaciones son comunes a los 3 DAOs:
- **Insertar** una entidad
- **Actualizar** una entidad
- **Eliminar** por ID
- **Buscar por ID**
- **Listar todos**

### Operaciones Específicas por DAO

**ClienteDAO:**
- Listar clientes por municipio con estadísticas
- Obtener clientes incumplidores
- Eliminar cliente en cascada con contratos

**MotoDAO:**
- Listar motos con kilometraje
- Listar situación de motos
- Cambiar estado de moto automáticamente
- Validar disponibilidad antes de alquilar

**ContratoDAO:**
- Listar contratos completos con joins
- Resumen por marcas y modelos
- Resumen por municipios
- Ingresos del año por mes

### Conclusión
**SÍ se deben crear interfaces**, siguiendo el Principio de Segregación de Interfaces (ISP):
- Una interfaz genérica `GenericDAO<T, ID>` para operaciones CRUD comunes
- Interfaces específicas para cada DAO con sus métodos particulares
- Opcionalmente, una clase abstracta para reutilizar código de implementación

---

## Principio de Segregación de Interfaces (ISP)

El ISP establece que:
> "Ningún cliente debe ser forzado a depender de métodos que no usa"

### Aplicado a nuestro proyecto:

✅ **CORRECTO**: Interfaces pequeñas y cohesivas
```java
interface GenericDAO<T, ID> {
    void insertar(T entity);
    void actualizar(T entity);
    void eliminar(ID id);
    Optional<T> buscarPorId(ID id);
    List<T> listarTodos();
}

interface IClienteDAO extends GenericDAO<Cliente, String> {
    List<ClienteDTO> listarClientesPorMunicipio();
    List<Cliente> obtenerClientesIncumplidores();
}
```

❌ **INCORRECTO**: Interfaz monolítica
```java
interface DAO {
    void insertar(Object entity);
    void actualizar(Object entity);
    void eliminar(Object id);
    List<?> listarTodos();
    List<?> listarPorMunicipio();        // No todos lo necesitan
    List<?> obtenerIncumplidores();       // No todos lo necesitan
    List<?> listarSituacion();            // No todos lo necesitan
    // ... fuerza métodos innecesarios
}
```

---

## Por qué usar `<T, ID>` en lugar de solo `<E>` o `Object`

### Problema con `Object`
```java
interface GenericDAO {
    void eliminar(Object id);              // ❌ Sin type safety
    Object buscarPorId(Object id);         // ❌ Requiere casting
}

class ClienteDAO implements GenericDAO {
    public void eliminar(Object id) {
        String ci = (String) id;           // ❌ Casting manual
        // ... puede fallar en runtime
    }
}
```

### Problema con solo `<E>`
```java
interface GenericDAO<E> {
    void eliminar(E entity);               // ❌ Necesitas toda la entidad
    E buscarPorId(???);                    // ❌ ¿Qué tipo usar para el ID?
}

class ClienteDAO implements GenericDAO<Cliente> {
    public void eliminar(Cliente cliente) {
        // ❌ Ineficiente: construir toda la entidad solo para eliminar
        String ci = cliente.getCiCliente();
        // ...
    }
}
```

### Solución con `<T, ID>`
```java
interface GenericDAO<T, ID> {
    void insertar(T entity);
    void actualizar(T entity);
    void eliminar(ID id);                  // ✅ Solo el ID, type safe
    Optional<T> buscarPorId(ID id);        // ✅ Type safe
    List<T> listarTodos();
}

class ClienteDAO implements GenericDAO<Cliente, String> {
    public void eliminar(String ci) {
        // ✅ Type safe, sin casting, eficiente
        String sql = "DELETE FROM cliente WHERE ci_cliente = ?";
        // ...
    }
}
```

### Ventajas de `<T, ID>`:
1. **Type Safety**: El compilador verifica tipos en tiempo de compilación
2. **Sin Casteos**: No necesitas casting manual
3. **Eficiencia**: No construyes entidades completas para operaciones simples
4. **Flexibilidad**: Cada DAO puede tener diferente tipo de ID (String, Long, clave compuesta)
5. **Estándar de la industria**: Usado en Spring Data JPA, Hibernate, etc.

---

## Arquitectura Propuesta

```
┌─────────────────────────────────┐
│   GenericDAO<T, ID>             │ ← Interfaz genérica (CRUD común)
│   - insertar(T)                 │
│   - actualizar(T)               │
│   - eliminar(ID)                │
│   - buscarPorId(ID)             │
│   - listarTodos()               │
└─────────────────────────────────┘
          ▲           ▲
          │           │
    ┌─────┴─────┐     └──────────────────┐
    │           │                        │
┌───┴────────┐ ┌┴─────────────┐  ┌──────┴────────┐
│IClienteDAO │ │  IMotoDAO    │  │ IContratoDAO  │ ← Interfaces específicas
│+ métodos   │ │+ métodos     │  │+ métodos      │
│  específicos│ │  específicos│  │  específicos  │
└────────────┘ └──────────────┘  └───────────────┘
      ▲              ▲                   ▲
      │              │                   │
┌─────┴──────┐ ┌────┴─────┐  ┌──────────┴────┐
│ClienteDAO  │ │ MotoDAO  │  │  ContratoDAO  │ ← Implementaciones concretas
│(SQL)       │ │ (SQL)    │  │  (SQL)        │   (con lógica SQL específica)
└────────────┘ └──────────┘  └───────────────┘
```

**Nota sobre ContratoDAO:**
- Tiene clave compuesta (fechaInicio + matriculaMoto)
- Puede no extender `GenericDAO` si la clave compuesta complica la implementación
- O crear un tipo `ContratoId` para encapsular la clave compuesta

---

## Implementación con Clase Abstracta (Opción 1)

### Beneficios:
- ✅ **DRY**: No repites el código de try-catch y manejo de PreparedStatement
- ✅ **Reutilización**: Lógica común de flujo CRUD
- ✅ **Mantenibilidad**: Cambios en el flujo CRUD se hacen en un solo lugar
- ✅ **Type Safety**: Con genéricos `<T, ID>`

### Estructura:

#### 1. Interfaz Genérica
```java
package org.proyectobdmotos.dao;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz genérica para operaciones CRUD comunes.
 * Aplica el Principio de Segregación de Interfaces (ISP):
 * solo incluye métodos que TODOS los DAOs necesitan.
 * 
 * @param <T> Tipo de la entidad (Cliente, Moto, etc.)
 * @param <ID> Tipo de la clave primaria (String, Long, clave compuesta, etc.)
 */
public interface GenericDAO<T, ID> {
    
    /**
     * Inserta una nueva entidad en la base de datos.
     * @param entity Entidad a insertar
     */
    void insertar(T entity);
    
    /**
     * Actualiza una entidad existente en la base de datos.
     * @param entity Entidad con los datos actualizados
     */
    void actualizar(T entity);
    
    /**
     * Elimina una entidad por su ID.
     * @param id Identificador de la entidad a eliminar
     */
    void eliminar(ID id);
    
    /**
     * Busca una entidad por su ID.
     * @param id Identificador de la entidad
     * @return Optional con la entidad si existe, vacío si no
     */
    Optional<T> buscarPorId(ID id);
    
    /**
     * Lista todas las entidades.
     * @return Lista de todas las entidades
     */
    List<T> listarTodos();
}
```

#### 2. Clase Abstracta (Implementación del Flujo Común)
```java
package org.proyectobdmotos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Clase abstracta que implementa la lógica común del flujo CRUD.
 * Usa el patrón Template Method: define el esqueleto del algoritmo
 * y delega los detalles específicos (SQL, mapeo) a las subclases.
 * 
 * @param <T> Tipo de la entidad
 * @param <ID> Tipo de la clave primaria
 */
public abstract class AbstractGenericDAO<T, ID> implements GenericDAO<T, ID> {
    
    protected final Connection connection;
    
    public AbstractGenericDAO(Connection connection) {
        this.connection = connection;
    }
    
    // ===== MÉTODOS ABSTRACTOS (implementados por cada DAO concreto) =====
    
    /**
     * @return SQL para insertar la entidad
     */
    protected abstract String getInsertSQL();
    
    /**
     * @return SQL para actualizar la entidad
     */
    protected abstract String getUpdateSQL();
    
    /**
     * @return SQL para eliminar la entidad
     */
    protected abstract String getDeleteSQL();
    
    /**
     * @return SQL para buscar por ID
     */
    protected abstract String getFindByIdSQL();
    
    /**
     * @return SQL para listar todas las entidades
     */
    protected abstract String getFindAllSQL();
    
    /**
     * Configura los parámetros del PreparedStatement para inserción.
     * @param ps PreparedStatement a configurar
     * @param entity Entidad con los datos
     */
    protected abstract void setInsertParameters(PreparedStatement ps, T entity) throws SQLException;
    
    /**
     * Configura los parámetros del PreparedStatement para actualización.
     * @param ps PreparedStatement a configurar
     * @param entity Entidad con los datos
     */
    protected abstract void setUpdateParameters(PreparedStatement ps, T entity) throws SQLException;
    
    /**
     * Configura el parámetro del ID en el PreparedStatement.
     * @param ps PreparedStatement a configurar
     * @param id ID de la entidad
     */
    protected abstract void setIdParameter(PreparedStatement ps, ID id) throws SQLException;
    
    /**
     * Mapea un ResultSet a una entidad.
     * @param rs ResultSet con los datos
     * @return Entidad mapeada
     */
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;
    
    // ===== IMPLEMENTACIÓN DEL FLUJO COMÚN =====
    
    @Override
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
    }
}
```

#### 3. Interfaz Específica de Cliente
```java
package org.proyectobdmotos.dao;

import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.dto.ClienteDTO;
import java.util.List;

/**
 * Interfaz específica para ClienteDAO.
 * Extiende GenericDAO para heredar operaciones CRUD
 * y agrega métodos específicos de Cliente.
 */
public interface IClienteDAO extends GenericDAO<Cliente, String> {
    
    /**
     * Lista clientes agrupados por municipio con estadísticas.
     * Reporte requerido en líneas 18-26 del documento.
     * @return Lista de DTOs con datos del reporte
     */
    List<ClienteDTO> listarClientesPorMunicipio();
    
    /**
     * Obtiene clientes que entregaron la moto después de la fecha pactada.
     * Reporte requerido en líneas 58-62 del documento.
     * @return Lista de clientes incumplidores
     */
    List<Cliente> obtenerClientesIncumplidores();
    
    /**
     * Elimina un cliente y todos sus contratos (cascada).
     * Requisito en línea 101 del documento.
     * @param ci CI del cliente a eliminar
     */
    void eliminarConCascada(String ci);
}
```

#### 4. Implementación Concreta de ClienteDAO
```java
package org.proyectobdmotos.dao;

import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.models.Sexo;
import org.proyectobdmotos.dto.ClienteDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación concreta del DAO de Cliente.
 * Extiende AbstractGenericDAO para heredar la lógica del flujo CRUD
 * e implementa IClienteDAO para los métodos específicos.
 */
public class ClienteDAO extends AbstractGenericDAO<Cliente, String> implements IClienteDAO {
    
    public ClienteDAO(Connection connection) {
        super(connection);
    }
    
    // ===== IMPLEMENTACIÓN DE MÉTODOS ABSTRACTOS (SQL específico) =====
    
    @Override
    protected String getInsertSQL() {
        return "INSERT INTO cliente (ci_cliente, nombre_cliente, primer_apellido, " +
               "segundo_apellido, edad, sexo, numero_contacto, id_municipio) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }
    
    @Override
    protected String getUpdateSQL() {
        return "UPDATE cliente SET nombre_cliente = ?, primer_apellido = ?, " +
               "segundo_apellido = ?, edad = ?, sexo = ?, numero_contacto = ?, " +
               "id_municipio = ? WHERE ci_cliente = ?";
    }
    
    @Override
    protected String getDeleteSQL() {
        return "DELETE FROM cliente WHERE ci_cliente = ?";
    }
    
    @Override
    protected String getFindByIdSQL() {
        return "SELECT * FROM cliente WHERE ci_cliente = ?";
    }
    
    @Override
    protected String getFindAllSQL() {
        return "SELECT * FROM cliente ORDER BY nombre_cliente, primer_apellido";
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, Cliente cliente) throws SQLException {
        ps.setString(1, cliente.getCiCliente());
        ps.setString(2, cliente.getNombreCLiente());
        ps.setString(3, cliente.getPrimerApellido());
        ps.setString(4, cliente.getSegundoApellido());
        ps.setInt(5, cliente.getEdad());
        ps.setString(6, cliente.getSexo().name());
        ps.setString(7, cliente.getNumeroContacto());
        ps.setString(8, cliente.getIdMunicipio());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, Cliente cliente) throws SQLException {
        ps.setString(1, cliente.getNombreCLiente());
        ps.setString(2, cliente.getPrimerApellido());
        ps.setString(3, cliente.getSegundoApellido());
        ps.setInt(4, cliente.getEdad());
        ps.setString(5, cliente.getSexo().name());
        ps.setString(6, cliente.getNumeroContacto());
        ps.setString(7, cliente.getIdMunicipio());
        ps.setString(8, cliente.getCiCliente()); // WHERE clause
    }
    
    @Override
    protected void setIdParameter(PreparedStatement ps, String ci) throws SQLException {
        ps.setString(1, ci);
    }
    
    @Override
    protected Cliente mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getString("ci_cliente"),
            rs.getString("nombre_cliente"),
            rs.getString("primer_apellido"),
            rs.getString("segundo_apellido"),
            rs.getInt("edad"),
            Sexo.valueOf(rs.getString("sexo")),
            rs.getString("numero_contacto"),
            rs.getString("id_municipio")
        );
    }
    
    // ===== MÉTODOS ESPECÍFICOS DE CLIENTE =====
    
    @Override
    public List<ClienteDTO> listarClientesPorMunicipio() {
        String sql = """
            SELECT 
                c.ci_cliente,
                c.nombre_cliente,
                c.primer_apellido,
                c.segundo_apellido,
                m.nombre_municipio,
                COUNT(ct.fecha_inicio) as cant_alquileres,
                COALESCE(SUM(
                    (DATEDIFF(ct.fecha_fin, ct.fecha_inicio) * ?) + 
                    (ct.dias_prorroga * ?) + 
                    (CASE WHEN ct.seguro_adicional = 1 THEN ? ELSE 0 END)
                ), 0) as total_alquileres
            FROM cliente c
            INNER JOIN municipio m ON c.id_municipio = m.id_municipio
            LEFT JOIN contrato ct ON c.ci_cliente = ct.ci_cliente
            GROUP BY c.ci_cliente, m.nombre_municipio
            ORDER BY m.nombre_municipio, c.nombre_cliente
            """;
        
        List<ClienteDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // Aquí irían las tarifas como parámetros
            // ps.setDouble(1, Contrato.getTarifaNormal());
            // ps.setDouble(2, Contrato.getTarifaProrroga());
            // ps.setDouble(3, COSTO_SEGURO);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ClienteDTO dto = new ClienteDTO(
                        rs.getString("ci_cliente"),
                        rs.getString("nombre_cliente") + " " + 
                            rs.getString("primer_apellido") + " " +
                            rs.getString("segundo_apellido"),
                        rs.getString("nombre_municipio"),
                        rs.getInt("cant_alquileres"),
                        rs.getDouble("total_alquileres")
                    );
                    lista.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar clientes por municipio: " + e.getMessage(), e);
        }
        return lista;
    }
    
    @Override
    public List<Cliente> obtenerClientesIncumplidores() {
        String sql = """
            SELECT DISTINCT c.*
            FROM cliente c
            INNER JOIN contrato ct ON c.ci_cliente = ct.ci_cliente
            WHERE ct.fecha_entrega IS NOT NULL 
              AND ct.fecha_entrega > ct.fecha_fin
            ORDER BY c.nombre_cliente, c.primer_apellido
            """;
        
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener clientes incumplidores: " + e.getMessage(), e);
        }
        return lista;
    }
    
    @Override
    public void eliminarConCascada(String ci) {
        try {
            connection.setAutoCommit(false);
            
            // Primero eliminar contratos (por integridad referencial)
            String sqlContratos = "DELETE FROM contrato WHERE ci_cliente = ?";
            try (PreparedStatement ps = connection.prepareStatement(sqlContratos)) {
                ps.setString(1, ci);
                ps.executeUpdate();
            }
            
            // Luego eliminar cliente
            eliminar(ci);
            
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error al hacer rollback: " + ex.getMessage(), ex);
            }
            throw new RuntimeException("Error al eliminar cliente con cascada: " + e.getMessage(), e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                // Log error
            }
        }
    }
}
```

#### 5. Ejemplo Similar para MotoDAO
```java
package org.proyectobdmotos.dao;

import org.proyectobdmotos.models.Moto;
import org.proyectobdmotos.models.Situacion;
import org.proyectobdmotos.dto.MotoDTO;
import org.proyectobdmotos.dto.SituacionMotoDTO;
import java.util.List;

public interface IMotoDAO extends GenericDAO<Moto, String> {
    List<MotoDTO> listarMotosConKilometraje();
    List<SituacionMotoDTO> listarSituacionMotos();
    void cambiarEstado(String matricula, Situacion nuevaSituacion);
    boolean estaDisponible(String matricula);
}

public class MotoDAO extends AbstractGenericDAO<Moto, String> implements IMotoDAO {
    
    public MotoDAO(Connection connection) {
        super(connection);
    }
    
    @Override
    protected String getInsertSQL() {
        return "INSERT INTO moto (matricula_moto, id_modelo, situacion, " +
               "cant_km_recorridos, id_color) VALUES (?, ?, ?, ?, ?)";
    }
    
    @Override
    protected String getUpdateSQL() {
        return "UPDATE moto SET id_modelo = ?, situacion = ?, " +
               "cant_km_recorridos = ?, id_color = ? WHERE matricula_moto = ?";
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
        ps.setString(3, moto.getSituacion().name());
        ps.setDouble(4, moto.getCantKmRecorridos());
        ps.setString(5, moto.getIdColor());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, Moto moto) throws SQLException {
        ps.setString(1, moto.getIdModelo());
        ps.setString(2, moto.getSituacion().name());
        ps.setDouble(3, moto.getCantKmRecorridos());
        ps.setString(4, moto.getIdColor());
        ps.setString(5, moto.getMatriculaMoto()); // WHERE
    }
    
    @Override
    protected void setIdParameter(PreparedStatement ps, String matricula) throws SQLException {
        ps.setString(1, matricula);
    }
    
    @Override
    protected Moto mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new Moto(
            rs.getString("matricula_moto"),
            rs.getString("id_modelo"),
            Situacion.valueOf(rs.getString("situacion")),
            rs.getDouble("cant_km_recorridos"),
            rs.getString("id_color")
        );
    }
    
    // Métodos específicos...
    @Override
    public boolean estaDisponible(String matricula) {
        String sql = "SELECT situacion FROM moto WHERE matricula_moto = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Situacion situacion = Situacion.valueOf(rs.getString("situacion"));
                    return situacion == Situacion.DISPONIBLE;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar disponibilidad: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void cambiarEstado(String matricula, Situacion nuevaSituacion) {
        String sql = "UPDATE moto SET situacion = ? WHERE matricula_moto = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nuevaSituacion.name());
            ps.setString(2, matricula);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al cambiar estado de moto: " + e.getMessage(), e);
        }
    }
    
    // Implementar otros métodos específicos...
}
```

---

## Implementación sin Clase Abstracta (Opción 2)

### Beneficios:
- ✅ **Simplicidad**: Sin herencia, todo el código está en un solo lugar
- ✅ **Control total**: Puedes personalizar cada aspecto del flujo CRUD
- ✅ **Fácil de entender**: No hay capas de abstracción
- ✅ **Flexibilidad**: Puedes cambiar la implementación sin afectar una jerarquía

### Desventajas:
- ❌ **Duplicación de código**: Repites try-catch y manejo de PreparedStatement
- ❌ **Menos DRY**: Cambios en el flujo CRUD deben hacerse en todos los DAOs

### Estructura:

#### 1. Interfaz Genérica (igual que antes)
```java
package org.proyectobdmotos.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDAO<T, ID> {
    void insertar(T entity);
    void actualizar(T entity);
    void eliminar(ID id);
    Optional<T> buscarPorId(ID id);
    List<T> listarTodos();
}
```

#### 2. Interfaz Específica (igual que antes)
```java
package org.proyectobdmotos.dao;

import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.dto.ClienteDTO;
import java.util.List;

public interface IClienteDAO extends GenericDAO<Cliente, String> {
    List<ClienteDTO> listarClientesPorMunicipio();
    List<Cliente> obtenerClientesIncumplidores();
    void eliminarConCascada(String ci);
}
```

#### 3. Implementación Directa (sin clase abstracta)
```java
package org.proyectobdmotos.dao;

import org.proyectobdmotos.models.Cliente;
import org.proyectobdmotos.models.Sexo;
import org.proyectobdmotos.dto.ClienteDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación directa del DAO de Cliente.
 * Todo el código SQL y de manejo de PreparedStatement está aquí.
 */
public class ClienteDAO implements IClienteDAO {
    
    private final Connection connection;
    
    public ClienteDAO(Connection connection) {
        this.connection = connection;
    }
    
    @Override
    public void insertar(Cliente cliente) {
        String sql = "INSERT INTO cliente (ci_cliente, nombre_cliente, primer_apellido, " +
                     "segundo_apellido, edad, sexo, numero_contacto, id_municipio) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cliente.getCiCliente());
            ps.setString(2, cliente.getNombreCLiente());
            ps.setString(3, cliente.getPrimerApellido());
            ps.setString(4, cliente.getSegundoApellido());
            ps.setInt(5, cliente.getEdad());
            ps.setString(6, cliente.getSexo().name());
            ps.setString(7, cliente.getNumeroContacto());
            ps.setString(8, cliente.getIdMunicipio());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar cliente: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void actualizar(Cliente cliente) {
        String sql = "UPDATE cliente SET nombre_cliente = ?, primer_apellido = ?, " +
                     "segundo_apellido = ?, edad = ?, sexo = ?, numero_contacto = ?, " +
                     "id_municipio = ? WHERE ci_cliente = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cliente.getNombreCLiente());
            ps.setString(2, cliente.getPrimerApellido());
            ps.setString(3, cliente.getSegundoApellido());
            ps.setInt(4, cliente.getEdad());
            ps.setString(5, cliente.getSexo().name());
            ps.setString(6, cliente.getNumeroContacto());
            ps.setString(7, cliente.getIdMunicipio());
            ps.setString(8, cliente.getCiCliente());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar cliente: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void eliminar(String ci) {
        String sql = "DELETE FROM cliente WHERE ci_cliente = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ci);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar cliente: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<Cliente> buscarPorId(String ci) {
        String sql = "SELECT * FROM cliente WHERE ci_cliente = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ci);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cliente cliente = new Cliente(
                        rs.getString("ci_cliente"),
                        rs.getString("nombre_cliente"),
                        rs.getString("primer_apellido"),
                        rs.getString("segundo_apellido"),
                        rs.getInt("edad"),
                        Sexo.valueOf(rs.getString("sexo")),
                        rs.getString("numero_contacto"),
                        rs.getString("id_municipio")
                    );
                    return Optional.of(cliente);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Cliente> listarTodos() {
        String sql = "SELECT * FROM cliente ORDER BY nombre_cliente, primer_apellido";
        List<Cliente> lista = new ArrayList<>();
        
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Cliente cliente = new Cliente(
                    rs.getString("ci_cliente"),
                    rs.getString("nombre_cliente"),
                    rs.getString("primer_apellido"),
                    rs.getString("segundo_apellido"),
                    rs.getInt("edad"),
                    Sexo.valueOf(rs.getString("sexo")),
                    rs.getString("numero_contacto"),
                    rs.getString("id_municipio")
                );
                lista.add(cliente);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar clientes: " + e.getMessage(), e);
        }
        return lista;
    }
    
    @Override
    public List<ClienteDTO> listarClientesPorMunicipio() {
        String sql = """
            SELECT 
                c.ci_cliente,
                c.nombre_cliente,
                c.primer_apellido,
                c.segundo_apellido,
                m.nombre_municipio,
                COUNT(ct.fecha_inicio) as cant_alquileres,
                COALESCE(SUM(ct.importe_total), 0) as total_alquileres
            FROM cliente c
            INNER JOIN municipio m ON c.id_municipio = m.id_municipio
            LEFT JOIN contrato ct ON c.ci_cliente = ct.ci_cliente
            GROUP BY c.ci_cliente, m.nombre_municipio
            ORDER BY m.nombre_municipio, c.nombre_cliente
            """;
        
        List<ClienteDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                ClienteDTO dto = new ClienteDTO(
                    rs.getString("ci_cliente"),
                    rs.getString("nombre_cliente") + " " + 
                        rs.getString("primer_apellido") + " " +
                        rs.getString("segundo_apellido"),
                    rs.getString("nombre_municipio"),
                    rs.getInt("cant_alquileres"),
                    rs.getDouble("total_alquileres")
                );
                lista.add(dto);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar clientes por municipio: " + e.getMessage(), e);
        }
        return lista;
    }
    
    @Override
    public List<Cliente> obtenerClientesIncumplidores() {
        String sql = """
            SELECT DISTINCT c.*
            FROM cliente c
            INNER JOIN contrato ct ON c.ci_cliente = ct.ci_cliente
            WHERE ct.fecha_entrega IS NOT NULL 
              AND ct.fecha_entrega > ct.fecha_fin
            ORDER BY c.nombre_cliente, c.primer_apellido
            """;
        
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Cliente cliente = new Cliente(
                    rs.getString("ci_cliente"),
                    rs.getString("nombre_cliente"),
                    rs.getString("primer_apellido"),
                    rs.getString("segundo_apellido"),
                    rs.getInt("edad"),
                    Sexo.valueOf(rs.getString("sexo")),
                    rs.getString("numero_contacto"),
                    rs.getString("id_municipio")
                );
                lista.add(cliente);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener clientes incumplidores: " + e.getMessage(), e);
        }
        return lista;
    }
    
    @Override
    public void eliminarConCascada(String ci) {
        try {
            connection.setAutoCommit(false);
            
            String sqlContratos = "DELETE FROM contrato WHERE ci_cliente = ?";
            try (PreparedStatement ps = connection.prepareStatement(sqlContratos)) {
                ps.setString(1, ci);
                ps.executeUpdate();
            }
            
            eliminar(ci);
            
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error al hacer rollback: " + ex.getMessage(), ex);
            }
            throw new RuntimeException("Error al eliminar cliente con cascada: " + e.getMessage(), e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                // Log error
            }
        }
    }
}
```

---

## Comparación de Enfoques

| Aspecto | Con Clase Abstracta | Sin Clase Abstracta |
|---------|---------------------|---------------------|
| **Reutilización de código** | ✅ Alta (flujo CRUD común) | ❌ Baja (código duplicado) |
| **Simplicidad** | ⚠️ Media (abstracción) | ✅ Alta (todo en un lugar) |
| **Mantenibilidad** | ✅ Alta (cambios en un lugar) | ⚠️ Media (cambios en múltiples lugares) |
| **Flexibilidad** | ⚠️ Media (limitado por plantilla) | ✅ Alta (control total) |
| **Curva de aprendizaje** | ⚠️ Media (entender herencia) | ✅ Baja (código directo) |
| **Type Safety** | ✅ Completa (`<T, ID>`) | ✅ Completa (`<T, ID>`) |
| **Testabilidad** | ✅ Alta (mockear métodos abstractos) | ✅ Alta (mockear interfaz) |
| **Líneas de código** | ✅ Menos en DAOs concretos | ❌ Más en DAOs concretos |
| **Mejor para proyectos** | Grandes, muchas entidades | Pequeños, pocas entidades |

---

## Recomendación Final

### Usar Clase Abstracta (Opción 1) SI:
- ✅ Tienes **5 o más entidades** con CRUD similar
- ✅ El equipo está familiarizado con **patrones de diseño** (Template Method)
- ✅ Prefieres **DRY** sobre simplicidad
- ✅ El proyecto crecerá en el futuro
- ✅ Necesitas **mantenibilidad a largo plazo**

### Usar Implementación Directa (Opción 2) SI:
- ✅ Tienes **3-4 entidades** solamente
- ✅ El equipo prefiere **código directo** sobre abstracciones
- ✅ Cada entidad tiene **lógica CRUD muy diferente**
- ✅ Es un **proyecto académico** o prototipo
- ✅ Prefieres **simplicidad** sobre reutilización

### Para este Proyecto (Renta Motos Habana):
**Recomiendo Opción 1 (con clase abstracta)** porque:
1. Tienes 3 entidades principales + nomencladores
2. El CRUD de Cliente y Moto es muy similar
3. Es un proyecto educativo ideal para aprender patrones de diseño
4. Los reportes complejos se benefician de tener CRUD limpio y reutilizable

### Excepción: ContratoDAO
Por su **clave compuesta** (fechaInicio + matriculaMoto), ContratoDAO puede:
- **Opción A**: Crear una clase `ContratoId` para encapsular la clave y usar `GenericDAO<Contrato, ContratoId>`
- **Opción B**: Implementar directamente sin extender la clase abstracta (más simple)

```java
// Opción A: Con clase wrapper para ID compuesto
public class ContratoId {
    private final LocalDate fechaInicio;
    private final String matriculaMoto;
    
    // constructor, equals, hashCode
}

public interface IContratoDAO extends GenericDAO<Contrato, ContratoId> {
    // métodos específicos...
}

// Opción B: Sin GenericDAO (MÁS SIMPLE)
public interface IContratoDAO {
    void insertar(Contrato contrato);
    void actualizar(Contrato contrato);
    void eliminar(LocalDate fechaInicio, String matricula);
    Optional<Contrato> buscarPorClave(LocalDate fechaInicio, String matricula);
    List<Contrato> listarTodos();
    // métodos específicos...
}
```

---

## DTOs Necesarios

Para los reportes, necesitarás crear clases DTO (Data Transfer Object):

```java
package org.proyectobdmotos.dto;

/**
 * DTO para el reporte de clientes por municipio
 */
public class ClienteDTO {
    private String ci;
    private String nombreCompleto;
    private String municipio;
    private int cantidadAlquileres;
    private double valorTotal;
    
    // Constructor, getters, setters
}

/**
 * DTO para el reporte de situación de motos
 */
public class SituacionMotoDTO {
    private String matricula;
    private String marca;
    private Situacion situacion;
    private LocalDate fechaFinContrato; // nullable si no está alquilada
    
    // Constructor, getters, setters
}

/**
 * DTO para el resumen por marcas y modelos
 */
public class ResumenMarcaDTO {
    private String marca;
    private String modelo;
    private int cantidadMotos;
    private int diasTotalesAlquilados;
    private double ingresosTarjeta;
    private double ingresosCheque;
    private double ingresosEfectivo;
    
    // Constructor, getters, setters
}
```

---

## Resumen de Principios SOLID Aplicados

1. **Single Responsibility Principle (SRP)**
   - Cada DAO maneja solo una entidad
   - AbstractGenericDAO maneja solo el flujo CRUD común
   - DAOs concretos manejan SQL específico

2. **Open/Closed Principle (OCP)**
   - Abierto a extensión: puedes crear nuevos DAOs extendiendo AbstractGenericDAO
   - Cerrado a modificación: no necesitas cambiar AbstractGenericDAO para nuevas entidades

3. **Liskov Substitution Principle (LSP)**
   - Cualquier `GenericDAO<T, ID>` puede ser sustituido por su implementación concreta
   - Los métodos de la interfaz funcionan igual en todas las implementaciones

4. **Interface Segregation Principle (ISP)**
   - GenericDAO solo tiene métodos CRUD comunes
   - Cada interfaz específica (IClienteDAO, IMotoDAO) tiene solo sus métodos necesarios
   - Ningún cliente está forzado a depender de métodos que no usa

5. **Dependency Inversion Principle (DIP)**
   - Los servicios dependen de interfaces (IClienteDAO), no de implementaciones (ClienteDAO)
   - Las abstracciones (GenericDAO) no dependen de detalles (SQL específico)

---

## Próximos Pasos

1. **Decidir qué opción usar** (con o sin clase abstracta)
2. **Crear la estructura de paquetes**:
   ```
   org.proyectobdmotos.dao/
   ├── GenericDAO.java           (interfaz)
   ├── AbstractGenericDAO.java   (clase abstracta, opcional)
   ├── IClienteDAO.java          (interfaz específica)
   ├── ClienteDAO.java           (implementación)
   ├── IMotoDAO.java
   ├── MotoDAO.java
   ├── IContratoDAO.java
   └── ContratoDAO.java
   
   org.proyectobdmotos.dto/
   ├── ClienteDTO.java
   ├── MotoDTO.java
   ├── SituacionMotoDTO.java
   ├── ResumenMarcaDTO.java
   └── ...
   ```
3. **Implementar los DAOs** siguiendo el patrón elegido
4. **Crear tests unitarios** para cada DAO
5. **Integrar con los servicios existentes**

---

**Fecha de creación**: 2026-04-06  
**Autor**: Análisis para proyecto Renta Motos Habana  
**Versión**: 1.0
