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
- Examples use scientific pitch notation such as `C4`; final accidental and chord-symbol syntax remains a Phase 0 decision.
- The service does not infer, transpose, or repair unsupported musical input.
- A request is not persisted. `id` identifies the response instance for tracing and is not a retrievable resource guarantee.

## 3. Create harmonization

`POST /api/v1/harmonizations`

Creates a harmonization synchronously from aligned chords and Soprano melody pitches.

### 3.1 Initial technical-milestone request

```json
{
  "key": "C",
  "chords": ["C", "F", "G", "C"],
  "melody": ["E4", "F4", "G4", "C5"]
}
```

Validation rules:

- `key`, `chords`, and `melody` are required and non-null;
- `chords` and `melody` are non-empty and have equal lengths;
- length does not exceed the approved initial phrase limit;
- every key, chord, and pitch uses supported canonical notation;
- every Soprano pitch is in the approved Soprano range;
- event `i` pairs `chords[i]` with `melody[i]`.

### 3.2 Success response

Status: `201 Created`

No `Location` header is required because retrieval/persistence is out of scope.

```json
{
  "id": "01K6Y3X8QTBH0FQJVCAVQ8R8M3",
  "engineVersion": "1.0.0",
  "ruleSetVersion": "satb-initial-1",
  "key": "C",
  "voices": {
    "soprano": ["E4", "F4", "G4", "C5"],
    "alto": ["C4", "C4", "D4", "E4"],
    "tenor": ["G3", "A3", "B3", "G3"],
    "bass": ["C3", "F3", "G3", "C3"]
  },
  "evaluation": {
    "score": 12,
    "selectionKey": "C3-G3-C4-E4|F3-A3-C4-F4|G3-B3-D4-G4|C3-G3-E4-C5",
    "events": [
      {
        "eventIndex": 0,
        "rules": [
          {
            "ruleId": "COMMON_TONE_RETENTION",
            "category": "REWARD",
            "outcome": "NOT_APPLICABLE",
            "scoreContribution": 0
          }
        ]
      }
    ]
  }
}
```

The notes above illustrate shape only. They are not normative until the Music Theory Agent confirms they satisfy the approved initial rules. `selectionKey` is a stable description of the chosen path/tie-break, not a public algorithm promise; clients should display it only for diagnostics.

Required success invariants:

- all four voice arrays have the same length as `chords`;
- `voices.soprano` exactly equals request `melody`;
- all returned voices satisfy every enabled HARD rule;
- `evaluation.score` equals the sum defined by the versioned scoring rules;
- rules and events appear in a documented stable order.

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
      "code": "UNSUPPORTED_PITCH",
      "message": "The pitch is not supported by this rule set.",
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
| `422 Unprocessable Content` | `UNSUPPORTED_MUSICAL_ELEMENT` | Request is structurally valid but contains notation or a musical construct outside the enabled rule set. |
| `422 Unprocessable Content` | `NO_VALID_HARMONIZATION` | Supported, valid input has no path satisfying all HARD rules. |
| `415 Unsupported Media Type` | `UNSUPPORTED_MEDIA_TYPE` | Content type is not accepted. |
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

### 5.1 No-solution example

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

Before implementation, Phase 0 must assign concrete values for:

- maximum events per request;
- maximum JSON body size;
- supported keys, chord types, and pitch range;
- processing timeout.

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
7. no-solution mapping;
8. unsupported media type;
9. deterministic musical payload across repeated requests;
10. absence of stack traces or internal exception details in errors.

## 10. Open contract questions

1. What canonical key, chord, accidental, pitch, and duration grammar will be accepted?
2. What concrete request and phrase limits apply?
3. Should supported-but-out-of-range pitch return `400` or `422`? The current proposal uses `400` as a value constraint.
4. Should the initial success use `200 OK` for a computation or `201 Created` for a produced arrangement? This draft proposes `201` without persistence; review is required.
5. How much selected-path explanation is required by the mobile UI?
6. Will the rhythmic event shape replace the parallel `chords`/`melody` arrays before public `v1`, avoiding two long-lived request shapes?

