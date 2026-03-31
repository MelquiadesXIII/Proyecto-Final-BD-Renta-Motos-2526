# Guía de configuración de la base de datos

## Requisitos previos

- PostgreSQL instalado
- Maven instalado
- Tener el repositorio clonado

---

## 1. Crear la base de datos

Abre una terminal y corre:

```bash
psql -h localhost -U postgres -c "CREATE DATABASE renta_motos;"
```

Te pedirá la password de tu usuario `postgres` local.

---

## 2. Crear tu archivo de configuración

En la raíz del proyecto existe un archivo `src/main/resources/config.properties.example`.  
Copia ese archivo y renómbralo a `config.properties` en la misma carpeta:

```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
```

Luego ábrelo y pon tu password:

```properties
db.url=jdbc:postgresql://localhost:5432/renta_motos
db.user=postgres
db.password=tu_password_aqui
```

> `config.properties` está en el `.gitignore`, nunca se sube al repositorio.  
> Cada integrante tiene el suyo con su password local.

---

## 3. Correr las migraciones

Con esto se crean todas las tablas y se insertan los datos iniciales automáticamente:

```bash
mvn compile exec:java
```

Si todo está bien verás al final:

```
Flyway ... Successfully applied X migrations
Base de datos lista.
```

Puedes verificar las migraciones ejecutadas con:

```bash
psql -h localhost -U postgres -d renta_motos -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

---

## Cómo funciona Flyway

Flyway mantiene la base de datos sincronizada entre todos usando scripts SQL versionados ubicados en:

```
src/main/resources/db/migration/
```

Cada script sigue el formato `V{numero}__{descripcion}.sql`, por ejemplo:

```
V1__crear_tablas.sql
V2__datos_iniciales.sql
V3__agregar_columna_precio.sql   ← próximo cambio
```

Flyway lleva un registro interno en la tabla `flyway_schema_history` de qué scripts ya ejecutó en cada máquina. Al correr la app, solo ejecuta los scripts nuevos que aún no corrió.

---

## Reglas para trabajar en equipo

| ✅ Hacer | ❌ No hacer |
|---|---|
| Crear un nuevo `V3__...sql` para cada cambio | Modificar scripts que ya existen (`V1`, `V2`, etc.) |
| Subir el script nuevo al repositorio | Subir `config.properties` con tu password |
| Correr `mvn compile exec:java` al hacer pull | Editar la base de datos manualmente sin crear un script |

---

## Flujo cuando un compañero hace un cambio en la BD

1. El compañero crea `V3__descripcion.sql` con el cambio
2. Lo sube al repositorio
3. Tú haces `git pull`
4. Corres `mvn compile exec:java`
5. Flyway detecta que te falta `V3` y lo ejecuta automáticamente
