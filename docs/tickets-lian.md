# Tickets de Lian (Líder técnico)

> Objetivo: terminar tu parte sin hacer todo de golpe, priorizando primero lógica estable y después UI JavaFX/FXML.

---

## 0) Reglas de trabajo para tu flujo (Lian)

- Un ticket = una funcionalidad concreta + un PR chico.
- No arrancar UI hasta cerrar **freeze de lógica**.
- Cada PR debe incluir: alcance, archivos tocados, riesgos y cómo probar manualmente.
- Si un ticket cambia firmas públicas de `services`, avisar al equipo antes del merge.

---

## 1) Backlog de funcionalidades (solo tu parte)

## Funcionalidad F1 — Contrato técnico estable para UI

### Ticket L1 — Definir API final de servicios para consumo UI

**Objetivo**
Cerrar firmas de métodos, tipos de retorno y excepciones esperadas para que Darel/Dario trabajen sin bloqueos.

**Archivos a modificar/crear**
- `src/main/java/org/proyectobdmotos/services/AgenciaService.java`
- `src/main/java/org/proyectobdmotos/services/ContratoService.java`
- `src/main/java/org/proyectobdmotos/services/ClienteService.java`
- `src/main/java/org/proyectobdmotos/services/MotoService.java`
- `docs/contrato-integracion-ui.md` (crear)

**Checklist de implementación**
- [x] Revisar coherencia de métodos públicos en services.
- [x] Unificar nombres de operaciones para UI (crear/listar/buscar/actualizar/eliminar/finalizar).
- [x] Definir excepciones de negocio estándar por caso.
- [x] Documentar contrato final en `docs/contrato-integracion-ui.md`.

**Criterio de aceptación**
- El documento de contrato existe y refleja métodos reales del código.
- No hay métodos ambiguos o duplicados para el mismo caso de uso.

**Qué investigar/estudiar antes de implementar**
- Diseño de contratos de aplicación (service API) para UI desacoplada.
- Estrategia de manejo de excepciones de negocio (mensajes útiles para controllers).

---

### Ticket L2 — Cerrar deuda técnica de identidad de contrato

**Objetivo**
Refactorizar identidad de contrato para usar `id_contrato` entero en toda la capa de dominio/DAO/services,
y remover referencias a clave compuesta en el contrato de integración.

**Archivos a modificar**
- `src/main/java/org/proyectobdmotos/dao/IContratoDAO.java`
- `src/main/java/org/proyectobdmotos/dao/ContratoDAO.java`
- `src/main/java/org/proyectobdmotos/services/ContratoService.java`
- `docs/contrato-integracion-ui.md`
- `docs/estado-actual.md`

**Checklist de implementación**
- [x] Unificar el uso de `Integer idContrato` en DAO/Service.
- [x] Eliminar dependencia de `ContratoID` (ya no existe en el modelo).
- [x] Actualizar documentación de integración UI para reflejar el ID entero.

**Criterio de aceptación**
- Todas las operaciones de contrato usan `Integer idContrato`.
- La documentación no referencia `ContratoID` ni PK compuesta.

**Qué investigar/estudiar antes de implementar**
- Impacto de cambiar PK compuesta a PK simple en consultas y reportes.

---

## Funcionalidad F2 — Lógica de finalización de contrato robusta

### Ticket L3 — Consolidar finalización de contrato y cálculo de importe

**Objetivo**
Centralizar reglas de finalización (fechas, km, retorno de moto a disponible, cálculo final) en dominio + servicio.

**Archivos a modificar**
- `src/main/java/org/proyectobdmotos/models/Contrato.java`
- `src/main/java/org/proyectobdmotos/services/ContratoService.java`
- `src/main/java/org/proyectobdmotos/services/AgenciaService.java`

**Checklist de implementación**
- [x] Definir método de cálculo final en `Contrato`.
- [x] Orquestar finalización en `ContratoService`.
- [x] Exponer operación en `AgenciaService` para consumo de UI.
- [x] Asegurar consistencia de transición de estado de moto.

**Criterio de aceptación**
- El flujo de finalización queda encapsulado en service/model (no en controller).
- El contrato de servicio para finalizar queda documentado.

**Qué investigar/estudiar antes de implementar**
- Modelado de reglas de negocio en capa de dominio vs capa servicio.
- Validaciones de fechas y kilometraje para contratos.

---

## Funcionalidad F3 — Shell de UI JavaFX/FXML e integración base

### Ticket L4 — Reemplazar placeholder por shell navegable

**Objetivo**
Tener estructura base de UI con navegación y vistas de lectura iniciales.

**Definición de investigación previa (obligatoria antes de implementar)**

> Base consultada: JavaFX 21 (`FXMLLoader`, `setControllerFactory`, `fx:include`, ciclo de vida `initialize()`) según documentación oficial OpenJFX (vía Context7).

#### Preguntas que deben quedar respondidas

1. ¿Cómo usar `FXMLLoader.setControllerFactory(...)` sin romper `fx:controller` y manteniendo constructor injection?
2. ¿Cómo componer pantallas con `fx:include` sin generar dependencia circular entre controllers?
3. ¿Qué lógica va en `initialize()` y qué lógica debe quedar fuera (servicios, navegación, reglas de negocio)?
4. ¿Qué responsabilidades quedan en `FxApp`, `ScreenLoader`, controllers y stores en arquitectura por capas?

#### Entregable mínimo de investigación (antes de tocar código)

- Documento corto dentro del ticket/PR con:
  - decisiones de arquitectura de carga de pantallas,
  - riesgos (ciclo de vida de controller, null en `@FXML`, includes),
  - patrón de navegación elegido para L4.

#### Criterio de salida de investigación

- Existe consenso técnico sobre:
  - uso de `ScreenLoader` como único punto de carga FXML,
  - uso de `controllerFactory` para inyección,
  - composición inicial de `main.fxml` + vistas hijas de listas,
  - límites claros controller vs service/store.

**Archivos a modificar/crear**
- `src/main/java/org/proyectobdmotos/ui/FxApp.java`
- `src/main/java/org/proyectobdmotos/ui/navigation/ScreenLoader.java`
- `src/main/resources/fxml/main.fxml` (crear)
- `src/main/resources/fxml/cliente-lista.fxml` (crear)
- `src/main/resources/fxml/moto-lista.fxml` (crear)
- `src/main/resources/fxml/contrato-lista.fxml` (crear)

**Checklist de implementación**
- [x] Reemplazar placeholder en `FxApp` por carga de `main.fxml` vía `ScreenLoader`.
- [x] Crear `main.fxml` con `BorderPane` y contenedor central para navegación.
- [x] Crear vistas base de lectura: `cliente-lista.fxml`, `moto-lista.fxml`, `contrato-lista.fxml`.
- [x] Definir estrategia de composición (carga dinámica con `ScreenLoader` y/o `fx:include` para componentes presentacionales).
- [x] Validar ciclo de vida de controllers (`@FXML` inyectado + `initialize()` sin acceso prematuro a nodos).
- [x] Verificar carga inicial sin errores de FXML ni controllers no registrados.

**Plan técnico L4 (listo para ejecutar)**

### Fase A — Shell y arranque
- `src/main/resources/fxml/main.fxml`: definir shell (`BorderPane`) y región `center` para contenido.
- `src/main/java/org/proyectobdmotos/ui/FxApp.java`: cargar shell real en lugar del placeholder.

### Fase B — Pantallas de listas base
- `src/main/resources/fxml/cliente-lista.fxml`
- `src/main/resources/fxml/moto-lista.fxml`
- `src/main/resources/fxml/contrato-lista.fxml`

Cada pantalla inicia en modo lectura (tabla + layout base), sin ABM todavía.

### Fase C — Navegación y composición
- `src/main/java/org/proyectobdmotos/ui/navigation/ScreenLoader.java`: consolidar carga de vistas y fallback de errores.
- Composición: shell carga vistas hijas por acción de navegación; `fx:include` reservado para componentes reusables, no para navegación global.

### Fase D — Verificación manual
- Arranque de app sin placeholder.
- Navegación entre 3 vistas base.
- Sin instanciación manual de controllers desde UI.
- Sin lógica de negocio/SQL en controllers.

**Criterio de aceptación**
- La app abre pantalla principal real (sin placeholder).
- Se visualiza navegación entre vistas base.
- La composición de pantallas sigue arquitectura por capas documentada en `docs/diseno-ui-javafx.md`.
- El ticket deja trazabilidad explícita de investigación previa y decisiones tomadas.

**Qué investigar/estudiar antes de implementar**
- Estructuración de JavaFX con FXML en apps por capas.
- Buenas prácticas de composición de pantallas (main + vistas hijas).

---

### Ticket L5 — Inyección de controladores vía ScreenLoader

**Objetivo**
Asegurar que controladores se creen por constructor injection desde el composition root.

**Archivos a modificar**
- `src/main/java/org/proyectobdmotos/ui/navigation/ScreenLoader.java`
- `src/main/java/org/proyectobdmotos/ui/AppCompositionRoot.java`

**Checklist de implementación**
- [x] Implementar/ajustar `FXMLLoader.setControllerFactory(...)`.
- [x] Registrar mapping de controladores actuales.
- [x] Validar que cada vista carga su controller con dependencias.

**Criterio de aceptación**
- Ningún controller de UI se instancia manualmente en vistas.
- AppCompositionRoot centraliza wiring.

**Qué investigar/estudiar antes de implementar**
- Patrón Composition Root aplicado a JavaFX.
- Errores comunes de controller factory y ciclo de vida `initialize()`.

---

## Funcionalidad F4 — Integración y coordinación técnica del equipo

### Ticket L6 — Congelar lógica y habilitar fase UI del equipo

**Objetivo**
Formalizar el “logic freeze” para que Darel y Dario avancen UI/reportes sin cambios inesperados de contrato.

**Archivos a modificar**
- `docs/estado-actual.md`
- `docs/distribucion-proyecto.md`

**Checklist de implementación**
- [ ] Actualizar estado indicando “servicios congelados para UI”.
- [ ] Publicar lista de métodos públicos permitidos para consumo.
- [ ] Dejar explícito protocolo de cambios de contrato (si hay excepción).

**Criterio de aceptación**
- Equipo tiene una referencia única y vigente de APIs y restricciones.

**Qué investigar/estudiar antes de implementar**
- Estrategias de coordinación técnica en equipos chicos (freeze + PR policy).

---

## 2) Plan diario sugerido (8 días, sin sobrecarga)

## Día 1 — Preparación de arquitectura
- Ejecutar Ticket **L1** (análisis y borrador de contrato de integración).
- Resultado esperado: primer borrador de `docs/contrato-integracion-ui.md`.

## Día 2 — Cierre de contrato técnico
- Terminar Ticket **L1** y abrir PR.
- Mini-sync con equipo para validar firmas.

## Día 3 — Deuda técnica de identidad
- Ejecutar Ticket **L2** completo.
- PR chico solo de `ContratoID`.

## Día 4 — Reglas de finalización (parte 1)
- Iniciar Ticket **L3**: lógica en `Contrato`.

## Día 5 — Reglas de finalización (parte 2)
- Terminar Ticket **L3**: orquestación en `ContratoService` + exposición en `AgenciaService`.

## Día 6 — UI shell base
- Ejecutar Ticket **L4** completo (main + vistas de lista).

## Día 7 — Wiring e inyección
- Ejecutar Ticket **L5** completo.

## Día 8 — Freeze y coordinación final
- Ejecutar Ticket **L6**.
- Actualizar docs y dejar cancha lista para Darel/Dario.

---

## 3) Definición de Done (DoD) por ticket

- [ ] Ticket implementado en branch propia.
- [ ] PR con alcance acotado (sin mezclar funcionalidades).
- [ ] Archivos impactados listados en descripción del PR.
- [ ] Sin romper contratos públicos ya acordados.
- [ ] Documentación actualizada en `docs/` cuando aplique.

---

## 4) Riesgos y mitigaciones (tu rol de líder)

- **Riesgo:** cambios de contrato tarde en fase UI.
  - **Mitigación:** freeze explícito + cambios sólo por PR excepcional.

- **Riesgo:** conflicto entre controllers y services por firmas inestables.
  - **Mitigación:** `docs/contrato-integracion-ui.md` como fuente de verdad.

- **Riesgo:** bloqueos por conectividad de Dario.
  - **Mitigación:** dividir tickets SQL en micro-PRs y priorizar offline tasks.

---

## 5) Vista rápida de tickets (resumen)

- **L1:** API final de servicios + contrato UI
- **L2:** `ContratoID` robusto (`equals/hashCode`)
- **L3:** Finalización de contrato + cálculo de importe
- **L4:** Shell JavaFX/FXML base
- **L5:** ScreenLoader + controllerFactory + composition root
- **L6:** Logic freeze + documentación de coordinación

Con esto tenés un roadmap ordenado, loco: primero cimientos (lógica), después estructura (UI), y recién ahí decoración (flujos completos).
