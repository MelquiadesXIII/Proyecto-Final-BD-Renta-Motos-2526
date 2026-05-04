# Plan de refactorización de resultados esperados en tests según `Validator`

## Objetivo

Refactorizar **únicamente** los tests existentes para que los datos usados en fixtures y resultados esperados (longitudes, formato y rangos) sean coherentes con la lógica de validación definida en `src/main/java/org/proyectobdmotos/utils/Validator.java`.

> Alcance explícito: **no** crear una suite nueva para testear `Validator`; solo alinear los tests actuales del proyecto.

## Archivos revisados

- `src/test/java/org/proyectobdmotos/services/ClienteServiceContractTest.java`
- `src/test/java/org/proyectobdmotos/services/MotoServiceContractTest.java`
- `src/test/java/org/proyectobdmotos/services/ContratoServiceContractTest.java`
- `src/test/java/org/proyectobdmotos/services/ServiceApiDocumentationContractTest.java`
- `src/test/java/org/proyectobdmotos/services/BusinessExceptionContractTest.java`
- `src/test/java/org/proyectobdmotos/AppTest.java`

## Reglas canónicas tomadas de `Validator`

- Texto (`validateText`): 3..25 caracteres, solo letras/espacios (con tildes y ñ).
- Edad (`validateAge`): 18..99.
- Teléfono (`validateTelephoneNumber`): exactamente 8 dígitos y empieza por `5` o `6`.
- Positivo (`validatePositive`): `> 0`.
- Matrícula (`validatePlate`): exactamente 6 alfanuméricos.
- CI (`validateCI`): comentario indica 11 dígitos (la implementación actual todavía no lo valida).

## Hallazgos concretos en tests actuales

1. **CI inválido en fixtures de cliente**
   - Se usa `"123"` y `"C1"` en objetos `Cliente` de tests de servicios.
   - No concuerda con la regla documentada para CI (11 dígitos).

2. **Matrículas inválidas en invocaciones de búsqueda**
   - Se invoca `buscarPorMatricula("NO-EXISTE")` y `buscarPorMatricula("XX")`.
   - No concuerda con `validatePlate` (6 alfanuméricos).

3. **Teléfonos en fixtures**
   - Se usa `"55512345"` (válido), pero no está estandarizado en todos los tests con un patrón de dato canónico reutilizable.

4. **Nombres y edad**
   - En general están dentro de reglas (`"Ana"`, `"Perez"`, `30`), pero conviene estandarizar dataset para no introducir futuros valores fuera de rango.

## Plan de refactorización (solo tests existentes)

## Archivos a modificar

- `src/test/java/org/proyectobdmotos/services/ClienteServiceContractTest.java`
- `src/test/java/org/proyectobdmotos/services/MotoServiceContractTest.java`
- `src/test/java/org/proyectobdmotos/services/ContratoServiceContractTest.java`

## Archivos a crear

- Ninguno.

## Archivos a eliminar

- Ninguno.

## Tareas por archivo

### 1) `ClienteServiceContractTest.java`

- Reemplazar CI de fixture `"123"` por un CI con 11 dígitos (ejemplo: `"99123112345"`).
- Mantener teléfono con formato válido de 8 dígitos iniciando en 5.
- Mantener nombres/apellidos en formato texto válido (3..25, sin símbolos).

### 2) `ContratoServiceContractTest.java`

- Reemplazar CI de fixture `"C1"` por CI de 11 dígitos.
- Validar que todos los `Cliente` de fixtures respeten edad 18..99 y teléfono válido.
- Verificar que matrículas utilizadas por helpers (`crearMoto`) y datos de prueba mantengan 6 alfanuméricos.

### 3) `MotoServiceContractTest.java`

- Cambiar `buscarPorMatricula("NO-EXISTE")` por una matrícula inválida semánticamente para el caso “no existe”, pero **válida en formato** (ejemplo: `"ZZZ999"`).
- Cambiar `buscarPorMatricula("XX")` por un valor válido de 6 alfanuméricos (ejemplo: `"AA0001"`) para no mezclar error de formato con error del DAO.
- Mantener fixtures de moto con matrículas válidas de 6 alfanuméricos (`ABC123`, `XYZ999`, etc.).

## Estándar de datos de prueba recomendado

Usar de forma consistente estos patrones en tests de servicios:

- `ciValido`: 11 dígitos (`"99123112345"`).
- `telefonoValido`: 8 dígitos iniciando en `5` o `6` (`"55512345"`, `"65512345"`).
- `matriculaValida`: 6 alfanuméricos (`"ABC123"`, `"ZZZ999"`).
- `nombreValido`: 3..25 letras/espacios (`"Ana"`, `"Maria Elena"`).
- `edadValida`: entero en rango (`30`).

## Criterios de aceptación

- Ningún test de servicios usa fixtures con valores que contradicen reglas de `Validator`.
- Los casos de negocio siguen probando el mismo comportamiento funcional original (DAO/service contracts), pero con datos válidos de entrada.
- No se agregan nuevos tests a `Validator` ni nuevos archivos de test para ese componente.

## Riesgos y mitigación

- Riesgo: cambiar datos y alterar intención original del test.
  - Mitigación: preservar el objetivo del caso (éxito/fallo por contrato de servicio) y solo ajustar formato/rangos.

- Riesgo: mezclar “dato inválido” con “dato no encontrado”.
  - Mitigación: en casos de “no existe”, usar valores sintácticamente válidos pero ausentes en fake DAO.
