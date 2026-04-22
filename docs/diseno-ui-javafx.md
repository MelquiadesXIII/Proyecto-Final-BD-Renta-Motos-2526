# Diseño de Interfaz y Arquitectura UI (JavaFX)

Este documento define la estructura visual, los componentes de JavaFX y el flujo de navegación de la aplicación, basándose en las referencias de diseño (imágenes del sistema base) y las reglas de negocio del sistema de renta de motos.

## 1. Contexto de Roles y Permisos

La aplicación distingue estrictamente entre dos tipos de usuarios:
- **Cliente:** Solo puede crear nuevos contratos ("New Order") y ver su propia información. NO tiene acceso a la gestión de clientes ni al listado global de contratos.
- **Administrador:** Tiene acceso completo al CRUD del sistema, listado de contratos, clientes e inventario.

## 2. Navegación Principal (Shell - `main.fxml`)

El contenedor principal utiliza un `BorderPane`. El panel izquierdo (`left`) contiene el menú lateral de navegación (`VBox`), y el centro (`center`) es un `StackPane` (o el propio centro del BorderPane) donde el `ScreenLoader` inyecta las vistas dinámicamente.

### Pestañas por Rol:

**Administrador:**
- Nuevo Contrato
- Contratos (Tabla global)
- Clientes
- Inventario (Catálogo visual)
- Reportes
- Ayuda

**Cliente:**
- Nuevo Contrato
- Mis Contratos
- Inventario (Catálogo visual)
- Ayuda

## 3. Desglose de Pantallas y Componentes JavaFX

### A. Inventario / Catálogo de Motos
- **Layout Base:** En lugar de una tabla tradicional, se usa un `TilePane` o `FlowPane` para mostrar una grilla responsiva.
- **Tarjeta de Moto:** Cada ítem es un `VBox` estilizado como tarjeta (bordes redondeados, sombra ligera) que contiene un `ImageView` (foto) y un `Label` (nombre/marca).
- **Vista de Detalle:** Al hacer clic en una tarjeta, se abre un panel (o un `SplitPane` / `Dialog`) con la foto en grande, título, precio, descripción y un calendario/grilla de disponibilidad.

### B. Listado de Contratos
- **Layout Base:** Un `TableView` clásico.
- **Columnas:** ID, Cliente (oculta para el rol Cliente), Moto, Fecha Inicio, Fecha Fin, Estado, Importe.
- **Interacción:** Doble clic en una fila abre la vista de Detalle del Contrato.

### C. Detalle de Contrato (Vista Admin)
- **Diseño Visual:** Fondo principal gris claro con contenedores `VBox` tipo "tarjetas" blancas con bordes redondeados (estilo web moderno).
- **Encabezado:** `HBox` superior con el ID del contrato, un Badge de estado (`Label` con fondo de color) y botones de acción a la derecha (ej. "Finalizar").
- **Tarjetas de Información (`GridPane` / `HBox`):**
  - *Customer:* Círculo con iniciales, nombre y correo.
  - *Rental Period:* Componentes `DatePicker` para inicio/fin.
- **Sección Central:** Un `TableView` limpio con el detalle del alquiler y debajo un resumen de importes alineado a la derecha.
- **Nota de alcance:** Se excluye intencionalmente cualquier panel lateral derecho relacionado con correos electrónicos, adaptando el diseño a las necesidades reales del software.

### D. Nuevo Contrato ("New Order")
- **Rol Cliente:** La tarjeta "Customer" está pre-llenada y bloqueada con sus datos de sesión.
- **Rol Admin:** Incluye un buscador (`ComboBox` autocompletable o un `Dialog`) para seleccionar a qué cliente facturarle.
- **Selección de Moto:** Botón que abre el catálogo visual (Inventario) en un modal para elegir una moto disponible.
- **Resumen:** Tabla o listado inferior que calcula dinámicamente el precio estimado según las fechas ingresadas y la tarifa de la moto seleccionada.

## 4. Arquitectura de Componentes Presentacionales

Para evitar archivos FXML monolíticos e inmanejables, se aplicará el patrón de **Componentes Presentacionales**.
Las vistas complejas se dividirán en FXMLs más pequeños que se integrarán en las vistas principales usando la etiqueta `<fx:include source="..."/>`.

**Ejemplos de componentes reusables:**
- `componente-tarjeta-cliente.fxml`
- `componente-tarjeta-fechas.fxml`
- `componente-item-moto.fxml`

Esta modularidad permite que distintos desarrolladores trabajen en diferentes partes de la UI simultáneamente sin generar conflictos de merge masivos, facilitando el mantenimiento y la escalabilidad del diseño en JavaFX.