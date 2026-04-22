# Plan de refactorización Java para adaptar el modelo (PK numéricas + nomencladores relacionales)

## Objetivo

Adaptar **toda la lógica Java** al nuevo modelo de datos definido en `docs/modelacion.md`, manteniendo intactos los archivos SQL por ahora.

Cambios objetivo del modelo:
- `cliente`: usar `id_cliente` como PK técnica y dejar `ci_cliente` como identificador de negocio único.
- `moto`: usar `id_moto` como PK técnica y dejar `matricula_moto` como identificador de negocio único.
- `sexo`, `situacion`, `forma_pago`: tratarlos como entidades/lookup con ID numérico (no enum en la capa de persistencia).

## Restricciones explícitas

- **No modificar archivos SQL ni migraciones** en este plan.
- Refactorizar únicamente Java (`src/main/java`) y sus pruebas/documentación asociada.
- Mantener arquitectura por capas (Controller → Service → DAO) y sin lógica de negocio en controllers.

---

## Diagnóstico resumido del estado actual

1. Ya existe migración a tablas lookup (`sexo`, `situacion`, `forma_pago`) y DAOs lookup.
2. La lógica Java aún usa claves naturales como identidad principal:
   - `Cliente` identificado por `ciCliente`.
   - `Moto` identificada por `matriculaMoto`.
   - `ContratoID` usa `(fechaInicio, matriculaMoto)`.
3. Servicios y DAOs aún realizan CRUD principal por CI/Matrícula.
4. Contratos y tests están fuertemente acoplados a esas claves naturales.

Conclusión: el refactor previo cubrió ENUM → lookup, pero falta migrar la **identidad técnica** a IDs numéricos en Java.

---

## Estrategia recomendada (sin romper UI de golpe)

### Opción A — Refactor por compatibilidad (recomendada)

Mantener temporalmente métodos públicos orientados a negocio (`buscarPorCi`, `buscarPorMatricula`) y cambiar internamente a PK numéricas.

**Ventajas**
- Menor ruptura inmediata de UI.
- Migración gradual y verificable.

**Costo**
- Convivencia temporal de doble identidad (ID técnica + clave de negocio).

### Opción B — Corte total inmediato

Cambiar toda la API pública a IDs numéricos de una vez.

**Ventajas**
- Modelo limpio desde el día 1.

**Costo**
- Rompe contrato de servicios/UI y exige cambios simultáneos grandes.

---

## Archivos impactados

## 1) Modelos de dominio (modificar)
- `src/main/java/org/proyectobdmotos/models/Cliente.java`
- `src/main/java/org/proyectobdmotos/models/Moto.java`
- `src/main/java/org/proyectobdmotos/models/Contrato.java`
- `src/main/java/org/proyectobdmotos/models/ContratoID.java`

## 2) Nomencladores / catálogos (modificar o reemplazar)
- `src/main/java/org/proyectobdmotos/models/Sexo.java`
- `src/main/java/org/proyectobdmotos/models/Situacion.java`
- `src/main/java/org/proyectobdmotos/models/FormaPago.java`

## 3) Contratos DAO y abstracciones (modificar)
- `src/main/java/org/proyectobdmotos/dao/GenericDAO.java` (solo si cambia tipado por ID en implementaciones)
- `src/main/java/org/proyectobdmotos/dao/IClienteDAO.java`
- `src/main/java/org/proyectobdmotos/dao/IMotoDAO.java`
- `src/main/java/org/proyectobdmotos/dao/IContratoDAO.java`
- `src/main/java/org/proyectobdmotos/dao/ClienteDAO.java`
- `src/main/java/org/proyectobdmotos/dao/MotoDAO.java`
- `src/main/java/org/proyectobdmotos/dao/ContratoDAO.java`
- `src/main/java/org/proyectobdmotos/dao/ISexoDAO.java`
- `src/main/java/org/proyectobdmotos/dao/ISituacionDAO.java`
- `src/main/java/org/proyectobdmotos/dao/IFormaPagoDAO.java`
- `src/main/java/org/proyectobdmotos/dao/SexoDAO.java`
- `src/main/java/org/proyectobdmotos/dao/SituacionDAO.java`
- `src/main/java/org/proyectobdmotos/dao/FormaPagoDAO.java`

## 4) Servicios de negocio (modificar)
- `src/main/java/org/proyectobdmotos/services/ClienteService.java`
- `src/main/java/org/proyectobdmotos/services/MotoService.java`
- `src/main/java/org/proyectobdmotos/services/ContratoService.java`
- `src/main/java/org/proyectobdmotos/services/AgenciaService.java` (ajustes menores de fachada si aplica)

## 5) DTOs / capas de reporte (modificar si cambia firma/tipos)
- `src/main/java/org/proyectobdmotos/dto/ClienteDTO.java`
- `src/main/java/org/proyectobdmotos/dto/MotoDTO.java`
- `src/main/java/org/proyectobdmotos/dto/SituacionMotoDTO.java`

## 6) Composición e inyección (revisar/modificar si se agregan servicios de resolución)
- `src/main/java/org/proyectobdmotos/ui/AppCompositionRoot.java`

## 7) Tests y contrato documental (modificar)
- `src/test/java/org/proyectobdmotos/models/ContratoIDTest.java`
- `src/test/java/org/proyectobdmotos/services/ClienteServiceContractTest.java`
- `src/test/java/org/proyectobdmotos/services/MotoServiceContractTest.java`
- `src/test/java/org/proyectobdmotos/services/ContratoServiceContractTest.java`
- `src/test/java/org/proyectobdmotos/services/ServiceApiDocumentationContractTest.java`
- `docs/contrato-integracion-ui.md`

---

## Plan por fases

## Fase 0 — Congelar reglas de transición de identidad

### Tareas
1. Definir política de identidad dual temporal (ID técnica + clave natural) en:
   - `docs/contrato-integracion-ui.md`
   - `src/main/java/org/proyectobdmotos/services/ClienteService.java`
   - `src/main/java/org/proyectobdmotos/services/MotoService.java`
2. Acordar firma transicional para `ContratoID` con `idMoto` en lugar de `matriculaMoto`:
   - `src/main/java/org/proyectobdmotos/models/ContratoID.java`

### Resultado esperado
- Decisión explícita de API transicional para evitar ruptura masiva.

## Fase 1 — Refactor de modelos (núcleo de dominio)

### Tareas
1. `Cliente.java`: incorporar `idCliente` (Integer) como identidad técnica y conservar `ciCliente` como dato de negocio único.
2. `Moto.java`: incorporar `idMoto` (Integer) como identidad técnica y conservar `matriculaMoto` como dato de negocio único.
3. `ContratoID.java`: cambiar clave compuesta a `(fechaInicio, idMoto)`.
4. `Contrato.java`: reemplazar referencias a `ciCliente`/`matriculaMoto` por `idCliente`/`idMoto` en identidad relacional interna.
5. Modelar nomencladores como entidad de catálogo (ID + nombre) o encapsular ID+nombre en tipos equivalentes para:
   - `Sexo.java`
   - `Situacion.java`
   - `FormaPago.java`

### Resultado esperado
- Dominio expresado con PK técnicas numéricas y claves naturales solo como atributos de negocio.

## Fase 2 — Refactor DAO e interfaces

### Tareas
1. Cambiar contratos de DAO:
   - `IClienteDAO extends GenericDAO<Cliente, Integer>`
   - `IMotoDAO extends GenericDAO<Moto, Integer>`
   - `IContratoDAO` con `ContratoID(fechaInicio, idMoto)`.
2. Agregar búsquedas de negocio explícitas (sin usarlas como PK):
   - `buscarPorCi(String ci)` en cliente.
   - `buscarPorMatricula(String matricula)` en moto.
3. Actualizar SQL en:
   - `ClienteDAO.java` (joins y CRUD principal por `id_cliente`).
   - `MotoDAO.java` (joins y CRUD principal por `id_moto`).
   - `ContratoDAO.java` (`id_cliente`, `id_moto`, `id_forma_pago`).
4. Validar que reportes (`ClienteDTO`, `MotoDTO`, `SituacionMotoDTO`) sigan mostrando CI/matrícula como datos de salida si UI los necesita.

### Resultado esperado
- Persistencia alineada al nuevo modelo relacional sin perder trazabilidad por claves de negocio.

## Fase 3 — Refactor servicios y casos de uso

### Tareas
1. `ClienteService.java`:
   - conservar API de negocio (`buscarPorCi`) y agregar API por ID técnica cuando convenga.
2. `MotoService.java`:
   - conservar API de negocio (`buscarPorMatricula`) y agregar API por ID técnica cuando convenga.
3. `ContratoService.java`:
   - validar existencia por `idCliente` e `idMoto`.
   - actualizar logs/errores para usar identidad técnica y, cuando aplique, datos de negocio como contexto.
4. `AgenciaService.java`:
   - revisar fachada para no filtrar acoplamiento a claves naturales como identidad primaria.

### Resultado esperado
- Reglas de negocio operan con IDs técnicos, sin romper los flujos UI existentes.

## Fase 4 — Contrato UI, documentación y pruebas

### Tareas
1. Actualizar contrato de integración:
   - `docs/contrato-integracion-ui.md`
2. Ajustar pruebas de contrato/API y modelo:
   - `ContratoIDTest.java`
   - `ClienteServiceContractTest.java`
   - `MotoServiceContractTest.java`
   - `ContratoServiceContractTest.java`
   - `ServiceApiDocumentationContractTest.java`
3. Incorporar casos de prueba de regresión para:
   - búsqueda por clave de negocio + operación interna por ID técnica.
   - finalización de contrato con `ContratoID(fechaInicio, idMoto)`.

### Resultado esperado
- Suite de pruebas y documentación sincronizadas con el nuevo modelo Java.

---

## Criterios de aceptación del refactor

1. No existen DAOs CRUD principales de `Cliente`/`Moto` usando CI/Matrícula como PK de infraestructura.
2. `ContratoID` usa `idMoto` (numérico) y no matrícula.
3. `Contrato` persiste referencias por `idCliente` e `idMoto`.
4. Nomencladores (`sexo`, `situacion`, `forma_pago`) se consumen en Java como entidades de catálogo/lookup con ID.
5. Pruebas de servicios y documentación contractual pasan alineadas al nuevo diseño.
6. Los métodos de UI orientados a negocio (buscar por CI/matrícula) continúan funcionando durante transición.

---

## Riesgos y mitigaciones

1. **Riesgo:** ruptura de contrato UI por cambio abrupto de firmas.
   - **Mitigación:** estrategia de compatibilidad (Fase 0) y transición con métodos de negocio + técnicos.

2. **Riesgo:** inconsistencias entre modelo Java y pruebas contractuales.
   - **Mitigación:** Fase 4 obligatoria con actualización de tests y `docs/contrato-integracion-ui.md`.

3. **Riesgo:** mapeos ambiguos entre ID técnica y clave natural.
   - **Mitigación:** crear métodos explícitos de resolución en DAOs y centralizar validaciones en servicios.

---

## Entregable

Refactor Java completo (sin tocar SQL en esta iteración) que deje lista la base de código para operar con el nuevo modelo conceptual definido en `docs/modelacion.md`, minimizando ruptura de UI mediante transición controlada.
