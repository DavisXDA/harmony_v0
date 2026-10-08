# M1 Backend Self-Review

## Scope

- Milestone: M1 — Core music model and input validation
- Branch: `feature/m1-core-music-model`
- Role: Backend Agent
- Java: 21

## Acceptance evidence

- The project is a single minimal Maven module with JUnit 5 as a test-only dependency.
- Production code is pure Java under `br.com.harmony`.
- Canonical pitch classes, pitches, supported major keys, major/minor chord symbols, voices, and inclusive voice ranges are implemented.
- Technical-milestone input validation returns stable structured error identifiers and suppresses key-dependent errors when the key is unavailable.
- Chord and melody sequences are required, non-empty, aligned, and limited to 1–8 events.
- The regression `C` major / chord `C` / Soprano `D4` passes request validation because chord membership is not an M1 input rule.
- `mvn test` passes 100 tests with zero failures, errors, or skips.

## Scope boundary review

No candidate generation, chord-membership evaluator, harmonization rule, scoring, voice-leading evaluation, search, selection, Spring, REST/HTTP, JSON, Flutter, persistence, recognition, export, or AI SDK behavior was implemented.

## Review status

Backend self-review: PASS.

An independent QA review remains a separate governance activity; this document does not claim to replace it.

## Independent QA remediation

- Remediation branch: `fix/m1-validation-remediation`
- Findings addressed: H-01, H-02, H-03, M-01, and M-02.
- H-01: Soprano absolute-range validation now runs after successful pitch parsing even when key parsing fails; only key-dependent checks are suppressed.
- H-02: Soprano absolute-range and selected-key pitch-support checks are independent, so both errors can be returned for one melody event.
- H-03: collected errors are explicitly sorted by known field order (`key`, `chords`, `melody`), numeric array index, and error-code name.
- M-01: `ValidatedHarmonizationInput` now enforces the complete M1 validated-state invariant in its public constructor while retaining immutable defensive copies.
- M-02: `ValidationResult` now permits only success (value and no errors) or failure (no value and one or more errors), with factories for those states.
- Regression coverage includes numeric array-index ordering and confirms that `C` / `C` / `D4` remains valid M1 input without adding chord-membership validation.
- Final Java 21 result: `mvn test` completed with `BUILD SUCCESS` — 118 tests run, 0 failures, 0 errors, 0 skipped.
- Production dependencies remain empty; JUnit Jupiter remains test-scoped only.
- Scope confirmation: remediation is limited to the M1 pure-Java music model and input validation. No M2 harmonization rules or engine behavior, Spring, HTTP/REST, JSON, persistence, Flutter, AI, or other deferred functionality was added.
