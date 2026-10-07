# Independent Phase 0 QA Review

## 1. Review identity and decision

- Role: independent QA Agent; the reviewer did not author the Phase 0 specifications.
- Source baseline: `origin/docs/music-rules` at commit `b552496` (`docs: align API examples with SATB rules`).
- QA branch: `docs/phase-0-qa-review`.
- Phase reviewed: M0 / Phase 0 only. No application code was reviewed or created.
- Documents reviewed: `AGENTS.md`, `docs/PRD.md`, `docs/ARCHITECTURE.md`, `docs/ROADMAP.md`, `docs/API.md`, and `docs/MUSIC_RULES.md`.
- Method: cross-document comparison plus independent pitch-number, chord-membership, interval, motion, range, scoring, fixture, and error-category calculations. Claims in examples were not treated as proof.

Phase 0 is **not ready for approval**. The five nominally valid corpus phrases do pass every HARD rule, and the central scoring and tie-break algorithms are deterministic. Approval is blocked by ambiguous supported-pitch semantics, invalid boundary fixtures, and unresolved M4 contract decisions.

## 2. Finding summary

| Severity | Count |
|---|---:|
| CRITICAL | 0 |
| HIGH | 5 |
| MEDIUM | 3 |
| LOW | 1 |

### Blocking findings

#### H-01 — Key-sensitive pitch support is contradictory and not implementable unambiguously

- Severity: HIGH
- Affected document: `docs/MUSIC_RULES.md`; `docs/API.md`
- Affected section/rule: MUSIC_RULES 2.2, 2.3, `SUPPORTED_PITCH_NOTATION`, 4.2; API 5
- Evidence: MUSIC_RULES 2.2 says G major accepts “natural pitch classes plus F#” and F major accepts “natural pitch classes plus Bb.” That wording includes F-natural in G and B-natural in F. The same bullets say F and B are rejected as substitutes where F# and Bb are required. Section 4.2 says a canonical, in-range Soprano is supported even when it is not a chord member. API 5, however, defines `UNSUPPORTED_PITCH` to include a syntactically valid pitch outside the key context.
- Impact: `F4` in G major and `B4` in F major do not have one defined outcome. An implementation could reasonably classify either as supported non-chord input leading to `NO_VALID_HARMONIZATION`, or as `UNSUPPORTED_PITCH` leading to `UNSUPPORTED_MUSICAL_ELEMENT`. This blocks M1 parsing, M3 no-solution behavior, and M4 error mapping. It also prevents a conclusive guarantee that unsupported enharmonic/key spellings cannot enter the rule set.
- Expected behavior: explicitly enumerate the accepted pitch classes per selected key and define whether an in-range diatonic/non-diatonic Soprano spelling is supported input, unsupported input, or a supported no-solution case. Preserve N1 as the explicit supported non-chord case.
- Recommended correction: replace “naturals plus” prose with exact per-key accepted sets and add transport examples for at least G/F-natural and F/B-natural, plus `Gb`, `A#`, `F#`, and `Bb` cases.

#### H-02 — Two documented passing boundary fixtures violate `VOICE_RANGE`

- Severity: HIGH
- Affected document: `docs/MUSIC_RULES.md`
- Affected section/rule: 10.4 Boundary fixtures; `VOICE_RANGE`; `ADJACENT_VOICE_SPACING`
- Evidence:
  - “Highest Tenor” uses `S=G5/A=E5/T=G4/B=C3`. Alto `E5` is pitch 76, above the inclusive Alto maximum `D5` = 74.
  - “Maximum A–T spacing” uses `S=G5/A=E5/T=E4/B=C3`. Alto `E5` is again outside the absolute Alto range.
- Impact: both fixtures claim a passing result while violating an unrelated HARD rule. M1 boundary tests and M2 isolated rule tests cannot adopt the corpus as written. ROADMAP M2 requires boundary contributions and HARD examples to be correct.
- Expected behavior: every passing boundary fixture must satisfy every other HARD rule while exercising equality at the target boundary.
- Recommended correction: replace each sonority with a fully valid sonority that retains the intended Tenor or spacing boundary, then recalculate all other rules.

#### H-03 — The HTTP status for a supported-format, out-of-range Soprano remains unresolved

- Severity: HIGH
- Affected document: `docs/API.md`; `docs/MUSIC_RULES.md`; `docs/ARCHITECTURE.md`
- Affected section/rule: API 5 and 10.3; MUSIC_RULES 11; Architecture decision gate 6
- Evidence: API 5 maps numeric bounds to `400 INVALID_REQUEST` and lists `SOPRANO_OUT_OF_RANGE`, but API 10.3 still asks whether the result is 400 or 422. MUSIC_RULES 11 marks the status “DEFERRED TO TECH LEAD.” Architecture 12 requires API error semantics to be approved before implementation.
- Impact: M4 contract tests cannot assert one normative status, and clients cannot rely on stable transport semantics.
- Expected behavior: one normative status/application-code/field-code tuple for a syntactically supported pitch outside the Soprano absolute range.
- Recommended correction: resolve the open question and remove the conflicting “proposal/deferred” wording from all documents.

#### H-04 — Required M4 operational limits have no values

- Severity: HIGH
- Affected document: `docs/API.md`; `docs/ARCHITECTURE.md`; `docs/MUSIC_RULES.md`
- Affected section/rule: API 7 and 10.2; Architecture 10–12; MUSIC_RULES 11
- Evidence: API 7 says Phase 0 “must assign” maximum JSON body size and processing timeout before implementation, but provides neither value. API 10 keeps both open. MUSIC_RULES defers both to the Tech Lead. Architecture requires request-size enforcement and finalization of notation and limits before implementation.
- Impact: M4 has no testable request-size or timeout boundary and therefore cannot satisfy its own security/limits contract without a post-M0 design decision.
- Expected behavior: numeric limits and their exact HTTP/application error mapping are fixed before M4 starts, or the Phase 0 gate explicitly and consistently moves them to a named pre-M4 decision gate with acceptance criteria.
- Recommended correction: record values, enforcement semantics, and contract tests, or revise every gate consistently so the decision is demonstrably non-blocking for M4.

#### H-05 — The REST success schema does not define transition and three-event explanations

- Severity: HIGH
- Affected document: `docs/API.md`; `docs/PRD.md`; `docs/ARCHITECTURE.md`
- Affected section/rule: API 3.2 and 6; PRD FR-15/PR-07; Architecture 7 and decision gate 6
- Evidence: the normative response contains only `evaluation.events[].rules`. The rule set includes transition rules and three-event-window rules, but the API supplies no `transitions`/window shape or normative placement rule. API 6 promises ordered rule outcomes and score contributions; PRD FR-15 requires sufficient selected-path explanation; Architecture 7 requires per-event or per-transition identity and contribution.
- Impact: two conforming M4 implementations could serialize the same transition contribution in incompatible locations, and contract tests cannot verify the promised explanation payload.
- Expected behavior: define a stable schema and ordering for vertical, adjacent-transition, and three-event-window contributions, including the index convention.
- Recommended correction: add a normative multi-event response or schema fragment covering all three scopes before approving the API field-shape gate.

### Non-blocking findings that still require correction

#### M-01 — Several invalid examples are not isolated from unrelated HARD failures

- Severity: MEDIUM
- Affected document: `docs/MUSIC_RULES.md`
- Affected section/rule: 6.5, 6.6, 6.8, 10.3
- Evidence:
  - `CHORD_MEMBERSHIP` invalid example `D4/C4/G3/C3` on C also fails `COMPLETE_TRIAD` because E is absent.
  - `COMPLETE_TRIAD` invalid example `G4/G3/C3/C2` also fails `VOICE_RANGE` because Bass C2 = 36 is below E2 = 40.
  - `LEADING_TONE_DOUBLING` invalid example `B4/G4/D4/B2` on G also fails `ROOT_POSITION_BASS` because Bass is B rather than G.
  - The `PARALLEL_OCTAVES` corpus fixture also creates Bass/Alto parallel fifths: C3/G3 to D3/A3.
  - The `PARALLEL_UNISONS` fixture also creates parallel octaves for Alto/Bass and Tenor/Bass, and its displayed sonorities are incomplete if interpreted as full chord candidates.
- Expected behavior: each advertised isolated fixture passes all non-target HARD rules, or explicitly documents the complete expected violation set.
- Recommended correction: replace contaminated examples/fixtures with isolated ones and retain multi-failure cases separately for combined-validation tests.

#### M-02 — The maximum-leap invalid examples use unsupported pitch notation and multiple unrelated leaps

- Severity: MEDIUM
- Affected document: `docs/MUSIC_RULES.md`
- Affected section/rule: 7.2 and 10.3 `MAX_MELODIC_LEAP`
- Evidence: Section 7.2 uses `G#4` and `C#4`, neither of which is accepted by the canonical initial pitch grammar. The corpus fixture moves Soprano 19, Alto 16, and Tenor 10 semitones and also violates `VOICE_OVERLAP`, rather than isolating one maximum-leap failure.
- Expected behavior: a supported, canonical pair exceeds exactly one voice-specific maximum while all other applicable HARD transition rules pass.
- Recommended correction: use canonical pitches from one supported key/chord context and isolate upper-voice and Bass overflow separately.

#### M-03 — Multi-error validation precedence and ordering are under-specified

- Severity: MEDIUM
- Affected document: `docs/API.md`; `docs/ARCHITECTURE.md`; `docs/MUSIC_RULES.md`
- Affected section/rule: API 2, 4–5; Architecture 8; MUSIC_RULES 12
- Evidence: Architecture permits multiple validation errors; API says error ordering is deterministic but defines no ordering key and no precedence when syntax, key context, and range fail together. MUSIC_RULES specifies Rule ID order for combined HARD violations, but that does not define field-error order or the envelope application code when errors span 400 and 422 categories.
- Expected behavior: deterministic field traversal/order and an explicit envelope/status precedence rule for mixed validation failures.
- Recommended correction: specify whether validation is fail-fast or accumulative, the stable ordering tuple, and the status/application code chosen for mixed categories.

#### L-01 — Parallel-motion definition points to the wrong sections

- Severity: LOW
- Affected document: `docs/MUSIC_RULES.md`
- Affected section/rule: 3.3
- Evidence: the definition says parallel perfect motion is governed by Sections 6.4–6.6, which are spacing, chord membership, and complete triad. The actual rules are Sections 7.10–7.12.
- Expected behavior: the cross-reference points to the parallel-fifths, parallel-octaves, and parallel-unisons rules.
- Recommended correction: update only the section references.

## 3. Cross-document consistency matrix

| Topic | PRD | Architecture | Roadmap | API | Music rules | QA result |
|---|---|---|---|---|---|---|
| First MVP | Key, BPM, meter, chords, melody, durations; SATB + explanation | Modular monolith, REST and mobile later, no persistence | M1–M7 culminate in rhythmic mobile MVP | Rhythm added at M5 | Pitch-only rules explicitly limited to technical milestone | PASS |
| First technical milestone | Key/chords/aligned Soprano; deterministic SATB JSON | Pure domain search and outcomes | M1–M3 domain, M4 transport | Key/chords/melody arrays | One chord + immutable Soprano per event | PASS |
| Supported keys | Delegated to rules | Delegated to rules | Approved subset required | Delegated to rules | C, G, F major | PASS |
| Supported chords | Delegated to rules | Listed subset only | Round-trip approved subset | Unsupported constructs are 422 | Six non-diminished diatonic triads/key | PASS |
| Canonical pitch notation | Delegated to rules | Parse once; reject unsupported enharmonics | Round-trip required | Case-sensitive canonical notation | SPN grammar and spelling rules | **FAIL — H-01** |
| Phrase length | 1–8 | Bounded short input | Explicit limits required | 1–8 | 1–8 inclusive | PASS |
| SATB ranges | Delegated to rules | Domain invariant/tests | Boundaries required | Soprano validated; ranges delegated | Exact absolute/comfortable ranges | PASS definition; **fixtures fail H-02** |
| Error semantics | Invalid/unsupported/no-solution distinct | Three outcomes | 400/422 contract tests | Status table plus open status question | Input HARD vs candidate HARD | **FAIL — H-01/H-03/M-03** |
| No-solution | Supported valid input with no path | Explicit domain outcome | M3/M4 gate | 422 | HARD rules never weakened | PASS |
| Deterministic scoring | Required | Integer stable selection | Repeated-run gate | Score deterministic | Integer additive, higher wins | PASS |
| Tie-breaking | Stable documented order | Delegated to rules | Tie tests required | Selection key deterministic | Event-major B/T/A/S numeric lexicographic | PASS |
| API statuses | Deferred to API | API owns mapping | 200, 400/422, no-solution 422 | Status table | Out-of-range status deferred | **FAIL — H-03** |
| Deferred features | Rhythm later; exclusions explicit | Persistence/cloud/auth deferred | M5 rhythm, M6 mobile | M5 schema provisional | Rhythm outside this rule set | PASS |
| Milestone gates | Phase 0 approval first | Six pre-implementation gates | M0 then M1–M7 | Limits/status still open | Some transport decisions deferred | **FAIL — H-03/H-04/H-05** |

No contradiction was found in the declared first-MVP versus technical-milestone product scope, the three supported keys, chord lists, 1–8 phrase length, SATB range values, no-solution concept, scoring direction, tie-break order, or deferred product features. The contradictions/ambiguities are those explicitly identified above.

## 4. Independent musical-subset validation

### 4.1 Keys, scales, and leading tones

| Key | Independently calculated major scale | Leading tone | Result |
|---|---|---|---|
| C | C D E F G A B | B | PASS |
| G | G A B C D E F# | F# | PASS |
| F | F G A Bb C D E | E | PASS |

F# is required by G major and maps to pitch class 6. Bb is required by F major and maps to pitch class 10. `Gb` and `A#` are not canonical substitutes. The exact treatment of F-natural in G and B-natural in F remains blocked by H-01.

### 4.2 Supported chord audit

| Key | Chord | Independently calculated members | Diatonic/quality/spelling |
|---|---|---|---|
| C | C | C E G | PASS — major |
| C | Dm | D F A | PASS — minor |
| C | Em | E G B | PASS — minor |
| C | F | F A C | PASS — major |
| C | G | G B D | PASS — major |
| C | Am | A C E | PASS — minor |
| G | G | G B D | PASS — major |
| G | Am | A C E | PASS — minor |
| G | Bm | B D F# | PASS — minor; F# canonical |
| G | C | C E G | PASS — major |
| G | D | D F# A | PASS — major; F# canonical |
| G | Em | E G B | PASS — minor |
| F | F | F A C | PASS — major |
| F | Gm | G Bb D | PASS — minor; Bb canonical |
| F | Am | A C E | PASS — minor |
| F | Bb | Bb D F | PASS — major; Bb canonical |
| F | C | C E G | PASS — major |
| F | Dm | D F A | PASS — minor |

All 18 listed chords are diatonic major/minor triads and none is diminished. The excluded vii° triads are Bdim in C, F#dim in G, and Edim in F.

## 5. SATB range validation

Pitch numbers use `12 × (octave + 1) + pitchClass`. Every published endpoint mapping is correct and inclusive.

| Voice | Absolute lower | One semitone below | Absolute upper | One semitone above | Comfortable range | Result |
|---|---:|---:|---:|---:|---|---|
| Soprano | C4 = 60 | B3 = 59 | G5 = 79 | G#5/Ab5 = 80 | D4 = 62 through F5 = 77 | PASS mapping |
| Alto | G3 = 55 | F#3/Gb3 = 54 | D5 = 74 | D#5/Eb5 = 75 | G3 = 55 through C5 = 72 | PASS mapping |
| Tenor | C3 = 48 | B2 = 47 | G4 = 67 | G#4/Ab4 = 68 | C3 = 48 through E4 = 64 | PASS mapping |
| Bass | E2 = 40 | D#2/Eb2 = 39 | C4 = 60 | C#4/Db4 = 61 | G2 = 43 through G3 = 55 | PASS mapping |

The numeric range model is objective. Some adjacent semitones have no supported canonical spelling in the rule-set grammar, so parser tests and numeric domain-invariant tests must be separated. Comfortable endpoints are inclusive; only values strictly outside the comfortable interval and still inside the absolute interval receive −1. H-02 records the invalid published boundary fixtures.

## 6. HARD-rule validation

### 6.1 Vertical HARD rules

| Rule | Objective and implementable | Valid example passes | Invalid example fails target | Isolated from other HARD rules | QA result |
|---|---|---|---|---|---|
| `SOPRANO_PRESERVATION` | Yes: exact spelling and octave equality | Yes | Yes | Yes when interpreted on C | PASS |
| `VOICE_RANGE` | Yes: inclusive numeric intervals | Yes, as individual boundaries | Yes | Yes as individual voice checks | PASS |
| `VOICE_CROSSING` | Yes: S ≥ A ≥ T ≥ B | Yes | Yes | Yes | PASS |
| `ADJACENT_VOICE_SPACING` | Yes: ≤12, ≤12, ≤19 | Yes | Yes | Pair examples only | PASS |
| `CHORD_MEMBERSHIP` | Yes: pitch-class set membership | Yes | Yes | No; definition example also lacks third | PARTIAL — M-01 |
| `COMPLETE_TRIAD` | Yes: all three classes present | Yes | Yes | No; definition example has Bass below range | PARTIAL — M-01 |
| `ROOT_POSITION_BASS` | Yes: Bass class equals root | Yes | Yes | Pair-level example; corpus fixture isolates it | PASS |
| `LEADING_TONE_DOUBLING` | Yes: count ≤1 | Yes | Yes | No; definition example also has non-root Bass | PARTIAL — M-01 |

Equality in `VOICE_CROSSING` is explicitly legal. Consequently a unison between adjacent voices is vertically legal but may become illegal across a transition only under the separate `PARALLEL_UNISONS` conditions.

### 6.2 Voice-leading HARD rules

| Rule | Independent semantic check | Example/fixture assessment | QA result |
|---|---|---|---|
| `VOICE_OVERLAP` | For each adjacent pair, new Lower ≤ previous Upper and new Upper ≥ previous Lower; equality legal | Definition example is correct; corpus fixture also violates the target | PASS |
| `MAX_MELODIC_LEAP` | Absolute motion ≤7 for S/A/T and ≤12 for Bass; equality legal | Valid boundaries correct; invalid examples/fixture are contaminated | PARTIAL — M-02 |
| `PARALLEL_FIFTHS` | All six unordered voice pairs; both move same nonzero direction; start and end class 7 | 7 and 19 are both fifth class; stationary voice does not trigger | PASS definition; fixture isolates cited pair |
| `PARALLEL_OCTAVES` | All six pairs; both move same nonzero direction; both distances positive multiples of 12 | 12 and 24 qualify; zero does not; stationary voice does not trigger | PASS definition; fixture not isolated (M-01) |
| `PARALLEL_UNISONS` | All six pairs; same nonzero direction; both distances exactly zero | Unison is not an octave because octave requires positive distance | PASS definition; fixture not isolated (M-01) |
| `LEADING_TONE_RESOLUTION` | Only when next chord is tonic; every prior leading tone must move +1 | Definition and corpus failure correctly apply exact upward semitone | PASS |

Direction semantics are objective: multiplication/sign equivalence is not enough unless both directed motions are nonzero and have the same sign. Compound intervals are handled correctly by modulo-12 fifth class and positive-multiple-of-12 octave tests.

## 7. SOFT and REWARD scoring validation

All published numeric weights and single-rule arithmetic are internally consistent:

| Rule | Recalculated contribution | Cumulative/once semantics | Result |
|---|---:|---|---|
| `COMFORTABLE_RANGE` | −1 per outside-comfortable voice/event | Independent per voice/event | PASS |
| `THIRD_DOUBLING` | −2 | Once per event | PASS |
| `FIFTH_DOUBLING` | −1 | Once per event | PASS |
| `ROOT_DOUBLING` | +2 | Once per event | PASS |
| `MELODIC_LEAP_PENALTY` | 0, −1, or −2 by voice/size table | Per voice/transition | PASS |
| `SIMILAR_OUTER_MOTION` | −1 | Once per transition | PASS |
| `HIDDEN_DIRECT_FIFTH` | −2 | Once; not scored after parallel-fifth rejection | PASS |
| `HIDDEN_DIRECT_OCTAVE` | −2 | Once; not scored after parallel-octave rejection | PASS |
| `REPEATED_DIRECTION_LEAPS` | −2 | Per voice/window, when third event arrives | PASS |
| `LARGE_LEAP_COMPENSATION` | −2 | Per voice/window, when third event arrives; no end-of-phrase penalty | PASS |
| `STEPWISE_MOTION` | +1 | Per voice/transition | PASS |
| `REPEATED_NOTE` | +1 | Per voice/transition | PASS |
| `COMMON_TONE_RETENTION` | +1 | Alto/Tenor only, cumulative with repeated note | PASS |
| `CONTRARY_OUTER_MOTION` | +1 | Once per transition | PASS |
| `OBLIQUE_OUTER_MOTION` | +1 | Once per transition | PASS |

`REPEATED_NOTE + COMMON_TONE_RETENTION` is intentionally +2 for a qualifying retained Alto/Tenor common tone. Under the universal additive convention, `SIMILAR_OUTER_MOTION` and a qualifying hidden/direct interval total −3; and both three-event penalties may apply to the same voice/window if both predicates are true. Those combinations are deterministic, not accidental. Each three-event window `(i-2, i-1, i)` is evaluated exactly once when event `i` is added.

No incorrect claimed numerical score was found. The API score is independently confirmed in Section 12.

## 8. Deterministic selection validation

The selection algorithm defines a total deterministic order:

1. Reject all invalid complete paths.
2. Select the highest integer total score.
3. Flatten each tied path in event order.
4. Within every event append numeric Bass, Tenor, Alto, Soprano values.
5. Lexicographically compare the flattened integer sequences; the lower first differing value wins.

The exact sequence is event-major `B, T, A, S`. Numeric pitch values are totally ordered integers. Equal flattened sequences describe the same pitches; explanation ordering then uses event index, `VERTICAL` before `VOICE_LEADING`, and Rule ID ascending. No selection behavior depends on `HashMap`/`Set` iteration, enumeration order, object identity, threads, randomness, time, or floating point.

Result: PASS.

## 9. Independent validation of V1–V5

Legend: each column reports the named check over every applicable event or transition in the phrase.

| Phrase | Spelling | Membership / complete / root Bass | Leading-tone count | Ranges / order / spacing | Overlap | Leaps | Parallel 5/8/1 | Leading-tone resolution | Overall HARD result |
|---|---|---|---|---|---|---|---|---|---|
| V1 C: C–F–G–C | PASS | PASS | PASS | PASS | PASS | PASS | PASS | PASS (B3→C4) | PASS |
| V2 G: G–C–D–G | PASS, F# canonical | PASS | PASS | PASS; final T–B = 19 | PASS | PASS | PASS | PASS (F#4→G4) | PASS |
| V3 F: F–Bb–C–F | PASS, Bb canonical | PASS | PASS | PASS; Bass C4 endpoint | PASS | PASS | PASS | PASS (E4→F4) | PASS |
| V4 C: C–G–C | PASS | PASS | PASS | PASS | PASS | PASS | PASS; stationary Soprano is not parallel motion | PASS (B3→C4) | PASS |
| V5 F: F–Bb–F | PASS, Bb canonical | PASS | PASS | PASS | PASS | PASS | PASS | Not triggered | PASS |

Independent interval review covered all six unordered pairs on every transition. No supposedly valid V1–V5 phrase violates a HARD rule.

## 10. Invalid-fixture review

### 10.1 Vertical fixtures

The eight fixtures in Section 10.2 each fail their stated target. Unlike three definition-level examples noted in M-01, the Section 10.2 fixtures for `CHORD_MEMBERSHIP`, `COMPLETE_TRIAD`, and `LEADING_TONE_DOUBLING` were improved and do isolate their targets. The Section 10.2 set is therefore PASS.

### 10.2 Voice-leading fixtures

| Fixture | Stated rule fails | Unrelated HARD behavior | Result |
|---|---|---|---|
| `VOICE_OVERLAP` | Yes | Other displayed transition limits pass; chord context omitted | PASS target |
| `MAX_MELODIC_LEAP` | Yes | Also S/A/T excessive leaps and overlap; unsupported accidentals occur in definition examples | FAIL isolation |
| `PARALLEL_FIFTHS` | Yes | C→Dm interpretation makes both sonorities and other transition checks legal | PASS isolation |
| `PARALLEL_OCTAVES` | Yes | Also Bass/Alto parallel fifths | FAIL isolation |
| `PARALLEL_UNISONS` | Yes | Also parallel octaves; displayed full sonorities are incomplete | FAIL isolation |
| `LEADING_TONE_RESOLUTION` | Yes | Other HARD checks pass for G→C in C major | PASS isolation |

## 11. Boundary and off-by-one validation

| Boundary | Required semantics | Independent result |
|---|---|---|
| Absolute range endpoints | Inclusive | PASS numerically |
| One semitone outside endpoints | Reject | PASS definition; canonical-parser isolation must be separate |
| S–A = 12 | Legal | PASS fixture |
| A–T = 12 | Legal | **Fixture FAILS unrelated Alto range (H-02)** |
| T–B = 19 | Legal | PASS fixture |
| Upper-voice leap = 7 | Legal, −2 | PASS |
| Bass leap = 12 | Legal, −2 | PASS |
| Adjacent voices equal | Legal vertically | PASS (`T=B=C4` boundary) |
| New adjacent voice equals other voice's previous pitch | Legal overlap boundary | PASS definition |
| Comfortable endpoints | No penalty at inclusive endpoint | PASS definition |
| Highest Tenor G4 | Legal | **Fixture FAILS unrelated Alto range (H-02)** |

There is no off-by-one defect in the written comparison operators. There are fixture-construction defects.

## 12. NO_VALID_HARMONIZATION validation and API contract

### 12.1 N1–N3

| Case | Syntax/subset/range | Why every path fails | Correct category |
|---|---|---|---|
| N1 C / C / D4 | Valid, supported, in range | Immutable D4 is not in C triad; every vertical candidate fails `CHORD_MEMBERSHIP` | `NO_VALID_HARMONIZATION` |
| N2 C / C–G / C4–G5 | Valid, supported, both chord members and in range | Immutable Soprano motion is 19 > 7; every transition fails `MAX_MELODIC_LEAP` | `NO_VALID_HARMONIZATION` |
| N3 C / G–C / B4–G4 | Valid, supported, both chord members and in range | Prior leading tone B4 moves −4 rather than +1 to C5 before tonic | `NO_VALID_HARMONIZATION` |

All three are domain no-solution cases, not `INVALID_REQUEST` or `UNSUPPORTED_MUSICAL_ELEMENT`. N1 in particular must remain no-solution.

### 12.2 Normative API success example

For C major / chord C / Soprano C5, response `C5/E4/G3/C3` in S/A/T/B order independently passes all HARD rules. All voices are comfortable. The triad is complete and root-position. C occurs exactly twice, so `ROOT_DOUBLING = +2`; no other vertical score fires and no transition exists. Total score is exactly `+2`. The event-major B/T/A/S key is exactly `C3-G3-E4-C5`. `200 OK` is correct for synchronous successful computation.

### 12.3 Error categories

| Example/category | Expected transport/domain result | QA assessment |
|---|---|---|
| `H4` | 400 `INVALID_REQUEST` / `INVALID_PITCH_FORMAT` | PASS |
| Canonical syntax but disallowed enharmonic/key spelling | 422 `UNSUPPORTED_MUSICAL_ELEMENT` / `UNSUPPORTED_PITCH` | Ambiguous for some naturals — H-01 |
| Canonical Soprano outside absolute range | `SOPRANO_OUT_OF_RANGE` | Status unresolved — H-03 |
| Unsupported key/chord/construct | 422 `UNSUPPORTED_MUSICAL_ELEMENT` with stable field code | PASS at category level |
| Supported valid input with no HARD-valid path | 422 `NO_VALID_HARMONIZATION` | PASS |

The distinction between malformed syntax, unsupported musical content, range failure, and supported no-solution is conceptually sound, but H-01, H-03, and M-03 prevent complete contract approval.

## 13. Requirements traceability for M0–M4

| Requirement | Source | Covered by | Status |
|---|---|---|---|
| Explicit short-passage input | PR-01 | PRD 4/6; MUSIC_RULES 2.5; API 3 | PASS |
| Preserve melody as Soprano | PR-02, FR-03, FR-17 | `SOPRANO_PRESERVATION`; API invariant; V1–V5 | PASS |
| Produce valid A/T/B | PR-03 | MUSIC_RULES HARD catalog; corpus; Architecture search | PASS |
| Deterministic result | PR-04, NFR-01 | MUSIC_RULES 8.3; API 6; Roadmap M3 | PASS |
| Actionable invalid/unsupported errors | PR-05, FR-18, NFR-07 | API 4–5; Architecture 8 | **PARTIAL — H-01/H-03/M-03** |
| Structured JSON | PR-06 | API 3; Roadmap M4 | PASS |
| Versions and explanation | PR-07, FR-15, NFR-04 | Architecture 7; API 3.2/6 | **PARTIAL — H-05** |
| Canonical supported key | FR-01 | MUSIC_RULES 2.1/2.3/2.6 | PASS |
| Ordered supported chords | FR-02 | MUSIC_RULES 2.4/2.6 | PASS |
| Ordered Soprano pitch sequence | FR-03 | MUSIC_RULES 2.5; API 3 | PASS |
| One chord per event | FR-04 | `EVENT_ALIGNMENT`; API indexing | PASS |
| Syntax/value/alignment/range validation | FR-05 | Input rules; ranges; API errors | **PARTIAL — H-01/H-03** |
| Generate chord-member candidates | FR-09 | MUSIC_RULES 4.2; Architecture 6.2 | PASS |
| Reject every HARD violation | FR-10, NFR-02 | Scoring order; catalog; acceptance notes | PASS |
| Deterministic penalties/rewards | FR-11 | MUSIC_RULES 8 | PASS |
| Adjacent-event voice leading | FR-12 | MUSIC_RULES 7 | PASS |
| Stable tie-break | FR-13 | MUSIC_RULES 8.3 | PASS |
| Best result or no-solution | FR-14 | Architecture outcomes; MUSIC_RULES 4.2/10.5 | PASS |
| Four aligned voice sequences | FR-16 | API invariant; Roadmap M3 | PASS |
| Plain-Java testability | NFR-03 | Architecture boundaries/testing; Roadmap M1–M3 | PASS |
| Modular monolith/no speculative infra | NFR-05 | Architecture 2–5 | PASS |
| Explicit bounded work | NFR-06 | 1–8 event limit; benchmark gate | PASS for M1–M3; **PARTIAL for M4 — H-04** |
| Individually testable categorized rules | NFR-08 | Rule catalog; Roadmap M2 | **PARTIAL — H-02/M-01/M-02 fixture evidence** |
| Versioned REST contract | NFR-09 | `/api/v1`; rule-set versioning | PASS |
| Correlation/timing without payload logs | NFR-10 | Architecture 4.5/10; API envelopes | PASS specification |
| Homorhythmic one-to-one events | PRD constraint | PRD 8; MUSIC_RULES 1/2.5 | PASS |
| No HARD-rule weakening | PRD constraint/AC | PRD 8; MUSIC_RULES 1/4.2 | PASS |
| Invalid/boundary/no-solution tests | PRD AC 7–8 | MUSIC_RULES corpus; API 9; Roadmap M1–M4 | **PARTIAL — H-02/M-01/M-02** |
| API mapping for M4 | PRD AC 9 | API; Roadmap M4 | **PARTIAL — H-03/H-04/H-05** |
| Independent Music Theory + QA review | PRD AC 10 | MUSIC_RULES status; this report | PARTIAL pending correction/re-review |

No blocking requirement is silently marked PASS. Later-MVP rhythm requirements FR-06–FR-08 and FR-19 are explicitly DEFERRED to M5 and are not blockers for M1–M4.

## 14. Open and deferred decisions

| Decision/question | Classification | QA assessment |
|---|---|---|
| Exact accepted pitch classes per key, including F-natural in G and B-natural in F | BLOCKS M1 | Unresolved/ambiguous (H-01) |
| Out-of-range Soprano HTTP status and envelope mapping | BLOCKS M4 | Unresolved (H-03) |
| Maximum JSON body size | BLOCKS M4 | No value (H-04) |
| Processing timeout and timeout response | BLOCKS M4 | No value (H-04) |
| Transition/three-event explanation JSON shape | BLOCKS M4 | Undefined (H-05) |
| Mixed validation error aggregation, status precedence, and ordering | BLOCKS M4 | Under-specified (M-03) |
| Search implementation: exhaustive vs dynamic programming | BLOCKS M3 only after benchmark if performance requires a choice | Safely assigned to implementation; musical result constrained |
| Duration grammar | BLOCKS M5+ | Correctly deferred |
| BPM range | BLOCKS M5+ | Correctly deferred |
| Meter, ties, rests, pickups, multiple notes/chord | BLOCKS M5+ | Correctly deferred |
| Rhythmic v1 evolution/new version | BLOCKS M5+ | Correctly deferred |
| Mobile-facing explanation depth | BLOCKS M5+ | Diagnostic minimum exists; UI depth correctly deferred |
| Persistence/cloud/auth/export/advanced UI | NON-BLOCKING | Correctly out of scope/deferred |

The unresolved items required for M1 and M4 mean ROADMAP M0 acceptance criteria 4, 5, and 7 are not satisfied. Tech Lead approval to leave Phase 0 has also not been evidenced in the reviewed documents.

## 15. Completion assessment and recommended action

- Overall result: FAIL.
- Findings: 0 CRITICAL, 5 HIGH, 3 MEDIUM, 1 LOW.
- Invalid supposedly-valid fixtures: none among V1–V5; two Section 10.4 passing boundary fixtures are invalid because Alto E5 exceeds the Alto maximum.
- Scoring inconsistencies: none in claimed arithmetic; cumulative overlaps are deterministic. Fixture defects still weaken scoring/rule test evidence.
- API inconsistencies: unresolved out-of-range status, undefined mixed-error precedence/order, missing transition/window explanation schema, and unset body-size/timeout limits.
- Blocking unresolved questions: exact key-sensitive pitch acceptance; out-of-range transport mapping; operational limits; full explanation schema; mixed-error semantics.
- Can Phase 0 leave M0? No.
- Recommended next action: the owning Tech Lead and Music Theory Agent should correct the five HIGH findings and the contaminated fixtures in the source specifications, record the M4 decisions, and request a fresh independent QA review. Do not begin M1 application implementation before the corrected Phase 0 set passes.

PHASE 0 QA: FAIL
