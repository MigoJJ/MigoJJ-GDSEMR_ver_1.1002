# GDSEMR_ver_1.1001

JavaFX EMR prototype targeting Java 25 and JavaFX 25.

## Architecture

A single-module JavaFX desktop app. See [docs/architecture.md](docs/architecture.md)
for the feature package layout, persistence conventions, and auth model.

- **app**: the entire product — a JavaFX EMR with ~15 clinical feature areas
  (thyroid, medication, KCD coding, allergy, vaccine, clinical labs, etc.),
  each opened as its own window from the main shell (`IttiaApp`).

## Requirements
- JDK 25 (Gradle toolchains will download/use it automatically if available)
- JavaFX 25 SDK artifacts (fetched from Maven Central by the OpenJFX Gradle plugin)
- SQLite JDBC (declared as a dependency; no manual install needed)

## Build & Run
- Root task: `./gradlew run` (delegates to `:app:run`)
- Module tasks: `./gradlew :app:run`, `./gradlew :app:test`, etc.
- If multiple JDKs are installed, point Gradle at Java 25 with `export ORG_GRADLE_JAVA_HOME=/path/to/jdk-25`.
- `./run-gradle.sh` is available as a convenience wrapper; update its paths if you move the project.

## Notes
- Java toolchain and version properties are centralized in `gradle.properties`.
- JavaFX version is configurable via `gradle.properties` (`javafxVersion`).
- Kotlin DSL templates for Gradle 9.2 live in `templates/` (`build.gradle.kts.template`, `app.build.gradle.kts.template`, `build-logic.build.gradle.kts.template`) to help migrate without version drift.

## Changelog

### 2026-09-10 — Phase 0 cleanup (build hygiene)
Follow-up to an architecture review that flagged several low-risk/high-value cleanup items. All changes verified via `./gradlew :app:compileJava :app:compileTestJava :server:compileJava`.
- **Removed the `core` module.** `org.example.list.LinkedList`/`StringUtils`/`JoinUtils`/`SplitUtils` were unmodified `gradle init` scaffolding, never imported anywhere in `app/` or `server/`. Dropped from `settings.gradle.kts`, `app/build.gradle.kts`, and deleted the `core/` directory.
- **Resolved a `DatabaseManager` naming collision.** Two unrelated classes shared the same simple name in different packages, risking import mix-ups:
  - `com.emr.gds.features.kcd.db.DatabaseManager` → renamed to `KcdDatabaseManager`.
  - `com.emr.gds.features.medication.db.DatabaseManager` → renamed to `MedicationDatabaseManager` (test class renamed to `MedicationDatabaseManagerTest` to match).
  - Updated all call sites: `KCDDatabaseManagerJavaFX`, `IAMButtonAction`, `medication/controller/LauncherController`, `medication/controller/MainController`.
- **Deleted stale `.old` build files** (`build.gradle.old`, `app/build.gradle.old`, `server/build.gradle.old`, `settings.gradle.old`) — leftover Groovy-DSL predecessors from an earlier (`ver_0.3`) migration to Kotlin DSL, no longer referenced by Gradle.

Deferred to later phases (see architecture review): centralizing the ~8 separate SQLite connection-management classes under one shared manager, unifying feature package structure around the hexagonal pattern used in `features/history`, deciding the fate of the unused `server/` REST skeleton, and replacing the non-authenticating login screen.

### 2026-09-10 — Phase 1 (build/version alignment)
- **Fixed the `server` module's Java version mismatch.** It previously force-locked compiled bytecode to Java 21 (`options.release.set(21)`) while the root toolchain targets Java 25 — an undocumented inconsistency flagged by the architecture review. Investigated the root cause: Spring Boot 3.3.13's bundled ASM (via Spring Framework 6.1) cannot parse Java 25 class files during `@SpringBootTest` classpath scanning, throwing `BeanDefinitionStoreException: Incompatible class format`. Fix: bumped `springBoot` to **3.5.6** in `gradle/libs.versions.toml` (which does support Java 25 class files) and switched `server/build.gradle.kts` to `options.release.set(25)`. Verified with `./gradlew clean compileJava compileTestJava test` — all tests pass on real Java 25 bytecode now, no workaround needed.
- **Resolved dependency version drift** between `gradle.properties` (actual build inputs) and `gradle/libs.versions.toml` (partially-used catalog): bumped `sqliteVersion` 3.45.3.0 → **3.46.0.0** and `slf4jVersion` 2.0.16 → **2.0.17** to match the catalog's already-current values.
- **Removed the dead `springBootVersion` property** from `gradle.properties` — it was never actually read by any build file; the Spring Boot plugin version comes from `gradle/libs.versions.toml` (`springBoot`) instead, which was the sole source of truth all along.

Deferred to later phases: centralizing the ~8 separate SQLite connection-management classes under one shared manager, unifying feature package structure around the hexagonal pattern used in `features/history`, deciding the fate of the unused `server/` REST skeleton, and replacing the non-authenticating login screen.

### 2026-09-10 — Phase 2 (SQLite path-resolution consolidation)
Investigated the ~8 separate database-access classes first: all of them already open-and-close a JDBC `Connection` per call (no leaked long-lived connections outside `AppDatabaseManager`'s 3 shared ones), so the real duplication was in **path-resolution logic**, not connection lifecycle — 4 near-identical "walk up to project root, resolve `app/db/<file>`" implementations had drifted slightly (different root markers, different fallback behavior).
- **Added `com.emr.gds.core.db.DbPaths`** as the single canonical path resolver for the `app/db/*.db` family: walks up to the project root (`gradlew` or `.git`), then checks `app/db/<file>` before falling back to a legacy `db/<file>` location.
- **Migrated onto it**: `AppDatabaseManager`, `SqliteProblemRepository` (`prolist.db`), `SqlitePlanHistoryRepository` (`plan_history.db`), `MedicationDatabaseManager` (`med_data.db`), `ClinicalLabDatabase` (`ClinicalLabItemsSqlite3.db`) — removing 4 duplicate/drifted path-resolution implementations in the process (including `ClinicalLabDatabase`'s fragile hardcoded-relative-path fallback list, which broke if the app was launched from an unexpected working directory).
- **Deleted `SqliteDatabasePaths`**, made fully redundant by `DbPaths`.
- **Deliberately left `features/kcd` alone.** Its database (`kcd_database.db`) actually lives at `src/main/resources/database/` — a bundled classpath resource, not an `app/db/` file — so routing it through `DbPaths` would have silently pointed it at a different (nonexistent) file. Confirmed via `find` before touching anything.
- Verified with `./gradlew clean compileJava compileTestJava test` — all 12 tests across `app` and `server` pass, including `MedicationDatabaseManagerTest`, which round-trips real SQLite persistence through the new resolver.

Also noticed but **not acted on** (flagging for a future decision, not a code change): stray duplicate `.db` files outside the `app/db/` convention — `app/med_data.db` (root of `app/`) and `app/bin/main/database/*` (build-output copies) — worth a deliberate cleanup pass rather than blind deletion, since they may be stale build artifacts or may matter.

Deferred to later phases: unifying feature package structure around the hexagonal pattern used in `features/history`, deciding the fate of the unused `server/` REST skeleton, and replacing the non-authenticating login screen.

### 2026-09-10 — Phase 3 (thyroid feature restructure, pilot)
Restructured `features/thyroid` (previously 5 flat files, ~2600 lines, including a 1149-line `ThyroidPane`) into the same hexagonal layering `features/history` uses:
- **`domain/`**: `ThyroidEntry`, `ThyroidRiskCalculator` — moved as-is, already pure (no JavaFX/JDBC).
- **`application/ThyroidSummaryService`** (new): extracted the specialist-summary text-generation logic that was previously private methods inline in `ThyroidPane` (`buildSpecialistSummary`/`addLabLine`/`getLabIndicator`, plus 10 lab reference-range constants). It took UI state (a `CheckBox` map, a `Label`'s text) as hidden dependencies before; now it's a pure function taking plain `Map`/`String` parameters, independently testable without a running UI.
- **`adapter/in/ui/`**: `ThyroidPane`, `ThyroidPregnancy`, `ThyroidLauncher` — moved as-is (pure UI).
- No persistence adapter — thyroid has no database, so there's nothing under `adapter/out/`.

Verified by driving the real running app (see `docs/architecture.md` § Verifying UI changes): opened the real windows via `ThyroidLauncher`, fired the real "Generate Specialist Summary" button against the live scene graph, confirmed the output text matched the old format including correct out-of-range lab indicators (▲/▽), and confirmed the sibling `Thyroid Pregnancy` window still opens. `docs/architecture.md` (previously empty) now documents this as the target convention for the remaining flat features, to be migrated incrementally rather than all at once.

### 2026-09-10 — Phase 4 (real authentication + architecture docs)
- **Replaced the non-authenticating login screen.** It previously only checked that username/password fields were non-empty. Added `com.emr.gds.core.auth`: `PasswordHasher` (PBKDF2WithHmacSHA256, 120k iterations, JDK-only) and `SqliteCredentialRepository` (single local password, hashed, stored in a new `app/db/auth.db` via `AppDatabaseManager`). Scope was deliberately a single shared password rather than per-user accounts, to fit this single-clinician desktop app rather than building unneeded multi-user infrastructure.
- `IttiaApp`'s login scene now detects first-run (no stored credential) vs. normal sign-in: first run shows a create-password flow (with confirmation + a 6-character minimum), subsequent runs verify the entered password against the stored hash and reject wrong ones with "Invalid password."
- Verified end-to-end by driving the real, unmodified `IttiaApp.start(Stage)` directly: confirmed setup-mode UI, mismatched-password rejection, too-short-password rejection, successful setup, then a fresh second run correctly detected the existing credential, rejected a wrong password, and accepted the correct one through to the main scene.
- **Wrote `docs/architecture.md`**, empty until now. Documents the module layout, the flat-vs-hexagonal feature convention (and which to use for new work), the `DbPaths` persistence convention, the new auth layer, and the harness-based UI verification approach used in Phases 3–4.

Deferred: migrating the remaining ~13 flat feature packages to the hexagonal convention, deciding the fate of the unused `server/` REST skeleton.

### 2026-09-10 — Phase 5 (remove dead `server` module; DB tracking cleanup)
- **Removed the `server` module entirely.** It was a working Spring Boot CRUD skeleton, but `app` never called it over the network (confirmed no HTTP client anywhere in `app`) — pure dead weight since it was written. Removed `server/` from `settings.gradle.kts` and the root `build.gradle.kts` (`runServer` task gone), deleted the now-unused `springBoot` version/plugin entries from `gradle/libs.versions.toml`, and deleted the `server/` directory. `docs/architecture.md` and the README's architecture section updated accordingly (the README's module diagram was also already stale, referencing `utilities`/`list` modules that mapped to the `core` module removed back in Phase 0 — fixed while touching this section).
- **Cleaned up accidentally-committed database files outside the `app/db/` convention**: `app/bin/main/database/*.db` (4 files, a stale Eclipse-style build-output directory that had been committed to git despite `.gitignore` covering `**/bin/`), `app/build/resources/main/database/*.db` (4 files, live Gradle build output that had also been committed despite `**/build/` being gitignored — untracked via `git rm --cached`, left on disk since it's regenerated on every build), and `app/med_data.db` (an orphaned duplicate at the root of `app/`, confirmed via checksum to differ from the actual active `app/db/med_data.db`). None of the real, actively-used `app/db/*.db` files were touched.
- Verified with a full `./gradlew clean compileJava compileTestJava test` after each change.