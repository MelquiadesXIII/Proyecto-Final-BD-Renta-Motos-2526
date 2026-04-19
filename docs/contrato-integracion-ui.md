# Contrato de integración UI ↔ Services (Fase 1)

Este documento define el contrato vigente entre UI y capa de servicios para la Fase 1 del ticket L1.

## API canónica pública

| Servicio | Operación canónica | Firma | Comportamiento |
|---|---|---|---|
| `ClienteService` | `buscarPorCi` | `Optional<Cliente> buscarPorCi(String ci)` | Busca un cliente por identificador (`ci`). |
| `ClienteService` | `listarTodos` | `List<Cliente> listarTodos()` | Lista todos los clientes. |
| `MotoService` | `buscarPorMatricula` | `Optional<Moto> buscarPorMatricula(String matricula)` | Busca una moto por identificador (`matricula`). |
| `MotoService` | `listarTodos` | `List<Moto> listarTodos()` | Lista todas las motos. |
| `ContratoService` | `buscarPorId` | `Optional<Contrato> buscarPorId(ContratoID id)` | Busca un contrato por PK compuesta. |
| `ContratoService` | `listarTodos` | `List<Contrato> listarTodos()` | Lista todos los contratos. |
| `ContratoService` | `crearContrato` | `void crearContrato(Contrato contrato)` | Valida reglas de negocio y crea contrato. En fallas lanza `ValidationException` con `BusinessErrorCode`. |

## Métodos removidos en Fase 1

| Servicio | Método removido | Reemplazo canónico |
|---|---|---|
| `ClienteService` | `buscarPorId(String id)` | `buscarPorCi(String ci)` |
| `MotoService` | `buscarPorId(String id)` | `buscarPorMatricula(String matricula)` |
| `MotoService` | `listarTodas()` | `listarTodos()` |

## Contrato de excepciones de negocio para UI

### Tipos

- `BusinessException` (`RuntimeException`): excepción base de negocio con `errorCode` estable y mensaje contractual.
- `ValidationException`: subtipo para fallas de validación/regla en servicios.

### Catálogo de `errorCode`

| Código | Mensaje contractual esperado | Cuándo ocurre |
|---|---|---|
| `CLIENTE_NO_ENCONTRADO` | `No se puede crear el contrato: cliente no encontrado` | `ContratoService.crearContrato(...)` cuando el cliente no existe. |
| `MOTO_NO_DISPONIBLE` | `No se puede crear el contrato: moto no disponible` | `ContratoService.crearContrato(...)` cuando la moto no está disponible. |
| `CONTRATO_VALIDACION_FALLIDA` | `No se puede crear el contrato: validaciones fallidas` | Fallback defensivo de validación si no aplica un código más específico. |

### Guía de consumo UI

1. La UI debe ramificar por `errorCode`, no por parsing de texto libre.
2. El `message` se considera contractual para trazabilidad y fallback de presentación.
3. Errores técnicos inesperados no deben ser tratados como contrato primario de negocio.

## Política de deprecación (Fase 1)

- Fase 1 elimina aliases/deprecados de búsqueda/listado redundantes en servicios.
- El contrato público para UI queda limitado a API canónica activa.
- Consumidores UI deben usar exclusivamente `buscarPorCi`, `buscarPorMatricula` y `listarTodos` según el servicio.
