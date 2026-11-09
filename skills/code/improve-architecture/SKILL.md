---
name: improve-architecture
description: Architecture check-in for a growing Kotlin Android app. Finds hot spots, proposes deeper modules, and refactors one step per commit with behavior proven unchanged. Use when a milestone ends, before a big feature, when a file passes 400 lines, when one feature touches many files, when bugs repeat in one area, or when the user asks to refactor or clean up the code.
---

# Improve architecture

Features pile up, and an app that was easy to change gets hard to change.
A **check-in** catches that early: survey, propose, let the user pick, then
refactor in small steps that keep behavior the same.

## When to run

Any one signal is enough:

- The end of a milestone (the `improve-loop` skill calls this skill there).
- Before a big feature. Ask: "What change to the code would make this
  feature easy?"
- A file over 400 lines (act at 700), or a composable over 250 lines.
- One feature touched more than 6 files.
- The same area shows up in 3 fix-log entries, or the same kind of bug
  happens twice.
- A screen recomposes on every change for state it does not show.
- Logic that a test needs lives inside a composable or a ViewModel.

## Vocabulary

Use these words exactly, so proposals stay comparable:

- **Module**: anything with an interface and an implementation (a
  function, a file, a package, a Gradle module).
- **Interface**: everything a caller must know: signatures, ordering,
  error modes, threading, performance.
- **Depth**: how much a module does per unit of interface. Deep modules
  hide a lot behind a small interface. Shallow modules are pass-throughs.
- **Seam**: a place where you can swap behavior without editing callers.
- **Adapter**: one implementation behind a seam. One adapter is a
  hypothetical seam. Two (production and test) make it real. `Scanner` with
  `DataWedgeScanner` and `FakeScanner` is a real seam.
- **Locality**: a change touches one place. **Leverage**: callers get a lot
  for little.

## 1. Survey

Change no code in this step.

1. Read `CLAUDE.md`, `ARCHITECTURE.md`, `docs/adr/`, and `docs/FIXLOG.md`
   where they exist.
2. Find hot spots:

   ```bash
   git log --since=30.days --name-only --format= | sort | uniq -c | sort -rn | head -20
   git ls-files 'app/src/main/*.kt' | xargs wc -l | sort -n | tail -20
   ```

3. Map the structure: features and their files, what imports what, where
   Room is read and written.
4. Look for Kotlin and Compose friction:
   - One screen or ViewModel that owns everything (`AppRoot.kt` with every handler).
   - Classes that only forward calls (a repository that wraps a DAO and adds nothing).
   - DAO calls spread through many ViewModels instead of one repository per feature.
   - Pure logic inside composables or ViewModels, which unit tests cannot reach.
     Move it into `lib/`.
   - Parameters passed through three composables that no middle one reads.
   - Copies that drifted: the same helper or color defined in two files.
   - A settings card or dialog that lives far from its feature.
5. **Deletion test** for each candidate: if you deleted this module, would
   its complexity concentrate in one place (keep it) or spread to every
   caller (it earns its place)? Pass-throughs fail the test.

## 2. Propose

Write `docs/architecture-review-<YYYY-MM-DD>.md`, one card per candidate:

```markdown
## <Title in domain nouns>  [Strong | Worth exploring | Speculative]

Files: ui/AppRoot.kt (820 lines), ...
Signal: <which trigger above>
Problem: <what is hard to change today, with one example>
Proposal: <the new module and its interface, 15 lines of Kotlin at most>
Benefits: <locality and leverage; which tests get simpler>
Proof of same behavior: <unit tests, self-test, snapshots, perf baseline>
Plan: <one commit per step>
```

End with a **top recommendation** and a short "considered, not
recommended" list. "The code is fine" is a valid result: then every card
is `Speculative` or there are none.

Stop and ask the user which candidate to do. In an unattended loop, do the
top recommendation only if it is `Strong`. Leave the rest in the file.

## 3. Refactor

- **Before:** record the proof: `./gradlew check` passes, and the PNGs in
  `shots/` are copied aside for comparison. Add a performance baseline where
  speed matters.
- One refactor per commit. Behavior stays the same. No features mixed in.
- New code goes in new files that compile first. Then move callers over.
  `./gradlew assembleDebug` passes after every step.
- Replacing an engine: keep the old path behind a flag for one milestone,
  then delete it.
- Replace tests, do not layer them: delete the old shallow tests that the
  new interface tests cover.
- If a diff passes about 10 files, split it.
- If the interface is open, design it twice: sketch two or three
  interfaces under different constraints (fewest entry points, simplest
  common caller, ports and adapters) and pick one with a reason.

## 4. When to split into Gradle modules

Stay in one `:app` module with packages until a reason shows up:

- A second app, a widget process, or a test fixture needs the same code.
  Make a library module (the scanner overlay's `:scanner` is one).
- Build time hurts: a change in one feature rebuilds everything. Split
  the slow, stable part out.
- A boundary needs enforcing: a `:data` module that has no Compose
  dependency stops UI code from reaching Room directly.

Each module has its own `build.gradle.kts`, an `include(":name")` in
`settings.gradle.kts`, and an `implementation(project(":name"))`. Move one
package at a time and run `./gradlew check` after each move.

## 5. Record

- Update `ARCHITECTURE.md`: features, layers, dependency direction, seams.
- Add an ADR in `docs/adr/` only when the decision is hard to reverse,
  surprising without context, and a real trade-off.
- Add a `docs/FIXLOG.md` entry for any bug the refactor found. Every entry
  ends with a `Rule:` line.

Done when the review file exists, the chosen refactor is committed in
steps, `./gradlew check` passes before and after, the snapshots match, and
`ARCHITECTURE.md` matches the code.
