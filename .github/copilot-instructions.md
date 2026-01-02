<!-- Copilot instructions for AI coding agents in this repo -->
# PWSEditor — Copilot Instructions

Purpose: help an AI contributor become productive quickly in this Java Swing repository.

- Quick start (build & run):
  - Compile all sources (no build system present):

    ```sh
    javac -d out $(find src -name '*.java')
    ```

  - Run the main demo (package `editor`):

    ```sh
    java -cp out editor.Main
    ```

- Big picture (what to read first):
  - Model layer: `src/pws` and `src/machinery` contain core statechart model classes (e.g. `PWSStateMachine.java`, `PWSState.java`, `StateMachine.java`, `StateInterface.java`).
  - Semantics & analysis: `src/pws/semantics` implements semantics computation (`Semantics.java`, `SemanticsVisitor.java`).
  - Editor / UI: `src/editor` and `src/pws/editor` hold Swing UI components and annotation widgets (`PWSEditor.java`, `PWSStateMachinePanel.java`, `AssemblyPanel.java`).
  - Assembly generation: `src/assembly` and `src/pws/semantics/AssemblyGenerator.java` show how assemblies and truth tables are produced.
  - Persistence: `src/serializer/BinaryModelSerializer.java` is the model serializer to inspect for saving/loading behavior.

- Key patterns and conventions (project-specific):
  - Explicit model / view separation: model classes live under `pws` + `machinery`; UI under `editor` and `pws/editor`. Prefer changing model logic in `pws` or `machinery` and view logic in `pws/editor`.
  - Interface suffix: interfaces use `*Interface.java` (e.g. `StateInterface.java`, `TransitionInterface.java`) — follow existing naming for new contracts.
  - Annotations subsystem: interactive annotations and editable guards/actions are in `src/pws/editor/annotation` — editing flows update model objects directly.
  - Semantics visitors: semantic computations use visitor patterns (`SemanticsVisitor`) — extend visitors rather than scattering logic across model classes.

- Integration points & data flow to watch:
  - UI -> Model: UI panels create/update `PWSState` and `PWSTransition` objects; saving goes through `BinaryModelSerializer`.
  - Model -> Semantics: `PWSStateMachine` passes structures to `Semantics`/`SemanticsVisitor` for computed constraints/semantics.
  - Assembly generator: `assembly/AssemblyGenerator.java` and `pws/semantics/AssemblyGenerator.java` show transformations from statecharts to assembly representations.

- Developer workflows and gotchas:
  - No Maven/Gradle present: use the manual `javac`/`java` commands above. Tests/frameworks are not present.
  - Code may assume Swing UI thread for view updates — run UI code on the AWT event thread when adding UI changes.
  - Many classes are package-scoped and rely on simple file structure; avoid moving files across packages without updating imports.

- Where to look for examples when implementing changes:
  - `src/pws/editor/PWSEditor.java` — wiring of top-level editor components and menus.
  - `src/pws/semantics/Semantics.java` and `SemanticsVisitor.java` — the canonical place for semantic logic.
  - `src/pws/editor/annotation/*` — how guard/action editors are implemented and bound to model objects.

- Notes for the AI agent:
  - Make minimal, focused changes; follow existing naming and layering.
  - If adding new public APIs, mirror the `*Interface.java` convention and update usages.
  - When editing UI files, prefer preserving layout code and only change event handling; keep model changes in `pws`.

Please review and tell me if any sections need more detail or examples.
