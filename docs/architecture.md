# GDSEMR Architecture

This document describes the actual current structure of the codebase and the
conventions new code should follow. It reflects the state after the Phase
0-4 cleanup pass recorded in the [README changelog](../README.md#changelog);
see that log for the history and reasoning behind each decision below.

## Modules

A single Gradle module: **app**, the entire product. A JavaFX desktop EMR
with ~15 clinical feature areas (thyroid, medication, KCD coding, allergy,
vaccine, clinical labs, etc.), each opened as an independent window from
the main shell (`IttiaApp`).

There is no `core` module — it was deleted in Phase 0 (it was unused
`gradle init` scaffolding, never imported by `app`).

There is no `server` module — a Spring Boot REST API skeleton existed
through Phase 4 but was never called by `app` over the network (no HTTP
client anywhere in `app`, dead weight). Removed in Phase 5 rather than
carrying an unused module indefinitely. If remote/multi-client access is
ever needed, it should be designed against `app`'s actual persistence
layer (see below) rather than resurrected from the old skeleton, which
only had an in-memory, non-persistent `Patient`/`Template` API.

## Feature package structure

Feature code lives under `app/src/main/java/com/emr/gds/features/<name>/`.
Two styles coexist:

1. **Flat** (most features): UI, domain logic, and persistence mixed
   together in a handful of large classes. This is the legacy style and
   still the majority of the codebase.
2. **Layered** (`features/history`, `features/thyroid`, `features/medication`,
   `features/clinicalLab`, `features/allergy`, `features/kcd`): split into sub-packages by responsibility,
   following a light hexagonal (ports-and-adapters) convention:
   - `domain/` — plain Java, no JavaFX and no JDBC. Entities, enums,
     calculators, repository *interfaces*. **Pragmatic exception**:
     `features/allergy`'s `AllergyCause`/`SymptomItem` use JavaFX
     `StringProperty`/`BooleanProperty` wrappers for `TableView` binding
     convenience — technically not "plain Java", but they're the feature's
     real entities (not UI layout/control code), so they were moved to
     `domain/` as-is rather than invented a parallel plain-POJO type just to
     satisfy the letter of this rule. Splitting a bindable view-model out
     from a plain domain type is legitimate future work, not something to
     force during a package-move pilot.
   - `application/` — orchestration and business logic that depends only
     on `domain/`. Pure functions/services, independently testable without
     a running UI. Not every feature needs one — `features/medication`
     doesn't have a meaty pure-function candidate to extract the way
     `features/thyroid` did, so it has no `application/` package, and
     that's fine.
   - `adapter/in/ui/` — JavaFX `Stage`/`Pane`/`Controller` (including FXML
     controllers) classes. Depends
     on `domain/` and `application/`, never the reverse.
   - `adapter/out/persistence/` — persistence code, only present for
     features with real data to store. `features/history` implements this
     properly behind a `domain/HistoryRepository` interface;
     `features/clinicalLab` follows the same interface-backed style
     (`domain/ClinicalLabRepository` / `adapter/out/persistence/JdbcClinicalLabRepository`),
     since its existing `ClinicalLabDatabase` was already plain stateless
     CRUD with no extra in-memory state, so extracting an interface was a
     clean fit. `features/medication` just moved its existing concrete
     `MedicationDatabaseManager` here without extracting an interface,
     since it already mixes persistence with in-memory caching/pending-changes
     state that doesn't map cleanly onto a generic repository contract —
     forcing one in wasn't worth it for a package move. Prefer the
     interface-backed style (`history`'s, `clinicalLab`'s) for new persisted
     features; don't feel obligated to retrofit one onto `medication` unless
     you're touching its persistence logic anyway.

**When adding a new feature or substantially touching an existing flat one,
prefer the layered structure.** `features/history` is the reference example
for a persisted feature with a clean repository interface;
`features/thyroid` is the reference example for a calculator/UI feature
with no persistence (see its `application/ThyroidSummaryService`, extracted
from a 1149-line UI class that had report-generation logic embedded in
it); `features/medication` is the reference example for a persisted
feature with FXML-based controllers whose persistence class wasn't worth
splitting behind an interface; `features/clinicalLab` is the reference
example for a persisted, FXML-based feature whose persistence class *was*
a clean fit for a repository interface (note: moving FXML controllers
means also updating the `fx:controller` attribute in the corresponding
`.fxml` resource file — this is a runtime-only failure if missed,
`./gradlew compileJava` won't catch it); `features/allergy` is the
reference example for a persisted-nowhere feature (pure static reference
data, no database at all) with a JavaFX-property-based domain model (see
the `domain/` pragmatic-exception note above) — its
`AllergyControllerUiTest` is also the reference example for testing a
code-built (non-FXML) scene with TestFX; `features/kcd` is the reference
example for a persisted feature whose persistence class *was* a clean fit
for a repository interface (like `clinicalLab`) despite being entirely
`static` methods before the move (an interface with instance methods works
fine as the seam even when the implementation happens to hold no instance
state) — also notable for moving a connection-string constant that had
oddly lived on the UI class back onto the persistence class where it
belongs, and for a bundled-classpath-resource database (see the
Persistence section's `kcd` exception) rather than an `app/db/*.db` file.

Migrating the remaining flat features is intentionally incremental — it was
scoped as one feature at a time (thyroid, then medication, then clinicalLab,
then allergy, then kcd) rather than an all-at-once rewrite, since it
touches working clinical UI with (still, mostly) no automated UI test
suite — see "Verifying UI changes" below for current TestFX coverage. Do
it feature-by-feature, verifying the actual running UI after each move.

**Flagged for whoever picks the next feature**: `features/ReferenceFile`
(1053 lines, currently the largest flat feature) looks like an obvious next
candidate by size, but its persistence is unusually entangled — its
`ReferenceItem` domain model is actually owned by
`com.emr.gds.repository.ReferenceRepository` /
`com.emr.gds.service.ReferenceService`, which live *outside* the feature
package entirely, alongside the app-wide abbreviations/problems/plan-history
services (an older, separate "central repository/service" convention that
predates the per-feature hexagonal layering described here). Restructuring
`ReferenceFile` cleanly means deciding whether
to pull those central classes into the feature (touching shared code used
elsewhere) or leave them and only relocate the feature's own UI classes
(`ReferenceController`, `ReferenceItemEditController`) into
`adapter/in/ui/` — a much thinner move than the other pilots. Decide
deliberately rather than defaulting into it because it's next by size. Also
noticed in passing: `features/ReferenceFile/ai_studio_code.csv` is a stray
clinical-lab reference-range CSV that doesn't belong in this feature at
all (unrelated content, not read by any code) — worth relocating or
deleting separately, not part of this restructuring work.

Also noticed (Phase 10) and left alone:
`features/kcd/adapter/out/persistence/CsvToSqliteImporter.java` is a
standalone one-off dev tool (has its own `main()`, never invoked by the
live app) with hardcoded absolute paths from a different machine/user
(`/home/migowj/git/GDSEMR_ver_0.2/...`) — dead on this machine as-is. Moved
into the layered structure alongside the feature's real persistence code
since it's conceptually a persistence utility, but its broken paths were
not fixed (out of scope for a package-move pilot; fixing it would mean
guessing what the "correct" paths should be for a tool nobody's run in a
while).

## Persistence

- SQLite, one `.db` file per concern, living in `app/db/*.db`.
- **Path resolution**: always use `com.emr.gds.core.db.DbPaths` to resolve a
  database file's path. It walks up from the working directory to the
  project root (`gradlew` or `.git`) and resolves `app/db/<file>`, so
  resolution doesn't depend on where the app happens to be launched from.
  Do not reimplement this — four independent, subtly-drifted copies of this
  logic existed before Phase 2 consolidated them.
- **Connections**: SQLite connections are opened per call and closed
  immediately (try-with-resources) everywhere except `AppDatabaseManager`,
  which holds a few shared long-lived connections (abbreviations, history,
  references, auth) for the "central" cross-cutting data. Follow whichever
  pattern the feature you're touching already uses; don't introduce a third
  connection-lifecycle style.
- **Exception**: `features/kcd`'s database
  (`src/main/resources/database/kcd_database.db`) is a bundled classpath
  resource, not an `app/db/` file. It intentionally does not use
  `DbPaths` — don't "fix" this without a deliberate migration decision, it
  would silently point KCD at a different (likely nonexistent) file. The
  connection URL constant lives on
  `features/kcd/adapter/out/persistence/JdbcKcdRepository` (moved there in
  Phase 10 from the UI class, `KCDDatabaseManagerJavaFX`, which is where it
  used to oddly live — persistence code should own its own connection
  string, not read it off a UI class).

## Authentication

`com.emr.gds.core.auth`:
- `PasswordHasher` — PBKDF2WithHmacSHA256 (120,000 iterations, JDK-only, no
  new dependency).
- `CredentialRepository` / `SqliteCredentialRepository` — a single local
  password stored (hashed) in `app/db/auth.db`, via `AppDatabaseManager`.

This is deliberately a single-password model (not per-user accounts) to fit
a single-clinician desktop install. `IttiaApp`'s login scene detects
whether a credential exists yet: no credential → setup mode (create +
confirm password, min 6 characters); credential exists → normal sign-in
mode (checked against the stored hash). If multi-user support is ever
needed, extend `CredentialRepository` to key by username rather than a
singleton row — don't bolt a second parallel auth path on top.

## Build

- Java 25 toolchain (`gradle.properties` / `gradle/libs.versions.toml`,
  kept in sync — see Phase 1 changelog entry for why they can drift).
- No `.old` build files should exist alongside the Kotlin DSL files; if you
  see one, it's stale — delete it, don't merge from it.
- If server-side/remote access is ever added back, note for posterity: a
  Spring Boot module existed through Phase 4 and required Spring Boot ≥ 3.5
  to compile real Java 25 bytecode (earlier versions' bundled ASM can't
  parse Java 25 class files during `@SpringBootTest` classpath scanning).

## Verifying UI changes

As of Phase 8, `app` has a small automated UI test suite using TestFX
(`org.testfx:testfx-core` / `testfx-junit5`, see `app/build.gradle.kts`).
`./gradlew test` passing now does mean *something* about the UI works for
covered features — but coverage is still thin (as of Phase 10:
`clinicalLab`, `allergy`, `kcd`; see `ClinicalLabControllerUiTest` for an
FXML-loaded example, `AllergyControllerUiTest`/`KCDDatabaseManagerJavaFXUiTest`
for code-built-scene examples), so for features without one, `./gradlew
test` passing still does not mean the UI works. Phases 3, 4, and 6 verified changes by temporarily pointing
`application.mainClass` (in `app/build.gradle.kts`) at a throwaway harness
`Application` that calls the real, unmodified entry point directly (e.g.
`new IttiaApp().start(new Stage())`, `ThyroidLauncher.openThyroidEmr()`, or
`new MedicationCategory().start(stage)`) and drives real controls via
`Button.fire()` / `TextField.setText()` against the real rendered scene
graph, then reverts the `mainClass` change and deletes the harness. Prefer
a real TestFX test over this manual harness for new/touched features going
forward — write it once, keep it, instead of a throwaway per phase — but
the harness technique is still fair game for a one-off spot-check.

**TestFX's OS-level synthetic input is unreliable in this dev environment.**
`FxRobot.clickOn(...)`/`.write(...)`/`.type(...)` go through the
xdg-desktop-portal RemoteDesktop interface for pointer/keyboard injection,
which this environment's session denies ("Session is not allowed to call
NotifyPointer methods"). This surfaced in Phase 8 as an intermittent
`NoSuchElementException` from TestFX's `WindowFinder` when the UI test ran
alongside other test classes — flaky, not a real bug. **Use
`FxRobot.interact(Runnable)` instead**: it runs directly on the FX
Application Thread (e.g. `robot.interact(() -> { field.setText("x");
field.getOnAction().handle(new ActionEvent()); })`), which exercises the
real control and its real event handler without going through OS-level
input injection at all. `interact` is the way to get a real TestFX test
working reliably in this environment despite the portal restriction.

Two gotchas hit while doing this:
- **FXML `fx:controller` mismatches are a runtime-only failure.** Moving an
  `@FXML`-annotated controller class to a new package compiles fine but
  breaks the app at `FXMLLoader.load()` unless the `.fxml` resource's
  `fx:controller` attribute is updated to match. Grep for the class's old
  fully-qualified name across `src/main/resources/**/*.fxml` before
  considering an FXML-backed move done.
- **`Control.getChildrenUnmodifiable()` is unreliable for verification
  traversal.** Walking the scene graph through a `Control` (e.g.
  `Accordion`, `TitledPane`, `ListView`) to find its rendered children can
  return empty if no CSS/layout pass has happened yet, even though the
  control has real data. This produced a false "0 items rendered" during
  the medication verification pass. Prefer reading the control's own model
  properties directly (`Accordion.getPanes()`, `TitledPane.getContent()`,
  `ListView.getItems()`) over blind scene-graph traversal when verifying
  data actually landed in a control.
