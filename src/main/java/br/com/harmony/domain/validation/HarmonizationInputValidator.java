package br.com.harmony.domain.validation;

import br.com.harmony.domain.music.Chord;
import br.com.harmony.domain.music.Key;
import br.com.harmony.domain.music.Pitch;
import br.com.harmony.domain.music.SupportedChords;
import br.com.harmony.domain.music.Voice;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public final class HarmonizationInputValidator {
    private static final int MAX_EVENTS = 8;
    private static final Pattern KEY_FORMAT = Pattern.compile("^[A-G](?:#|b)?$");
    private static final Pattern CHORD_FORMAT = Pattern.compile("^(C|D|E|F|G|A|B|F#|Bb)m?$");
    private static final Comparator<ValidationError> ERROR_ORDER = Comparator
            .comparingInt((ValidationError error) -> fieldOrder(error.field()))
            .thenComparing(ValidationError::field, HarmonizationInputValidator::compareLocations)
            .thenComparing(error -> error.code().name());

    public ValidationResult validate(HarmonizationInput input) {
        if (input == null) return ValidationResult.failure(List.of(error("request", ValidationErrorCode.REQUIRED_FIELD, "Input is required.", null)));
        List<ValidationError> errors = new ArrayList<>();
        Key key = validateKey(input.key(), errors);
        validateSequences(input, errors);
        List<Chord> chords = validateChords(input.chords(), key, errors);
        List<Pitch> melody = validateMelody(input.melody(), key, errors);
        if (!errors.isEmpty()) {
            errors.sort(ERROR_ORDER);
            return ValidationResult.failure(errors);
        }
        return ValidationResult.success(new ValidatedHarmonizationInput(key, chords, melody));
    }

    private Key validateKey(String value, List<ValidationError> errors) {
        if (value == null) { errors.add(error("key", ValidationErrorCode.REQUIRED_FIELD, "Key is required.", null)); return null; }
        if (!KEY_FORMAT.matcher(value).matches()) { errors.add(error("key", ValidationErrorCode.INVALID_KEY_FORMAT, "Key is not canonical.", value)); return null; }
        try { return Key.parse(value); }
        catch (IllegalArgumentException exception) { errors.add(error("key", ValidationErrorCode.UNSUPPORTED_KEY, "Key is unsupported.", value)); return null; }
    }

    private void validateSequences(HarmonizationInput input, List<ValidationError> errors) {
        if (input.chords() == null) errors.add(error("chords", ValidationErrorCode.REQUIRED_FIELD, "Chords are required.", null));
        else if (input.chords().isEmpty()) errors.add(error("chords", ValidationErrorCode.EMPTY_SEQUENCE, "Chords must not be empty.", "[]"));
        else if (input.chords().size() > MAX_EVENTS) errors.add(error("chords", ValidationErrorCode.PHRASE_TOO_LONG, "Phrase may contain at most 8 events.", String.valueOf(input.chords().size())));
        if (input.melody() == null) errors.add(error("melody", ValidationErrorCode.REQUIRED_FIELD, "Melody is required.", null));
        else if (input.melody().isEmpty()) errors.add(error("melody", ValidationErrorCode.EMPTY_SEQUENCE, "Melody must not be empty.", "[]"));
        else if (input.melody().size() > MAX_EVENTS) errors.add(error("melody", ValidationErrorCode.PHRASE_TOO_LONG, "Phrase may contain at most 8 events.", String.valueOf(input.melody().size())));
        if (input.chords() != null && input.melody() != null && input.chords().size() != input.melody().size())
            errors.add(error("chords", ValidationErrorCode.SEQUENCE_LENGTH_MISMATCH, "Chord and melody lengths must match.", input.chords().size() + "/" + input.melody().size()));
    }

    private List<Chord> validateChords(List<String> values, Key key, List<ValidationError> errors) {
        List<Chord> result = new ArrayList<>();
        if (values == null) return result;
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            if (value == null || !CHORD_FORMAT.matcher(value).matches()) {
                errors.add(error("chords[" + i + "]", ValidationErrorCode.INVALID_CHORD_FORMAT, "Chord is not canonical.", value)); continue;
            }
            Chord chord = Chord.parse(value);
            result.add(chord);
            if (key != null && !SupportedChords.supports(key, chord))
                errors.add(error("chords[" + i + "]", ValidationErrorCode.UNSUPPORTED_CHORD, "Chord is unsupported in the selected key.", value));
        }
        return result;
    }

    private List<Pitch> validateMelody(List<String> values, Key key, List<ValidationError> errors) {
        List<Pitch> result = new ArrayList<>();
        if (values == null) return result;
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            Pitch pitch;
            try { pitch = Pitch.parse(value); }
            catch (IllegalArgumentException exception) {
                errors.add(error("melody[" + i + "]", ValidationErrorCode.INVALID_PITCH_FORMAT, "Pitch is not canonical.", value)); continue;
            }
            result.add(pitch);
            if (!Voice.SOPRANO.isWithinAbsoluteRange(pitch))
                errors.add(error("melody[" + i + "]", ValidationErrorCode.SOPRANO_OUT_OF_RANGE, "Pitch is outside the Soprano absolute range.", value));
            if (key != null && !key.supports(pitch.pitchClass()))
                errors.add(error("melody[" + i + "]", ValidationErrorCode.UNSUPPORTED_PITCH, "Pitch is unsupported in the selected key.", value));
        }
        return result;
    }

    private static int fieldOrder(String field) {
        if (field.equals("key")) return 0;
        if (field.equals("chords") || field.startsWith("chords[")) return 1;
        if (field.equals("melody") || field.startsWith("melody[")) return 2;
        return 3;
    }

    private static int compareLocations(String left, String right) {
        String leftBase = baseField(left);
        String rightBase = baseField(right);
        int baseComparison = leftBase.compareTo(rightBase);
        if (baseComparison != 0) return baseComparison;
        return Integer.compare(arrayIndex(left), arrayIndex(right));
    }

    private static String baseField(String field) {
        int bracket = field.indexOf('[');
        return bracket < 0 ? field : field.substring(0, bracket);
    }

    private static int arrayIndex(String field) {
        int openingBracket = field.indexOf('[');
        if (openingBracket < 0) return -1;
        return Integer.parseInt(field.substring(openingBracket + 1, field.length() - 1));
    }

    private static ValidationError error(String field, ValidationErrorCode code, String message, String rejectedValue) {
        return new ValidationError(field, code, message, rejectedValue);
    }
}
