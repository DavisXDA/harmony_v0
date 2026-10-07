# Harmony Initial SATB Music Rules

## 1. Status and purpose

- Phase: Phase 0 — Product and Architecture Definition
- Rule-set identifier: `satb-initial-1`
- Status: Proposed for Music Theory and QA review
- Scope: first technical milestone only

This document is the normative musical specification for the first deterministic SATB harmonization engine. It defines a deliberately small hymn-harmony subset. Every generated result must satisfy every HARD rule. The engine must return `NO_VALID_HARMONIZATION` when no complete path satisfies all HARD rules; it must never relax a HARD rule.

The input melody is preserved as Soprano. Each event contains one chord and one Soprano pitch. Output is homorhythmic, with one Soprano, Alto, Tenor, and Bass pitch per event.

Normative words:

- **HARD**: violation rejects the input, candidate, transition, or complete path.
- **SOFT**: violation remains legal and adds the stated negative integer score.
- **REWARD**: satisfaction adds the stated positive integer score.

## 2. Initial supported musical subset

### 2.1 Supported keys and modes

The only supported keys are `C`, `G`, and `F`, all in major mode.

| Key | Scale pitch classes | Leading tone |
|---|---|---|
| `C` | C, D, E, F, G, A, B | B |
| `G` | G, A, B, C, D, E, F# | F# |
| `F` | F, G, A, Bb, C, D, E | E |

Minor keys and modal keys are unsupported. A key string other than exactly `C`, `G`, or `F` fails `SUPPORTED_KEY`.

### 2.2 Accidentals and pitch spelling

The supported written pitch classes are `C`, `D`, `E`, `F`, `G`, `A`, `B`, `F#`, and `Bb`. `#` means one semitone above the natural letter and `b` means one semitone below it. Double accidentals and natural signs are unsupported.

Spelling is key-sensitive:

- `C` major accepts natural pitch classes only;
- `G` major accepts natural pitch classes plus `F#`, and rejects `Gb` and `F` as substitutes for F# where F# is required;
- `F` major accepts natural pitch classes plus `Bb`, and rejects `A#` and `B` as substitutes for Bb where Bb is required.

Enharmonic spellings not listed above are unsupported even when they map to the same semitone.

### 2.3 Canonical notation

- Pitch: scientific pitch notation matching `^(C|D|E|F|G|A|B|F#|Bb)[0-9]+$`, then restricted by the selected key and voice range. Middle C is `C4`.
- Key: exactly `C`, `G`, or `F`.
- Major chord: root only, for example `C`, `F`, `G`, `Bb`.
- Minor chord: root followed immediately by lowercase `m`, for example `Am`, `Dm`, `Em`.
- Chord symbols contain no spaces, slash, inversion suffix, figured bass, added tone, or octave.

Although the pitch grammar permits multiple octave digits, the initial voice ranges make only octaves 2 through 5 usable.

### 2.4 Supported chords

Only the six non-diminished diatonic triads in each supported major key are accepted.

| Key | Supported chords |
|---|---|
| `C` | `C`, `Dm`, `Em`, `F`, `G`, `Am` |
| `G` | `G`, `Am`, `Bm`, `C`, `D`, `Em` |
| `F` | `F`, `Gm`, `Am`, `Bb`, `C`, `Dm` |

Supported qualities are major triad and minor triad only. Every chord is root position. Inversions, seventh chords, diminished chords, augmented chords, suspended chords, power chords, added-note chords, altered chords, secondary dominants, borrowed chords, and chromatic chords are unsupported.

Non-chord tones are unsupported. Every Soprano and generated pitch at an event must be a member of that event's triad. Passing tones, neighbor tones, suspensions, anticipations, appoggiaturas, escape tones, pedal tones outside the current chord, and accented non-chord tones are therefore unsupported.

### 2.5 Input size and alignment

- A phrase contains 1 through 8 events inclusive.
- `chords.length` must equal `melody.length`.
- Event `i` pairs `chords[i]` with `melody[i]`.
- The Soprano pitch is immutable output data, not a suggestion.

The eight-event limit bounds initial search complexity and is part of `satb-initial-1`. BPM, meter, and duration limits belong to M5 and are not defined by this pitch-only rule set.

### 2.6 Input rules

#### `SUPPORTED_KEY` — HARD

- Condition: key is exactly `C`, `G`, or `F`.
- Outcome: reject the request otherwise; score none.
- Rationale: limits the first implementation to three major-key signatures.
- Valid: `G`.
- Invalid: `D`, `Am`, or `C major`.

#### `SUPPORTED_PITCH_NOTATION` — HARD

- Condition: every pitch follows Sections 2.2–2.3, uses the selected key's spelling, and parses to one pitch number.
- Outcome: reject the request otherwise; score none.
- Rationale: prevents enharmonic and parsing ambiguity.
- Valid: `F#4` in G major and `Bb4` in F major.
- Invalid: `Gb4`, `A#4`, `C♯4`, `c4`, or `C 4`.

#### `SUPPORTED_CHORD` — HARD

- Condition: every chord is listed for the selected key in Section 2.4.
- Outcome: reject the request otherwise; score none.
- Rationale: confines generation to diatonic root-position major/minor triads.
- Valid: `Dm` in C major.
- Invalid: `D` in C major, `Bdim`, `G7`, or `C/E`.

#### `EVENT_ALIGNMENT` — HARD

- Condition: chord and melody sequences are both non-null, non-empty, and have equal length.
- Outcome: reject the request otherwise; score none.
- Rationale: every event requires exactly one chord and one immutable Soprano pitch.
- Valid: two chords and two Soprano pitches.
- Invalid: two chords and one Soprano pitch.

#### `PHRASE_LENGTH` — HARD

- Condition: aligned event count is 1 through 8 inclusive.
- Outcome: reject the request otherwise; score none.
- Rationale: provides a useful short phrase while bounding initial search.
- Valid boundaries: 1 event and 8 events.
- Invalid: 0 events or 9 events.

## 3. Pitch and interval semantics

### 3.1 Numeric pitch mapping

Map pitch classes to integers:

| Pitch class | Number | Pitch class | Number |
|---|---:|---|---:|
| C | 0 | F# | 6 |
| C# | 1 | G | 7 |
| D | 2 | G# | 8 |
| Eb | 3 | A | 9 |
| E | 4 | Bb | 10 |
| F | 5 | B | 11 |

`C#`, `G#`, and `Eb` appear only to make the numeric mapping complete; their notation is unsupported in `satb-initial-1`.

For a parsed pitch with octave `o` and pitch-class number `pc`:

```text
pitchNumber = 12 × (o + 1) + pc
```

Thus `C4 = 60`, `F#4 = 66`, `Bb4 = 70`, and `C5 = 72`. Pitch numbers are MIDI-equivalent comparison values; MIDI input/output is not in scope.

Enharmonic comparison uses pitch numbers only after notation validation. Unsupported spellings are rejected before comparison and are never normalized silently.

### 3.2 Harmonic intervals

For lower pitch `L` and upper pitch `U`, harmonic distance is `U - L` semitones. Valid voice order guarantees this value is non-negative.

- unison: distance `0`;
- octave-equivalent interval class: `distance mod 12`;
- perfect fifth class: `distance mod 12 = 7`;
- octave class: `distance > 0` and `distance mod 12 = 0`.

Compound perfect fifths such as 19 semitones are perfect fifths. Compound octaves such as 24 semitones are octaves.

### 3.3 Melodic intervals and motion

For a voice with previous pitch `p` and next pitch `n`:

```text
directedMotion = n - p
melodicSize = absoluteValue(directedMotion)
```

- ascending: `directedMotion > 0`;
- descending: `directedMotion < 0`;
- stationary: `directedMotion = 0`;
- step: melodic size 1 or 2 semitones;
- skip: melodic size 3 or 4 semitones;
- large leap for compensation rules: melodic size at least 5 semitones.

For two voices:

- contrary motion: both move and their directions differ;
- oblique motion: exactly one moves;
- similar motion: both move in the same direction;
- parallel perfect motion: similar motion whose starting and ending harmonic intervals are the same perfect interval class governed by Sections 6.4–6.6.

## 4. Chord-member generation

### 4.1 Quality formulas

All pitch-class arithmetic is modulo 12.

| Quality | Formula from root | Members |
|---|---|---|
| Major triad | 0, 4, 7 | root, major third, perfect fifth |
| Minor triad | 0, 3, 7 | root, minor third, perfect fifth |

For example, `C = {C,E,G}`, `Am = {A,C,E}`, `D` in G major is `{D,F#,A}`, and `Bb` in F major is `{Bb,D,F}`.

### 4.2 Candidate generation

For each event:

1. Parse and validate key, chord, and Soprano notation.
2. Preserve the supplied Soprano pitch exactly.
3. Reject the event if Soprano is not a member of the chord.
4. Enumerate chord-member pitches inside the absolute Alto, Tenor, and Bass ranges.
5. Constrain Bass to the chord root pitch class.
6. Construct four-voice candidates and apply every vertical HARD rule.

Because only root-position triads are supported, a slash chord or non-root Bass request is unsupported. Chromatically altered chord members cannot be candidates; consequently a doubled altered tone is impossible. `SUPPORTED_CHORD` rejects the altered chord before voicing, rather than scoring an altered-tone duplication.

## 5. SATB ranges

Ranges are inclusive.

| Voice | Absolute range | Comfortable range | Outside comfortable range |
|---|---|---|---|
| Soprano | `C4`–`G5` (60–79) | `D4`–`F5` (62–77) | `COMFORTABLE_RANGE`, −1 per event |
| Alto | `G3`–`D5` (55–74) | `G3`–`C5` (55–72) | `COMFORTABLE_RANGE`, −1 per event |
| Tenor | `C3`–`G4` (48–67) | `C3`–`E4` (48–64) | `COMFORTABLE_RANGE`, −1 per event |
| Bass | `E2`–`C4` (40–60) | `G2`–`G3` (43–55) | `COMFORTABLE_RANGE`, −1 per event |

`VOICE_RANGE` is HARD: a pitch below or above the absolute range rejects the candidate. Comfortable-range scoring applies independently to each voice and event, including Soprano.

## 6. Vertical voicing rules

All examples use voice order `S/A/T/B`.

### 6.1 `SOPRANO_PRESERVATION` — HARD

- Condition: candidate Soprano pitch must equal the input Soprano pitch at the same event, including spelling and octave.
- Outcome: reject a candidate that changes it.
- Score: none.
- Rationale: the supplied melody is authoritative.
- Valid: input `E4`, candidate `E4/C4/G3/C3`.
- Invalid: input `E4`, candidate `G4/E4/C4/C3`.

### 6.2 `VOICE_RANGE` — HARD

- Condition: every pitch must be inside its inclusive absolute range in Section 5.
- Outcome: reject the candidate if any voice is outside range.
- Score: none.
- Rationale: maintains singable initial tessituras.
- Valid boundary: `S=C4`, `A=G3`, `T=C3`, `B=E2` are individually in range.
- Invalid: `S=B3`, `A=E5`, `T=B2`, or `B=D2` in their respective voices.

### 6.3 `VOICE_CROSSING` — HARD

- Condition: `S >= A >= T >= B` by pitch number. Equality is permitted.
- Outcome: reject if an upper voice is below an adjacent lower voice.
- Score: none.
- Rationale: preserves SATB ordering while permitting temporary unisons.
- Valid: `G4/E4/C4/C3`.
- Invalid: `E4/G4/C4/C3` because Alto is above Soprano.
- Boundary: `D4/D4/G3/G3` does not cross.

### 6.4 `ADJACENT_VOICE_SPACING` — HARD

- Condition: `S − A <= 12`, `A − T <= 12`, and `T − B <= 19` semitones.
- Outcome: reject when any limit is exceeded.
- Score: none.
- Rationale: bounds upper-voice spread while allowing a wider Tenor–Bass foundation.
- Valid boundaries: `S=C5/A=C4` and `A=C5/T=C4` are 12; `T=C4/B=F2` is 19.
- Invalid: `S=D5/A=C4` (14), `A=D5/T=C4` (14), or `T=C4/B=E2` (20).

### 6.5 `CHORD_MEMBERSHIP` — HARD

- Condition: every voice pitch class must belong to the event chord.
- Outcome: reject otherwise.
- Score: none.
- Rationale: non-chord tones are outside the initial subset.
- Valid on `C`: `E4/C4/G3/C3`.
- Invalid on `C`: `D4/C4/G3/C3` because D is not in `{C,E,G}`.

### 6.6 `COMPLETE_TRIAD` — HARD

- Condition: root, third, and fifth must each occur at least once among the four voices. With four voices, exactly one member is doubled.
- Outcome: reject an incomplete triad.
- Score: none.
- Rationale: guarantees an unambiguous complete triadic sonority.
- Valid on `C`: `G4/E4/C4/C3`.
- Invalid on `C`: `G4/G3/C3/C2` because E is missing.

### 6.7 `ROOT_POSITION_BASS` — HARD

- Condition: Bass pitch class must equal the chord root.
- Outcome: reject otherwise.
- Score: none.
- Rationale: inversions are not supported in the first rule set.
- Valid on `C`: Bass `C3`.
- Invalid on `C`: Bass `E3` or `G2`.

### 6.8 `LEADING_TONE_DOUBLING` — HARD

- Condition: the key's leading-tone pitch class may occur at most once in a candidate.
- Outcome: reject if it occurs in two or more voices.
- Score: none.
- Rationale: prevents two simultaneous mandatory resolutions to tonic.
- Valid in C major on `G`: `G4/D4/B3/G3` contains one B.
- Invalid in C major on `G`: `B4/G4/D4/B2` contains two Bs.

### 6.9 `THIRD_DOUBLING` — SOFT

- Condition: exactly two voices have the chord's third pitch class.
- Outcome: add −2 once for the event. A doubled leading-tone third is already rejected by `LEADING_TONE_DOUBLING`.
- Rationale: permits necessary voicings but favors root or fifth duplication.
- Penalized on `C`: `E5/C5/E4/C3`, score −2.

### 6.10 `FIFTH_DOUBLING` — SOFT

- Condition: exactly two voices have the chord's fifth pitch class.
- Outcome: add −1 once for the event.
- Rationale: a doubled fifth is valid but is ranked below root doubling.
- Penalized on `C`: `G4/E4/G3/C3`, score −1.

### 6.11 `ROOT_DOUBLING` — REWARD

- Condition: exactly two voices have the chord root pitch class.
- Outcome: add +2 once for the event.
- Rationale: establishes the root-position harmony without making it mandatory.
- Rewarded on `C`: `E4/C4/G3/C3`, score +2.

### 6.12 `COMFORTABLE_RANGE` — SOFT

- Condition: a voice is inside its absolute range but outside its comfortable range.
- Outcome: add −1 for each such voice at each event.
- Rationale: ranks central tessituras above legal extremes.
- Valid without penalty: Soprano `F5`.
- Penalized boundary: Soprano `G5`, score −1; `G5` remains legal.

### 6.13 Voice overlap classification

Voice overlap cannot be determined from one sonority. It is defined as the HARD transition rule `VOICE_OVERLAP` in Section 7.1 and is not duplicated as a vertical score.

## 7. Voice-leading rules

Transition rules compare event `i−1` with event `i`. Three-event rules are evaluated when event `i` makes the needed third point available.

### 7.1 `VOICE_OVERLAP` — HARD

- Condition for each adjacent pair Upper/Lower: new Lower must not exceed previous Upper, and new Upper must not fall below previous Lower.
- Outcome: reject the transition on either violation.
- Score: none.
- Rationale: prevents adjacent voices from occupying the other voice's previous register.
- Valid A/T: previous `A=C4,T=G3`; next `A=B3,T=G3`.
- Invalid A/T: previous `A=C4,T=G3`; next Tenor `D4` exceeds previous Alto `C4`.
- Boundary: equality with the other voice's previous pitch is legal.

### 7.2 `MAX_MELODIC_LEAP` — HARD

- Condition: Soprano, Alto, and Tenor melodic size must be at most 7 semitones; Bass melodic size must be at most 12 semitones.
- Outcome: reject a transition exceeding the voice-specific maximum.
- Score: none.
- Rationale: bounds melodic discontinuity without encoding every species-counterpoint restriction.
- Valid boundaries: Alto `C4→G4` is 7; Bass `C3→C4` is 12.
- Invalid: Soprano `C4→G#4` is 8; Bass `C3→C#4` is 13.

### 7.3 `MELODIC_LEAP_PENALTY` — SOFT

- Condition and score per voice per transition:

| Melodic size | Soprano/Alto/Tenor | Bass |
|---:|---:|---:|
| 0 | 0 | 0 |
| 1–2 | 0 | 0 |
| 3–4 | −1 | −1 |
| 5–7 | −2 | −1 |
| 8–12 | illegal | −2 |

- Outcome: add the table value.
- Rationale: ranks stepwise lines above legal skips and leaps.
- Penalized: Alto `C4→E4`, −1; Tenor `C3→G3`, −2; Bass `C3→G3`, −1.

### 7.4 `STEPWISE_MOTION` — REWARD

- Condition: melodic size is 1 or 2 semitones.
- Outcome: add +1 per voice per transition.
- Rationale: favors conjunct lines.
- Rewarded: Alto `B3→C4`, +1.

### 7.5 `REPEATED_NOTE` — REWARD

- Condition: the voice repeats the identical pitch number.
- Outcome: add +1 per voice per transition.
- Rationale: rewards stable, easily sung lines.
- Rewarded: Tenor `G3→G3`, +1.

### 7.6 `COMMON_TONE_RETENTION` — REWARD

- Condition: Alto or Tenor repeats an identical pitch whose pitch class belongs to both adjacent chords.
- Outcome: add +1 per qualifying Alto/Tenor voice per transition, in addition to `REPEATED_NOTE`.
- Rationale: specifically favors retaining inner-voice common tones; the cumulative +2 is intentional.
- Rewarded from `C` to `Am`: Alto `E4→E4`, +1 under this rule.

### 7.7 `CONTRARY_OUTER_MOTION` — REWARD

- Condition: Soprano and Bass both move and have opposite directions.
- Outcome: add +1 once per transition.
- Rationale: favors independence of the outer voices.
- Rewarded: Soprano `E4→F4`, Bass `C3→A2`.

### 7.8 `OBLIQUE_OUTER_MOTION` — REWARD

- Condition: exactly one of Soprano and Bass is stationary.
- Outcome: add +1 once per transition.
- Rationale: favors outer-voice independence without penalizing a retained melody or bass.
- Rewarded: Soprano `G4→G4`, Bass `C3→G3`.

### 7.9 `SIMILAR_OUTER_MOTION` — SOFT

- Condition: Soprano and Bass both move in the same direction, after the transition passes parallel-perfect HARD rules.
- Outcome: add −1 once per transition.
- Rationale: similar motion is legal but ranked below contrary or oblique motion.
- Penalized: Soprano `E4→F4`, Bass `C3→D3`, −1.

### 7.10 `PARALLEL_FIFTHS` — HARD

- Condition: for any unordered pair of distinct voices, both voices move nonzero in the same direction, and both starting and ending harmonic interval classes equal 7.
- Outcome: reject the transition.
- Score: none.
- Rationale: excludes parallel perfect fifths, including compound fifths.
- Invalid: Bass/Tenor `C3/G3→D3/A3` (7→7).
- Valid boundary: `C3/G3→C3/A3` is oblique, not parallel.

### 7.11 `PARALLEL_OCTAVES` — HARD

- Condition: for any voice pair, both move nonzero in the same direction; starting and ending distances are positive multiples of 12.
- Outcome: reject the transition.
- Score: none.
- Rationale: preserves voice independence.
- Invalid: Bass/Soprano `C3/C4→D3/D4` (12→12).
- Valid boundary: `C3/C4→C3/D4` is oblique.

### 7.12 `PARALLEL_UNISONS` — HARD

- Condition: for any voice pair, both move nonzero in the same direction; starting and ending distances both equal 0.
- Outcome: reject the transition.
- Score: none.
- Rationale: prevents two voices from moving as one line in unison.
- Invalid: Alto/Tenor `C4/C4→D4/D4`.
- Valid boundary: `C4/C4→C4/D4` is oblique and separates the voices.

### 7.13 `HIDDEN_DIRECT_FIFTH` — SOFT

- Condition: Soprano and Bass move in similar motion, end at harmonic interval class 7, and Soprano melodic size is greater than 2 semitones. A transition rejected by `PARALLEL_FIFTHS` is not scored.
- Outcome: add −2 once per transition.
- Rationale: direct fifths with a leaping Soprano are discouraged but not mandatory exclusions in the initial hymn style.
- Penalized: Bass `C3→D3`, Soprano `E4→A4`, ending at a compound fifth.
- Not penalized: same ending interval with Soprano moving 1 or 2 semitones.

### 7.14 `HIDDEN_DIRECT_OCTAVE` — SOFT

- Condition: Soprano and Bass move in similar motion, end at a positive multiple of 12 semitones, and Soprano melodic size is greater than 2. A transition rejected by `PARALLEL_OCTAVES` is not scored.
- Outcome: add −2 once per transition.
- Rationale: direct octaves with a leaping Soprano are discouraged but remain available.
- Penalized: Bass `C3→D3`, Soprano `G4→D5`, ending two octaves apart.

### 7.15 `LEADING_TONE_RESOLUTION` — HARD

- Condition: when the next chord is the tonic triad of the key, every voice sounding the key leading tone at the previous event must move upward exactly 1 semitone to the tonic pitch in the same or next octave.
- Outcome: reject the transition otherwise.
- Score: none.
- Rationale: the only supported tendency tone is the diatonic leading tone; seventh-chord tendency tones are outside the subset.
- Valid in C major `G→C`: Tenor `B3→C4`.
- Invalid in C major `G→C`: Soprano `B4→G4` or `B4→B4`.
- Boundary: the rule is not applied when the next chord is not tonic.

### 7.16 `REPEATED_DIRECTION_LEAPS` — SOFT

- Condition: within one voice over three consecutive events, both melodic motions have size at least 3 and have the same nonzero direction.
- Outcome: add −2 per qualifying voice when the third event is added.
- Rationale: discourages two consecutive skips/leaps that continue in one direction.
- Penalized: Alto `C4→E4→G4`, −2.
- Not penalized: `C4→E4→F4` because the second motion is a step.

### 7.17 `LARGE_LEAP_COMPENSATION` — SOFT

- Condition: after a motion of at least 5 semitones, the next motion in the same voice must be 1 or 2 semitones in the opposite direction. If no such compensating motion occurs, add the penalty when the third event is added.
- Outcome: add −2 per qualifying voice.
- Rationale: ranks recovery from a large leap above continued or stationary motion.
- Unpenalized: Tenor `C3→G3→F3`.
- Penalized: Tenor `C3→G3→G3` or `C3→G3→A3`, −2.
- End-of-phrase policy: a large leap in the final transition receives no compensation penalty because no following event exists.

## 8. Scoring and selection

### 8.1 Scoring convention

Start every complete path at score 0.

1. Apply input and vertical HARD rules; reject failures.
2. Add vertical SOFT penalties and REWARD bonuses for each event.
3. Apply transition HARD rules; reject failures.
4. Add transition SOFT penalties and REWARD bonuses for each adjacent event pair.
5. At each third event, add three-event penalties whose condition has become decidable.
6. The complete-path score is the integer sum of all contributions. Higher is better.

No normalization, random value, floating-point value, time-dependent input, or unspecified default weight is permitted.

### 8.2 Numerical weights

| Rule | Contribution |
|---|---:|
| `COMFORTABLE_RANGE` | −1 per voice/event outside comfortable range |
| `THIRD_DOUBLING` | −2 per event |
| `FIFTH_DOUBLING` | −1 per event |
| `ROOT_DOUBLING` | +2 per event |
| `MELODIC_LEAP_PENALTY` | −1 or −2 per Section 7.3 |
| `STEPWISE_MOTION` | +1 per voice/transition |
| `REPEATED_NOTE` | +1 per voice/transition |
| `COMMON_TONE_RETENTION` | +1 per qualifying Alto/Tenor/transition |
| `CONTRARY_OUTER_MOTION` | +1 per transition |
| `OBLIQUE_OUTER_MOTION` | +1 per transition |
| `SIMILAR_OUTER_MOTION` | −1 per transition |
| `HIDDEN_DIRECT_FIFTH` | −2 per transition |
| `HIDDEN_DIRECT_OCTAVE` | −2 per transition |
| `REPEATED_DIRECTION_LEAPS` | −2 per voice/three-event window |
| `LARGE_LEAP_COMPENSATION` | −2 per voice/three-event window |

Weights are intentionally small. A root-doubling bonus can offset two minor preferences but can never offset a HARD violation.

### 8.3 Deterministic tie-breaking

After rejecting invalid paths and selecting the highest complete-path score, compare tied paths lexicographically as follows:

1. Build a numeric sequence by event order from first to last.
2. For each event append pitch numbers in the exact order Bass, Tenor, Alto, Soprano.
3. Compare sequences from the first number onward.
4. At the first difference, the path with the lower number wins.

The Soprano entries are included even though tied paths share them, making the selection key fully descriptive. Candidate enumeration order, collection/hash order, parallel execution, object identity, and randomness must not affect selection. If the numeric sequences are identical, the paths are musically identical and either object representation yields the same serialized musical result; rule explanations must then be ordered by event index, scope order `VERTICAL` before `VOICE_LEADING`, and Rule ID ascending.

## 9. Rule catalog

| Rule ID | Name | Category | Scope | Score | Summary |
|---|---|---|---|---:|---|
| `SUPPORTED_KEY` | Supported key | HARD | INPUT | reject | Key is exactly C, G, or F major. |
| `SUPPORTED_PITCH_NOTATION` | Supported pitch notation | HARD | INPUT | reject | Pitch spelling follows Sections 2.2–2.3. |
| `SUPPORTED_CHORD` | Supported chord | HARD | INPUT | reject | Chord is a listed diatonic root-position major/minor triad. |
| `EVENT_ALIGNMENT` | Event alignment | HARD | INPUT | reject | Chord and melody lists are non-empty and equal length. |
| `PHRASE_LENGTH` | Phrase length | HARD | INPUT | reject | Phrase contains 1–8 events. |
| `SOPRANO_PRESERVATION` | Preserve melody | HARD | VERTICAL | reject | Output Soprano exactly equals input. |
| `VOICE_RANGE` | Absolute voice range | HARD | VERTICAL | reject | Every voice is within its inclusive range. |
| `VOICE_CROSSING` | Voice crossing | HARD | VERTICAL | reject | S >= A >= T >= B. |
| `ADJACENT_VOICE_SPACING` | Adjacent spacing | HARD | VERTICAL | reject | S–A <=12, A–T <=12, T–B <=19. |
| `CHORD_MEMBERSHIP` | Chord membership | HARD | VERTICAL | reject | Every pitch is a chord member. |
| `COMPLETE_TRIAD` | Complete triad | HARD | VERTICAL | reject | Root, third, and fifth are present. |
| `ROOT_POSITION_BASS` | Root in Bass | HARD | VERTICAL | reject | Bass pitch class equals chord root. |
| `LEADING_TONE_DOUBLING` | Leading-tone doubling | HARD | VERTICAL | reject | Key leading tone occurs at most once. |
| `COMFORTABLE_RANGE` | Comfortable range | SOFT | VERTICAL | −1 each | Penalize each voice outside comfortable range. |
| `THIRD_DOUBLING` | Third doubling | SOFT | VERTICAL | −2 | Penalize doubled third. |
| `FIFTH_DOUBLING` | Fifth doubling | SOFT | VERTICAL | −1 | Penalize doubled fifth. |
| `ROOT_DOUBLING` | Root doubling | REWARD | VERTICAL | +2 | Reward doubled root. |
| `VOICE_OVERLAP` | Voice overlap | HARD | VOICE_LEADING | reject | Adjacent voices do not enter each other's previous register. |
| `MAX_MELODIC_LEAP` | Maximum melodic leap | HARD | VOICE_LEADING | reject | S/A/T <=7; Bass <=12 semitones. |
| `PARALLEL_FIFTHS` | Parallel fifths | HARD | VOICE_LEADING | reject | No same-direction perfect fifth class to fifth class. |
| `PARALLEL_OCTAVES` | Parallel octaves | HARD | VOICE_LEADING | reject | No same-direction positive octave to octave. |
| `PARALLEL_UNISONS` | Parallel unisons | HARD | VOICE_LEADING | reject | No same-direction unison to unison. |
| `LEADING_TONE_RESOLUTION` | Leading-tone resolution | HARD | VOICE_LEADING | reject | Leading tone rises one semitone before tonic triad. |
| `MELODIC_LEAP_PENALTY` | Melodic leap penalty | SOFT | VOICE_LEADING | −1/−2 | Penalize legal skips and leaps by size. |
| `SIMILAR_OUTER_MOTION` | Similar outer motion | SOFT | VOICE_LEADING | −1 | Penalize similar Soprano/Bass motion. |
| `HIDDEN_DIRECT_FIFTH` | Hidden/direct fifth | SOFT | VOICE_LEADING | −2 | Penalize similar outer motion into fifth with Soprano skip/leap. |
| `HIDDEN_DIRECT_OCTAVE` | Hidden/direct octave | SOFT | VOICE_LEADING | −2 | Penalize similar outer motion into octave with Soprano skip/leap. |
| `REPEATED_DIRECTION_LEAPS` | Repeated-direction leaps | SOFT | VOICE_LEADING | −2 each | Penalize two consecutive same-direction motions >=3. |
| `LARGE_LEAP_COMPENSATION` | Large-leap compensation | SOFT | VOICE_LEADING | −2 each | Penalize an uncompensated motion >=5. |
| `STEPWISE_MOTION` | Stepwise motion | REWARD | VOICE_LEADING | +1 each | Reward motion of 1–2 semitones. |
| `REPEATED_NOTE` | Repeated note | REWARD | VOICE_LEADING | +1 each | Reward exact pitch retention. |
| `COMMON_TONE_RETENTION` | Common-tone retention | REWARD | VOICE_LEADING | +1 each | Reward retained Alto/Tenor common tone. |
| `CONTRARY_OUTER_MOTION` | Contrary outer motion | REWARD | VOICE_LEADING | +1 | Reward contrary Soprano/Bass motion. |
| `OBLIQUE_OUTER_MOTION` | Oblique outer motion | REWARD | VOICE_LEADING | +1 | Reward exactly one stationary outer voice. |

## 10. Validation corpus

All voice lists are ordered by event. These are validity fixtures, not guaranteed highest-scoring outputs until the implementation independently computes scores and tie-breaks.

### 10.1 Valid phrases

#### V1 — C-major I–IV–V–I

- Key: `C`
- Chords: `C | F | G | C`
- Soprano: `E4 | F4 | D4 | E4`
- Alto: `C4 | C4 | B3 | C4`
- Tenor: `G3 | A3 | G3 | G3`
- Bass: `C3 | F3 | G3 | C3`

All sonorities are complete root-position triads. B3 is the single leading tone and resolves to C4. No voice exceeds its leap limit or forms parallel perfect fifths, octaves, or unisons.

#### V2 — G-major I–IV–V–I

- Key: `G`
- Chords: `G | C | D | G`
- Soprano: `B4 | C5 | A4 | B4`
- Alto: `G4 | G4 | F#4 | G4`
- Tenor: `D4 | E4 | D4 | D4`
- Bass: `G3 | C3 | D3 | G2`

The F# spelling is canonical in G major. F#4 resolves upward to G4 at the tonic arrival. Bass movement remains within the 12-semitone maximum and Tenor–Bass spacing reaches, but does not exceed, 19 semitones at the final event.

#### V3 — F-major I–IV–V–I

- Key: `F`
- Chords: `F | Bb | C | F`
- Soprano: `A4 | Bb4 | G4 | A4`
- Alto: `F4 | F4 | E4 | F4`
- Tenor: `C4 | D4 | C4 | C4`
- Bass: `F3 | Bb3 | C4 | F3`

The Bb spelling is canonical in F major. E4 is the single leading tone and resolves to F4. Bass C4 is the inclusive maximum Bass pitch.

#### V4 — C-major I–V–I with oblique Soprano

- Key: `C`
- Chords: `C | G | C`
- Soprano: `G4 | G4 | G4`
- Alto: `E4 | D4 | E4`
- Tenor: `C4 | B3 | C4`
- Bass: `C3 | G3 | C3`

The stationary Soprano creates oblique outer motion. Tenor B3 resolves to C4. The outer intervals change 19→12→19 semitones, so there is no parallel perfect interval.

#### V5 — F-major I–IV–I

- Key: `F`
- Chords: `F | Bb | F`
- Soprano: `A4 | Bb4 | A4`
- Alto: `F4 | F4 | F4`
- Tenor: `C4 | D4 | C4`
- Bass: `F3 | Bb3 | F3`

Every event is a complete root-position triad. Alto retains the common tone F4, while the remaining voices move within their limits and create no parallel perfect intervals.

### 10.2 Invalid vertical fixtures

Each row is independently invalid. `S/A/T/B` identifies the tested sonority.

| HARD rule | Context | Invalid fixture | Exact reason |
|---|---|---|---|
| `SOPRANO_PRESERVATION` | C major, chord C, input S=`E4` | `G4/E4/C4/C3` | Candidate changes Soprano to G4. |
| `VOICE_RANGE` | C major, chord G | `B3/G3/D3/G2` | Soprano B3 is below C4. |
| `VOICE_CROSSING` | C major, chord C | `E4/G4/C4/C3` | Alto G4 is above Soprano E4. |
| `ADJACENT_VOICE_SPACING` | C major, chord C | `E5/C4/G3/C3` | Soprano–Alto distance is 16 > 12. |
| `CHORD_MEMBERSHIP` | C major, chord C | `A4/E4/G3/C3` | A is not in C major triad. |
| `COMPLETE_TRIAD` | C major, chord C | `G4/G3/C3/C3` | Third E is absent. |
| `ROOT_POSITION_BASS` | C major, chord C | `G4/E4/C4/E3` | Bass is the third, not root. |
| `LEADING_TONE_DOUBLING` | C major, chord G | `B4/D4/B3/G3` | B leading tone occurs twice. |

For isolated rule tests, all non-target fields must satisfy the other HARD rules so the asserted violation set is explicit.

### 10.3 Invalid voice-leading fixtures

| HARD rule | Previous `S/A/T/B` | Next `S/A/T/B` | Exact reason |
|---|---|---|---|
| `VOICE_OVERLAP` | `G4/C4/G3/C3` | `A4/E4/D4/D3` | New Tenor D4 exceeds previous Alto C4. |
| `MAX_MELODIC_LEAP` | `C4/G3/E3/C3` | `G5/B4/D4/G3` | Soprano moves 19 semitones, greater than 7. |
| `PARALLEL_FIFTHS` | `E4/C4/G3/C3` | `F4/D4/A3/D3` | Bass/Tenor pair C3/G3→D3/A3 is 7→7 in the same direction. |
| `PARALLEL_OCTAVES` | `C4/G3/E3/C3` | `D4/A3/F3/D3` | Bass/Soprano move C3/C4→D3/D4, 12→12. |
| `PARALLEL_UNISONS` | `G4/C4/C4/C3` | `A4/D4/D4/D3` | Alto/Tenor move together from unison to unison. |
| `LEADING_TONE_RESOLUTION` | C major G: `B4/G4/D4/G3` | C: `G4/E4/C4/C3` | Soprano B4 moves to G4 instead of C5. |

The parallel-fifths fixture is interval-isolation data; a unit test should construct the cited Bass/Tenor pair and legal values for the other voices, then assert that `PARALLEL_FIFTHS` appears among violations.

### 10.4 Boundary fixtures

| Boundary | Fixture | Expected result |
|---|---|---|
| Lowest Soprano | C chord, `S=C4/A=G3/T=E3/B=C3` | `VOICE_RANGE` passes. |
| Highest Soprano | G chord, `S=G5/A=B4/T=D4/B=G3` | Absolute range passes; Soprano receives a comfortable-range penalty. |
| Lowest Alto | C chord, `S=E4/A=G3/T=E3/B=C3` | Alto G3 passes. |
| Highest Alto | Bb chord in F, `S=F5/A=D5/T=F4/B=Bb2` | Alto D5 passes and receives −1 comfortable-range penalty. |
| Lowest Tenor | C chord, `S=G4/A=E4/T=C3/B=C3` | Tenor C3 passes. |
| Highest Tenor | C chord, `S=G5/A=E5/T=G4/B=C3` | Tenor G4 passes and receives −1. |
| Lowest Bass | Em chord in C, `S=G4/A=E4/T=B3/B=E2` | Bass E2 passes and receives −1. |
| Highest Bass | C chord, `S=G4/A=E4/T=C4/B=C4` | Bass C4 passes and receives −1; equal T/B does not cross. |
| Maximum S–A spacing | C chord, `S=E5/A=E4/T=G3/B=C3` | 12 semitones passes. |
| Maximum A–T spacing | C chord, `S=G5/A=E5/T=E4/B=C3` | 12 semitones passes. |
| Maximum T–B spacing | G chord, `S=B4/A=G4/T=D4/B=G2` | 19 semitones passes. |
| Maximum upper-voice leap | Alto `C4→G4` | 7 semitones passes, with −2 leap penalty. |
| Maximum Bass leap | Bass `C3→C4` | 12 semitones passes, with −2 leap penalty. |

### 10.5 No-solution fixtures

All three inputs are syntactically valid and use supported keys, chords, pitches, ranges, and phrase lengths.

#### N1 — Soprano is not a chord member

- Key: `C`
- Chords: `C`
- Soprano: `D4`
- Expected: `NO_VALID_HARMONIZATION`
- Reason: immutable Soprano D is not in `{C,E,G}`. Every candidate fails `CHORD_MEMBERSHIP`.

#### N2 — Immutable Soprano exceeds transition leap limit

- Key: `C`
- Chords: `C | G`
- Soprano: `C4 | G5`
- Expected: `NO_VALID_HARMONIZATION`
- Reason: both Soprano pitches are chord members and within range, but the fixed 19-semitone leap exceeds the 7-semitone Soprano maximum. Every transition fails `MAX_MELODIC_LEAP`.

#### N3 — Immutable leading tone does not resolve to tonic

- Key: `C`
- Chords: `G | C`
- Soprano: `B4 | G4`
- Expected: `NO_VALID_HARMONIZATION`
- Reason: both pitches are chord members and the 4-semitone motion is within the leap limit, but B4 must rise to C5 before the tonic triad. Every transition fails `LEADING_TONE_RESOLUTION`.

## 11. Resolution of Phase 0 musical questions

| Question | Decision |
|---|---|
| Supported keys and modes | C, G, and F major only; minor and modes unsupported. |
| Supported qualities | Diatonic major/minor triads listed in Section 2.4 only. |
| Inversions | Unsupported; Bass must be root. |
| Sevenths, diminished, augmented, suspended, altered chords | Unsupported. |
| Non-chord tones | Unsupported. |
| Canonical pitch/key/chord notation | Resolved in Sections 2.2–2.4. Duration notation is **DEFERRED TO TECH LEAD** with M5 because rhythm is outside the technical milestone. |
| SATB ranges and comfortable ranges | Resolved in Section 5. |
| Spacing | 12 semitones S–A, 12 A–T, 19 T–B, inclusive. |
| Doubling | Complete triad; no doubled leading tone; root +2, fifth −1, third −2. Altered tones cannot enter the subset. |
| Parallel-motion policy | Parallel fifths, octaves, and unisons are HARD; direct fifths/octaves are SOFT −2 under exact conditions. |
| Melodic-leap policy | Upper voices max 7, Bass max 12; legal leap weights and compensation are in Section 7. |
| Scoring convention and weights | Integer additive higher-is-better model in Section 8. |
| Tie-break | Lexicographically lowest event-major B/T/A/S pitch-number sequence. |
| Phrase length | 1–8 events inclusive for `satb-initial-1`. |
| BPM range | **DEFERRED TO TECH LEAD** with Music Theory input in M5; BPM does not affect pitch-only search complexity. |
| Maximum JSON body size and processing timeout | **DEFERRED TO TECH LEAD**; these are operational constraints. |
| Out-of-range HTTP status | **DEFERRED TO TECH LEAD**; API transport semantics are not music theory. |
| End-user explanation depth | **DEFERRED TO TECH LEAD** with Product/Mobile input; selected-path rule IDs and scores remain required by architecture. |
| Rhythmic event schema, ties, rests, pickups, multiple notes per chord | **DEFERRED TO TECH LEAD** with Music Theory input in M5. The first technical milestone remains one chord and one pitch per event. |

## 12. Implementation-neutral acceptance notes

- Every catalog rule must have an isolated test named from its stable Rule ID.
- Combined validation may report multiple HARD violations in deterministic Rule ID order.
- Candidate generation may prune immediately after a HARD violation but must not change the musical result or tie-break.
- Golden fixtures must assert both exact output where intended and all invariants; an exact fixture alone is not proof of validity.
- Any future change to supported notation, rule category, weight, or tie-break requires a new `ruleSetVersion`.
