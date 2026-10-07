# Harmony REST API Contract

## 1. Status and scope

- Status: Draft for Phase 0 review
- Contract version: `v1`
- Initial operation: create a deterministic SATB harmonization
- Base path: `/api/v1`

This document defines the proposed contract; it does not create or authorize a Spring Boot application. The initial REST milestone supports key, chords, and Soprano pitches. BPM, time signature, and durations are added in a later MVP milestone as described in Section 8.

## 2. Conventions

- Media type: `application/json`.
- Field names: `camelCase`.
- Event and error ordering is deterministic.
- Unknown fields are rejected to catch client mistakes early.
- Musical notation is case-sensitive and must use the canonical subset approved in `docs/MUSIC_RULES.md`.
- Pitch, key, accidental, and chord syntax follows the canonical initial subset in `docs/MUSIC_RULES.md`.
- The service does not infer, transpose, or repair unsupported musical input.
- A request is not persisted. `id` identifies the response instance for tracing and is not a retrievable resource guarantee.

## 3. Create harmonization

`POST /api/v1/harmonizations`

Creates a harmonization synchronously from aligned chords and Soprano melody pitches.

### 3.1 Initial technical-milestone request

```json
{
  "key": "C",
  "chords": ["C"],
  "melody": ["C5"]
}
```

Validation rules:

- `key`, `chords`, and `melody` are required and non-null;
- `chords` and `melody` are non-empty and have equal lengths;
- length is 1–8 events inclusive;
- every pitch first uses the global canonical grammar and then belongs to the exact pitch-class set accepted by the selected key in `docs/MUSIC_RULES.md`;
- every Soprano pitch is in the approved Soprano range; a canonically formatted, key-supported pitch outside that range is `400 INVALID_REQUEST` with field code `SOPRANO_OUT_OF_RANGE`;
- event `i` pairs `chords[i]` with `melody[i]`.

### 3.2 Success response

Status: `200 OK`

The response is the result of a synchronous computation. No harmonization resource is created or persisted, and no `Location` header is returned.

```json
{
  "id": "01K6Y3X8QTBH0FQJVCAVQ8R8M3",
  "engineVersion": "1.0.0",
  "ruleSetVersion": "satb-initial-1",
  "key": "C",
  "voices": {
    "soprano": ["C5"],
    "alto": ["E4"],
    "tenor": ["G3"],
    "bass": ["C3"]
  },
  "evaluation": {
    "score": 2,
    "selectionKey": "C3-G3-E4-C5",
    "events": [
      {
        "eventIndex": 0,
        "rules": [
          {
            "ruleId": "ROOT_DOUBLING",
            "category": "REWARD",
            "outcome": "APPLIED",
            "scoreContribution": 2
          }
        ]
      }
    ],
    "transitions": [],
    "windows": []
  }
}
```

This is a normative `satb-initial-1` success example. All four pitches are inside their absolute and comfortable ranges; the complete C-major triad is in root position with C doubled; and no transition rules apply to the single event. `ROOT_DOUBLING` is the only nonzero score contribution, so the total is +2. `selectionKey` uses Bass, Tenor, Alto, Soprano order. It is a stable description of the chosen path/tie-break, not a public algorithm promise; clients should display it only for diagnostics.

Required success invariants:

- all four voice arrays have the same length as `chords`;
- `voices.soprano` exactly equals request `melody`;
- all returned voices satisfy every enabled HARD rule;
- `evaluation.events` contains vertical/event contributions, `evaluation.transitions` contains adjacent-event contributions, and `evaluation.windows` contains three-event contributions;
- all three arrays exist even when empty;
- `evaluation.score` equals the sum of every nonzero `scoreContribution` in those three arrays, with no contribution duplicated across scopes;
- events are ordered by `eventIndex` ascending; transitions by `fromEventIndex`, then `toEventIndex`; windows by `startEventIndex`, then `middleEventIndex`, then `endEventIndex`; and `rules` within every container by `ruleId` ascending.

### 3.3 Normative evaluation shape

The selected-path explanation has exactly these three rule containers:

```json
{
  "evaluation": {
    "score": -1,
    "selectionKey": "C3-G3-E4-E4|F3-A3-C4-F4|G3-G3-B3-G4",
    "events": [
      {
        "eventIndex": 0,
        "rules": [
          {
            "ruleId": "ROOT_DOUBLING",
            "category": "REWARD",
            "outcome": "APPLIED",
            "scoreContribution": 2
          }
        ]
      }
    ],
    "transitions": [
      {
        "fromEventIndex": 0,
        "toEventIndex": 1,
        "rules": [
          {
            "ruleId": "STEPWISE_MOTION",
            "category": "REWARD",
            "outcome": "APPLIED",
            "scoreContribution": 1
          }
        ]
      }
    ],
    "windows": [
      {
        "startEventIndex": 0,
        "middleEventIndex": 1,
        "endEventIndex": 2,
        "rules": [
          {
            "ruleId": "LARGE_LEAP_COMPENSATION",
            "category": "SOFT",
            "outcome": "APPLIED",
            "scoreContribution": -2
          },
          {
            "ruleId": "REPEATED_DIRECTION_LEAPS",
            "category": "SOFT",
            "outcome": "APPLIED",
            "scoreContribution": -2
          }
        ]
      }
    ]
  }
}
```

This fragment specifies shape, placement, ordering, and score reconciliation (`2 + 1 - 2 - 2 = -1`); it is not a complete harmonization example. `REPEATED_DIRECTION_LEAPS` and `LARGE_LEAP_COMPENSATION` always belong in `windows`. HARD rules contribute no score and need not appear in a successful selected-path payload; if included as passed audit records, their contribution is zero and they remain in their natural scope. No rule evaluation or contribution may be copied into another scope.

## 4. Error response

All controlled errors share this envelope:

```json
{
  "type": "https://harmony.example/problems/validation-error",
  "title": "Request validation failed",
  "status": 400,
  "code": "INVALID_REQUEST",
  "detail": "One or more request fields are invalid.",
  "instance": "/api/v1/harmonizations",
  "correlationId": "f1f4ad8b-0f63-4da7-9764-82034d5356d5",
  "errors": [
    {
      "field": "melody[1]",
      "code": "INVALID_PITCH_FORMAT",
      "message": "The pitch cannot be parsed using canonical pitch syntax.",
      "rejectedValue": "H4"
    }
  ]
}
```

The envelope follows the shape of Problem Details while adding stable application `code` and field `errors`. Final implementation should use the registered Problem Details media type if adopted consistently.

For privacy and log safety, `rejectedValue` may be omitted. Clients must branch on `status`, `code`, and field error `code`, not on message text.

## 5. Status and error semantics

| HTTP status | Application code | Meaning |
|---|---|---|
| `400 Bad Request` | `MALFORMED_JSON` | Body is not valid JSON. |
| `400 Bad Request` | `INVALID_REQUEST` | Required fields, types, array alignment, numeric bounds, or strict schema are invalid. |
| `413 Payload Too Large` | `REQUEST_TOO_LARGE` | Raw request body exceeds 16384 bytes; rejected before JSON processing. |
| `422 Unprocessable Content` | `UNSUPPORTED_MUSICAL_ELEMENT` | Request is structurally valid but contains notation or a musical construct outside the enabled rule set. |
| `422 Unprocessable Content` | `NO_VALID_HARMONIZATION` | Supported, valid input has no path satisfying all HARD rules. |
| `415 Unsupported Media Type` | `UNSUPPORTED_MEDIA_TYPE` | Content type is not accepted. |
| `503 Service Unavailable` | `HARMONIZATION_TIMEOUT` | Deterministic harmonization computation exceeded 2 seconds; no partial harmonization is returned. |
| `500 Internal Server Error` | `INTERNAL_ERROR` | Unexpected failure; no implementation details are exposed. |

Representative field error codes:

- `REQUIRED_FIELD`;
- `UNKNOWN_FIELD`;
- `EMPTY_SEQUENCE`;
- `SEQUENCE_LENGTH_MISMATCH`;
- `PHRASE_TOO_LONG`;
- `INVALID_KEY_FORMAT`;
- `UNSUPPORTED_KEY`;
- `INVALID_CHORD_FORMAT`;
- `UNSUPPORTED_CHORD`;
- `INVALID_PITCH_FORMAT`;
- `UNSUPPORTED_PITCH`;
- `SOPRANO_OUT_OF_RANGE`.

`INVALID_PITCH_FORMAT` means the string cannot be parsed using the global canonical grammar; `H4`, `Gb4`, and `A#4` are examples. `UNSUPPORTED_PITCH` means the pitch is globally canonical but its pitch class is not accepted by the selected key: `F4` in G major, `B4` in F major, and `F#4` or `Bb4` in C major. `F#4` is supported in G major and `Bb4` is supported in F major. A supported C-major scale pitch that is not a current chord member, such as `D4` over chord `C`, passes request validation and can produce `NO_VALID_HARMONIZATION` through `CHORD_MEMBERSHIP`. These codes and outcomes must not be used interchangeably.

### 5.1 Validation aggregation, precedence, and ordering

Validation proceeds in this order:

1. Reject a raw body over 16384 bytes before JSON processing with `413 REQUEST_TOO_LARGE`.
2. Fail immediately on malformed JSON with `400 MALFORMED_JSON`.
3. For structurally readable JSON, collect all independently determinable category-1 structural, type, required, format, and range errors.
4. Evaluate category-2 supported-subset and key-context errors only where their prerequisites parsed successfully, collecting all independently determinable errors.
5. Run harmonization domain evaluation only if request validation is completely successful.

If any category-1 error exists, the envelope is `400 INVALID_REQUEST`, even when category-2 errors were also independently determinable. If no category-1 error exists but at least one category-2 error exists, the envelope is `422 UNSUPPORTED_MUSICAL_ELEMENT`. `NO_VALID_HARMONIZATION` is never combined with request-validation errors.

Dependent validation does not invent secondary errors. For example, an unparseable `key` may coexist with independently determinable melody format or Soprano-range errors, but it suppresses key-dependent pitch-context and chord-context checks.

Field errors use this deterministic order:

1. known top-level fields in the order `key`, `chords`, `melody`;
2. array elements at the same field in ascending index order;
3. multiple errors at the same location by field error `code` ascending;
4. unknown top-level fields after known fields, in lexical field-name order.

### 5.2 No-solution example

Status: `422 Unprocessable Content`

```json
{
  "type": "https://harmony.example/problems/no-valid-harmonization",
  "title": "No valid harmonization",
  "status": 422,
  "code": "NO_VALID_HARMONIZATION",
  "detail": "No harmonization satisfies all mandatory rules.",
  "instance": "/api/v1/harmonizations",
  "correlationId": "72bf603f-dbc7-48a6-b2f8-017a68dbbbef",
  "errors": []
}
```

The first contract does not promise a complete list of rejected candidates. A later diagnostic field may identify the event where all candidate paths were eliminated, provided it remains bounded and deterministic.

## 6. Determinism and versioning contract

For identical normalized input, `engineVersion`, and `ruleSetVersion`, these fields are deterministic:

- all voice pitches;
- total score;
- selection key;
- ordered rule outcomes and score contributions.

`id` and `correlationId` are operational metadata and need not repeat. Changes that alter notation, rules, weights, or tie-breaking require a new `ruleSetVersion` and release notes. Breaking JSON or HTTP semantic changes require a new path version.

## 7. Limits and security controls

The musical phrase limit is 1–8 events inclusive. It is separate from these operational limits:

- maximum raw JSON request body: 16 KiB = 16384 bytes; byte 16384 is accepted and byte 16385 is rejected with `413 REQUEST_TOO_LARGE` before JSON processing;
- harmonization processing timeout: 2 seconds; if deterministic harmonization computation exceeds this protective ceiling, return `503 HARMONIZATION_TIMEOUT` and no partial harmonization.

The 2-second ceiling is a protective processing bound, not a latency SLO. Request reading and validation order follow Section 5.1; harmonization timing begins when validated input enters deterministic harmonization computation.

Supported keys, chord types, and pitch/voice ranges are defined in `docs/MUSIC_RULES.md`.

The API must enforce these limits before or during bounded search. Rate limiting and authentication are deployment concerns and are not part of the first MVP.

## 8. First-MVP rhythmic extension

M5 extends the request without changing the core meaning of Soprano. The intended shape is:

```json
{
  "key": "C",
  "tempo": {
    "bpm": 80
  },
  "timeSignature": {
    "beats": 4,
    "beatUnit": 4
  },
  "events": [
    { "chord": "C", "soprano": { "pitch": "E4", "duration": "QUARTER" } },
    { "chord": "F", "soprano": { "pitch": "F4", "duration": "QUARTER" } },
    { "chord": "G", "soprano": { "pitch": "G4", "duration": "QUARTER" } },
    { "chord": "C", "soprano": { "pitch": "C5", "duration": "QUARTER" } }
  ]
}
```

This shape is intentionally provisional. Before M5 implementation, decide:

- whether to evolve `v1` before release or introduce a new media/path version;
- canonical duration notation and support for dots, ties, rests, and pickup measures;
- supported time signatures and measure validation;
- BPM bounds.

The initial rhythmic arrangement is homorhythmic: generated Alto, Tenor, and Bass events inherit the aligned Soprano event duration. Supporting multiple melody notes per chord or independent voice rhythms is outside the first MVP unless separately approved.

## 9. Contract acceptance tests

At minimum, implementation must test:

1. documented success serialization and four aligned voices;
2. exact Soprano preservation;
3. missing, null, empty, unknown, and incorrectly typed fields;
4. unequal chord/melody lengths and phrase limits;
5. malformed versus unsupported key, chord, and pitch notation;
6. Soprano range boundaries;
7. exact key-sensitive pitch cases: `F4` in G, `B4` in F, `F#4` and `Bb4` in C, supported `F#4` in G and `Bb4` in F, and invalid-format `Gb4` and `A#4`;
8. `400 INVALID_REQUEST` / `SOPRANO_OUT_OF_RANGE` and mixed-error precedence, dependency suppression, and deterministic field-error ordering;
9. no-solution mapping, including C major/chord C/Soprano D4 through `CHORD_MEMBERSHIP`;
10. bodies of exactly 16384 and 16385 bytes, proving rejection occurs before JSON processing;
11. the 2-second harmonization ceiling, `503 HARMONIZATION_TIMEOUT`, and absence of partial output;
12. event, transition, and three-event-window explanation shapes, empty arrays, deterministic ordering, non-duplication, and exact score reconciliation;
13. unsupported media type;
14. deterministic musical payload across repeated requests;
15. absence of stack traces or internal exception details in errors.

## 10. Open contract questions

1. What canonical duration grammar will M5 accept? Initial key, chord, accidental, and pitch grammar is resolved in `docs/MUSIC_RULES.md`.
2. How much of the required selected-path explanation should the mobile UI display?
3. Will the rhythmic event shape replace the parallel `chords`/`melody` arrays before public `v1`, avoiding two long-lived request shapes?
