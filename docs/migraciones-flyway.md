# Migraciones con Flyway (proyecto-bd-renta-motos)

Este proyecto usa **Flyway** para mantener la base de datos sincronizada entre todos los colaboradores mediante **scripts SQL versionados**.

> TL;DR: **No se editan migraciones ya aplicadas**. Cada cambio va en un **nuevo archivo** `V{n}__descripcion.sql` con un **número de versión único y creciente** (V3, V4, V5…). Flyway ejecuta solo lo que falta y valida que lo ya ejecutado no cambió.

---

## 1) ¿Dónde están las migraciones?

Flyway busca los scripts en:

```
src/main/resources/db/migration/
```

En este repo ya existen, por ejemplo:

- `V1__crear_tablas.sql`
- `V2__datos_iniciales.sql`

La siguiente migración nueva será `V3__...sql`, y luego `V4__...sql`, etc.

---

## 2) ¿Cuándo se ejecutan?

Al correr la app, se ejecuta:

- `mvn compile exec:java`

El código llama a Flyway en `org.proyectobdmotos.database.DatabaseConnection`:

- `runMigrations()` → `Flyway.configure().dataSource(...).load().migrate()`

Eso hace que Flyway:

1. Se conecte a tu PostgreSQL usando tu `config.properties`
2. Revise el historial
3. Ejecute las migraciones pendientes

---

## 3) ¿Qué guarda Flyway como “historial”?

Flyway crea/usa una tabla en tu BD llamada:

- `flyway_schema_history`

Ahí registra cada script aplicado (entre otras cosas):

- **version** (ej: 1, 2, 3…)
- **description** (la parte después de `__`)
- **script** (nombre del archivo)
- **checksum** (huella del contenido del archivo)
- **success** (si se aplicó bien)

Puedes verlo con:

```sql
SELECT installed_rank, version, description, script, checksum, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

---

## 4) Convención de nombres (muy importante)

Formato obligatorio:

```
V{numero}__{descripcion}.sql
```

Ejemplos:

- `V3__agregar_columna_precio.sql`
- `V4__crear_tabla_sucursales.sql`

Reglas:

- El **número** es la versión (V3, V4, V5…).
- La **descripción** es libre (usa snake_case o algo legible).
- **Una versión no se repite**: no pueden existir dos archivos con `V3__...`.

---

## 5) Respuestas a preguntas frecuentes

### 5.1) “¿Los 3 colaboradores debemos escribir todas las queries en un solo script V3__`<nombre>`.sql?”

**No.**

- Flyway funciona por **versiones**: `V3`, `V4`, `V5`…
- **V3 es una única versión**. No es un “contenedor” compartido por todos.

Lo que sí es válido:

- En **un mismo cambio/PR**, meter varias sentencias SQL relacionadas dentro de **un solo archivo**, por ejemplo `V3__nuevo_modelo_contratos.sql`.

Lo que NO es válido:

- Pretender que las 3 personas trabajen en el mismo `V3` en paralelo.

### 5.2) “¿Podemos hacer más de un archivo con V3?”

**No.**

- Dos scripts con la misma versión (`V3__...` y `V3__...`) causan error (version duplicada).

### 5.3) “Si Flyway guarda el historial y ejecuto una vez el V3, si edito el archivo V3 ¿lo ejecuta de nuevo?”

**No lo vuelve a ejecutar.**

Cuando una migración ya fue aplicada:

- Flyway la considera parte del historial
- Guarda un **checksum** del contenido

Si tú editas ese archivo después:

- Flyway **detecta que cambió el checksum**
- Lo normal es que falle en validación con un error tipo **checksum mismatch** (para protegerte de inconsistencias)

**Conclusión:**

- **No se edita** un `V3` ya aplicado.
- Si te faltó algo, creas un **nuevo** `V4__...sql` con el ajuste.

> Nota: Existe el comando `flyway repair` (en general) para “arreglar” checksums del historial, pero en equipo suele ser mala práctica salvo casos excepcionales y acordados, porque puede esconder divergencias entre entornos.

### 5.4) “¿Puedo tener un V4, V5, V6…? Creo que en el pasado me dio un error eso, creo que nada más se puede el V3”

**Sí, puedes y debes tener V4, V5, V6…**

Si te dio error, típicas causas:

- Había **dos V3** (versiones duplicadas)
- O editaste una migración ya aplicada (checksum mismatch)
- O el script tenía SQL inválido / dependía de objetos que no existían aún

### 5.5) “¿Debo tener varios V3 con diferentes nombres?”

**No.**

- Solo puede existir **un V3**.
- Si necesitas más cambios, van en `V4`, `V5`, etc.

### 5.6) “¿No puedo borrar los scripts usados?”

**No es recomendable borrarlos.**

Razones:

- Un compañero (o un entorno nuevo) necesita correr desde `V1` hasta la última para crear la BD desde cero.
- Flyway también valida historial vs. scripts disponibles (dependiendo de configuración).
- Borrarlos rompe trazabilidad y reproducibilidad.

Regla práctica:

- **Nunca borres ni cambies V1/V2/V3... ya publicados.**

---

## 6) Flujo recomendado en equipo (para evitar choques)

### Caso normal (sin conflictos)

1. Haces cambios en BD → creas `V3__mi_cambio.sql`.
2. Lo subes en un PR.
3. El resto hace `git pull`.
4. Al correr `mvn compile exec:java`, Flyway ejecuta **solo esa migración** si todavía no la tiene.

### Caso típico de conflicto (dos personas crean V3 a la vez)

Si Ana y Bruno crean ambos `V3__...` en ramas diferentes, al mergear habrá problema porque:

- En el repo final quedarían **dos V3** (inválido)

Cómo se resuelve:

- El PR que se mergea primero se queda con `V3__...`.
- El segundo PR se **renumera** a `V4__...` (y se vuelve a subir el cambio).

---

## 7) Buenas prácticas para escribir migraciones

- Haz migraciones **pequeñas y atómicas** (un objetivo claro por versión).
- Incluye cambios de schema (DDL) y datos (DML) solo si son parte del mismo cambio.
- Mantén el orden lógico: primero crear tablas/columnas, luego constraints/triggers, luego inserts.
- Piensa en entornos limpios: tu script debe funcionar en una BD que solo tiene migraciones previas aplicadas.

---

## 8) Cómo verificar qué se aplicó

En PostgreSQL:

```sql
SELECT version, description, script, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Y al correr la app deberías ver algo como:

- “Successfully applied X migrations”

---

## 9) Regla del proyecto (importante)

En este repo se sigue la regla:

- ✅ **Agregar una nueva migración** para cambios nuevos.
- ❌ **No modificar migraciones existentes** (ej: `V1`, `V2`, etc.).

---

## 10) Ejemplo rápido

Si hoy necesitas agregar una columna:

- Creas `src/main/resources/db/migration/V3__agregar_columna_precio.sql`

Con algo como:

```sql
ALTER TABLE moto
ADD COLUMN precio_por_dia NUMERIC(10,2);
```

Mañana necesitas ajustar un trigger:

- Creas `V4__ajustar_trigger_situacion.sql`

---

Si quieren, puedo revisar con ustedes el próximo PR de BD para asegurar que el versionado (V3/V4/…) y el orden de sentencias está correcto.
