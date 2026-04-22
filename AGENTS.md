# Agent Development Guidelines

This document outlines the coding standards and practices that all agents must follow when working on this project.

---

## 1. Single Return Statement Per Function

All functions must have **exactly one unique return statement**. Early returns are strictly prohibited.

### Requirements

- Each function must return at most once
- Flow control must be managed using boolean flags and result accumulator variables
- Complex logic should be structured within conditional blocks, not dispersed across multiple returns

### Incorrect Pattern ❌

```java
public String procesar(int x) {
    if (x < 0) return "error";
    if (x == 0) return "cero";
    return "positivo";
}
```

### Correct Pattern ✅

```java
public String procesar(int x) {
    boolean error = false;
    String resultado = "";

    if (x < 0) {
        error = true;
    }

    if (!error) {
        if (x == 0) {
            resultado = "cero";
        } else {
            resultado = "positivo";
        }
    }

    return error ? "error" : resultado;
}
```

### Adaptation

This pattern should be adapted according to the specific circumstances and requirements of each function. Use flags and accumulators strategically to manage control flow while maintaining code clarity.

---

## 2. Loop Control with Boolean Flags

All loops must be controlled exclusively using boolean flags. The following control flow statements are **strictly forbidden**:

- `break` (except within switch statements)
- `continue`
- `finally`
- Any other statement that abruptly modifies code flow

### Requirements

- Loop termination must be managed by boolean flag conditions
- Loop iteration logic must be contained within conditional structures
- Switch statements are the only exception where `break` may be used

### Incorrect Pattern ❌

```java
for (int i = 0; i < list.size(); i++) {
    if (someCondition) break;
    if (anotherCondition) continue;
    // processing logic
}
```

### Correct Pattern ✅

```java
boolean shouldContinueLoop = true;
int i = 0;

while (shouldContinueLoop && i < list.size()) {
    if (someCondition) {
        shouldContinueLoop = false;
    } else if (anotherCondition) {
        // skip current iteration using else-if structure
    } else {
        // processing logic
    }
    i++;
}
```

---

## 3. Logging Requirements

All logging must use the centralized `Logger` utility class located at `org.proyectobdmotos.utils.Logger`. This is **strictly mandatory**.

### Forbidden Logging Methods ❌

- ~~`System.out.println(...)`~~
- ~~`System.err.println(...)`~~
- ~~`System.out.print(...)`~~
- ~~`System.err.print(...)`~~
- ~~`java.util.logging.Logger`~~
- ~~`org.slf4j.Logger`~~
- Any other default Java logging framework

### Mandatory Logger Usage ✅

Use the static methods from `org.proyectobdmotos.utils.Logger`:

```java
// Import the Logger
import org.proyectobdmotos.utils.Logger;

// Use Logger in your code
Logger.log("Informational message");           // Green text
Logger.logInfo("Initialization complete");     // Green text
Logger.logError("An error occurred");           // Red text
Logger.logWarn("This is a warning");            // Orange text
```

### Important Notes

- **No instantiation required**: `Logger` is an abstract class with static methods only
- **Automatic class detection**: The caller class is automatically detected from the StackTrace
- **Consistent formatting**: All logs include the calling class name in orange brackets: `[ClassName] message`
- **Color coding**: Messages are color-coded by severity level (green for info, red for errors, orange for warnings)

### Example

```java
public class ClienteService {
    public void crearCliente(Cliente cliente) {
        Logger.log("Creating new cliente: " + cliente.getCi());
        
        try {
            // service logic
            Logger.logInfo("Cliente created successfully");
        } catch (Exception e) {
            Logger.logError("Failed to create cliente: " + e.getMessage());
        }
    }
}
```

Output:
```
[ClienteService] Creating new cliente: 12345678901
[ClienteService] Cliente created successfully
[ClienteService] Failed to create cliente: Database error
```

---

## 4. Planning Requirements

When an agent is instructed to create or write a plan, the following rule is **mandatory**:

- **All plan documents must include the file paths** of every file that will be modified, created, or deleted
- File paths should be clearly listed with their full relative paths from the project root
- Each planned modification should reference its corresponding file path

### Example Plan Format

```
## Implementation Plan

### Modified Files
- `src/main/java/org/proyectobdmotos/services/ClienteService.java`
- `src/main/java/org/proyectobdmotos/dao/ClienteDAO.java`

### New Files
- `src/main/java/org/proyectobdmotos/dto/ClienteResponseDTO.java`

### Deleted Files
- `src/main/java/org/proyectobdmotos/utils/utiles.txt`

## Tasks

1. Task description referencing `src/main/java/org/proyectobdmotos/services/ClienteService.java`
2. Task description referencing `src/main/java/org/proyectobdmotos/dao/ClienteDAO.java`
...
```

---

## Summary

- **One Return**: Implement single-return functions using flags and accumulators
- **Loop Control**: Use boolean flags exclusively; `break` only in switch statements
- **Logging**: Always use `Logger` class from `org.proyectobdmotos.utils`, never System.out/err or Java's default loggers
- **Planning**: Always include file paths in plan documents

These guidelines ensure consistent, predictable, and maintainable code throughout the project.

---

## Role

Act as a **Senior JavaFX & SQL Developer** focused on maintainable layered design for this project. Work with MVC boundaries in UI, and implement persistence through DAO/Repository-style abstractions already present in `dao` interfaces and classes.

## Tech Stack & Conventions

- The project uses Java 21, JavaFX, PostgreSQL (JDBC), and Flyway migrations; keep changes aligned with this stack and package structure.
- Use **camelCase** for **all Java variable names** (local variables, fields, and parameters), with no snake_case and no Hungarian notation.
- Keep SQL naming consistent with existing migrations (snake_case identifiers, singular table names, explicit PK/FK constraints, and enum types).

## Forbidden Practices

- Do not put business rules or SQL access in JavaFX controllers; controllers only coordinate UI events and delegate to services/stores.
- Do not add `System.out.println`/`System.err.println` traces in application flow classes; use centralized logging strategy when observability is needed.
- Do not leave large commented-out code blocks or scaffold-only dead code in DAOs/services/controllers.
- Do not use `System.exit(...)` for regular control flow; prefer exception propagation and graceful shutdown paths.

## Quality Rules

- Use `PreparedStatement` with bound parameters for every DAO query/update to prevent SQL injection and keep statements explicit.
- Prefer constructor injection with `final` dependencies for services/controllers/infra components to keep objects immutable and testable.
- Keep Java model field types aligned with SQL schema definitions and apply all schema/data changes through Flyway migration files.

---

## 5. Project Runtime & Architecture Rules

These rules capture project-specific operational and structural constraints that agents must follow.

### Build / Run / Test Commands

- Build with `mvn package`.
- Run app with `mvn compile exec:java` or `mvn javafx:run`.
- Run tests with `mvn test`.
- Run a single test class with `mvn -Dtest=<ClassName> test`.

### Local Database Setup

- Use PostgreSQL and create the local database `renta_motos` when needed.
- Copy `src/main/resources/config.properties.example` to `src/main/resources/config.properties` for local runtime configuration.
- Ensure `db.url`, `db.user`, and `db.password` are configured in `src/main/resources/config.properties`.
- `org.proyectobdmotos.database.DatabaseConnection` loads `config.properties` from the classpath during static initialization.

### Flyway Migration Policy

- Migrations live in `src/main/resources/db/migration/` and must follow `V{n}__descripcion.sql` naming.
- **Never modify existing migrations** already committed (for example `V1`, `V2`); create a new migration (for example `V3__...sql`).

### Application Composition and Wiring

- `org.proyectobdmotos.App` is the entrypoint and must run migrations before launching JavaFX.
- `org.proyectobdmotos.ui.FxApp` is the JavaFX entry and should initialize the dependency graph through `AppCompositionRoot`.
- `org.proyectobdmotos.ui.AppCompositionRoot` must centralize object creation and wiring for `Connection`, DAOs, services, stores, and navigation helpers.
- Do not distribute object wiring across controllers or service classes.

### FXML Navigation and Controller Injection

- `org.proyectobdmotos.ui.navigation.ScreenLoader` must be used with `FXMLLoader.setControllerFactory(...)` so controllers are created via constructor injection.
- When adding a new controller, register it in the `ScreenLoader` controller factory mapping.
- **FXML Views**: Store all `.fxml` files in `src/main/resources/fxml/`.
- **Naming Convention**: FXML files should use `kebab-case` (e.g., `cliente-lista.fxml`) and their corresponding controllers should use `PascalCase` with the `Controller` suffix (e.g., `ClienteListaController.java`).
- **UI Error Handling**: Controllers must catch `BusinessException` (or its subclasses like `ValidationException`) and display them to the user using JavaFX `Alert` dialogs. Do not let business exceptions bubble up to the FxApp thread unhandled.
- **View-Controller Link**: All navigable screens must declare their controller association consistently with the `ScreenLoader`/`FXMLLoader` setup. Reusable presentational components included via `<fx:include/>` do not need a controller unless they contain behavior that must be handled from Java. Use `fx:id` only for nodes that are injected into or referenced from Java/controller code; do not require it for every interactive element by default.
- **Separation of Concerns**: Controllers are forbidden from containing business logic, SQL, or direct DAO access. They must only:
    1. Handle UI events (clicks, keypresses).
    2. Collect and validate basic UI input.
    3. Delegate to a `Service` or `Store`.
    4. Update the UI based on the result.

### Store and State Conventions

- Keep UI state in store classes such as `AgenciaStore` and `ReferenceDataStore`.
- Update JavaFX `ObservableList` instances through `setAll(...)` or store setters; do not replace list instances.

### Domain and Persistence Conventions

- PostgreSQL enum values are lowercase strings (for example `disponible`, `alquilada`, `taller`).
- Java enums (for example `Sexo`, `FormaPago`, `Situacion`) must keep the DB value mapping (for example via `valor` and `fromValor(String)`).
- `Contrato` uses a composite primary key `(fecha_inicio, matricula_moto)` represented by `org.proyectobdmotos.models.ContratoID`.
- Keep DAO implementation JDBC-first (no ORM), with explicit SQL in concrete DAO classes and contracts aligned with existing DAO interfaces/docs.

### SQL-Enforced Business Rules Awareness

- Respect database triggers that enforce rental constraints.
- Existing trigger rules include:
  - Preventing rental when moto `situacion` is not `disponible`.
  - Setting moto `situacion` to `alquilada` after inserting a `contrato`.
