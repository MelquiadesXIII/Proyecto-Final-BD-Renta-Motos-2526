# Diseño de Interfaz y Arquitectura UI (JavaFX)

**Versión de JavaFX:** 21+

Este documento define la **arquitectura objetivo** de la UI (no el estado implementado actual), basándose en las referencias de diseño y las reglas de negocio del sistema de renta de motos.

## Estado de implementación (post-refactor)

- `FxApp` inicializa el `AppCompositionRoot`, pero aún muestra una escena placeholder.
- `ScreenLoader` ya está preparado con `FXMLLoader.setControllerFactory(...)` para inyección por constructor.
- Existen `ClienteController`, `MotoController`, `ContratoController` como stubs.
- **Aún no existen FXML** en `src/main/resources/fxml/`.

> En consecuencia, este documento debe leerse como **blueprint de UI** para la siguiente fase de implementación.

**Nota sobre estilos:** Los estilos visuales mencionados (bordes redoneados, sombras, colores) se implementan mediante archivos CSS de JavaFX, siguiendo las convenciones de la plataforma.

## 1. Contexto de Roles y Permisos

La aplicación distingue estrictamente entre dos tipos de usuarios:
- **Cliente:** Solo puede crear nuevos contratos ("New Order") y ver su propia información. NO tiene acceso a la gestión de clientes ni al listado global de contratos.
- **Administrador:** Tiene acceso completo al CRUD del sistema, listado de contratos, clientes e inventario.

## 2. Navegación Principal Objetivo (Shell - `main.fxml`)

El contenedor principal objetivo usa un `BorderPane`. El panel izquierdo (`left`) contiene el menú lateral (`VBox`) y el centro (`center`) es un `StackPane` (o el propio centro del `BorderPane`) donde `ScreenLoader` inyecta vistas dinámicamente.

### Pestañas por Rol:

**Administrador:**
- Nuevo Contrato
- Contratos (Tabla global)
- Clientes
- Inventario (Catálogo visual de motos)
- Reportes
- Ayuda

**Cliente:**
- Nuevo Contrato
- Mis Contratos
- Inventario (Catálogo visual de motos)
- Ayuda

## 3. Desglose de Pantallas y Componentes JavaFX

### A. Inventario / Catálogo de Motos
- **Layout Base:** Se usa un `TilePane` para mostrar una grilla de tarjetas de tamaño uniforme. TilePane es preferible a FlowPane porque mantiene todas las tarjetas con dimensiones consistentes, creando una presentación visual más profesional tipo galería.
- **Tarjeta de Moto:** Cada ítem es un `VBox` estilizado como tarjeta mediante CSS (bordes redoneados con `-fx-background-radius`, sombra con `-fx-effect: dropshadow`) que contiene un `ImageView` (foto) y un `Label` (nombre/marca).
- **Vista de Detalle:** Al hacer clic en una tarjeta, se abre un `Dialog` modal con la foto en grande, título, precio, descripción y un calendario/grilla de disponibilidad. Se usa Dialog en lugar de SplitPane para mantener el foco del usuario en los detalles de la moto seleccionada sin distracciones.

### B. Listado de Contratos
- **Layout Base:** Un `TableView` clásico.
- **Columnas:** ID, Cliente (oculta para el rol Cliente), Moto, Fecha Inicio, Fecha Fin, Estado, Importe.
- **Interacción:** Doble clic en una fila abre la vista de Detalle del Contrato.

### C. Detalle de Contrato (Vista Admin)
- **Diseño Visual:** Fondo principal gris claro (definido en CSS con `-fx-background-color`) con contenedores `VBox` tipo "tarjetas" blancas con bordes redoneados mediante `-fx-background-radius` y `-fx-effect: dropshadow` para sombras sutiles.
- **Encabezado:** `HBox` superior con el ID del contrato, un Badge de estado (`Label` con `-fx-background-color` y `-fx-background-radius` para crear el efecto de badge) y botones de acción a la derecha (ej. "Finalizar").
- **Tarjetas de Información (`GridPane` / `HBox`):**
  - *Customer:* Círculo con iniciales (usando `Label` con `-fx-background-radius: 50%`), nombre y correo.
  - *Rental Period:* Componentes `DatePicker` para inicio/fin.
- **Sección Central:** Un `TableView` limpio con el detalle del alquiler y debajo un resumen de importes alineado a la derecha.
- **Nota de alcance:** Se excluye intencionalmente cualquier panel lateral derecho relacionado con correos electrónicos, adaptando el diseño a las necesidades reales del software.

### D. Nuevo Contrato ("New Order")
- **Rol Cliente:** La tarjeta "Customer" está pre-llenada y bloqueada con sus datos de sesión (usando `TextField` con `setEditable(false)`).
- **Rol Admin:** Incluye un buscador `ComboBox` con autocompletado (configurado con `setEditable(true)`) para seleccionar a qué cliente facturarle. El ComboBox es preferible a un Dialog porque permite búsqueda inline sin interrumpir el flujo.
- **Selección de Moto:** Botón que abre el catálogo visual (Inventario) en un `Dialog` modal para elegir una moto disponible, manteniendo el contexto del formulario principal.
- **Resumen:** `TableView` o `VBox` con `Label` inferior que calcula dinámicamente el precio estimado según las fechas ingresadas y la tarifa de la moto seleccionada, usando bindings de JavaFX para actualización automática.

## 4. Arquitectura de Componentes Presentacionales

Para evitar archivos FXML monolíticos e inmanejables, se aplicará el patrón de **Componentes Presentacionales**.
Las vistas complejas se dividirán en FXMLs más pequeños que se integrarán en las vistas principales usando la etiqueta `<fx:include source="..."/>`.

### Convenciones obligatorias de estructura

- **Ubicación FXML:** `src/main/resources/fxml/`
- **Naming FXML:** `kebab-case` (ej.: `cliente-lista.fxml`)
- **Naming controllers:** `PascalCase` con sufijo `Controller` (ej.: `ClienteListaController`)
- **Inyección de controllers:** siempre via `ScreenLoader` + `setControllerFactory(...)`
- **Responsabilidad de controller:** manejar eventos UI, delegar a Service/Store, y mostrar errores de negocio con `Alert`
- **Sin lógica de negocio/SQL en controllers**

**Ejemplos de componentes reusables:**
- `componente-tarjeta-cliente.fxml`
- `componente-tarjeta-fechas.fxml`
- `componente-item-moto.fxml`

Esta modularidad permite que distintos desarrolladores trabajen en diferentes partes de la UI simultáneamente sin generar conflictos de merge masivos, facilitando el mantenimiento y la escalabilidad del diseño en JavaFX.

## 5. Arquitectura por capas aplicada a JavaFX/FXML (guía de implementación)

### 5.1 Responsabilidades por capa

- **FxApp (arranque):** inicializa JavaFX, crea `AppCompositionRoot`, carga shell principal (`main.fxml`) y configura `Stage`.
- **AppCompositionRoot (wiring):** único lugar para construir dependencias (Connection, DAOs, Services, Stores, navegación).
- **ScreenLoader (infra UI):** único punto para cargar FXML y resolver controllers mediante `setControllerFactory(...)`.
- **Controller (adaptador UI):** recibe eventos, lee/escribe controles JavaFX, delega a Service/Store, muestra `Alert` ante `BusinessException`.
- **Service (aplicación/dominio):** reglas de negocio y validaciones; no depende de JavaFX.
- **DAO (persistencia):** SQL/JDBC con `PreparedStatement`; sin conocimiento de UI.
- **Store (estado observable):** estado compartido de UI (`ObservableList`) y sincronización entre pantallas.

### 5.2 Reglas de composición de pantallas

- **Navegación global:** por carga dinámica de vistas en el contenedor central del shell (`main.fxml`) usando `ScreenLoader`.
- **Composición local:** por `fx:include` solo para componentes presentacionales reusables dentro de una pantalla.
- **No mezclar responsabilidades:** `fx:include` no sustituye el flujo de navegación global.

### 5.3 Ciclo de vida de controllers (JavaFX 21)

- `@FXML` se inyecta durante el `load()` del `FXMLLoader`.
- `initialize()` se ejecuta cuando el árbol FXML ya fue procesado.
- En `initialize()`: preparar bindings/listeners/config visual.
- Fuera de `initialize()`: wiring de dependencias por constructor (vía `controllerFactory`).
- Prohibido acceder a nodos `@FXML` antes de la carga del FXML.

### 5.4 Pauta concreta para L4

1. Crear `main.fxml` como shell (`BorderPane`).
2. Reemplazar placeholder de `FxApp` para cargar `main.fxml`.
3. Crear vistas base de lectura (`cliente-lista.fxml`, `moto-lista.fxml`, `contrato-lista.fxml`).
4. Mantener controllers sin lógica de negocio; delegar siempre a services/stores.
5. Registrar controllers nuevos en `ScreenLoader`.

## 6. Ajustes recomendados para mantener el documento sincronizado

1. Mantener una mini sección de “Estado actual” al inicio (qué existe vs qué falta).
2. Cuando se cree cada FXML real, enlazarlo aquí con su path exacto.
3. Actualizar esta guía junto con `docs/estado-actual.md` en cada avance de UI para evitar deriva documental.
