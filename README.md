# PWSEditor: A Plant‑Wide State‑Machine Editing Environment

**PWSEditor** is a graphical editing environment for designing and analysing *Plant‑Wide State Machines (PWS)*—a formalism for modelling the synchronous interaction of autonomous plant components with a supervisory controller.  This prototype allows you to define component‑level automata, compose them into global states, and annotate each control state with formal constraints, computed semantics, and reactive spaces.

## Features

- **Graphical control-state editor:** create and arrange control states, initial pseudo‑states, and transitions using an intuitive drag‑and‑drop interface.
- **Semantic annotation:** automatically computes and displays for each state its *constraint*, the set of *admissible plant configurations* satisfying that constraint, and the *reactive space* reachable under autonomous drift.  Violations and uncovered reactive successors are highlighted.
- **Event- and guard-triggered transitions:** supports both event‑triggered transitions with optional guard predicates and action emissions, and guard‑only transitions for autonomous evolution or fail‑safe repair.
- **Customisable action and guard annotations:** edit triggers, guards and emitted actions via in‑place annotation widgets.
- **Separation of model and view:** PWS state machines are serialisable; annotations can be hidden, shown, saved, and restored.
- **Integration with PWS toolchain:** planned integration with model-checking and code-generation back‑ends for Plant‑Wide Controllers.

## Building and Running

This repository contains only the source code for the editor.  It is a Java Swing application and currently has no Maven/Gradle build script.  You can compile it manually with a Java compiler:

```sh
javac -d out $(find src -name '*.java')
```

and run the appropriate entry point from your IDE or via `java`.  A complete build script and packaged releases will be provided in the near future.

## Reference and Citation

A description of PWSEditor will be submitted to the **18th NASA Formal Methods Symposium (NFM 2026)**, which is scheduled to take place at the University of Southern California in Los Angeles, California, USA, 5–7 May 2026.  The symposium brings together researchers and practitioners in formal methods to address the specification, verification and certification of mission‑ and safety‑critical systems.  A link to the conference is available at [https://nfm2026.github.io](https://nfm2026.github.io).  Please cite this tool once a formal publication becomes available.

## License

© 2025 Luca Pazzi (UNIMORE).  All rights reserved.

This project is released under the **MIT Licence**.  Redistribution and
use in source and binary forms, with or without modification, are
permitted provided that the above copyright notice and this permission
notice appear in all copies or substantial portions of the software.

See the accompanying `LICENSE` file for the complete text of the licence.
