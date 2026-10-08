# M1 Independent QA Review

## Decision

- **M1 QA: FAIL**
- Reviewed implementation branch: `feature/m1-core-music-model`
- Reviewed implementation commit: `a4a9ece28655a9d24e6aecf1694844e3de951564`
- QA branch: `qa/m1-independent-review`
- Review date: 2026-10-07
- Approval recommendation: **Do not approve M1 until the blocking findings below are fixed and independently rerun.**

This review used the approved Phase 0 specifications, including `docs/API.md` Section 5.1, as the normative source. No production code was changed, nothing was merged, and M2 was not started.

## Findings summary

| ID | Severity | Finding | Blocks M1 |
|---|---|---|---|
| H-01 | HIGH | An unparseable key suppresses an independently determinable Soprano-range error. | Yes |
| H-02 | HIGH | A selected-key-unsupported pitch suppresses an independently determinable Soprano-range error. | Yes |
| H-03 | HIGH | Aggregated field errors do not follow the normative field ordering. | Yes |
| M-01 | MEDIUM | `ValidatedHarmonizationInput` permits invalid public construction states. | Yes |
| M-02 | MEDIUM | `ValidationResult` permits contradictory public result states. | Yes |

No CRITICAL or LOW finding was identified.

## HIGH findings

### H-01 — malformed key incorrectly suppresses Soprano range validation

Reproduction:

```text
key: C major
chords: [C]
melody: [C6]
```

Expected ordered field errors:

```text
key:INVALID_KEY_FORMAT
melody[0]:SOPRANO_OUT_OF_RANGE
```

Actual field errors:

```text
key:INVALID_KEY_FORMAT
```

`C6` parses independently of the key and is above the inclusive Soprano maximum `G5`. Per `docs/API.md` Section 5.1, failure to parse the key suppresses key-context checks, but it must not suppress this range check.

Affected code: `HarmonizationInputValidator.validateMelody`. The method executes `if (key == null) continue;` before evaluating the Soprano range.

Evidence: `M1IndependentQaTest.malformedKeyDoesNotSuppressIndependentSopranoRangeError` fails.

This blocks M1 because required validation aggregation and dependency suppression are incorrect.

### H-02 — unsupported selected-key pitch incorrectly suppresses Soprano range validation

Reproduction:

```text
key: G
chords: [G]
melody: [F6]
```

Expected ordered field errors:

```text
melody[0]:SOPRANO_OUT_OF_RANGE
melody[0]:UNSUPPORTED_PITCH
```

Actual field errors:

```text
melody[0]:UNSUPPORTED_PITCH
```

The pitch parses, so both its absolute Soprano range and its selected-key pitch-class support are independently determinable. The expected same-location ordering is error code ascending.

Affected code: `HarmonizationInputValidator.validateMelody`. The range check is an `else if` branch after the key-support check.

Evidence: `M1IndependentQaTest.unsupportedPitchDoesNotSuppressIndependentSopranoRangeError` fails.

This blocks M1 because the validator does not collect all independently determinable errors and therefore cannot apply the documented category-1/category-2 precedence correctly at the later API boundary.

### H-03 — mixed aggregate errors violate normative field ordering

Reproduction:

```text
key: C major
chords: nine C chord symbols
melody: ten C4 pitches
```

Expected ordered field errors:

```text
key:INVALID_KEY_FORMAT
chords:PHRASE_TOO_LONG
chords:SEQUENCE_LENGTH_MISMATCH
melody:PHRASE_TOO_LONG
```

Actual field errors:

```text
key:INVALID_KEY_FORMAT
chords:PHRASE_TOO_LONG
melody:PHRASE_TOO_LONG
chords:SEQUENCE_LENGTH_MISMATCH
```

The contract requires known fields in `key`, `chords`, `melody` order, locations/indexes ascending, then codes ascending at the same location. The implementation appends the cross-sequence error only after both per-sequence checks, moving a `chords` error behind a `melody` error.

Affected code: `HarmonizationInputValidator.validateSequences`; the validator has no final normative error sort.

Evidence: `M1IndependentQaTest.fieldErrorsFollowNormativeFieldLocationAndCodeOrdering` fails.

This blocks M1 because deterministic ordering is explicitly normative and an M1 acceptance requirement, not merely an implementation detail.

## MEDIUM findings

### M-01 — `ValidatedHarmonizationInput` does not protect its advertised invariants

Exact public constructions currently accepted include:

```java
new ValidatedHarmonizationInput(null, List.of(), List.of());
new ValidatedHarmonizationInput(Key.C, List.of(Chord.parse("C")), List.of());
new ValidatedHarmonizationInput(Key.C, Collections.nCopies(9, Chord.parse("C")),
        Collections.nCopies(9, Pitch.parse("C4")));
```

Expected: a publicly constructible value named `ValidatedHarmonizationInput` cannot have a null key, empty or misaligned sequences, or a phrase outside 1–8 events.

Actual: its compact constructor only copies the lists. It does not require a key or enforce non-empty, aligned, bounded contents.

Affected code: `ValidatedHarmonizationInput.ValidatedHarmonizationInput`.

The contained lists are immutable and reject null elements through `List.copyOf`; the finding concerns invalid construction states, not mutation leakage.

This blocks M1 because invalid input can bypass the validator and enter downstream code as an allegedly validated domain value.

### M-02 — `ValidationResult` permits contradictory result states

Exact public constructions currently accepted include:

```java
new ValidationResult(null, List.of());
new ValidationResult(validatedInput, List.of(validationError));
```

Expected: a valid result contains exactly one validated value and no errors; an invalid result contains no value and at least one error.

Actual: the first result reports `isValid() == true` while `validatedInput()` is empty, and the second contains both success data and errors.

Affected code: `ValidationResult.ValidationResult` and `ValidationResult.isValid`.

The error list is defensively copied; the finding concerns the missing success/failure invariant.

This blocks M1 because callers cannot safely rely on the structured validation result's public state.

## Acceptance verification

### Maven project and scope

- PASS: one Maven module.
- PASS: compiler release is Java 21.
- PASS: the only declared dependency is JUnit Jupiter and it has `test` scope.
- PASS: no Spring, HTTP, JSON, persistence, Flutter, AI, or other production dependency exists.
- PASS: no candidate generation, harmonization rule implementation, scoring, voice leading, search, REST adapter, or other M2+ work was introduced.

### Pitch model

- PASS: exact canonical written pitch classes are `C D E F G A B F# Bb`.
- PASS: exact semitone mapping is correct and `C4 = 60`.
- PASS: canonical parsing round-trips.
- PASS: `H4`, `Gb4`, `A#4`, `c4`, `C 4`, `C#4`, and pitch text without an octave are rejected.
- Specification clarification: `C4` is canonical and valid in the approved documents and implementation. The review therefore treated the occurrence of `C4` in the request's invalid list as a typo rather than contradicting the normative specification.

### Keys and chords

- PASS: only C, G, and F major are supported.
- PASS: all three exact selected-key pitch sets match the approved specification and are immutable.
- PASS: validation distinguishes malformed key syntax from a syntactically valid unsupported key.
- PASS: each key exposes exactly its approved six chords.
- PASS: only canonical major/minor symbols parse; inversions, sevenths, diminished, suspended, altered, augmented, and added-note forms are rejected.
- PASS: validation distinguishes malformed chord syntax from a syntactically valid chord unsupported in the selected key.

### Voice ranges

- PASS: Soprano absolute `C4–G5`, comfortable `D4–F5`.
- PASS: Alto absolute `G3–D5`, comfortable `G3–C5`.
- PASS: Tenor absolute `C3–G4`, comfortable `C3–E4`.
- PASS: Bass absolute `E2–C4`, comfortable `G2–G3`.
- PASS: every boundary is inclusive; representative values immediately beyond absolute bounds are rejected.
- PASS: `VoiceRange` is immutable and rejects unordered or out-of-absolute comfortable ranges.

### Input validation

- PASS: null input and null fields return structured required-field errors.
- PASS: empty sequences fail.
- PASS: one event and eight events pass.
- PASS: nine events fail and unequal lengths fail.
- PASS: malformed pitch and key-sensitive unsupported pitch are distinguished.
- PASS: ordinary Soprano out-of-range input fails.
- FAIL: independent range aggregation and dependency suppression (H-01, H-02).
- FAIL: exact normative error ordering (H-03).
- PASS, critical regression: `key=C`, `chords=[C]`, `melody=[D4]` passes M1 request validation. Chord membership was not introduced as an M1 request rule.

### Immutability and invariants

- PASS: `HarmonizationInput` defensively copies non-null input lists while still permitting invalid raw values to be represented for validation.
- PASS: `ValidatedHarmonizationInput` and `ValidationResult` expose unmodifiable copied lists.
- PASS: supported key pitch sets and supported chord sets are immutable.
- PASS: `Pitch`, `Chord`, and `VoiceRange` are immutable values.
- FAIL: invalid public construction states remain in `ValidatedHarmonizationInput` and `ValidationResult` (M-01, M-02).

## Test evidence

Environment note: `mvn` was not on the shell PATH. The installed Maven executable was run directly with installed JDK 23 while Maven compiled with `<maven.compiler.release>21</maven.compiler.release>`.

Baseline implementation suite before QA tests:

```text
Tests run: 100, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Full command after adding focused QA regressions:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-23.0.2'
& 'C:\Users\DaviFerreiradaSilva\dev-tools\maven-temp\apache-maven-3.10.0\bin\mvn.cmd' test
```

Result:

```text
Tests run: 103, Failures: 3, Errors: 0, Skipped: 0
BUILD FAILURE
```

All three failures are intentional normative regression tests in `M1IndependentQaTest`, one for each HIGH finding. No implementation test regressed.

## Final assessment

The project structure, music value model, canonical notation, supported key/chord subsets, voice ranges, ordinary validation cases, critical `C/C/D4` scope regression, production dependency boundary, and M1 scope boundary are correct. M1 cannot be approved because validation aggregation/dependency behavior and exact ordering contradict the approved API contract, and the public validated/result types admit invalid states.

**M1 QA: FAIL**

**M2 was not started.**
