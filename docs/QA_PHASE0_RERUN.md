# Independent Phase 0 QA Rerun

## 1. Review identity and decision

- Role: new independent QA Agent; the reviewer did not author the Phase 0 specification, the original QA report, or the remediation.
- Reviewed baseline: `origin/docs/phase-0-remediation`.
- QA branch: `docs/phase-0-qa-rerun`.
- Phase: M0 / Phase 0 only. No application or build code was reviewed, created, or authorized.
- Documents reviewed: `AGENTS.md`, `docs/PRD.md`, `docs/ARCHITECTURE.md`, `docs/ROADMAP.md`, `docs/API.md`, and `docs/MUSIC_RULES.md`.
- Historical context: `docs/QA_PHASE0_REVIEW.md` from `origin/docs/phase-0-qa-review`; its conclusions were independently rechecked rather than presumed resolved.
- Method: cross-document comparison and independent recalculation of pitch classes, pitch numbers, chord membership, completeness, ranges, spacing, overlap, melodic motion, all six unordered voice pairs, leading-tone behavior, scoring, fixture outcomes, validation categories, API schemas, limits, and milestone traceability.

Result: **PASS**. All previous HIGH findings are fully resolved, no new CRITICAL or HIGH finding was found, all reviewed rules are objective and implementable, V1–V5 pass every applicable HARD rule, N1–N3 remain valid no-solution cases, and no unresolved question blocks M1–M4.

## 2. Previous finding recheck

| ID | Status | Independent evidence |
|---|---|---|
| H-01 | PASS | `MUSIC_RULES` 2.2 enumerates exact key-sensitive pitch-class sets. `API` 5 uses the same cases and distinguishes global grammar from selected-key support. F-natural in G, B-natural in F, and F#/Bb in C are unambiguously `UNSUPPORTED_PITCH`; N1 remains supported input because D is in C major's accepted set. |
| H-02 | PASS | The two defective boundary sonorities were replaced. Highest Tenor `E5/C5/G4/C3` on C has all voices in range, a complete triad, legal order and spacing. Maximum A–T spacing `D5/B4/B3/G3` on G is 12 semitones and passes every other vertical HARD rule. |
| H-03 | PASS | `API` 3.1 and 5, `MUSIC_RULES` 4.2 and 11, and Architecture error strategy agree: a canonical, key-supported Soprano outside C4–G5 is `400 INVALID_REQUEST` with field code `SOPRANO_OUT_OF_RANGE`. The former open/deferred wording is removed. |
| H-04 | PASS | The exact M4 limits are normative and cross-documented: raw body maximum 16384 bytes and deterministic harmonization ceiling 2 seconds. `API` defines 16384 accepted, 16385 rejected before parsing, `413 REQUEST_TOO_LARGE`, and `503 HARMONIZATION_TIMEOUT` without partial output. |
| H-05 | PASS | `API` 3.2–3.3 defines required `evaluation.events`, `evaluation.transitions`, and `evaluation.windows`, including indices, deterministic ordering, empty-array behavior, scope ownership, non-duplication, and score reconciliation. Architecture adopts the same schema. |
| M-01 | PASS | The vertical examples now isolate their targets. Full-sonority parallel-fifths and parallel-octaves fixtures isolate their named violations. The unavoidable parallel-unisons/overlap interaction explicitly declares the complete expected set `{PARALLEL_UNISONS, VOICE_OVERLAP}` and requires direct rule testing plus combined-validator testing. |
| M-02 | PASS | Both maximum-leap failures use supported spellings and complete legal sonorities. The C/C Soprano fixture isolates an 8-semitone Soprano excess; the F/C Bass fixture isolates a 19-semitone Bass excess. |
| M-03 | PASS | `API` 5.1 defines oversize and malformed fail-fast behavior, category-1 and category-2 collection, prerequisite suppression, 400 precedence, deterministic field ordering, and the prohibition on harmonization before validation succeeds. Architecture and Roadmap agree. |
| L-01 | PASS | `MUSIC_RULES` 3.3 correctly references `PARALLEL_FIFTHS`, `PARALLEL_OCTAVES`, and `PARALLEL_UNISONS` in Sections 7.10–7.12. |

Previous findings resolution summary: **9 PASS, 0 PARTIAL, 0 FAIL**.

## 3. Pitch support semantics

### 3.1 Exact accepted pitch classes

| Key | Exact accepted set | Result |
|---|---|---|
| C major | C D E F G A B | PASS |
| G major | G A B C D E F# | PASS |
| F major | F G A Bb C D E | PASS |

The global canonical grammar is exactly `(C|D|E|F|G|A|B|F#|Bb)` plus one or more octave digits. Selected-key support is applied only after successful parsing.

| Case | Syntax | Key context | Required outcome | Result |
|---|---|---|---|---|
| F4 in G | canonical | unsupported | `422 UNSUPPORTED_MUSICAL_ELEMENT`, field `UNSUPPORTED_PITCH` | PASS |
| B4 in F | canonical | unsupported | same | PASS |
| F#4 in G | canonical | supported | proceeds to remaining validation | PASS |
| Bb4 in F | canonical | supported | proceeds to remaining validation | PASS |
| F#4 in C | canonical | unsupported | `422 UNSUPPORTED_MUSICAL_ELEMENT`, field `UNSUPPORTED_PITCH` | PASS |
| Bb4 in C | canonical | unsupported | same | PASS |
| Gb4 | not canonical | not evaluated | `400 INVALID_REQUEST`, field `INVALID_PITCH_FORMAT` | PASS |
| A#4 | not canonical | not evaluated | same | PASS |

C major / chord C / Soprano D4 is canonical, accepted in the selected key, in Soprano range, and not a C-triad member. It therefore reaches harmonization and produces `422 NO_VALID_HARMONIZATION` through `CHORD_MEMBERSHIP`. Result: PASS.

## 4. HARD-rule verification

### 4.1 Vertical rules

| Rule | Objective predicate | Example/fixture verification | Result |
|---|---|---|---|
| `SOPRANO_PRESERVATION` | Candidate Soprano equals input spelling and octave at the event | Invalid fixture changes E4 to G4 while its resulting C sonority passes other vertical rules | PASS |
| `VOICE_RANGE` | Inclusive S C4–G5, A G3–D5, T C3–G4, B E2–C4 | Endpoints and out-of-range examples are numerically exact | PASS |
| `VOICE_CROSSING` | `S >= A >= T >= B`; equality legal | E4/G4/C4/C3 fails only because Alto exceeds Soprano | PASS |
| `ADJACENT_VOICE_SPACING` | S–A ≤12, A–T ≤12, T–B ≤19 | Equality boundaries and overflow examples are exact | PASS |
| `CHORD_MEMBERSHIP` | Every pitch class belongs to the event triad | A4/E4/G3/C3 on C retains C/E/G yet only A is outside the chord | PASS |
| `COMPLETE_TRIAD` | Root, third, and fifth all occur | G4/G3/C3/C3 on C omits E while satisfying membership, root Bass, range, order, and spacing | PASS |
| `ROOT_POSITION_BASS` | Bass pitch class equals chord root | G4/E4/C4/E3 on C has a complete member-only triad and fails root Bass | PASS |
| `LEADING_TONE_DOUBLING` | Key leading-tone pitch class occurs at most once | B4/D4/B3/G3 on G in C major has exactly two Bs and passes other vertical rules | PASS |

All vertical definitions are numeric or set-based, inclusive/exclusive boundaries are stated, and each advertised invalid vertical fixture fails only its target.

### 4.2 Voice-leading rules

| Rule | Independent check | Example/fixture result |
|---|---|---|
| `VOICE_OVERLAP` | For each adjacent Upper/Lower pair, new Lower ≤ previous Upper and new Upper ≥ previous Lower | C→Dm fixture fails because new Tenor D4 exceeds previous Alto C4; other HARD checks pass. PASS |
| `MAX_MELODIC_LEAP` | S/A/T absolute motion ≤7; Bass ≤12 | C/C fixture has only Soprano at 8; F/C fixture has only Bass at 19. Both sonorities and other transition rules pass. PASS |
| `PARALLEL_FIFTHS` | Any unordered voice pair, both nonzero same direction, start/end interval class 7 | C→Dm fixture has Bass/Soprano 19→19 and no other HARD failure. PASS |
| `PARALLEL_OCTAVES` | Any pair, both nonzero same direction, start/end positive multiples of 12 | C→Dm fixture has Bass/Soprano 24→24 and no parallel fifth/unison or other HARD failure. PASS |
| `PARALLEL_UNISONS` | Any pair, both nonzero same direction, start/end distance 0 | Predicate is objective. Full-validator isolation is mathematically impossible under overlap; the specification states the unavoidable two-rule violation set and direct-rule test requirement. PASS |
| `LEADING_TONE_RESOLUTION` | Before tonic, each leading tone moves exactly +1 semitone to tonic | G→C invalid fixture moves Soprano B4→G4 while other HARD rules pass. PASS |

The parallel rules quantify over all six unordered voice pairs. Fifths include compound fifths by interval class; octaves require a positive multiple of 12; unisons require distance zero. Stationary motion never counts as parallel motion. All definitions are implementable.

## 5. SOFT and REWARD rule verification

| Rule | Contribution and accumulation | Result |
|---|---|---|
| `COMFORTABLE_RANGE` | −1 per voice per event outside its inclusive comfortable range but inside its absolute range | PASS |
| `THIRD_DOUBLING` | −2 once per event when exactly two voices have the third | PASS |
| `FIFTH_DOUBLING` | −1 once per event when exactly two voices have the fifth | PASS |
| `ROOT_DOUBLING` | +2 once per event when exactly two voices have the root | PASS |
| `MELODIC_LEAP_PENALTY` | Per voice/transition: upper 3–4 = −1, 5–7 = −2; Bass 3–7 = −1, 8–12 = −2 | PASS |
| `STEPWISE_MOTION` | +1 per voice/transition for size 1–2 | PASS |
| `REPEATED_NOTE` | +1 per voice/transition for size 0 | PASS |
| `COMMON_TONE_RETENTION` | +1 per qualifying Alto/Tenor transition, intentionally cumulative with `REPEATED_NOTE` | PASS |
| `CONTRARY_OUTER_MOTION` | +1 once per transition when both outer voices move oppositely | PASS |
| `OBLIQUE_OUTER_MOTION` | +1 once per transition when exactly one outer voice is stationary | PASS |
| `SIMILAR_OUTER_MOTION` | −1 once per transition when both outer voices move in the same direction | PASS |
| `HIDDEN_DIRECT_FIFTH` | −2 once per legal transition with similar outer motion into class 7 and Soprano motion >2 | PASS |
| `HIDDEN_DIRECT_OCTAVE` | −2 once per legal transition with similar outer motion into a positive octave and Soprano motion >2 | PASS |
| `REPEATED_DIRECTION_LEAPS` | −2 per qualifying voice per three-event window | PASS |
| `LARGE_LEAP_COMPENSATION` | −2 per qualifying voice per three-event window; no final-transition penalty without a following event | PASS |

All contributions use one natural scope. Intentional cumulative effects are explicit: retained inner common tones may earn both retention and repeated-note rewards; hidden/direct penalties may coexist with the similar-motion penalty; both window predicates may apply to one voice/window. Rejected parallel-perfect transitions are not scored for corresponding hidden/direct rules. Each window `(i-2, i-1, i)` is evaluated once when event `i` arrives, and window contributions appear only in `evaluation.windows`. No accidental double-count path remains.

## 6. Corpus and fixture revalidation

### 6.1 Valid corpus V1–V5

Every event was checked for canonical/key spelling, Soprano preservation, all four ranges, order, adjacent spacing, chord membership, complete triad, root Bass, and leading-tone count. Every transition was checked for overlap, all voice leap limits, all six voice pairs under all three parallel rules, and leading-tone resolution.

| Phrase | Vertical HARD rules | Transition HARD rules | Notable independent evidence | Overall |
|---|---|---|---|---|
| V1 C: C–F–G–C | PASS | PASS | Single B3 resolves to C4; no perfect parallel in any pair | PASS |
| V2 G: G–C–D–G | PASS | PASS | F#4 is canonical and resolves to G4; final T–B spacing is exactly 19 | PASS |
| V3 F: F–Bb–C–F | PASS | PASS | Bb canonical; E4 resolves to F4; Bass C4 is inclusive maximum | PASS |
| V4 C: C–G–C | PASS | PASS | Stationary Soprano makes outer motion oblique; B3 resolves to C4 | PASS |
| V5 F: F–Bb–F | PASS | PASS | F4 common tone retained; all moving voices remain inside leap limits | PASS |

Corpus result: **V1–V5 all PASS every applicable HARD rule**.

### 6.2 No-solution corpus N1–N3

| Case | Request validity | Exhaustive failure reason | Result |
|---|---|---|---|
| N1 C / C / D4 | Canonical, key-supported, in range | Immutable Soprano D is not a C-triad member, so every candidate fails `CHORD_MEMBERSHIP` | PASS: `NO_VALID_HARMONIZATION` |
| N2 C / C–G / C4–G5 | Both pitches are supported chord members and in range | Immutable 19-semitone Soprano motion exceeds 7, so every path fails `MAX_MELODIC_LEAP` | PASS: `NO_VALID_HARMONIZATION` |
| N3 C / G–C / B4–G4 | Both pitches are supported chord members and in range | Immutable leading tone descends 4 instead of rising 1 before tonic, so every path fails `LEADING_TONE_RESOLUTION` | PASS: `NO_VALID_HARMONIZATION` |

### 6.3 Invalid, boundary, and maximum-leap fixtures

- All eight invalid vertical fixtures fail only the advertised rule.
- `VOICE_OVERLAP`, both `MAX_MELODIC_LEAP` fixtures, `PARALLEL_FIFTHS`, `PARALLEL_OCTAVES`, and `LEADING_TONE_RESOLUTION` full-transition fixtures isolate their target.
- `PARALLEL_UNISONS` correctly documents the mathematically unavoidable `VOICE_OVERLAP` co-violation and the exact combined expected set.
- All range endpoints, 12/12/19 spacing endpoints, 7-semitone upper leap, and 12-semitone Bass leap fixtures pass every applicable HARD rule.
- Claimed comfortable-range penalties on endpoint fixtures are correct.

Fixture result: **PASS**.

## 7. API success and explanation contract

For key C / chord C / Soprano C5, the normative result is Soprano C5, Alto E4, Tenor G3, Bass C3.

- All pitches are in absolute and comfortable ranges.
- Order is C5 ≥ E4 ≥ G3 ≥ C3 and all spacing limits pass.
- C/E/G are complete chord members and Bass is root.
- C is doubled exactly twice: `ROOT_DOUBLING = +2`.
- No other vertical score applies and no transition/window exists.
- Exact score: **+2**.
- Exact `selectionKey`: **`C3-G3-E4-C5`**.
- HTTP result: **`200 OK`**.

The schema requires `evaluation.events`, `evaluation.transitions`, and `evaluation.windows`, including empty arrays. Containers and their rule entries have deterministic index/Rule-ID ordering. The total score is the sum of all nonzero contributions across exactly those three containers, with no duplication. Three-event rules occur only in `windows`. Result: **PASS**.

## 8. HTTP errors, mixed validation, and operational limits

### 8.1 Error contract

| Condition | Required result | Result |
|---|---|---|
| Malformed JSON | `400 MALFORMED_JSON` | PASS |
| Structural/type/required/format/range error | `400 INVALID_REQUEST` | PASS |
| Canonical key-supported Soprano outside range | `400 INVALID_REQUEST`; field `SOPRANO_OUT_OF_RANGE` | PASS |
| Unsupported musical subset or selected-key context | `422 UNSUPPORTED_MUSICAL_ELEMENT` | PASS |
| Valid supported request with no HARD-compliant path | `422 NO_VALID_HARMONIZATION` | PASS |
| Raw body >16384 bytes | `413 REQUEST_TOO_LARGE` before JSON parsing | PASS |
| Raw body exactly 16384 bytes | accepted by body-size limit | PASS |
| Deterministic harmonization computation >2 seconds | `503 HARMONIZATION_TIMEOUT`, no partial result | PASS |

These semantics agree in PRD, Architecture, Roadmap, API, and Music Rules where applicable.

### 8.2 Mixed validation semantics

The normative sequence is unambiguous:

1. reject an oversized raw body;
2. fail fast on malformed JSON;
3. collect independently determinable category-1 structural, type, required, format, and range errors;
4. collect category-2 supported-subset/context errors only when prerequisites parsed;
5. choose 400 if any category-1 error exists, otherwise 422 for category 2;
6. run harmonization only after validation succeeds completely.

Dependent checks are suppressed when their prerequisite fails. Field ordering is known fields `key`, `chords`, `melody`; then array index ascending; then same-location code ascending; then unknown fields after known fields in lexical field-name order. Result: **PASS**.

### 8.3 Operational semantics

The 1–8 event musical limit and 16384-byte transport limit are explicitly distinct. The 2-second limit is explicitly a protective ceiling, not a latency SLO, and starts when validated input enters deterministic harmonization computation. M4 acceptance/contract tests cover bytes 16384/16385, timeout behavior, and absence of partial output. Result: **PASS**.

## 9. Cross-document consistency

| Topic | Independent comparison | Status |
|---|---|---|
| MVP vs first technical milestone | Rhythm belongs to MVP but key/chords/Soprano pitch-only processing is the narrower M1–M4 milestone | PASS |
| Supported keys | C, G, F major only | PASS |
| Supported chords | Six listed non-diminished diatonic major/minor root-position triads per key | PASS |
| Pitch grammar and key context | Global naturals/F#/Bb grammar followed by exact key set | PASS |
| Phrase length | 1–8 aligned events | PASS |
| SATB ranges and spacing | One normative set in Music Rules; other documents defer to it | PASS |
| HARD/SOFT/REWARD behavior | Reject / additive penalty / additive bonus consistently used | PASS |
| Scoring and tie-break | Higher integer sum wins; tied paths use lexicographically lowest event-major B/T/A/S pitch-number sequence | PASS |
| API statuses | 200, 400, 413, 415, 422, 503, and 500 meanings are non-conflicting | PASS |
| Explanation schema | Events, transitions, windows, ordering, reconciliation, and non-duplication agree | PASS |
| Operational limits | 16384 bytes and 2 seconds agree; ceiling is not an SLO | PASS |
| No-solution | Valid supported input may return 422 `NO_VALID_HARMONIZATION`; HARD rules are never weakened | PASS |
| Deferred work | BPM, meter, duration grammar/schema, ties/rests/pickups, mobile display depth, and other M5+ features remain explicitly deferred | PASS |

No contradiction unrelated to the historical QA report was found.

## 10. Requirements traceability for M0–M4

| Requirement | Source | Covered by | Status |
|---|---|---|---|
| Phase 0 specification set exists and is reviewable | Roadmap M0.1 | PRD, Architecture, Roadmap, API, Music Rules; this rerun | PASS |
| MVP and technical-milestone boundaries are explicit | PRD 4; Roadmap M0/M1–M4 | Cross-document review Section 9 | PASS |
| Supported notation/subset is complete | PRD FR-01–FR-05; Roadmap M0.4/M1 | Music Rules 2; pitch audit Section 3 | PASS |
| Supplied melody is immutable Soprano | PR-02, FR-03, FR-17 | `SOPRANO_PRESERVATION`; API invariant; V1–V5 | PASS |
| Valid SATB output satisfies mandatory rules | PR-03, FR-10, NFR-02 | HARD catalog; corpus audit Sections 4 and 6 | PASS |
| Deterministic output | PR-04, NFR-01 | Music Rules 8.3; API 6; Roadmap M3 | PASS |
| Actionable invalid/unsupported errors | PR-05, FR-18, NFR-07 | API 4–5; Sections 3 and 8 | PASS |
| Structured JSON contract | PR-06; Roadmap M4 | API 3–6 | PASS |
| Version and explainability metadata | PR-07, FR-15, NFR-04 | API 3.2–3.3; Architecture 7 | PASS |
| One chord per Soprano event | FR-04 | `EVENT_ALIGNMENT`; API 3.1 | PASS |
| Candidate generation uses current chord and preserves Soprano | FR-09 | Music Rules 4.2; Architecture 6.2 | PASS |
| Penalties and rewards are deterministic | FR-11 | Music Rules 7–8; scoring audit Section 5 | PASS |
| Adjacent voice leading is evaluated | FR-12 | Music Rules 7; HARD transition audit | PASS |
| Equal-score selection is stable | FR-13 | Music Rules 8.3; API selection key | PASS |
| Best valid result or explicit no-solution | FR-14 | Architecture outcomes; N1–N3 audit | PASS |
| Four aligned voice arrays | FR-16 | API success invariants; Roadmap M3/M4 | PASS |
| Pure Java domain independent of Spring | NFR-03; Roadmap M1 | Architecture boundaries/packages/testing | PASS |
| Modular monolith without speculative infrastructure | NFR-05 | Architecture 2–5; scope exclusions | PASS |
| Bounded protective processing | NFR-06; Roadmap M4 | 1–8 events, 16384 bytes, 2-second ceiling | PASS |
| Individually testable categorized rules | NFR-08; Roadmap M2 | Rule catalog and trustworthy fixtures | PASS |
| Versioned REST contract | NFR-09 | `/api/v1`; versioning rules | PASS |
| Safe correlation/timing telemetry | NFR-10 | Architecture 4.5/10; API errors | PASS |
| M1 model, parsing, range, and input-boundary tests defined | Roadmap M1 | Music Rules 2–5; API validation cases | PASS |
| M2 HARD/scoring/window rule tests defined | Roadmap M2 | Music Rules 6–10; Sections 4–6 | PASS |
| M3 valid-path, no-solution, score, explanation, and tie tests defined | Roadmap M3 | Music Rules 8/10; Architecture 6–9 | PASS |
| M4 HTTP schema/error/limit contract tests defined | Roadmap M4 | API 3–9; Section 8 | PASS |
| Prior blocking findings remediated | Original QA H-01–H-05 | Section 2 | PASS |
| Independent QA review completed | PRD AC10; Roadmap M0.6 | This report | PASS |
| Tech Lead approval to leave Phase 0 | Roadmap M0.8 | Must be recorded after successful independent QA | DEFERRED |
| BPM, meter, duration, and rhythmic schema | PRD FR-06–FR-08/FR-19; Roadmap M5 | Explicit M5 decision and acceptance gate | DEFERRED |

No blocking M1–M4 requirement is PARTIAL or DEFERRED. Tech Lead approval is a governance action following this QA result, not an unresolved specification requirement.

## 11. Open questions

| Question | Classification | Assessment |
|---|---|---|
| Canonical duration grammar, dots, ties, rests, and pickups | BLOCKS M5+ | Correctly deferred to M5 |
| Supported BPM range | BLOCKS M5+ | Correctly deferred to M5 |
| Supported meters and metric consistency | BLOCKS M5+ | Correctly deferred to M5 |
| Whether M5 keeps one chord per note or adds multiple notes per chord | BLOCKS M5+ | Correctly deferred to M5 |
| Whether rhythmic input evolves unreleased v1 or uses a new version | BLOCKS M5+ | Correctly deferred to M5 contract design |
| How much selected-path explanation the mobile UI displays | BLOCKS M5+ | Transport minimum is resolved; presentation depth is safely deferred |
| Exhaustive search versus dynamic programming | NON-BLOCKING | Implementation choice is constrained by identical deterministic musical results and benchmark evidence |
| Formal Tech Lead approval to leave Phase 0 | NON-BLOCKING | Governance step now eligible to be recorded; it does not alter M1–M4 semantics |

Nothing remains classified `BLOCKS M1`, `BLOCKS M2`, `BLOCKS M3`, or `BLOCKS M4`.

## 12. New findings

No new CRITICAL, HIGH, MEDIUM, or LOW finding was identified.

## 13. Completion assessment and recommended action

- Overall result: PASS.
- Previous findings: H-01–H-05, M-01–M-03, and L-01 all PASS.
- New findings: none.
- Corpus: V1–V5 pass every HARD rule; N1–N3 retain valid `NO_VALID_HARMONIZATION` semantics.
- API: success, exact +2 normative score, selection key, explanation containers, validation/error precedence, and operational limits all PASS.
- Remaining blockers: none for M1–M4.
- M0 closure: the specification and independent QA gates now pass. M0 can close once the Tech Lead records the approval required by Roadmap M0.8.
- Recommended next action: record Tech Lead approval to leave Phase 0, then begin M1 on a separate implementation branch. Do not begin M1 before that approval is recorded.

PHASE 0 QA: PASS
