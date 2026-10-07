# Harmony - Agent Instructions

## 1. Project Mission

Harmony is a mobile application for creating vocal arrangements for hymns.

The initial goal is to receive:

- key
- time signature
- BPM
- chord progression
- melody
- note durations

The provided melody will initially be treated as the Soprano line.

The system must generate:

- Soprano
- Alto
- Tenor
- Bass

The first version of the harmonization engine must be deterministic and
rule-based.

Do not use generative AI to choose musical notes in the production
harmonization engine.

---

## 2. MVP Scope

The first technical milestone is:

Input:

- musical key
- sequence of chords
- short melody

Output:

- valid SATB harmonization in JSON

Example:

Input:

Key: C

Chords:

C | F | G | C

Melody:

E4 | F4 | G4 | C5

Output:

Soprano
E4 | F4 | G4 | C5

Alto
C4 | C4 | D4 | E4

Tenor
G3 | A3 | B3 | G3

Bass
C3 | F3 | G3 | C3

The exact generated notes may vary as long as all mandatory harmony
rules are satisfied.

---

## 3. Out of Scope for the First MVP

Do not implement unless explicitly requested:

- authentication
- user accounts
- cloud infrastructure
- database
- audio recognition
- microphone input
- AI-generated harmonization
- payments
- subscriptions
- social features
- automatic chord recognition
- automatic melody recognition
- PDF generation
- MusicXML export
- MIDI export
- advanced mobile UI

These features may be added in later milestones.

---

## 4. Planned Technology Stack

### Backend

- Java
- Spring Boot
- Maven
- JUnit 5

### Mobile

- Flutter
- Dart

### Future database

- PostgreSQL

Do not introduce additional frameworks without documenting the reason.

---

## 5. Architecture Principles

Prefer:

- simple solutions
- explicit domain models
- deterministic behavior
- testable business rules
- separation of domain logic from infrastructure
- dependency inversion where useful
- small classes
- small commits

Avoid:

- premature microservices
- unnecessary abstractions
- framework coupling inside the music domain
- overengineering
- speculative features

The musical harmonization engine must not depend on Spring.

It must be possible to test the harmonization engine using plain Java
unit tests.

---

## 6. Core Domain

Expected initial domain concepts include:

- Note
- Pitch
- Duration
- Chord
- ChordType
- Key
- Voice
- VoiceRange
- Melody
- Harmony
- Harmonization
- HarmonizationRule

Expected voices:

- SOPRANO
- ALTO
- TENOR
- BASS

Domain terminology may evolve, but changes must be documented.

---

## 7. Harmonization Strategy

The SATB engine will use:

1. candidate generation
2. mandatory-rule validation
3. scoring of valid candidates
4. voice-leading evaluation
5. best-candidate selection

Rules should be categorized as:

### HARD

Violation makes the candidate invalid.

Example:

- voice crossing
- voice outside permitted range

### SOFT

Violation is allowed but adds a penalty.

Example:

- unnecessary melodic leap

### REWARD

Desirable behavior increases the candidate score.

Example:

- retaining a common tone between chords

The engine must expose enough information to explain why a candidate
was selected.

---

## 8. Testing Rules

No feature is considered complete without tests.

Tests must cover:

- happy paths
- boundary conditions
- invalid inputs
- musical rule violations

Music rules should be individually testable.

Example:

VoiceCrossingRuleTest

ParallelFifthsRuleTest

VoiceRangeRuleTest

MelodicLeapRuleTest

When fixing a bug:

1. create a failing test reproducing the bug
2. implement the fix
3. verify the test passes

---

## 9. Agent Roles

Agents should operate under clear responsibilities.

### Tech Lead Agent

Responsible for:

- architecture
- technical decisions
- milestone planning
- domain boundaries
- reviewing major architectural changes

Should avoid implementing large features directly when the work can
be delegated.

### Music Theory Agent

Responsible for:

- SATB rules
- voice-leading rules
- musical constraints
- documenting musical reasoning
- reviewing generated harmonizations

Must translate subjective musical recommendations into objective,
testable rules whenever possible.

### Backend Agent

Responsible for:

- Java implementation
- harmonization engine
- REST API
- automated tests

### Mobile Agent

Responsible for:

- Flutter application
- user interaction
- score/melody input
- playback interface

The Mobile Agent should not duplicate harmonization logic implemented
by the backend.

### QA Agent

Responsible for:

- reviewing requirements
- reviewing tests
- identifying edge cases
- verifying acceptance criteria
- detecting regressions

Whenever possible, the QA agent should not be the same agent that
implemented the feature being reviewed.

---

## 10. Agent Workflow

Before implementing a task, an agent must:

1. read AGENTS.md
2. read relevant files under docs/
3. identify the milestone being worked on
4. understand acceptance criteria

For substantial tasks:

1. analyze
2. propose a plan
3. implement
4. test
5. self-review
6. report changes

Agents must not silently expand the scope.

---

## 11. Git Rules

The main branch must remain stable.

Prefer one branch per task.

Suggested naming:

feature/<description>

fix/<description>

docs/<description>

refactor/<description>

Examples:

feature/note-domain-model

feature/chord-parser

feature/satb-candidate-generator

fix/parallel-fifths-rule

docs/music-rules

Commits should be small and focused.

Do not mix unrelated changes in the same commit.

---

## 12. Documentation

Architecture decisions should be documented.

The project will use:

docs/PRD.md

docs/ARCHITECTURE.md

docs/MUSIC_RULES.md

docs/ROADMAP.md

docs/API.md

Agents must update documentation when a change makes existing
documentation inaccurate.

---

## 13. Definition of Done

A task is complete only when:

- implementation is complete
- automated tests exist
- tests pass
- acceptance criteria are satisfied
- no unrelated changes were introduced
- relevant documentation is updated
- code has been reviewed

---

## 14. Priority

When there is conflict between goals, use this priority:

1. musical correctness
2. correctness
3. testability
4. simplicity
5. maintainability
6. performance
7. feature quantity

Do not sacrifice correctness for additional features.

---

## 15. Current Project Phase

The project is currently in:

PHASE 0 - PRODUCT AND ARCHITECTURE DEFINITION

Do not start application implementation until Phase 0 documentation
has been completed and reviewed.
