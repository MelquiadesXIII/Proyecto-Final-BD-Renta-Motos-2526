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
