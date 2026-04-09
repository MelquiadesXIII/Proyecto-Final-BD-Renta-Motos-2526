# Copilot instructions (proyecto-bd-renta-motos)

## Build / run / test
- Java version: **21** (see `maven-compiler-plugin` `release` in `pom.xml`).
- Build: `mvn package`
- Run app (exec plugin): `mvn compile exec:java`
  - Runs Flyway migrations (PostgreSQL) and then launches JavaFX (`org.proyectobdmotos.App`).
- Run app (JavaFX plugin): `mvn javafx:run`
- Tests (JUnit 3): `mvn test`
- Single test class: `mvn -Dtest=AppTest test`

## Local DB setup (PostgreSQL + Flyway)
- Create DB (default name used in docs/examples):
  - `psql -h localhost -U postgres -c "CREATE DATABASE renta_motos;"`
- Create local config (required at runtime; not committed):
  - `cp src/main/resources/config.properties.example src/main/resources/config.properties`
  - Edit `db.url`, `db.user`, `db.password` in `src/main/resources/config.properties`
  - `org.proyectobdmotos.database.DatabaseConnection` loads `config.properties` from the classpath during static init.
- Migrations live in `src/main/resources/db/migration/` and follow `V{n}__descripcion.sql`.
  - Team rule (see `docs/guia-base-de-datos.md`): **never modify existing migrations** (e.g. `V1`, `V2`); add a new `V3__...sql` instead.

## High-level architecture
- **Entrypoint:** `org.proyectobdmotos.App`
  - Calls `DatabaseConnection.runMigrations()` (Flyway) then launches JavaFX (`org.proyectobdmotos.ui.FxApp`).
- **JavaFX entry:** `org.proyectobdmotos.ui.FxApp`
  - Builds the dependency graph via `AppCompositionRoot` and currently shows a placeholder scene.
- **Composition root / DI:** `org.proyectobdmotos.ui.AppCompositionRoot`
  - Owns the (singleton-ish) JDBC `Connection`, constructs DAOs → Services → Stores → `ScreenLoader`.
  - When adding new DAOs/Services/Stores, wire them here (keep object creation centralized).
- **FXML navigation + controller injection:** `org.proyectobdmotos.ui.navigation.ScreenLoader`
  - `load("/fxml/xyz.fxml")` uses `FXMLLoader.setControllerFactory(...)` so controllers are created via constructor injection.
  - If you add a new controller, **register it** in the controller factory mapping (otherwise it fails with `IllegalStateException`).
- **State stores (UI-friendly):**
  - `AgenciaStore` and `ReferenceDataStore` keep JavaFX `ObservableList<T>`.
  - Update lists via `setAll(...)` / store setters (do not replace list instances).

## Key conventions (project-specific)
- **Agent/code constraints (see `AGENTS.md`):**
  - One `return` statement per method (no early returns).
  - No `break`/`continue` in loops (except `break` inside `switch`). Use boolean flags to control loops.
- **DB enums ↔ Java enums:**
  - PostgreSQL ENUM values are lowercase strings (`disponible`, `alquilada`, `taller`, etc.).
  - Java enums (`Sexo`, `FormaPago`, `Situacion`) store a `valor` string and provide `fromValor(String)` for DB→Java mapping.
- **Contrato primary key is composite:**
  - DB PK is `(fecha_inicio, matricula_moto)`; Java models this via `org.proyectobdmotos.models.ContratoID`.
- **Business rules enforced in SQL (Flyway `V1__crear_tablas.sql`):**
  - Trigger prevents renting a moto whose `situacion` is not `disponible`.
  - Trigger sets moto `situacion` to `alquilada` after inserting a `contrato`.
- **DAO layer is JDBC-first (no ORM):**
  - DAOs extend `AbstractGenericDAO<T, ID>` and implement `I*DAO` interfaces.
  - CRUD method contracts exist in `docs/plan-interfaces.md`; `GenericDAO`/`AbstractGenericDAO` generic ops are scaffolded/commented out—expect explicit SQL per DAO.
