# Harmony Roadmap

## 1. Roadmap principles

- Complete and review Phase 0 before application implementation.
- Keep each milestone independently reviewable and acceptance-driven.
- Prioritize musical correctness, correctness, testability, and simplicity.
- Do not add authentication, persistence, cloud infrastructure, AI note selection, recognition, exports, or advanced UI during the first MVP.
- A milestone is complete only when its implementation, tests, acceptance evidence, documentation, and review are complete.

## 2. Milestone overview

| Milestone | Outcome | Primary owner |
|---|---|---|
| M0 | Approved product, architecture, musical rules, roadmap, and API contract | Tech Lead + Music Theory + QA |
| M1 | Pure Java music model and validated technical-milestone input | Backend |
| M2 | Individually tested mandatory and scoring rules | Music Theory + Backend |
| M3 | Deterministic engine generates one valid SATB harmonization in domain JSON-equivalent output | Backend |
| M4 | Versioned REST endpoint exposes M3 behavior | Backend |
| M5 | BPM, time signature, and durations complete the MVP musical input contract | Backend + Music Theory |
| M6 | Minimal Flutter client submits input and displays SATB output | Mobile |
| M7 | MVP verification and release-readiness review | QA + Tech Lead |

Milestones M1–M7 are planned work, not authorization to start before M0 approval.

## 3. Milestones and acceptance criteria

### M0 — Product and architecture definition

Deliverables:

- `docs/PRD.md`;
- `docs/ARCHITECTURE.md`;
- `docs/ROADMAP.md`;
- `docs/API.md`;
- `docs/MUSIC_RULES.md` completed by a Music Theory Agent;
- recorded resolution of the decision gates in `ARCHITECTURE.md`.

Acceptance criteria:

1. The first MVP and narrower first technical milestone are unambiguously separated.
2. Functional and non-functional requirements, boundaries, risks, test strategy, and exclusions are documented.
3. Architecture keeps the engine plain Java and independent of Spring.
4. API examples and error semantics agree with the PRD.
5. `MUSIC_RULES.md` defines objective, testable initial rules, ranges, notation, weights, and examples.
6. A Music Theory Agent reviews musical decisions, and a QA Agent checks completeness and contradictions.
7. Open questions blocking M1–M4 are resolved or explicitly assigned with an owner.
8. The Tech Lead records approval to leave Phase 0.

### M1 — Core music model and input validation

Scope:

- create the minimal Maven/JUnit 5 Java project only after M0 approval;
- implement the approved pitch, key, chord, voice, and harmonization input values;
- parse and validate only the approved notation subset;
- no Spring.

Acceptance criteria:

1. Domain code has no Spring, HTTP, JSON, database, Flutter, or AI dependency.
2. Supported key, chord, and pitch values round-trip through canonical notation.
3. Invalid syntax, unsupported values, sequence-length mismatch, size limits, and out-of-range Soprano notes are rejected with structured causes.
4. One chord per Soprano event is enforced.
5. Unit tests cover happy paths, all specified boundaries, and invalid cases.
6. Documentation remains accurate and Backend self-review plus QA review are recorded.

### M2 — Harmonization rules

Scope:

- implement only the approved HARD, SOFT, and REWARD rules;
- provide vertical and adjacent-event evaluation contexts as needed;
- expose deterministic outcomes and score contributions.

Acceptance criteria:

1. Every rule in the initial rule catalog maps to a named implementation and focused test class.
2. Each HARD rule accepts a valid example and rejects every documented violation example.
3. Each SOFT and REWARD rule produces the documented score contribution at boundary cases.
4. Rules do not depend on Spring or infrastructure.
5. Music Theory review confirms implementation matches `MUSIC_RULES.md`.
6. QA verifies voice pairs, motion directions, ranges, spacing, chord positions, and invalid inputs are covered.

### M3 — First deterministic SATB engine

Scope:

- generate candidates;
- validate mandatory rules;
- score valid candidates and transitions;
- select a best complete path with stable tie-breaking;
- return a domain result suitable for JSON mapping.

Acceptance criteria:

1. Given every supported phrase in the approved corpus, the engine returns four equally sized voice sequences.
2. Soprano exactly matches the input melody.
3. The independent validator reports no enabled HARD-rule violation.
4. Identical input and versions produce identical notes, score, rule outcomes, and selection identity across repeated runs.
5. No-solution input returns the explicit no-solution outcome without weakening rules.
6. Candidate and input limits prevent unbounded search; benchmark results are recorded before any performance SLO is adopted.
7. Happy, boundary, invalid, musical-violation, tie-break, and regression tests pass using plain Java/JUnit 5.
8. The Music Theory Agent and QA Agent approve the result corpus and evidence.

### M4 — Initial REST API

Scope:

- create the minimal Spring Boot adapter;
- implement `POST /api/v1/harmonizations` for the M3 input subset;
- map application outcomes to the documented JSON and HTTP contract;
- no database or authentication.

Acceptance criteria:

1. Valid documented requests return `200` and conform to `docs/API.md`.
2. Invalid/unsupported requests return `400` or `422` with stable error codes as documented.
3. Valid requests with no solution return `422` with `NO_VALID_HARMONIZATION`.
4. Contract tests cover examples, unknown/missing fields, status codes, and media type.
5. REST DTOs and Spring annotations do not enter the music domain.
6. API responses expose engine and rule-set versions plus selected-path explanation.
7. No persistence, account, cloud, or export code is added.

### M5 — Rhythmic MVP input

Scope:

- add BPM, time signature, and note durations to the input and output model;
- keep the initial arrangement homorhythmic;
- preserve M3 deterministic note selection unless an approved musical rule explicitly uses rhythm.

Acceptance criteria:

1. The API requires BPM, time signature, and one positive duration per melody event for the finalized MVP route/schema.
2. Approved BPM, meter, duration vocabulary, bounds, and measure-consistency rules are documented and tested.
3. Each voice event preserves the aligned duration.
4. Invalid tempo, meter, duration, alignment, and metric totals return structured validation errors.
5. Existing pitch-only engine tests continue to pass or are deliberately versioned with documented reasons.
6. Music Theory and QA reviews confirm rhythmic semantics.

### M6 — Minimal mobile client

Scope:

- create a minimal Flutter application;
- collect explicit supported input;
- submit it to the REST API;
- display four aligned voices and understandable errors.

Acceptance criteria:

1. A user can enter all first-MVP fields using the supported notation.
2. The supplied melody is visibly identified as Soprano.
3. A successful request displays Soprano, Alto, Tenor, and Bass in event order.
4. Validation, no-solution, connectivity, and server errors are distinguishable.
5. The client does not implement or duplicate harmonization rules.
6. Widget/unit tests cover the primary flow and error states; API integration uses the versioned contract.
7. No advanced score editor, authentication, storage, recognition, payment, social, or export feature is added.

### M7 — MVP verification and release readiness

Scope:

- validate the integrated MVP against requirements and the approved musical corpus;
- resolve release-blocking defects;
- produce a release decision, not new features.

Acceptance criteria:

1. Every PRD requirement planned for the first MVP is traced to passing automated or documented acceptance evidence.
2. All enabled HARD-rule validations pass for the reviewed corpus.
3. Determinism and API contract suites pass in a clean build.
4. Supported limits and known limitations are documented for users and maintainers.
5. No unresolved critical correctness or musical-correctness defect remains.
6. QA, Music Theory, Backend, Mobile, and Tech Lead reviews are recorded.

## 4. Delegation plan for specialized agents

### Music Theory Agent

- author `docs/MUSIC_RULES.md`;
- define the initial notation subset, SATB ranges, spacing, chord membership/doubling, parallels, leaps, and cadence policy;
- classify every rule as HARD, SOFT, or REWARD and propose objective weights;
- supply valid, invalid, boundary, and no-solution examples;
- review M2, M3, M5, and the golden corpus.

### Backend Agent

- after M0 approval, implement M1–M5 within the defined package boundaries;
- keep the engine framework-independent;
- write rule, engine, regression, and API contract tests;
- benchmark bounded search and document results.

### Mobile Agent

- after M4/M5 contract stabilization, implement the minimal M6 client;
- validate presentation/input ergonomics without duplicating authoritative rules;
- test success and error-state rendering.

### QA Agent

- review Phase 0 documents for contradictions and missing acceptance cases;
- build requirement-to-test traceability;
- independently review rule boundaries, invalid inputs, deterministic ties, and no-solution behavior;
- run milestone and regression acceptance checks.

### Tech Lead Agent

- resolve cross-domain decisions and approve leaving Phase 0;
- review dependency direction and scope;
- ensure deferred features do not enter milestones silently;
- coordinate contract changes and document architecture decisions.

## 5. Recommended sequence and gates

```text
M0 -> M1 -> M2 -> M3 -> M4 -> M5 -> M6 -> M7
```

M1 model discovery may inform M2, but M2 cannot be accepted before its rule catalog is approved. M6 should not begin against an unstable API. Parallel work is appropriate only when contracts are already explicit and the work does not bypass a milestone gate.
