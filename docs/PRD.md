# Harmony Product Requirements Document

## 1. Document status

- Phase: Phase 0 — Product and Architecture Definition
- Status: Draft for review
- Product: Harmony
- Initial platform: Mobile client backed by a harmonization service

This document defines the product boundary and requirements. It does not authorize application implementation. Phase 1 may begin only after the Phase 0 documents have been reviewed and approved.

## 2. Product vision

Harmony helps a musician create a four-part vocal arrangement for a hymn from explicit musical input. The user supplies a melody, initially treated as the Soprano line, together with its harmonic and rhythmic context. Harmony returns a deterministic Soprano, Alto, Tenor, and Bass (SATB) arrangement.

The product is intended to make a musically valid starting arrangement easier to obtain. It is not intended to replace an arranger's artistic judgment.

## 3. Target user and primary need

The initial user is a hymn arranger, choir leader, or musician who knows the key, chords, melody, and rhythm of a short passage but needs a usable SATB realization.

Primary need: produce the same valid, inspectable SATB result for the same supported input without manually writing all four voices.

## 4. Scope

### 4.1 First MVP

The first MVP accepts:

- musical key;
- BPM;
- time signature;
- chord progression;
- melody pitches;
- melody note durations.

The supplied melody is the Soprano voice. The MVP returns:

- Soprano;
- Alto;
- Tenor;
- Bass;
- enough rule and score information to explain why the result was selected.

The production harmonization engine is deterministic and rule-based. Generative AI must not select notes.

### 4.2 First technical milestone

The first technical milestone is intentionally narrower than the complete MVP:

- input: one supported key, a short chord progression, and an equally sized sequence of Soprano pitches;
- processing: deterministic candidate generation, hard-rule validation, scoring, and best-candidate selection;
- output: one valid SATB harmonization as JSON.

BPM, time signature, and durations belong to the MVP product contract but are deferred from this milestone's engine behavior. The API may not require those deferred fields until the roadmap milestone that introduces rhythmic context.

### 4.3 Explicitly out of scope for the first MVP

- authentication and user accounts;
- cloud infrastructure and deployment design;
- database or persistence;
- audio recognition or microphone input;
- automatic chord or melody recognition;
- AI-generated note selection;
- payments, subscriptions, and social features;
- PDF, MusicXML, and MIDI export;
- advanced mobile UI.

## 5. Product requirements

| ID | Requirement |
|---|---|
| PR-01 | A user can describe a short hymn passage using explicit musical data. |
| PR-02 | The supplied melody is preserved exactly as the Soprano line. |
| PR-03 | Harmony produces Alto, Tenor, and Bass lines that form a valid SATB arrangement under the documented mandatory rules. |
| PR-04 | Identical normalized input and the same engine/rule-set version produce identical output. |
| PR-05 | Invalid or unsupported input is rejected with actionable validation information rather than silently altered. |
| PR-06 | The result is available as structured JSON for integration with a future mobile client. |
| PR-07 | The result identifies the rule-set/engine version and explains selection through rule evaluations and scoring. |

## 6. Functional requirements

### 6.1 Input and validation

| ID | Requirement | Initial milestone |
|---|---|---|
| FR-01 | Accept a supported musical key using one canonical textual notation. | Yes |
| FR-02 | Accept an ordered chord sequence in a documented subset. | Yes |
| FR-03 | Accept an ordered melody-pitch sequence and treat it as Soprano. | Yes |
| FR-04 | Require one chord per melody event for the initial milestone. | Yes |
| FR-05 | Validate syntax, supported values, sequence alignment, and Soprano range before harmonization. | Yes |
| FR-06 | Accept BPM within documented limits. | Later MVP milestone |
| FR-07 | Accept a supported time signature. | Later MVP milestone |
| FR-08 | Accept a positive duration for every melody event and validate metric consistency. | Later MVP milestone |

### 6.2 Harmonization

| ID | Requirement | Initial milestone |
|---|---|---|
| FR-09 | Generate candidate Alto, Tenor, and Bass notes from the current chord while preserving Soprano. | Yes |
| FR-10 | Reject candidates that violate any enabled HARD rule. | Yes |
| FR-11 | Apply deterministic penalties for SOFT rules and bonuses for REWARD rules. | Yes |
| FR-12 | Evaluate voice leading between adjacent events. | Yes |
| FR-13 | Resolve equal scores with a documented stable tie-break order. | Yes |
| FR-14 | Return a valid best candidate or a defined no-solution error. | Yes |
| FR-15 | Include rule outcomes and score contributions sufficient to explain selection. | Yes |

### 6.3 Output

| ID | Requirement | Initial milestone |
|---|---|---|
| FR-16 | Return aligned Soprano, Alto, Tenor, and Bass event sequences. | Yes |
| FR-17 | Preserve the input Soprano pitches without transposition or correction. | Yes |
| FR-18 | Return stable machine-readable error codes for invalid input and no-solution cases. | Yes |
| FR-19 | Add BPM, time signature, and durations to the returned arrangement when rhythmic context is introduced. | Later MVP milestone |

## 7. Non-functional requirements

| ID | Requirement |
|---|---|
| NFR-01 Determinism | The same normalized request, engine version, and rule-set version must return byte-equivalent musical content and scores. |
| NFR-02 Musical correctness | No returned harmonization may violate an enabled HARD rule. |
| NFR-03 Testability | The music domain and engine must run in plain Java tests without Spring or external services. |
| NFR-04 Explainability | Every scored result must identify applied rules and their score contributions; hard-rule violations must be identifiable during evaluation. |
| NFR-05 Simplicity | Use a modular monolith and in-process rule engine; no database, queue, or microservice is required. |
| NFR-06 Performance | Establish a measured baseline before setting a latency SLO. Candidate search is bounded by 1–8 events and a 2-second protective processing ceiling; the ceiling is not a latency SLO. |
| NFR-07 Reliability | Malformed or unsupported input must produce a controlled error response and must not expose implementation details. |
| NFR-08 Maintainability | Rules are individually testable and categorized as HARD, SOFT, or REWARD. |
| NFR-09 Compatibility | The REST contract is versioned from its first implementation. |
| NFR-10 Observability | Future API implementation records request correlation, outcome, engine version, and timing without logging full musical payloads by default. |

## 8. Product constraints and assumptions

- Initial harmony is homorhythmic: one chord corresponds to one Soprano event and one note in each generated voice.
- The first technical milestone supports only the key, chord, pitch, chord-type, accidental, and range subset approved in `docs/MUSIC_RULES.md`.
- Enharmonic spelling and inversion behavior must be intentional, not inferred ad hoc.
- The initial phrase length is 1–8 events as defined in `docs/MUSIC_RULES.md`; BPM bounds remain open for M5.
- The initial REST operation accepts at most 16384 raw request-body bytes and applies a 2-second harmonization processing ceiling, as defined in `docs/API.md`.
- When no valid candidate exists, the system reports failure; it does not weaken HARD rules.

## 9. First technical milestone acceptance criteria

The milestone is accepted when all of the following are true:

1. A supported request containing a key, short chord sequence, and aligned Soprano pitch sequence produces HTTP-independent domain output with four aligned voices.
2. Output Soprano pitches exactly equal input melody pitches.
3. Every returned note belongs to its permitted voice range and voices do not cross.
4. Every returned vertical sonority satisfies the approved chord-membership and doubling rules.
5. Every adjacent pair satisfies all approved mandatory voice-leading rules, including the decision on parallel perfect intervals.
6. Repeating a request produces the same musical result, rule evaluations, and score.
7. Invalid syntax, unequal sequence lengths, unsupported musical elements, out-of-range Soprano notes, and no-solution inputs have automated tests and defined errors.
8. Each rule has isolated unit tests, and representative end-to-end domain tests cover happy, boundary, and failure cases.
9. The JSON mapping conforms to `docs/API.md` once the API adapter milestone begins.
10. A Music Theory Agent and a QA Agent review the musical rule set and acceptance evidence before the milestone is closed.

## 10. Success measures for the MVP

- 100% of returned arrangements pass all enabled HARD rules in the automated validator.
- 100% repeatability for identical input and version.
- All supported input/error examples in the API contract pass contract tests.
- A Music Theory Agent approves a documented validation corpus of representative hymn phrases.

These are correctness gates, not adoption metrics. User adoption and arrangement-quality metrics require later product discovery.

## 11. Open product questions

The following require explicit decisions before implementation of the affected behavior:

1. What canonical duration notation will M5 support? Pitch, key, and chord notation are resolved in `docs/MUSIC_RULES.md`.
2. What BPM range will M5 support? The initial pitch-only phrase limit is resolved as 1–8 events.
3. Will M5 remain limited to one chord per melody note, or add multiple notes per chord and ties?
4. How much selected-path explanation should be displayed to end users versus retained for diagnostics?
