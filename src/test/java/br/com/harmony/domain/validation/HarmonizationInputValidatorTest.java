package br.com.harmony.domain.validation;

import static br.com.harmony.domain.validation.ValidationErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class HarmonizationInputValidatorTest {
    private final HarmonizationInputValidator validator = new HarmonizationInputValidator();

    @Test void oneEventPasses() { assertValid(new HarmonizationInput("C", List.of("C"), List.of("C4"))); }
    @Test void eightEventsPass() { assertValid(new HarmonizationInput("C", Collections.nCopies(8, "C"), Collections.nCopies(8, "E4"))); }

    @Test void zeroEventsFails() {
        ValidationResult result = validator.validate(new HarmonizationInput("C", List.of(), List.of()));
        assertEquals(2, count(result, EMPTY_SEQUENCE));
    }
    @Test void nineEventsFail() {
        ValidationResult result = validator.validate(new HarmonizationInput("C", Collections.nCopies(9, "C"), Collections.nCopies(9, "C4")));
        assertEquals(2, count(result, PHRASE_TOO_LONG));
    }
    @Test void unequalLengthsFail() { assertHasCode(new HarmonizationInput("C", List.of("C", "G"), List.of("C4")), SEQUENCE_LENGTH_MISMATCH); }
    @Test void nullListsFail() {
        ValidationResult result = validator.validate(new HarmonizationInput("C", null, null));
        assertEquals(2, count(result, REQUIRED_FIELD));
    }

    @ParameterizedTest @CsvSource({"G,F4", "F,B4", "C,F#4", "C,Bb4"})
    void unsupportedPitchContextFails(String key, String pitch) {
        assertHasCode(new HarmonizationInput(key, List.of(key), List.of(pitch)), UNSUPPORTED_PITCH);
    }

    @ParameterizedTest @ValueSource(strings = {"H4", "Gb4", "A#4", "c4", "C 4", "C#4"})
    void malformedPitchFails(String pitch) { assertHasCode(new HarmonizationInput("C", List.of("C"), List.of(pitch)), INVALID_PITCH_FORMAT); }

    @Test void outOfRangeSopranoFails() { assertHasCode(new HarmonizationInput("C", List.of("C"), List.of("B3")), SOPRANO_OUT_OF_RANGE); }

    @Test void supportedRequestReturnsTypedInput() {
        ValidationResult result = validator.validate(new HarmonizationInput("G", List.of("G", "D"), List.of("F#4", "D5")));
        assertValid(result);
        assertEquals("G", result.validatedInput().orElseThrow().key().toString());
        assertThrows(UnsupportedOperationException.class, () -> result.validatedInput().orElseThrow().chords().add(null));
    }

    @Test void nonChordToneSopranoPassesM1Validation() {
        assertValid(new HarmonizationInput("C", List.of("C"), List.of("D4")));
    }

    @Test void arrayElementErrorsUseNumericIndexOrdering() {
        List<String> melody = new java.util.ArrayList<>(Collections.nCopies(11, "C4"));
        melody.set(2, "H4");
        melody.set(10, "H4");

        List<String> fields = validator.validate(new HarmonizationInput(
                        "C", Collections.nCopies(11, "C"), melody))
                .errors().stream()
                .map(ValidationError::field)
                .filter(field -> field.startsWith("melody["))
                .toList();

        assertEquals(List.of("melody[2]", "melody[10]"), fields);
    }

    @ParameterizedTest @CsvSource({"D,UNSUPPORTED_KEY", "Am,INVALID_KEY_FORMAT", "C major,INVALID_KEY_FORMAT"})
    void distinguishesKeyErrors(String key, ValidationErrorCode expected) {
        assertHasCode(new HarmonizationInput(key, List.of("C"), List.of("C4")), expected);
    }

    @ParameterizedTest @CsvSource({"C,D,UNSUPPORTED_CHORD", "C,C7,INVALID_CHORD_FORMAT", "G,F,UNSUPPORTED_CHORD", "F,Bb7,INVALID_CHORD_FORMAT"})
    void distinguishesChordErrors(String key, String chord, ValidationErrorCode expected) {
        assertHasCode(new HarmonizationInput(key, List.of(chord), List.of(key + "4")), expected);
    }

    @Test void suppressesKeyDependentErrorsWhenKeyCannotParse() {
        ValidationResult result = validator.validate(new HarmonizationInput("C major", List.of("D"), List.of("F#4")));
        assertEquals(List.of(INVALID_KEY_FORMAT), result.errors().stream().map(ValidationError::code).toList());
    }

    private void assertValid(HarmonizationInput input) { assertValid(validator.validate(input)); }
    private void assertValid(ValidationResult result) { assertTrue(result.isValid(), () -> result.errors().toString()); assertTrue(result.validatedInput().isPresent()); }
    private void assertHasCode(HarmonizationInput input, ValidationErrorCode code) { assertTrue(validator.validate(input).errors().stream().anyMatch(error -> error.code() == code)); }
    private long count(ValidationResult result, ValidationErrorCode code) { return result.errors().stream().filter(error -> error.code() == code).count(); }
}
