# Guía de estado actual del proyecto y próximos pasos

Fecha de corte: **Actualizado** (post-implementación completa DAO/Services/Lógica de negocio)

## Update L1 (Freeze de lógica para UI)

- Estado: **Ticket L1 completado hasta Fase 4 (freeze)**.
- Fuente de verdad del contrato: [docs/contrato-integracion-ui.md](./contrato-integracion-ui.md).
- Política activa: cambios en API pública de `services` solo por PR excepcional con coordinación previa con UI.

### Métodos permitidos para consumo UI (freeze)

#### ClienteService
- `void crearCliente(Cliente cliente)`
- `void actualizarCliente(Cliente cliente)`
- `void eliminarCliente(String ci)`
- `Optional<Cliente> buscarPorCi(String ci)`
- `List<Cliente> listarTodos()`
- `List<ClienteDTO> listarClientesPorMunicipio()`
- `List<Cliente> obtenerClientesIncumplidores()`

#### MotoService
- `void crearMoto(Moto moto)`
- `void actualizarMoto(Moto moto)`
- `void eliminarMoto(String matricula)`
- `Optional<Moto> buscarPorMatricula(String matricula)`
- `List<Moto> listarTodos()`
- `boolean estaDisponible(String matricula)`
- `void cambiarEstado(String matricula, Situacion nuevaSituacion)`
- `List<MotoDTO> listarMotosConKilometraje()`
- `List<SituacionMotoDTO> listarSituacionMotos()`

#### ContratoService
- `void crearContrato(Contrato contrato)`
- `void actualizarContrato(Contrato contrato)`
- `void finalizarContrato(Contrato contrato)`
- `void eliminarContrato(Integer idContrato)`
- `Optional<Contrato> buscarPorId(Integer idContrato)`
- `List<Contrato> listarTodos()`
- `List<Contrato> listarContratosCompletos()`

#### Excepciones de negocio contractuales mínimas
- `CLIENTE_NO_ENCONTRADO`
- `MOTO_NO_ENCONTRADA`
- `MOTO_NO_DISPONIBLE`
- `CONTRATO_NO_ENCONTRADO`
- `CONTRATO_YA_FINALIZADO`
- `CONTRATO_VALIDACION_FALLIDA`

> Nota de coordinación: los controllers deben ramificar por `errorCode`, no por parsing de texto.

## 1) Resumen ejecutivo

El proyecto avanza sólidamente: **capa de persistencia (DAO) y servicios están operativos** con SQL real, validaciones de negocio y reportes base. La arquitectura por capas funciona correctamente con inyección de dependencias via interfaces.

- La **BD está modelada**, versionada y **V3 migración agrega km a contrato**.
- La capa **DAO/Service es 100% funcional**: CRUD completo + métodos específicos + validaciones.
- La **app JavaFX arranca**, pero aún usa pantalla placeholder (siguiente fase).
- Las pruebas actuales solo validan humo básico (no hay tests de DAOs/servicios aún).

Estado global sugerido: **70% estructura / 30% funcionalidad de negocio / 0% UI**.

---

## 2) Qué ya está implementado

### 2.1 Infraestructura y configuración

- Proyecto Maven con Java 21, JavaFX, PostgreSQL y Flyway en [pom.xml](../pom.xml).
- Conexión JDBC y ejecución de migraciones en [src/main/java/org/proyectobdmotos/database/DatabaseConnection.java](../src/main/java/org/proyectobdmotos/database/DatabaseConnection.java).
- Punto de entrada que corre migraciones y lanza UI en [src/main/java/org/proyectobdmotos/App.java](../src/main/java/org/proyectobdmotos/App.java).

### 2.2 Base de datos

- Esquema inicial completo (tablas, enums, triggers) en [src/main/resources/db/migration/V1__crear_tablas.sql](../src/main/resources/db/migration/V1__crear_tablas.sql).
- Datos semilla de nomencladores en [src/main/resources/db/migration/V2__datos_iniciales.sql](../src/main/resources/db/migration/V2__datos_iniciales.sql).
- Reglas importantes en SQL ya resueltas por trigger:
  - No alquilar motos no disponibles.
  - Al crear contrato, cambiar `situacion` de moto a `alquilada`.

### 2.3 Arquitectura de aplicación

- `AppCompositionRoot` centraliza creación de dependencias en [src/main/java/org/proyectobdmotos/ui/AppCompositionRoot.java](../src/main/java/org/proyectobdmotos/ui/AppCompositionRoot.java).
- `ScreenLoader` preparado para inyección de controladores por constructor en [src/main/java/org/proyectobdmotos/ui/navigation/ScreenLoader.java](../src/main/java/org/proyectobdmotos/ui/navigation/ScreenLoader.java).
- Stores JavaFX (`AgenciaStore`, `ReferenceDataStore`) listos para estado observable:
  - [src/main/java/org/proyectobdmotos/stores/AgenciaStore.java](../src/main/java/org/proyectobdmotos/stores/AgenciaStore.java)
  - [src/main/java/org/proyectobdmotos/stores/ReferenceDataStore.java](../src/main/java/org/proyectobdmotos/stores/ReferenceDataStore.java)

### 2.4 Dominio

- Modelos de dominio principales existentes (`Cliente`, `Moto`, `Contrato`, enums, etc.) bajo [src/main/java/org/proyectobdmotos/models/](../src/main/java/org/proyectobdmotos/models/).
- Mapeo enum BD ↔ Java contemplado con `fromValor(...)` en enums (por ejemplo `Situacion`).

### 2.5 Persistencia (DAO) ✅ **NUEVA**

- **GenericDAO** (interfaz pública) define contrato CRUD base en [src/main/java/org/proyectobdmotos/dao/GenericDAO.java](../src/main/java/org/proyectobdmotos/dao/GenericDAO.java).
- **AbstractGenericDAO** implementa template method con 5 operaciones CRUD comunes en [src/main/java/org/proyectobdmotos/dao/AbstractGenericDAO.java](../src/main/java/org/proyectobdmotos/dao/AbstractGenericDAO.java):
  - `insertar(T)`, `actualizar(T)`, `eliminar(ID)`, `buscarPorId(ID)`, `listarTodos()`
  - Usa `Logger` para tracing, single-return en métodos, boolean flags en loops (AGENTS.md compliance).
  
- **ClienteDAO** funcional en [src/main/java/org/proyectobdmotos/dao/ClienteDAO.java](../src/main/java/org/proyectobdmotos/dao/ClienteDAO.java):
  - CRUD completo de clientes con prepared statements.
  - `listarClientesPorMunicipio()` — reporte con agregación (COUNT contratos).
  - `obtenerClientesIncumplidores()` — listado de clientes con entrega tardía.
  - `eliminarConCascada(ci)` — transacción que elimina cliente y sus contratos.

- **MotoDAO** funcional en [src/main/java/org/proyectobdmotos/dao/MotoDAO.java](../src/main/java/org/proyectobdmotos/dao/MotoDAO.java):
  - CRUD completo de motos.
  - `estaDisponible(matricula)` — verifica estado antes de alquilar.
  - `cambiarEstado(matricula, situacion)` — actualiza situación (usado al crear/finalizar contrato).
  - `listarMotosConKilometraje()` — reporte con JOIN a marca/modelo.
  - `listarSituacionMotos()` — reporte de estado actual de cada moto.

- **ContratoDAO** funcional en [src/main/java/org/proyectobdmotos/dao/ContratoDAO.java](../src/main/java/org/proyectobdmotos/dao/ContratoDAO.java):
  - CRUD completo con PK compuesta (`ContratoID`: fecha_inicio + matricula_moto).
  - `listarContratosCompletos()` — consulta con JOINs a cliente/moto.
  - Manejo de fechas (`LocalDate` ↔ `java.sql.Date`).
  - Preparado para `cant_km_salida` y `cant_km_llegada` (columnas agregadas en V3).

- Interfaces específicas con métodos de negocio en:
  - [src/main/java/org/proyectobdmotos/dao/IClienteDAO.java](../src/main/java/org/proyectobdmotos/dao/IClienteDAO.java)
  - [src/main/java/org/proyectobdmotos/dao/IMotoDAO.java](../src/main/java/org/proyectobdmotos/dao/IMotoDAO.java)
  - [src/main/java/org/proyectobdmotos/dao/IContratoDAO.java](../src/main/java/org/proyectobdmotos/dao/IContratoDAO.java)

### 2.6 Servicios (Lógica de negocio) ✅ **COMPLETO**

- **ClienteService** en [src/main/java/org/proyectobdmotos/services/ClienteService.java](../src/main/java/org/proyectobdmotos/services/ClienteService.java):
  - Delega CRUD al DAO, expone métodos de negocio: `crearCliente`, `actualizarCliente`, `eliminarCliente`, `eliminarClienteConCascada`.
  - Métodos de consulta: `buscarPorCi`, `listarTodos`, `listarClientesPorMunicipio`, `obtenerClientesIncumplidores`.
  - Inyecta `IClienteDAO` (interfaz, no DAO concreto).
  - Validaciones con excepciones de negocio (`BusinessException`, `ValidationException`).

- **MotoService** en [src/main/java/org/proyectobdmotos/services/MotoService.java](../src/main/java/org/proyectobdmotos/services/MotoService.java):
  - Métodos: `crearMoto`, `actualizarMoto`, `eliminarMoto`, `cambiarEstado`, `estaDisponible`.
  - Métodos de reporte: `listarMotosConKilometraje`, `listarSituacionMotos`.
  - Inyecta `IMotoDAO` (interfaz).
  - Validaciones con excepciones de negocio.

- **ContratoService** en [src/main/java/org/proyectobdmotos/services/ContratoService.java](../src/main/java/org/proyectobdmotos/services/ContratoService.java):
  - Orquesta contratos involucrando cliente + moto.
  - `crearContrato(contrato)` — valida que cliente exista y moto esté disponible antes de persistir.
  - `finalizarContrato(contrato)` — actualiza contrato, calcula días de prórroga reales, calcula importe total teórico, y devuelve moto a estado DISPONIBLE.
  - `actualizarContrato(contrato)` — actualiza contrato existente con validaciones.
  - `eliminarContrato(idContrato)` — elimina contrato con validaciones.
  - Métodos de consulta: `buscarPorId`, `listarTodos`, `listarContratosCompletos`.
  - Inyecta `IContratoDAO`, `IClienteDAO`, `IMotoDAO` (todas interfaces).
  - Implementa lógica compleja de validación con flags booleanos (cumple AGENTS.md).

- **AgenciaService** (fachada) en [src/main/java/org/proyectobdmotos/services/AgenciaService.java](../src/main/java/org/proyectobdmotos/services/AgenciaService.java):
  - Punto de entrada único para todo el negocio (clientes, motos, contratos).
  - Inyecta los tres servicios principales.

- **Excepciones de negocio** en [src/main/java/org/proyectobdmotos/services/exceptions/](../src/main/java/org/proyectobdmotos/services/exceptions/):
  - `BusinessException` — excepción base.
  - `ValidationException` — excepción de validación.
  - `BusinessErrorCode` — enum con códigos de error:
    - `CLIENTE_NO_ENCONTRADO`
    - `MOTO_NO_ENCONTRADA`
    - `MOTO_NO_DISPONIBLE`
    - `CONTRATO_NO_ENCONTRADO`
    - `CONTRATO_YA_FINALIZADO`
    - `CONTRATO_FECHA_ENTREGA_INVALIDA`
    - `CONTRATO_KM_INVALIDO`
    - `CONTRATO_VALIDACION_FALLIDA`

### 2.7 DTOs para reportes ✅ **NUEVA**

- **ClienteDTO** en [src/main/java/org/proyectobdmotos/dto/ClienteDTO.java](../src/main/java/org/proyectobdmotos/dto/ClienteDTO.java):
  - Campos: `ci`, `nombreCompleto`, `municipio`, `cantidadAlquileres`.
  - Usado por `ClienteDAO.listarClientesPorMunicipio()`.

- **MotoDTO** en [src/main/java/org/proyectobdmotos/dto/MotoDTO.java](../src/main/java/org/proyectobdmotos/dto/MotoDTO.java):
  - Campos: `matricula`, `marca`, `modelo`, `kmRecorridos`.
  - Usado por `MotoDAO.listarMotosConKilometraje()`.

- **SituacionMotoDTO** en [src/main/java/org/proyectobdmotos/dto/SituacionMotoDTO.java](../src/main/java/org/proyectobdmotos/dto/SituacionMotoDTO.java):
  - Campos: `matricula`, `marca`, `situacion`, `fechaFinContrato` (nullable).
  - Usado por `MotoDAO.listarSituacionMotos()`.

### 2.8 Lógica de cálculo de importes ✅ **COMPLETO**

- **Contrato** en [src/main/java/org/proyectobdmotos/models/Contrato.java](../src/main/java/org/proyectobdmotos/models/Contrato.java):
  - `calcularDiasPactados()` — calcula días entre fecha_inicio y fecha_fin.
  - `calcularDiasProrrogaReal()` — calcula días de prórroga real (fecha_entrega - fecha_fin).
  - `calcularImporteBase()` — calcula importe base (dias_pactados × tarifa_normal).
  - `calcularRecargoProrroga()` — calcula recargo por prórroga (dias_prorroga_real × tarifa_prorroga).
  - `calcularImporteTotalTeorico()` — calcula importe total (importe_base + recargo_prorroga).
  - Todos los métodos siguen patrón single-return con flags booleanos (cumple AGENTS.md).

- **ContratoService.finalizarContrato()** usa estos métodos para:
  1. Calcular días de prórroga reales.
  2. Actualizar campo `dias_prorroga` en BD.
  3. Calcular importe total teórico.
  4. Registrar en logs el importe calculado.

### 2.9 Base de datos (migraciones)

- **V1__crear_tablas.sql** — Esquema completo con tablas, enums, constraints.
- **V2__datos_iniciales.sql** — Datos semilla de nomencladores.
- **V3__triggers.sql** — Triggers para validar reglas de negocio.
- **V4__eliminar_datos.sql** — Limpieza de datos.
- **V5__agregar_datos_forma_manual.sql** — Datos adicionales.
- **V6__sobrescribir_informacion.sql** — Actualización de datos.
- **V7__reportes.sql** — Placeholder para reportes SQL (vacío actualmente).

Nota: Las columnas `cant_km_salida` y `cant_km_llegada` ya están en V1 (no se necesitó V3 adicional).

---

## 3) Qué está incompleto o pendiente crítico

### 3.1 UI (Interfaz gráfica)

- No existen FXML en `src/main/resources/fxml/`.
- `FxApp` muestra placeholder en lugar de pantalla real en [src/main/java/org/proyectobdmotos/ui/FxApp.java](../src/main/java/org/proyectobdmotos/ui/FxApp.java).
- Controladores (`ClienteController`, `MotoController`, `ContratoController`) sin handlers `@FXML` ni lógica de UI.
- No hay navegación entre pantallas.

**Impacto:** Usuarios no pueden ver/interactuar con datos. DAOs/Services estan completos pero UI es un placeholder.

### 3.2 Casos de uso en la UI

- Pantalla de ABM Cliente (crear, listar, actualizar, eliminar).
- Pantalla de ABM Moto (crear, listar, actualizar, eliminar).
- Pantalla de creación de Contrato (picker cliente + moto, cálculo de fechas).
- Pantalla de listado de Contratos activos.
- Pantalla de "Finalizar contrato" (registrar entrega).

### 3.3 Reportes y vistas de negocio

- Reporte "Clientes por municipio con estadísticas" — DAO listo, UI no existe.
- Reporte "Motos con kilometraje" — DAO listo, UI no existe.
- Reporte "Estado de motos" — DAO listo, UI no existe.
- Reporte "Clientes incumplidores" — DAO listo, UI no existe.
- Reporte "Ingresos por mes" — DAO no tiene la consulta SQL aún, UI no existe.

### 3.4 Lógica de cálculo de importes ✅ **RESUELTO**

- ✅ `Contrato` tiene métodos completos de cálculo de importes.
- ✅ `ContratoService.finalizarContrato()` calcula y registra importes.
- ⏳ No hay persistencia de importe total en tabla `contrato` (se calcula en tiempo real).
- ⏳ No hay campo `seguro_adicional` considerado en cálculo de importe (solo está en modelo).
- ⏳ No hay métodos para calcular multa por entrega tardía (solo recargo por prórroga).

### 3.5 Calidad y pruebas

- Única prueba tipo plantilla en [src/test/java/org/proyectobdmotos/AppTest.java](../src/test/java/org/proyectobdmotos/AppTest.java) (38 líneas, solo compila, no valida nada).
- No hay pruebas unitarias de:
  - DAOs (verificar SQL correcto, mapeos).
  - Servicios (reglas de validación: cliente existe, moto disponible, etc.).
  - Modelos (métodos de cálculo en Contrato).
  - Integración (flujo crear → listar → finalizar contrato).
- No hay cobertura de código medida.

### 3.6 Observaciones técnicas a revisar

- Inconsistencia de tipos: `Cliente.idMunicipio` es `String` en Java pero `INT` en BD → DAOs hacen conversión con `String.valueOf()`.
- Igual con `Moto.idColor` e `Moto.idModelo` (String en Java, INT en BD).
- **Opción 1:** Corregir modelos Java a `int` (ruptura controlada).
- **Opción 2:** Mantener String pero documentar conversión explícita en DAOs.
- `ContratoID` no existe: el contrato usa `id_contrato` entero en BD y en servicios/DAO.

---

## 4) Qué implementar a continuación (prioridades)

### Fase 1 — UI mínima funcional (PRÓXIMA)

**Objetivo:** Pantalla principal con navegación y tablas básicas.

**Archivos a crear/modificar:**

- [src/main/java/org/proyectobdmotos/ui/FxApp.java](../src/main/java/org/proyectobdmotos/ui/FxApp.java) — Reemplazar placeholder con BorderPane + TabPane.
- [src/main/java/org/proyectobdmotos/ui/navigation/ScreenLoader.java](../src/main/java/org/proyectobdmotos/ui/navigation/ScreenLoader.java) — Implementar método `load(fxmlPath)` para cargar FXML.
- `src/main/resources/fxml/main.fxml` — (crear) Pantalla principal.
- `src/main/resources/fxml/cliente-lista.fxml` — (crear) Tabla de clientes.
- `src/main/resources/fxml/moto-lista.fxml` — (crear) Tabla de motos.
- `src/main/resources/fxml/contrato-lista.fxml` — (crear) Tabla de contratos.

**Entregable:**

- Pantalla inicial con TabPane (Clientes | Motos | Contratos).
- Tablas pobladas desde DAOs (sin edición aún, solo lectura).
- No hay botones de acción (eso es fase 2).

**Tiempo estimado:** 1-2 sesiones.

### Fase 2 — ABM básico (Crear, Listar, Eliminar)

**Objetivo:** Casos de uso más simples primero (Cliente/Moto), luego Contrato (más complejo).

**Archivos:**

- [src/main/java/org/proyectobdmotos/controller/ClienteController.java](../src/main/java/org/proyectobdmotos/controller/ClienteController.java) — Implementar handlers:
  - `onCrearCliente()` → abre dialog, valida, inserta via `clienteService.crearCliente()`.
  - `onEliminarCliente()` → confirma, elimina via `clienteService.eliminarCliente()`.
  - `onActualizar()` → recarga tabla desde `clienteService.listarTodos()`.

- [src/main/java/org/proyectobdmotos/controller/MotoController.java](../src/main/java/org/proyectobdmotos/controller/MotoController.java) — Igual que Cliente.

- `src/main/resources/fxml/cliente-form.fxml` — (crear) Form dialog para insertar/editar cliente.
- `src/main/resources/fxml/moto-form.fxml` — (crear) Form dialog para insertar/editar moto.

**Entregable:**

- Crear cliente desde UI → se persiste en BD.
- Crear moto desde UI → se persiste en BD.
- Listar refrescable con botón "Actualizar".
- Eliminar cliente/moto con confirmación.

**Tiempo estimado:** 2-3 sesiones.

### Fase 3 — Crear Contrato (lógica de negocio en acción)

**Objetivo:** Validar cliente + moto + reflejar estado.

**Archivos:**

- [src/main/java/org/proyectobdmotos/controller/ContratoController.java](../src/main/java/org/proyectobdmotos/controller/ContratoController.java):
  - `onCrearContrato()` → form con ComboBox cliente + ComboBox moto + pickers fechas → `contratoService.crearContrato()`.
  - Validar disponibilidad en tiempo real (deshabilitar motos no disponibles).

- `src/main/resources/fxml/contrato-form.fxml` — (crear) Form dialog con validaciones.

**Lógica:**

- Al seleccionar moto, verificar con `motoService.estaDisponible()`.
- Al guardar, `contratoService.crearContrato()` lanza excepción si cliente/moto inválido.
- Trigger BD automáticamente pone moto en "alquilada".
- UI refresca tabla de motos (antes "disponible", ahora "alquilada").

**Entregable:**

- Crear contrato desde UI con validaciones.
- Verificar que moto cambió de situación en tiempo real.

**Tiempo estimado:** 2 sesiones.

### Fase 4 — Finalizar Contrato y Cálculo de Importes ✅ **LÓGICA COMPLETA**

**Estado:** Lógica de negocio implementada completamente en backend.

**Implementado:**

- ✅ [src/main/java/org/proyectobdmotos/models/Contrato.java](../src/main/java/org/proyectobdmotos/models/Contrato.java) — Métodos de cálculo:
  - `calcularDiasPactados()`, `calcularDiasProrrogaReal()`, `calcularImporteBase()`, `calcularRecargoProrroga()`, `calcularImporteTotalTeorico()`.

- ✅ [src/main/java/org/proyectobdmotos/services/ContratoService.java](../src/main/java/org/proyectobdmotos/services/ContratoService.java):
  - `finalizarContrato(contrato)` — valida, calcula importes, persiste, cambia moto a "disponible".

**Pendiente:**

- ⏳ [src/main/java/org/proyectobdmotos/controller/ContratoController.java](../src/main/java/org/proyectobdmotos/controller/ContratoController.java):
  - `onFinalizarContrato()` — form con fecha entrega + km llegada → muestra importe final → confirma.

**Entregable backend:**

- ✅ Finalizar contrato reflejado en BD.
- ✅ Moto vuelve a "disponible".
- ✅ Importe calculado y registrado en logs.
- ⏳ UI para mostrar importe al usuario.

**Tiempo estimado UI:** 1 sesión.

### Fase 5 — Reportes (lectura pura)

**Objetivo:** Pantallas de reportes sin ABM.

**Archivos:**

- [src/main/java/org/proyectobdmotos/controller/ReportesController.java](../src/main/java/org/proyectobdmotos/controller/ReportesController.java) — (crear)
- `src/main/resources/fxml/reportes-tab.fxml` — (crear) Con sub-tabs para:
  - Clientes por municipio.
  - Motos por km.
  - Estado de motos.
  - Incumplidores.

**Entregable:**

- Tablas de reportes pobladas desde DAOs.
- Exportar a CSV (opcional).

**Tiempo estimado:** 1 sesión.

### Fase 6 — Pruebas e integración

**Objetivo:** Cobertura de pruebas unitarias e integración.

**Archivos:**

- `src/test/java/org/proyectobdmotos/dao/ClienteDAOTest.java` — (crear) Tests de CRUD.
- `src/test/java/org/proyectobdmotos/services/ContratoServiceTest.java` — (crear) Tests de validaciones.

**Entregable:**

- 70%+ de cobertura en DAOs/Services.
- Pruebas de flujo completo (crear → listar → finalizar).

**Tiempo estimado:** 1-2 sesiones.


---

## 5) Backlog funcional y técnico

### Funcional (nuevas capacidades)

1. ⏳ **CRUD Clientes** — Backend completo, UI pendiente (Fase 2).
2. ⏳ **CRUD Motos** — Backend completo, UI pendiente (Fase 2).
3. ⏳ **Crear Contrato con validaciones** — Backend completo, UI pendiente (Fase 3).
4. ✅ **Finalizar Contrato + Cálculo importes** — Backend completo, UI pendiente (Fase 4).
5. ⏳ **Reportes (municipio, km, situación, incumplidores)** — Backend completo, UI pendiente (Fase 5).
6. ⏳ **Historial de reparaciones** (agregar tabla `mantenimiento`).
7. ⏳ **Exportar reportes a PDF**.
8. ⏳ **Búsqueda avanzada** (filtrar por rango fechas, municipio, etc.).

### Técnico (deuda, refactoring, tests)

1. ⏳ **Resolver inconsistencia de tipos** (String vs int en IDs de catálogos).
2. ✅ **Contrato usa `id_contrato` entero (refactor aplicado)**.
3. ⏳ **Pruebas unitarias** (DAO, Service, validaciones) — Fase 6.
4. ⏳ **Pruebas de integración** (flujo completo cliente → contrato → finalizar).
5. ⏳ **Validar CI formato** (11 dígitos correctos).
6. ⏳ **Agregar campos de auditoría** (`fecha_creacion`, `fecha_modificacion` en tablas).
7. ⏳ **Rate limiting / seguridad** en acceso a datos (no crítico en educativo).

---

## 6) Criterios de MVP

Se considera **MVP listo** cuando:

- ❌ Se puede registrar cliente desde UI (backend listo).
- ❌ Se puede registrar moto desde UI (backend listo).
- ❌ Se puede crear contrato (cliente + moto + fechas) desde UI (backend listo).
- ❌ Se lista información en tablas JavaFX (clientes, motos, contratos).
- ✅ El estado de moto se actualiza automáticamente al crear/finalizar contrato (backend).
- ❌ Se puede finalizar contrato y ver importe calculado (backend listo, UI pendiente).
- ❌ Hay al menos un reporte funcional (ej: "Motos por situación") (backend listo, UI pendiente).
- ❌ Hay pruebas mínimas de integración DAOs/Services.
- ❌ Hay placeholders en pantalla inicial.

**Tiempo estimado total MVP:** 6-8 sesiones de desarrollo UI + 2 sesiones de pruebas (1-2 semanas).

**Progreso actual:** Backend 100% completo, UI 0% completo.

---

## 7) Próximo paso inmediato

**Empezar Fase 1 (UI mínima):**

1. Reemplazar placeholder en [FxApp.java](../src/main/java/org/proyectobdmotos/ui/FxApp.java) con `BorderPane` + `TabPane`.
2. Crear `src/main/resources/fxml/main.fxml` (estructura básica).
3. Implementar `ScreenLoader.load(fxmlPath)` para cargar FXML.
4. Crear tablas de clientes, motos, contratos (lectura pura, sin edición).
5. Ejecutar `mvn javafx:run` y verificar que se vean datos reales de la BD.

**Estimado:** 1 sesión.
