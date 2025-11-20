# PWSEditor: A Part-Whole Statecharts Editing Environment

**PWSEditor** is a graphical environment for designing and analysing
*Part-Whole Statecharts (PWS)*—a behavioural modelling formalism
for hierarchical assemblies of components interacting synchronously.
The editor supports the construction of PWS controllers, the specification
of state-level constraints, and the visualisation of computed semantics and
reactive spaces.

## Features

- **Graphical control-state editor:** create and arrange control states,
  initial pseudo-states, and transitions using an intuitive drag-and-drop
  interface.

- **Semantic annotation:** automatically computes and displays, for each
  control state, its declared *constraint* (\Sem), its *computed semantics*
  (\Comp), and its *reactive space*.  Misalignments (semantic violations) and
  uncovered reactive successors are highlighted.

- **Event- and guard-triggered transitions:** supports both event-triggered
  transitions with optional guard predicates and action emissions, and
  guard-only transitions for modelling autonomous evolution or fail-safe
  repair.

- **Editable guards and actions:** triggers, guards, and emissions can be
  modified via in-place annotation widgets.

- **Separation of model and view:** PWS statecharts and their graphical layout
  can be saved, restored, and selectively shown or hidden.

- **Planned integration with Part-Whole toolchain:** future releases will
  interface with model-checking, analysis, and code-generation back-ends for
  PWS-based controllers.

## Building and Running

The repository contains the Java Swing implementation of the editor.
It currently has no Maven/Gradle build script; compilation can be performed
manually:

```sh
javac -d out $(find src -name '*.java')
