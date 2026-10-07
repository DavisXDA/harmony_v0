# Harmony Architecture

## 1. Status and decision context

- Phase: Phase 0 — Product and Architecture Definition
- Status: Proposed for review
- Scope: First technical milestone and a minimal path to the first MVP

No Spring Boot, Flutter, database, or harmonization implementation is created in Phase 0.

## 2. Architecture decision summary

Harmony will begin as a modular monolith with a framework-independent Java music domain and harmonization engine. A later Spring Boot adapter will expose the use case over REST. A later Flutter client will consume that API. No persistence is needed because the first MVP is request/response only.

```text
Future Flutter client
        |
        | HTTPS / JSON
        v
Spring Boot REST adapter (later)
        |
        | calls application use case
        v
Application orchestration
        |
        v
Pure Java music domain + deterministic SATB engine
        |
        +-- candidate generation
        +-- HARD validation
        +-- SOFT penalties / REWARD bonuses
        +-- stable best-candidate selection
```

Dependencies point inward. The domain does not import Spring, JSON libraries, HTTP types, mobile code, database code, or external AI services.

## 3. Architectural principles

1. Musical correctness has priority over feature breadth and performance.
2. Domain models make musical invariants explicit.
3. Harmonization is deterministic for a fixed normalized input, engine version, and rule-set version.
4. Rules are isolated, categorized, named, and unit-testable.
5. Infrastructure translates data but does not decide notes.
6. Invalid input and no-solution outcomes are explicit domain/application results.
7. Add abstractions only when they protect a real boundary or make a rule independently testable.

## 4. Domain boundaries

### 4.1 Music model

Owns value objects and invariants that describe music, including:

- `Pitch`, pitch class, accidental, and octave;
- `Note` and, when rhythm enters scope, `Duration`;
- `Key`;
- `Chord` and `ChordType`;
- `Voice` and `VoiceRange`;
- `Melody`, aligned musical events, and `Harmonization`.

It does not own HTTP parsing, JSON annotations, controllers, persistence, or UI state.

### 4.2 Harmonization engine

Owns the deterministic transformation from validated musical input to SATB output:

1. generate legal vertical candidates for each event;
2. reject candidates violating HARD rules;
3. evaluate transitions and voice leading;
4. apply SOFT penalties and REWARD bonuses;
5. select the best complete path using a stable tie-break;
6. return the harmonization and explanation data.

The engine does not correct input, infer chords, call remote services, or relax mandatory rules when no solution exists.

### 4.3 Rule model

Owns rule identity, category, evaluation context, outcome, and score contribution.

- HARD: a violation invalidates a candidate or path.
- SOFT: a violation adds a deterministic penalty.
- REWARD: desired behavior adds a deterministic bonus.

Rules that inspect one chord and rules that inspect transitions may use distinct evaluation contexts if that keeps contracts small and explicit. A single highly generic rule framework is not required.

### 4.4 Application boundary

Owns use-case orchestration:

- validate/normalize the command through domain factories or parsers;
- invoke the engine;
- map domain outcomes to application outcomes;
- expose engine and rule-set version metadata.

It contains no HTTP or Spring types.

### 4.5 API adapter

A later Spring Boot adapter will own:

- JSON request/response DTOs;
- REST routing and HTTP status mapping;
- boundary validation and error serialization;
- correlation and timing telemetry.

DTOs must not become the core domain model.

### 4.6 Mobile client

A later Flutter application will own input interaction, client-side presentation checks, arrangement display, and playback interaction. It must not duplicate SATB generation or authoritative music-rule validation.

### 4.7 Persistence

There is no persistence boundary in the first MVP. A repository abstraction must not be introduced until a concrete persistence use case is approved.

## 5. Proposed Java project and package structure

Start with one Maven module to minimize build and dependency complexity. Package boundaries provide separation; a multi-module build can be considered only when dependency enforcement provides demonstrated value.

```text
br.com.harmony
├── domain
│   ├── music
│   │   ├── Pitch, Note, Duration, Key
│   │   ├── Chord, ChordType
│   │   ├── Voice, VoiceRange
│   │   └── Melody, Harmony, Harmonization
│   └── harmonization
│       ├── HarmonizationEngine
│       ├── HarmonizationRequest
│       ├── Candidate
│       ├── CandidateGenerator
│       ├── CandidateSelector
│       ├── Evaluation, Score, RuleViolation
│       └── rule
│           ├── HarmonizationRule
│           ├── RuleCategory
│           ├── vertical
│           └── voiceleading
├── application
│   └── harmonize
│       ├── HarmonizeUseCase
│       ├── HarmonizeCommand
│       └── HarmonizeResult
└── adapter
    └── in
        └── rest
            ├── HarmonizationController
            ├── dto
            └── ApiExceptionHandler
```

Names are proposals, not instructions to create empty classes. The initial implementation should add only types needed by the current milestone. Spring dependencies, once approved, are confined to `adapter.in.rest` and composition/bootstrap code.

## 6. Harmonization processing model

### 6.1 Input normalization

Text notation is parsed once at the system boundary into domain values. Internal comparison must not depend on raw strings. Unsupported enharmonic spellings or chord forms fail explicitly.

### 6.2 Search

For each aligned chord/Soprano event:

1. preserve the given Soprano pitch;
2. enumerate chord-member pitches within Alto, Tenor, and Bass ranges;
3. construct vertically ordered candidates;
4. apply vertical HARD rules immediately to prune invalid candidates;
5. evaluate valid transitions from prior candidates;
6. discard transitions violating voice-leading HARD rules;
7. accumulate penalties, rewards, and explanations.

A bounded dynamic-programming or exhaustive path search is sufficient for the initial short-input limits. The choice should be made after a small benchmark using the approved musical rules; no heuristic may make results nondeterministic.

### 6.3 Stable selection

Candidates are ordered by:

1. lowest total penalty / highest normalized score, according to one documented scoring convention;
2. stable musical tie-break fields, such as ordered MIDI-equivalent pitch values for Bass, Tenor, Alto, then Soprano;
3. never collection iteration order, object identity, randomness, or thread timing.

The exact scoring sign and tie-break fields must be finalized before implementation and recorded in `docs/MUSIC_RULES.md`.

### 6.4 Outcomes

The use case has three explicit outcomes:

- success: valid harmonization plus evaluation details;
- invalid input: one or more structured input errors;
- no solution: supported and valid input for which no candidate path satisfies all HARD rules.

## 7. Explainability model

At minimum, successful output records:

- engine version and rule-set version;
- total score;
- deterministic selection/tie-break identity;
- per-event or per-transition rule identifier, category, outcome, and score contribution where relevant.

Verbose traces for rejected candidates are not required in normal API responses; they may become test diagnostics. This avoids exposing an unbounded search trace while retaining an auditable selected path.

## 8. Error strategy

- Domain construction prevents invalid values where practical.
- Multiple request validation errors may be returned together when deterministically discoverable.
- Error codes are stable and machine-readable; messages are human-readable but are not contract identifiers.
- No-solution is distinct from invalid input and internal failure.
- API adapters map errors according to `docs/API.md`.

## 9. Testing strategy

### 9.1 Domain unit tests

Test value parsing, equality, ordering, supported notation, and invariants without Spring. Cover happy paths, boundaries, and invalid values.

### 9.2 Rule unit tests

Each rule receives focused tests for:

- a non-violation;
- a clear violation or scoring event;
- exact boundary conditions;
- relevant inversions, directions, and voice pairs;
- deterministic explanation and score.

Expected examples include `VoiceCrossingRuleTest`, `VoiceRangeRuleTest`, `ParallelFifthsRuleTest`, and `MelodicLeapRuleTest`, subject to the approved rule set.

### 9.3 Engine tests

- candidate generation for small known chords;
- pruning by HARD rules;
- cumulative scoring and stable tie-breaks;
- deterministic repeat tests;
- valid known phrases and defined no-solution phrases;
- Soprano preservation and four-voice alignment.

### 9.4 Property/invariant tests

For a curated set or generated supported inputs, assert that every successful result preserves Soprano, stays within voice ranges, avoids crossing, uses permitted chord tones, and violates no enabled HARD rule. This need not introduce a new property-testing framework initially; parameterized JUnit 5 tests are sufficient.

### 9.5 API contract tests

When the REST adapter is authorized, verify serialization, validation errors, status codes, media types, version path, and examples in `docs/API.md`.

### 9.6 End-to-end and regression tests

Maintain a small, music-theory-reviewed golden corpus. Golden results test determinism, but validity assertions must accompany exact-output assertions so tests do not confuse one valid voicing with the only valid voicing. Every bug fix begins with a failing regression test.

## 10. Security, privacy, and operations

The first MVP has no accounts and stores no arrangements. Even so:

- enforce request size and numeric bounds;
- reject unknown fields if contract strictness is adopted;
- avoid logging full request bodies by default;
- return correlation identifiers for internal errors;
- publish no cloud topology during Phase 0.

## 11. Technical risks and mitigations

| Risk | Impact | Mitigation / decision gate |
|---|---|---|
| Musical rules are underspecified or disputed. | Invalid or stylistically poor output. | Music Theory Agent defines objective rules and examples in `MUSIC_RULES.md`; approve before engine work. |
| Candidate count grows combinatorially. | High latency or memory usage. | Keep short explicit limits, prune HARD violations early, benchmark, then use bounded dynamic programming if needed. |
| Determinism is broken by unordered collections or ties. | Different outputs for identical requests. | Define total ordering and tie-breaks; add repeated-run tests. |
| Notation ambiguity and enharmonic spelling. | Incorrect parsing or chord membership. | Approve a canonical subset and reject unsupported notation explicitly. |
| Rules become coupled to Spring/JSON DTOs. | Hard-to-test domain and costly changes. | Enforce package dependency direction and plain Java domain tests. |
| A valid phrase has no solution under strict rules. | User receives no arrangement. | Return a specific no-solution outcome with relevant context; do not silently relax HARD rules. |
| Explanation payload grows with search space. | Large responses and implementation coupling. | Explain the selected path by default; keep rejected-candidate traces diagnostic only. |
| Exact musical output tests become brittle. | Safe scoring changes cause noisy failures. | Combine invariant tests with a small versioned golden corpus. |
| API freezes before notation is approved. | Breaking contract changes. | Mark contract draft; finalize supported notation and limits before implementation. |

## 12. Architecture decision gates

Before application implementation begins, Phase 0 review must approve:

1. the first supported musical subset and canonical notation;
2. HARD, SOFT, and REWARD rules and weights;
3. voice ranges, spacing, doubling, and parallel-motion policy;
4. score convention and stable tie-break order;
5. first technical milestone input limits;
6. API field shapes and error semantics.

## 13. Deferred decisions

- database technology and data model;
- cloud provider and deployment topology;
- authentication and authorization;
- asynchronous processing;
- export formats;
- advanced playback and score rendering;
- microservices or multi-module decomposition.

