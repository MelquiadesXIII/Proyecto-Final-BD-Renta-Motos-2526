# Contrato de integración UI ↔ Services (Ticket L1 — Freeze Fase 4)

Este documento define el **contrato congelado** que consumirá UI tras completar L1.
La Fase 4 deja estable la API pública de servicios, los retornos y el catálogo mínimo de excepciones de negocio.

---

## 1) Criterios del contrato congelado (Fase 4)

1. Verbos canónicos para UI: `crear`, `listar`, `buscar`, `actualizar`, `eliminar`, `finalizar`.
2. Retornos explícitos por caso:
   - `void` para comandos (crear/actualizar/eliminar/finalizar).
   - `Optional<T>` para búsquedas por identificador.
   - `List<T>` o `List<DTO>` para listados y reportes.
3. Excepciones de negocio estables por `errorCode` (no por parsing de texto).
4. Fuente de verdad para UI: **services** (sin SQL ni reglas de negocio en controllers).

---

## 2) API vigente validada contra código

### 2.1 `ClienteService`

| Operación canónica | Firma actual | Retorno | Estado |
|---|---|---|---|
| crear | `void crearCliente(Cliente cliente)` | `void` | vigente |
| actualizar | `void actualizarCliente(Cliente cliente)` | `void` | vigente |
| eliminar | `void eliminarCliente(String ci)` | `void` | vigente |
| eliminar (cascada) | `void eliminarClienteConCascada(String ci)` | `void` | vigente |
| buscar | `Optional<Cliente> buscarPorCi(String ci)` | `Optional<Cliente>` | vigente |
| listar | `List<Cliente> listarTodos()` | `List<Cliente>` | vigente |
| listar (reporte) | `List<ClienteDTO> listarClientesPorMunicipio()` | `List<ClienteDTO>` | vigente |
| listar (reporte) | `List<Cliente> obtenerClientesIncumplidores()` | `List<Cliente>` | vigente |

### 2.2 `MotoService`

| Operación canónica | Firma actual | Retorno | Estado |
|---|---|---|---|
| crear | `void crearMoto(Moto moto)` | `void` | vigente |
| actualizar | `void actualizarMoto(Moto moto)` | `void` | vigente |
| eliminar | `void eliminarMoto(String matricula)` | `void` | vigente |
| buscar | `Optional<Moto> buscarPorMatricula(String matricula)` | `Optional<Moto>` | vigente |
| listar | `List<Moto> listarTodos()` | `List<Moto>` | vigente |
| buscar (estado) | `boolean estaDisponible(String matricula)` | `boolean` | vigente |
| actualizar (estado) | `void cambiarEstado(String matricula, Situacion nuevaSituacion)` | `void` | vigente |
| listar (reporte) | `List<MotoDTO> listarMotosConKilometraje()` | `List<MotoDTO>` | vigente |
| listar (reporte) | `List<SituacionMotoDTO> listarSituacionMotos()` | `List<SituacionMotoDTO>` | vigente |

### 2.3 `ContratoService`

| Operación canónica | Firma actual | Retorno | Estado |
|---|---|---|---|
| crear | `void crearContrato(Contrato contrato)` | `void` | vigente |
| actualizar | `void actualizarContrato(Contrato contrato)` | `void` | vigente |
| finalizar | `void finalizarContrato(Contrato contrato)` | `void` | vigente |
| buscar | `Optional<Contrato> buscarPorId(ContratoID id)` | `Optional<Contrato>` | vigente |
| listar | `List<Contrato> listarTodos()` | `List<Contrato>` | vigente |
| listar (detalle) | `List<Contrato> listarContratosCompletos()` | `List<Contrato>` | vigente |
| eliminar | `void eliminarContrato(ContratoID id)` | `void` | vigente |

### 2.4 `AgenciaService`

| Operación actual | Firma actual | Estado |
|---|---|---|
| acceso a sub-servicio cliente | `ClienteService getClienteService()` | vigente |
| acceso a sub-servicio moto | `MotoService getMotoService()` | vigente |
| acceso a sub-servicio contrato | `ContratoService getContratoService()` | vigente |

> Nota de freeze: `AgenciaService` funciona como fachada de composición (gateway a sub-servicios), no como fachada de casos de uso unificados.

---

## 3) Contrato final para UI (congelado)

Este contrato queda como referencia estable para consumo UI. Cualquier cambio posterior requiere PR excepcional.

### 3.1 Operaciones CRUD + ciclo de contrato

| Dominio | Verbo | Firma objetivo | Retorno |
|---|---|---|---|
| Cliente | crear | `void crearCliente(Cliente cliente)` | `void` |
| Cliente | listar | `List<Cliente> listarTodos()` | `List<Cliente>` |
| Cliente | buscar | `Optional<Cliente> buscarPorCi(String ci)` | `Optional<Cliente>` |
| Cliente | actualizar | `void actualizarCliente(Cliente cliente)` | `void` |
| Cliente | eliminar | `void eliminarCliente(String ci)` | `void` |
| Moto | crear | `void crearMoto(Moto moto)` | `void` |
| Moto | listar | `List<Moto> listarTodos()` | `List<Moto>` |
| Moto | buscar | `Optional<Moto> buscarPorMatricula(String matricula)` | `Optional<Moto>` |
| Moto | actualizar | `void actualizarMoto(Moto moto)` | `void` |
| Moto | eliminar | `void eliminarMoto(String matricula)` | `void` |
| Contrato | crear | `void crearContrato(Contrato contrato)` | `void` |
| Contrato | listar | `List<Contrato> listarTodos()` | `List<Contrato>` |
| Contrato | buscar | `Optional<Contrato> buscarPorId(ContratoID id)` | `Optional<Contrato>` |
| Contrato | actualizar | `void actualizarContrato(Contrato contrato)` | `void` |
| Contrato | eliminar | `void eliminarContrato(ContratoID id)` | `void` |
| Contrato | finalizar | `void finalizarContrato(Contrato contrato)` | `void` |

### 3.2 Operaciones de soporte/reporte (vigentes)

| Dominio | Firma | Retorno |
|---|---|---|
| Cliente | `List<ClienteDTO> listarClientesPorMunicipio()` | `List<ClienteDTO>` |
| Cliente | `List<Cliente> obtenerClientesIncumplidores()` | `List<Cliente>` |
| Moto | `boolean estaDisponible(String matricula)` | `boolean` |
| Moto | `void cambiarEstado(String matricula, Situacion nuevaSituacion)` | `void` |
| Moto | `List<MotoDTO> listarMotosConKilometraje()` | `List<MotoDTO>` |
| Moto | `List<SituacionMotoDTO> listarSituacionMotos()` | `List<SituacionMotoDTO>` |
| Contrato | `List<Contrato> listarContratosCompletos()` | `List<Contrato>` |

---

## 4) Contrato de excepciones de negocio para UI

### 4.1 Tipos

- `BusinessException` (`RuntimeException`): excepción base de negocio con `errorCode` estable.
- `ValidationException`: subtipo para validaciones/reglas de negocio.

### 4.2 Catálogo vigente (implementado)

| Código | Mensaje contractual esperado | Operación |
|---|---|---|
| `CLIENTE_NO_ENCONTRADO` | `No se puede crear el contrato: cliente no encontrado` | `ContratoService.crearContrato(...)` |
| `MOTO_NO_ENCONTRADA` | `No se puede crear/finalizar el contrato: moto no encontrada` | `ContratoService.crearContrato(...)` y `finalizarContrato(...)` |
| `MOTO_NO_DISPONIBLE` | `No se puede crear el contrato: moto no disponible` | `ContratoService.crearContrato(...)` |
| `CONTRATO_NO_ENCONTRADO` | `No se puede actualizar/eliminar/finalizar el contrato: no existe` | `actualizarContrato(...)`, `eliminarContrato(...)`, `finalizarContrato(...)` |
| `CONTRATO_YA_FINALIZADO` | `No se puede finalizar el contrato: ya está finalizado` | `finalizarContrato(...)` |
| `CONTRATO_VALIDACION_FALLIDA` | `No se puede crear el contrato: validaciones fallidas` | fallback defensivo en `crearContrato(...)` |

### 4.3 Catálogo objetivo (pendiente para próximos tickets)

| Código propuesto | Mensaje contractual esperado | Caso de uso |
|---|---|---|
| `CLIENTE_DUPLICADO` | `No se puede crear el cliente: CI ya existe` | crear cliente duplicado |
| `CLIENTE_CONTRATOS_ACTIVOS` | `No se puede eliminar el cliente: tiene contratos activos` | eliminar cliente con bloqueo de negocio |
| `MOTO_EN_TALLER` | `No se puede crear el contrato: moto en taller` | crear contrato |
| `CONTRATO_FECHAS_INVALIDAS` | `No se puede crear/actualizar el contrato: fechas inválidas` | crear/actualizar contrato |

> Estos códigos se documentan para mantener estabilidad de UI y dejar claro el backlog de validaciones de negocio futuras.

---

## 5) Reglas de consumo para controllers UI

1. Los controllers deben consumir `services` (directo o vía `AgenciaService`), nunca DAOs.
2. La UI debe ramificar por `errorCode` (no por texto libre).
3. `Optional.empty()` representa “no encontrado” en operaciones de búsqueda.
4. Los comandos `void` se consideran exitosos si no lanzan excepción.

---

## 6) Estado de fase del Ticket L1

- ✅ Fase 1: inventario y diagnóstico.
- ✅ Fase 2: contrato final diseñado y documentado (este documento).
- ✅ Fase 3: alineación base de firmas y excepciones críticas en `ContratoService`.
- ✅ Fase 4: freeze y handoff final a UI.

---

## 7) Política de cambios post-freeze (L1)

1. La UI debe consumir únicamente los métodos listados en este documento.
2. No se permiten cambios de firma pública de `services` sin PR excepcional.
3. Un PR excepcional de contrato debe incluir obligatoriamente:
   - motivo técnico de negocio,
   - impacto en controllers/UI,
   - actualización de este documento,
   - plan de migración para consumidores.
4. Si el cambio altera `errorCode` o mensajes contractuales, se debe coordinar con equipo UI antes de merge.
