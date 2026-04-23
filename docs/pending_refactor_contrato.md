# Pendientes tras la refactorización de `Contrato`

Después de migrar la entidad **Contrato** a usar una clave primaria `id_contrato` (SERIAL) y mantener la restricción única `(fecha_inicio, id_moto)`, quedan los siguientes pasos por completar:

## 1️⃣ Eliminar la clase ya obsoleta
- `src/main/java/org/proyectobdmotos/models/ContratoID.java`
- Ejecutar `git rm` y confirmar el borrado.

## 2️⃣ Buscar y actualizar referencias restantes
- Ejecutar `grep -R "ContratoID" -n src/main/java src/test/java` para localizar cualquier uso que aún apunte a `ContratoID`.
- Reemplazar esas referencias por los nuevos campos (`idContrato`, `fechaInicio`, `idMoto`).

## 3️⃣ Actualizar la documentación del modelo
- En `docs/modelacion.md` agregar la nueva definición de la tabla `contrato`:
  ```sql
  CREATE TABLE contrato (
      id_contrato SERIAL PRIMARY KEY,
      fecha_inicio DATE NOT NULL,
      id_moto INT NOT NULL,
      ...
      UNIQUE (fecha_inicio, id_moto)
  );
  ```
- Documentar que la PK ahora es `id_contrato` y que la regla de unicidad sigue garantizando que una moto no pueda tener dos contratos en la misma fecha.

## 5️⃣ Verificar que todos los tests siguen pasando
- Ejecutar `mvn clean test` después de los cambios anteriores.
- Asegurarse de que no haya fallos en los tests de `ContratoService`, `ContratoDAO` y los fixtures.

## 6️⃣ Crear el Pull Request
- Hacer commit de los cambios restantes.
- Push a la rama y lanzar PR con título `refactor: contrato ahora usa id serial` y cuerpo describiendo los puntos anteriores.

---

**Resumen rápido**:
- **Eliminar** `ContratoID` y sus importaciones.
- **Actualizar** cualquier referencia a `ContratoID`.
- **Documentar** la nueva PK en `modelacion.md`.
- **Ejecutar** tests y crear PR.
