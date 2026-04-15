# Guía de estado actual del proyecto y próximos pasos

Fecha de corte: **15/04/2026**

## 1) Resumen ejecutivo

El proyecto tiene una **base técnica sólida** (arquitectura por capas, conexión a PostgreSQL, migraciones Flyway, modelos principales y composition root), pero está en fase de **esqueleto funcional**:

- La **BD ya está modelada y versionada** con migraciones.
- La **app JavaFX arranca**, pero usa una pantalla placeholder.
- La capa **DAO/Service/Controller está cableada**, pero aún sin casos de uso implementados.
- Las pruebas actuales solo validan humo básico (no reglas de negocio ni integración).

Estado global sugerido: **40% estructura / 15% funcionalidad de negocio**.

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

---

## 3) Qué está incompleto o pendiente crítico

### 3.1 UI

- No existen FXML en `src/main/resources`.
- `FxApp` muestra placeholder en lugar de pantalla real en [src/main/java/org/proyectobdmotos/ui/FxApp.java](../src/main/java/org/proyectobdmotos/ui/FxApp.java).
- Controladores (`ClienteController`, `MotoController`, `ContratoController`) sin handlers `@FXML`.

### 3.2 Persistencia (DAO)

- Interfaces y métodos CRUD están comentados/no expuestos en:
  - [src/main/java/org/proyectobdmotos/dao/GenericDAO.java](../src/main/java/org/proyectobdmotos/dao/GenericDAO.java)
  - [src/main/java/org/proyectobdmotos/dao/AbstractGenericDAO.java](../src/main/java/org/proyectobdmotos/dao/AbstractGenericDAO.java)
- `ClienteDAO`, `MotoDAO`, `ContratoDAO` son cascarones sin SQL real:
  - [src/main/java/org/proyectobdmotos/dao/ClienteDAO.java](../src/main/java/org/proyectobdmotos/dao/ClienteDAO.java)
  - [src/main/java/org/proyectobdmotos/dao/MotoDAO.java](../src/main/java/org/proyectobdmotos/dao/MotoDAO.java)
  - [src/main/java/org/proyectobdmotos/dao/ContratoDAO.java](../src/main/java/org/proyectobdmotos/dao/ContratoDAO.java)

### 3.3 Servicios (lógica de negocio)

- `ClienteService` y `MotoService` solo encapsulan el DAO.
- `ContratoService` no implementa casos clave (crear/finalizar contrato, validaciones de negocio).

### 3.4 Calidad y pruebas

- Única prueba tipo plantilla en [src/test/java/org/proyectobdmotos/AppTest.java](../src/test/java/org/proyectobdmotos/AppTest.java).
- No hay pruebas de:
  - DAOs contra PostgreSQL.
  - Servicios con reglas de negocio.
  - Integración de flujo principal (crear contrato y actualizar estado).

### 3.5 Observaciones técnicas a revisar

- Uso de `System.out.println`/`System.exit(...)` en flujo principal y UI, en conflicto con lineamientos del proyecto.
- Algunas inconsistencias de naming/tipos en modelos (por ejemplo `nombreCLiente`, IDs de catálogo como `String` cuando en BD son `INT`).

---

## 4) Qué implementar primero (orden recomendado)

## Fase 1 — Hacer operativa la capa DAO (prioridad máxima)

Objetivo: tener CRUD y consultas base funcionando con `PreparedStatement`.

Archivos objetivo:

- [src/main/java/org/proyectobdmotos/dao/GenericDAO.java](../src/main/java/org/proyectobdmotos/dao/GenericDAO.java)
- [src/main/java/org/proyectobdmotos/dao/AbstractGenericDAO.java](../src/main/java/org/proyectobdmotos/dao/AbstractGenericDAO.java)
- [src/main/java/org/proyectobdmotos/dao/ClienteDAO.java](../src/main/java/org/proyectobdmotos/dao/ClienteDAO.java)
- [src/main/java/org/proyectobdmotos/dao/MotoDAO.java](../src/main/java/org/proyectobdmotos/dao/MotoDAO.java)
- [src/main/java/org/proyectobdmotos/dao/ContratoDAO.java](../src/main/java/org/proyectobdmotos/dao/ContratoDAO.java)
- [src/main/java/org/proyectobdmotos/dao/IClienteDAO.java](../src/main/java/org/proyectobdmotos/dao/IClienteDAO.java)
- [src/main/java/org/proyectobdmotos/dao/IMotoDAO.java](../src/main/java/org/proyectobdmotos/dao/IMotoDAO.java)
- [src/main/java/org/proyectobdmotos/dao/IContratoDAO.java](../src/main/java/org/proyectobdmotos/dao/IContratoDAO.java)

Entregable mínimo:

- CRUD funcional para Cliente/Moto/Contrato.
- Consultas específicas iniciales de cada DAO.

## Fase 2 — Casos de uso de negocio en services

Objetivo: mover reglas a servicios y dejar controladores delgados.

Archivos objetivo:

- [src/main/java/org/proyectobdmotos/services/ClienteService.java](../src/main/java/org/proyectobdmotos/services/ClienteService.java)
- [src/main/java/org/proyectobdmotos/services/MotoService.java](../src/main/java/org/proyectobdmotos/services/MotoService.java)
- [src/main/java/org/proyectobdmotos/services/ContratoService.java](../src/main/java/org/proyectobdmotos/services/ContratoService.java)

Entregable mínimo:

- `crearContrato(...)` validando cliente/moto y persistiendo contrato.
- `finalizarContrato(...)` y actualización de situación de moto.
- Operaciones de listado para alimentar tablas UI.

## Fase 3 — Primera UI funcional con JavaFX + FXML

Objetivo: abandonar placeholder y tener al menos un flujo end-to-end visible.

Archivos objetivo:

- [src/main/java/org/proyectobdmotos/ui/FxApp.java](../src/main/java/org/proyectobdmotos/ui/FxApp.java)
- [src/main/java/org/proyectobdmotos/ui/navigation/ScreenLoader.java](../src/main/java/org/proyectobdmotos/ui/navigation/ScreenLoader.java)
- [src/main/java/org/proyectobdmotos/controller/ClienteController.java](../src/main/java/org/proyectobdmotos/controller/ClienteController.java)
- [src/main/java/org/proyectobdmotos/controller/MotoController.java](../src/main/java/org/proyectobdmotos/controller/MotoController.java)
- [src/main/java/org/proyectobdmotos/controller/ContratoController.java](../src/main/java/org/proyectobdmotos/controller/ContratoController.java)
- (nuevos) `src/main/resources/fxml/*.fxml`

Entregable mínimo:

- Pantalla principal con navegación.
- Tabla de motos y contrato básico desde UI.

## Fase 4 — Pruebas y hardening

Objetivo: consolidar calidad antes de escalar funcionalidades.

Archivos objetivo:

- [src/test/java/org/proyectobdmotos/AppTest.java](../src/test/java/org/proyectobdmotos/AppTest.java)
- Nuevas pruebas en `src/test/java/org/proyectobdmotos/dao/` y `src/test/java/org/proyectobdmotos/services/`.

Entregable mínimo:

- Tests de integración DAO.
- Tests de reglas de negocio críticas de contratos.

---

## 5) Backlog funcional sugerido (siguiente iteración)

1. ABM de clientes.
2. ABM de motos.
3. Crear contrato y reflejar estado de moto.
4. Finalizar contrato con cálculo de importes.
5. Reportes (municipio, ingresos por mes, estado de motos).

---

## 6) Criterios de “MVP listo”

Se puede considerar MVP cuando se cumpla todo:

- Se puede registrar cliente, moto y contrato desde UI.
- Se lista la información principal en tablas JavaFX.
- El estado de moto se actualiza de manera consistente al alquilar/finalizar.
- Hay pruebas de integración mínimas para DAOs y servicios críticos.
- No hay placeholders en la pantalla inicial.

---

## 7) Nota de priorización final

Si hay que elegir una sola línea de trabajo inmediata:

1. **Primero DAO + Services** (sin esto, la UI no tiene datos reales).
2. Luego **UI mínima funcional**.
3. Después **reportes y pulido**.
