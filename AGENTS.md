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

## 3. Planning Requirements

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
- Keep JavaFX logic separated from business/data layers: controllers -> services -> DAOs, with wiring centralized in `AppCompositionRoot`.
- Prefer constructor injection with `final` dependencies for services/controllers/infra components to keep objects immutable and testable.
- Keep Java model field types aligned with SQL schema definitions and apply all schema/data changes through Flyway migration files.
