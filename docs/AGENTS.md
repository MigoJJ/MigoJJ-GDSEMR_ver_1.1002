# Repository Guidelines

## Project Structure & Modules
- Single Gradle module: **`app`** — a JavaFX desktop EMR with ~15 clinical
  feature areas (thyroid, medication, KCD coding, allergy, vaccine, clinical
  labs, etc.), each opened as its own window from the main shell
  (`IttiaApp`). See [docs/architecture.md](architecture.md) for the feature
  package layout, persistence conventions, and auth model.
- There is no `server`, `list`, or `utilities` module — all three were
  removed as dead weight in earlier cleanup phases (see the
  [README changelog](../README.md#changelog), Phases 0 and 5).
- Build configuration lives in the root `build.gradle.kts`, `app/build.gradle.kts`,
  and shared versions in `gradle.properties` / `gradle/libs.versions.toml`.
  Wrapper scripts (`gradlew`, `run-gradle.sh`) keep toolchains consistent.

## Build, Test, and Development Commands
- `./gradlew run`: Launch the JavaFX client (delegates to `:app:run`).
- `./gradlew test`: Run all tests; scope to the module with `./gradlew :app:test`.
- `./run-gradle.sh <gradle-args>`: Convenience wrapper if you prefer local JDK settings pinned in the script.
- If multiple JDKs are installed, export `ORG_GRADLE_JAVA_HOME=/path/to/jdk-25` before running tasks.

## Coding Style & Naming Conventions
- Target Java 25 toolchain (set in `gradle.properties`).
- Use 4-space indentation; keep package names lower-case dot-separated (`com.emr.*`).
- Prefer descriptive class names matching file names; favor immutable data where practical.
- Use Gradle toolchains rather than local `JAVA_HOME` overrides; configure JavaFX version via `gradle.properties` (`javafxVersion`).
- When adding or substantially touching a feature, prefer the hexagonal
  (`domain`/`application`/`adapter`) package layering documented in
  `docs/architecture.md` over the legacy flat style.

## Testing Guidelines
- Unit and persistence tests use JUnit Jupiter; UI tests use TestFX
  (`org.testfx:testfx-core` / `testfx-junit5`) driving the real JavaFX scene
  graph — see `docs/architecture.md` § "Verifying UI changes" for the
  pattern and its gotchas (FXML `fx:controller` mismatches, scene-graph
  traversal timing).
- Name tests after behavior (`ClassNameTests`, methods like `doesReturnResultsForValidQuery`).
- Persistence tests must run against an isolated test database file (see
  `MedicationDatabaseManagerTest` / `JdbcClinicalLabRepositoryTest`), never
  the tracked seed files under `app/db/`.
- Run `./gradlew test` before commits; scope to a single test class while
  iterating (e.g. `./gradlew :app:test --tests '...ControllerUiTest'`).
- No enforced coverage threshold yet — aim to exercise new branches and
  integration points (database access, controllers, and UI-side service calls).

## Commit & Pull Request Guidelines
- Prefer imperative, scoped messages (`feature: brief change`, e.g., `medication: fix item refresh`).
- For PRs, include: summary of changes, testing performed (`./gradlew test` output), linked issues, and screenshots or logs for UI/API changes.
- Keep PRs small and focused (one feature or bugfix per PR). Document migrations or config changes in the description and update `README.md` if developer steps change.
